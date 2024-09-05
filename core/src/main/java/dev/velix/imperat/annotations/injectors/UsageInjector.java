package dev.velix.imperat.annotations.injectors;

import dev.velix.imperat.Imperat;
import dev.velix.imperat.annotations.*;
import dev.velix.imperat.annotations.element.CommandAnnotatedElement;
import dev.velix.imperat.annotations.element.MethodParameterElement;
import dev.velix.imperat.annotations.injectors.context.InjectionContext;
import dev.velix.imperat.annotations.injectors.context.ProxyCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.Named;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.help.CommandHelp;
import dev.velix.imperat.help.MethodHelpExecution;
import dev.velix.imperat.util.Pair;
import dev.velix.imperat.util.TypeUtility;
import dev.velix.imperat.util.TypeWrap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
final class UsageInjector<C> extends AnnotationDataInjector<CommandUsage<C>, C, Usage> {
	
	public UsageInjector(
					Imperat<C> dispatcher
	) {
		super(
						dispatcher,
						InjectionContext.of(Usage.class, TypeWrap.of(CommandUsage.class), AnnotationLevel.METHOD)
		);
	}
	
	@Override
	public <T> @NotNull CommandUsage<C> inject(
					ProxyCommand<C> proxyCommand,
					@Nullable CommandUsage<C> toLoad,
					AnnotationReader reader,
					AnnotationParser<C> parser,
					AnnotationRegistry annotationRegistry,
					AnnotationInjectorRegistry<C> injectorRegistry,
					@NotNull CommandAnnotatedElement<?> element,
					@NotNull Usage annotation
	) {
		
		Method method = (Method) element.getElement();
		
		var parametersInfo = loadParameters(proxyCommand, injectorRegistry, annotationRegistry, reader, parser, method);
		final boolean isHelp = parametersInfo.left();
		final List<CommandParameter> params = parametersInfo.right();
		
		var execution = isHelp ? new MethodHelpExecution<>(dispatcher, proxyCommand, method, parametersInfo.right())
						: new MethodCommandExecutor<>(proxyCommand, dispatcher, method, params);
		
		final CommandUsage<C> usage =
						CommandUsage.<C>builder().parameters(params)
										.execute(execution).build(isHelp);
		
		injectOthers(proxyCommand, usage, reader, parser, injectorRegistry, annotationRegistry, element);
		return usage;
	}
	
	@SuppressWarnings("unchecked")
	static <C, A extends Annotation> void injectOthers(
					ProxyCommand<C> proxyCommand,
					CommandUsage<C> usage,
					AnnotationReader reader,
					AnnotationParser<C> parser,
					AnnotationInjectorRegistry<C> injectorRegistry,
					AnnotationRegistry annotationRegistry,
					CommandAnnotatedElement<?> element
	) {
		injectorRegistry.forEachInjector((inj)-> !(inj instanceof UsageInjector) && inj.getContext().hasTargetType(new TypeWrap<CommandUsage<C>>() {})
						&& inj.getContext().isOnLevel(AnnotationLevel.METHOD), (inj)-> {
			A ann = (A) element.getAnnotation(inj.getContext().annClass());
			if(ann != null) {
				AnnotationDataInjector<CommandUsage<C>, C, A> dataInjector = (AnnotationDataInjector<CommandUsage<C>, C, A>) inj;
				dataInjector.inject(proxyCommand, usage, reader, parser, annotationRegistry, injectorRegistry, element, ann);
			}
		});
	}
	
	private Pair<List<CommandParameter>, Boolean> loadParameters(
					ProxyCommand<C> proxyCommand,
					AnnotationInjectorRegistry<C> injectorRegistry,
					AnnotationRegistry annotationRegistry,
					AnnotationReader reader,
					AnnotationParser<C> parser,
					Method method
	) {
		
		List<CommandParameter> commandParameters = new ArrayList<>();
		CommandParameterInjector<C> paramInjector = injectorRegistry.<CommandParameter, Named, CommandParameterInjector<C>>
						getInjector(Named.class, TypeWrap.of(CommandParameter.class), AnnotationLevel.PARAMETER)
						.orElseThrow();
		boolean help = false;
		for (Parameter parameter : method.getParameters()) {
			if (dispatcher.canBeSender(parameter.getType()) ) continue;
			if (dispatcher.hasContextResolver(parameter.getType())) continue;
			if(TypeUtility.areRelatedTypes(parameter.getType(), CommandHelp.class)) {
				//CommandHelp parameter
				help = true;
				continue;
			}
			MethodParameterElement element = new MethodParameterElement(annotationRegistry, parameter);
			CommandParameter commandParameter =
							paramInjector.inject(proxyCommand,null, reader,
											parser, annotationRegistry,
											injectorRegistry, element, element.getAnnotation(Named.class));
			
			commandParameters.add(commandParameter);
		}
		return new Pair<>(commandParameters, help);
	}
	
}
