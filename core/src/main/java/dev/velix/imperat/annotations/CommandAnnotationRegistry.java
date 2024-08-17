package dev.velix.imperat.annotations;

import dev.velix.imperat.annotations.types.Command;
import dev.velix.imperat.annotations.types.Description;
import dev.velix.imperat.annotations.types.Permission;
import dev.velix.imperat.annotations.types.methods.DefaultUsage;
import dev.velix.imperat.annotations.types.methods.Help;
import dev.velix.imperat.annotations.types.methods.SubCommand;
import dev.velix.imperat.annotations.types.methods.Usage;
import dev.velix.imperat.annotations.types.parameters.*;
import dev.velix.imperat.util.ClassMap;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;

public final class CommandAnnotationRegistry extends LinkedHashSet<Class<? extends Annotation>> {
	
	private final ClassMap<Annotation, AnnotationReplacer<?>> replacers = new ClassMap<>();
	
	public CommandAnnotationRegistry() {
		this.registerAnnotationTypes(Command.class, Description.class, Permission.class);
		this.registerAnnotationTypes(Usage.class, DefaultUsage.class, SubCommand.class, Help.class, Command.class);
		this.registerAnnotationTypes(DefaultValue.class, DefaultValueProvider.class,
						Flag.class, Greedy.class, Named.class, Optional.class, Range.class);
	}
	
	public <A extends Annotation> void registerAnnotationReplacer(Class<A> type, AnnotationReplacer<A> replacer) {
		this.replacers.put(type, replacer);
	}
	
	@SuppressWarnings("unchecked")
	public <A extends Annotation> @Nullable AnnotationReplacer<A> getAnnotationReplacer(Class<A> type) {
		return (AnnotationReplacer<A>) this.replacers.get(type);
	}
	
	public boolean hasReplacerFor(Class<? extends Annotation> clazz) {
		return getAnnotationReplacer(clazz) != null;
	}
	
	@SafeVarargs
	public final void registerAnnotationTypes(Class<? extends Annotation>... annotationClasses) {
		this.addAll(Arrays.asList(annotationClasses));
	}
	
	public boolean isRegistered(Class<? extends Annotation> annotationClass) {
		for (Class<? extends Annotation> aC : this) {
			if (aC.getName().equals(annotationClass.getName()))
				return true;
		}
		return false;
	}
	
	
}
