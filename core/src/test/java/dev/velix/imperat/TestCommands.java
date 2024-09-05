package dev.velix.imperat;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;

public final class TestCommands {
    
    public final static Command<TestSender> GROUP_CMD = Command.<TestSender>create("group")
            .defaultExecution((source, context) -> {
                source.reply("/group <group>");
            })
            .usage(CommandUsage.<TestSender>builder()
                    .parameters(CommandParameter.requiredText("group"))
                    .execute((source, context) -> {
                    })
                    .build()
            )
            .subCommand(
                    Command.<TestSender>create("setperm")
                            .usage(CommandUsage.<TestSender>builder()
                                    .parameters(
                                            CommandParameter.requiredText("permission"),
                                            CommandParameter.optionalBoolean("value").defaultValue(false)
                                    ).build()
                            )
                            .build()
            )
            .subCommand(Command.<TestSender>create("setprefix")
                    .usage(
                            CommandUsage.<TestSender>builder()
                                    .parameters(
                                            CommandParameter.requiredText("prefix")
                                    ).build()
                    )
                    .build()
            )
            .subCommand(Command.<TestSender>create("help").usage(
                    CommandUsage.<TestSender>builder()
                            .parameters(
                                    CommandParameter.optionalInt("page").defaultValue(1)
                            )
                            .build()).build()
            )
            .build();
    
    public final static Command<TestSender> MULTIPLE_OPTIONAL_CMD =
            Command.<TestSender>create("ot")
                    .usage(
                            CommandUsage.<TestSender>builder()
                                    .parameters(
                                            CommandParameter.requiredText("r1"),
                                            CommandParameter.optionalText("o1"),
                                            CommandParameter.requiredText("r2"),
                                            CommandParameter.optionalText("o2")
                                    )
                                    .build()
                    )
                    .build();
    
    
}
