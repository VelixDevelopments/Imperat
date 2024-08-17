package dev.velix.imperat.annotations.parameters;

import dev.velix.imperat.annotations.element.ParameterCommandElement;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.resolvers.OptionalValueSupplier;
import dev.velix.imperat.util.StringUtils;
import org.jetbrains.annotations.Nullable;

final class AnnotatedNormalParameter extends AnnotatedInputParameter {
	AnnotatedNormalParameter(String name,
	                         Class<?> type,
	                         boolean optional, boolean greedy,
	                         @Nullable OptionalValueSupplier<?, ?> optionalValueSupplier,
	                         ParameterCommandElement element) {
		super(name, type, optional, false, greedy, optionalValueSupplier, element);
	}
	
	/**
	 * Formats the usage parameter
	 * using the command
	 *
	 * @param command The command owning this parameter
	 * @return the formatted parameter
	 */
	@Override
	public <C> String format(Command<C> command) {
		String content = getName();
		if (isGreedy())
			content += "...";
		return StringUtils.normalizedParameterFormatting(content, isOptional());
	}
}
