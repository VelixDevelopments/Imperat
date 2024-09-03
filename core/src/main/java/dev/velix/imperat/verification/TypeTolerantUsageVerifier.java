package dev.velix.imperat.verification;

import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.util.TypeUtility;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;

/**
 * Represents a usage verifier where it checks for ambiguity in a slightly
 * different way than the {@link DefaultUsageVerifier}.
 * Example: If you have two usages of a command: `/command <arg1>` and `/command <arg2>`,
 * while arg1 is a parameter of type String, and arg2 is a parameter of type Boolean,
 * It will not consider this an ambiguity unless both parameters are of same {@link Type} !
 * @param <C> the command sender type
 */
@ApiStatus.Internal
final class TypeTolerantUsageVerifier<C> extends DefaultUsageVerifier<C>{
	
	@Override
	public boolean verify(CommandUsage<C> usage) {
		return super.verify(usage); //I just like the super here lol
	}
	
	@Override
	public boolean areAmbiguous(CommandUsage<C> firstUsage, CommandUsage<C> secondUsage) {
		int sizeDiff = firstUsage.getMinLength()-secondUsage.getMinLength();
		if(sizeDiff != 0) {
			return false;
		}
		
		int capacity = firstUsage.getMinLength();
		boolean noDiff = true;
		for (int depth = 0; depth < capacity; depth++) {
			var param1 = firstUsage.getParameter(depth);
			var param2 = secondUsage.getParameter(depth);
			assert param1 != null && param2 != null;
			
			if(areSimilarParameters(param1, param2)) {
				continue;
			}
			noDiff = false;
			break;
		}
		
		return noDiff;
	}

	private boolean areSimilarParameters(CommandParameter param1, CommandParameter param2) {
		if(!param1.isCommand() && !param2.isCommand()) {
			return TypeUtility.matches(param1.getType(), param2.getType());
		}else if(param1.isCommand() && param2.isCommand()) {
			return param1.equals(param2);
		}
		return false;
	}
	
}
