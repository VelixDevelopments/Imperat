package dev.velix.imperat.verification;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import org.jetbrains.annotations.ApiStatus;
import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
final class DefaultUsageVerifier<C> implements UsageVerifier<C> {
	
	DefaultUsageVerifier() {
	
	}
	
	/**
	 * @param usage the usage
	 * @return Verifies the usage to be acceptable
	 */
	@Override
	public boolean verify(CommandUsage<C> usage) {
		if (usage.getParameters().isEmpty()) {
			return false;
		}
		
		int greedyCount = 0;
		for (int i = 0; i < usage.getMaxLength(); i++) {
			CommandParameter param = usage.getParameters().get(i);
			if (param.isGreedy()) greedyCount++;
		}
		
		if (greedyCount > 1) {
			return false;
		}
		
		CommandParameter firstParameter = usage.getParameters().get(0);
		boolean firstArgIsRequired = firstParameter != null
						&& !firstParameter.isOptional();
		
		CommandParameter greedyParam = usage.getParameter(CommandParameter::isGreedy);
		if (greedyParam == null)
			return firstArgIsRequired;
		
		return greedyParam.getPosition() == usage.getMaxLength() - 1 && firstArgIsRequired;
	}
	
	/**
	 * Ensures
	 *
	 * @param usage1 the first usage
	 * @param usage2 the second usage
	 * @return whether both are usages similar and share similar indistinguishable parameters/syntax
	 */
	@Override
	
	public boolean areAmbiguous(CommandUsage<C> usage1, CommandUsage<C> usage2) {
		//check length
		boolean sameLength = usage1.getMinLength() == usage2.getMinLength();
		boolean hasSubCommands = usage1.hasParamType(Command.class)
						&& usage2.hasParamType(Command.class);
		
		if (sameLength && hasSubCommands) {
			List<CommandParameter> parameterList1 = new ArrayList<>(usage1.getParameters());
			parameterList1.removeIf((param) -> !param.isCommand());
			
			List<CommandParameter> parameterList2 = new ArrayList<>(usage2.getParameters());
			parameterList2.removeIf((param) -> !param.isCommand());
			
			return parameterList1.equals(parameterList2);
		}
		
		return sameLength;
	}
}
