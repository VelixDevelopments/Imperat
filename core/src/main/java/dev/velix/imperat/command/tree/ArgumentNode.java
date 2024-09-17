package dev.velix.imperat.command.tree;

import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.command.parameters.types.ParameterType;
import dev.velix.imperat.command.parameters.types.ParameterTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class ArgumentNode extends ParameterNode<CommandParameter> {
    
    ArgumentNode(@NotNull CommandParameter data) {
        super(data);
    }
    
    @Override
    public boolean matchesInput(String input) {
        if (data.isFlag())
            return data.asFlagParameter()
                    .getFlagData().acceptsInput(input);
        
        ParameterType type = ParameterTypes.getParamType(data.type());
        return type == null || type.matchesInput(input);
    }
    
    @Override
    public String format() {
        return data.format();
    }
    
    @Override
    public int priority() {
        return 1;
    }
    
    
}
