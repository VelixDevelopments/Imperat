package dev.velix.imperat.commands;

import dev.velix.imperat.TestRun;
import dev.velix.imperat.TestSource;
import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.help.CommandHelp;

@SuppressWarnings("unused")
public final class TestCommands {

    public final static Command<TestSource> GROUP_CMD = Command.<TestSource>create("group")
        .defaultExecution((source, context) -> {
            source.reply("/group <group>");
        })
        .usage(CommandUsage.<TestSource>builder()
            .parameters(CommandParameter.requiredText("group"))
            .execute((source, context) -> {
                source.reply("Executing /group " + context.getArgument("group") + " without any other args");
            })

        )
        .subCommand(
            Command.<TestSource>create("setperm")
                .usage(CommandUsage.<TestSource>builder()
                    .parameters(
                        CommandParameter.requiredText("permission"),
                        CommandParameter.<TestSource>optionalBoolean("value").defaultValue(false)
                    )
                    .execute((source, ctx) -> {
                        TestRun.USAGE_EXECUTED = true;
                        String group = ctx.getArgument("group");
                        String permission = ctx.getArgument("permission");
                        Boolean value = ctx.getArgument("value");
                        source.reply("Executing /group " + group + " setperm " + permission + " " + value);
                    })

                )
                .build()
        )
        .subCommand(Command.<TestSource>create("setprefix")
            .usage(
                CommandUsage.<TestSource>builder()
                    .parameters(
                        CommandParameter.requiredText("prefix")
                    )
                    .execute((source, ctx) -> {
                        TestRun.USAGE_EXECUTED = true;
                        String group = ctx.getArgument("group");
                        String prefix = ctx.getArgument("prefix");
                        source.reply("Executing /group " + group + " setprefix " + prefix);
                    })
            )
            .build()
        )
        .subCommand(Command.<TestSource>create("help")
            .usage(
                CommandUsage.<TestSource>builder()
                    .parameters(
                        CommandParameter.<TestSource>optionalInt("page").defaultValue(1)
                    )
                    .execute((source, context) -> {
                        Integer page = context.getArgument("page");

                        CommandHelp help = context.getContextResolvedArgument(CommandHelp.class);
                        assert help != null;
                        help.display(source);

                        source.sendMsg("Help page= " + page);
                        TestRun.USAGE_EXECUTED = true;
                    })

            ).build(), true
        )
        .build();

    public final static Command<TestSource> CHAINED_SUBCOMMANDS_CMD =
        Command.<TestSource>create("subs")
            .subCommand(
                Command.<TestSource>create("first")
                    .defaultExecution((source, context) -> {
                        source.reply("FIRST, DEF EXEC");
                    })
                    .usage(CommandUsage.<TestSource>builder()
                        .parameters(CommandParameter.requiredText("arg1"))
                        .execute((source, context) -> source.reply("Arg1= " + context.getArgument("arg1")))

                    )
                    .subCommand(
                        Command.<TestSource>create("second")
                            .defaultExecution((source, context) -> source.reply("SECOND, DEF EXEC"))
                            .usage(CommandUsage.<TestSource>builder()
                                .parameters(CommandParameter.requiredText("arg2"))
                                .execute((source, ctx) -> source.reply("Arg1= " + ctx.getArgument("arg1") + ", Arg2= " + ctx.getArgument("arg2")))
                            )
                            .subCommand(
                                Command.<TestSource>create("third")
                                    .defaultExecution((source, context) -> source.reply("THIRD, DEF EXEC"))
                                    .usage(CommandUsage.<TestSource>builder()
                                        .parameters(CommandParameter.requiredText("arg3"))
                                        .execute((source, ctx) -> source.reply("Arg1= " + ctx.getArgument("arg1") + ", " +
                                            "Arg2= " + ctx.getArgument("arg2") + ", Arg3= " + ctx.getArgument("arg3")))
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )

            .build();

    public final static Command<TestSource> MULTIPLE_OPTIONAL_CMD =
        Command.<TestSource>create("ot")
            .usage(
                CommandUsage.<TestSource>builder()
                    .parameters(
                        CommandParameter.requiredText("r1"),
                        CommandParameter.optionalText("o1"),
                        CommandParameter.requiredText("r2"),
                        CommandParameter.optionalText("o2")
                    )

            )
            .build();


}
