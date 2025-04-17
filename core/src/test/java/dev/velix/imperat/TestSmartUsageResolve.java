package dev.velix.imperat;


import static dev.velix.imperat.TestRun.IMPERAT;
import static dev.velix.imperat.TestRun.SOURCE;

import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.command.tree.CommandDispatch;
import dev.velix.imperat.commands.annotations.examples.BanCommand;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.util.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TestSmartUsageResolve {

    TestSmartUsageResolve() {
    }

    private final static ContextFactory<TestSource> FACTORY = IMPERAT.config.getContextFactory();

    private static ResolvedContext<TestSource> inputResolve(String command, String... args) {
        System.out.println("-------------");
        System.out.println("args= " + Arrays.toString(args));
        ArgumentQueue queue = ArgumentQueue.parse(args);
        System.out.println("QUEUE: " + queue.join(","));

        Command<TestSource> cmd = IMPERAT.getCommand(command);
        Assertions.assertNotNull(cmd);

        Context<TestSource> context = FACTORY.createContext(IMPERAT, SOURCE, cmd, command, queue);
        CommandDispatch<TestSource> res = cmd.contextMatch(context);
        res.visualize();

        CommandUsage<TestSource> usage = res.toUsage();

        if (usage == null) {
            System.out.println("USAGE IS NULL");
            var param = res.getLastNode();
            if (param != null && param.isCommand()) {
                usage = param.getData().asCommand().getDefaultUsage();
            }
        }
        Assertions.assertNotNull(usage);

        ResolvedContext<TestSource> resolvedContext = FACTORY.createResolvedContext(context, usage);
        Assertions.assertDoesNotThrow(resolvedContext::resolve);

        CommandUsage<TestSource> finalUsage = usage;
        Assertions.assertDoesNotThrow(() -> finalUsage.execute(IMPERAT, SOURCE, resolvedContext));
        return resolvedContext;
    }

    private static void test(String cmdLine, ResolvedArgsData expected) {
        String[] split = cmdLine.split(" ");
        String[] raws = new String[split.length - 1];
        System.arraycopy(split, 1, raws, 0, split.length - 1);
        //System.arraycopy(split, 1, raws, 0, raws.length - 1);

        var input = ResolvedArgsData.of(
            inputResolve(split[0], raws)
        );
        input.debug();
        Assertions.assertTrue(expected
            .matches(
                input
            )
        );
    }

    @Test
    public void banWithSwitchFlag() {
        IMPERAT.registerCommand(new BanCommand());
        test(
            "ban mqzen",
            ResolvedArgsData.empty()
                .arg("player", "mqzen")
                .flag("silent", false)
                .arg("duration", null)
                .arg("reason", "Breaking server laws")
        );
        // /ban <user> [reason]
        // /ban mqzen "for being a good man"
        test(
            "ban mqzen -s",
            ResolvedArgsData.empty()
                .arg("player", "mqzen")
                .flag("silent", true)
                .arg("duration", null)
                .arg("reason", "Breaking server laws")

        );
        test(
            "ban mqzen -s 1d",
            ResolvedArgsData.empty()
                .arg("player", "mqzen")
                .flag("silent", true)
                .arg("duration", "1d")
                .arg("reason", "Breaking server laws")

        );
        test(
            "ban mqzen -s 1d A disgrace to community",
            ResolvedArgsData.empty()
                .arg("player", "mqzen")
                .flag("silent", true)
                .arg("duration", "1d")
                .arg("reason", "A disgrace to community")

        );
    }


    @Test
    public void testInputFlag() {
        test("git", ResolvedArgsData.empty());
        test("git commit", ResolvedArgsData.empty().flag("message", null));
        test("git commit -m \"allah akbar\"", ResolvedArgsData.empty().flag("message", "allah akbar"));
    }

    @Test
    public void testInputParse() {
        ArgumentQueue queue = ArgumentQueue.parse("Its me again and this is my msg: \"sorry for interruption\"");
        queue.forEach(System.out::println);
    }

    @Test
    public void testKit() {
        test("kit create test", ResolvedArgsData.empty().arg("kit", "test")
            .arg("weight", 1));
    }

    static class ResolvedArgsData {

        private final Map<String, Object> args = new LinkedHashMap<>();
        private final Registry<String, Object> resolvedFlags = new Registry<>(LinkedHashMap::new);

        private ResolvedArgsData(ResolvedContext<TestSource> context) {
            if (context == null) return;

            for (var resolved : context.getResolvedArguments()) {
                args.put(resolved.parameter().name(), resolved.value());
            }

            for (var flag : context.getResolvedFlags()) {
                String name = flag.flag().name();
                resolvedFlags.setData(flag.flag().name(), context.getFlagValue(name));
            }
        }


        public static ResolvedArgsData of(ResolvedContext<TestSource> context) {
            return new ResolvedArgsData(context);
        }

        public static ResolvedArgsData empty() {
            return of(null);
        }

        public ResolvedArgsData arg(String paramName, Object resolvedValue) {
            args.put(paramName, resolvedValue);
            return this;
        }

        public ResolvedArgsData flag(String paramName, Object inputValue) {
            resolvedFlags.setData(paramName, inputValue);
            return this;
        }

        public boolean matches(ResolvedArgsData other) {
            System.out.println(this.args);
            System.out.println(other.args);
            System.out.println(this.resolvedFlags.getMap());
            System.out.println(other.resolvedFlags.getMap());


            if (args.size() != other.args.size() || resolvedFlags.size() != other.resolvedFlags.size()) {

                return false;
            }

            for (var entry : this.args.entrySet()) {
                var thisObj = entry.getValue();
                var otherObj = other.args.get(entry.getKey());
                if (!Objects.equals(thisObj, otherObj)) {
                    return false;
                }
            }

            for (String flagKey : resolvedFlags.getKeys()) {
                Object flagValue = resolvedFlags.getData(flagKey).orElse(null);
                if (!other.resolvedFlags.getMap().containsKey(flagKey)) {
                    return false;
                }
                if (!Objects.equals(flagValue, other.resolvedFlags.getData(flagKey)
                    .orElse(null))) {
                    return false;
                }
            }
            return true;
        }

        public void debug() {

            System.out.println("----------------");
            System.out.println("Args: ");
            for (var arg : args.keySet()) {
                System.out.println(" '" + arg + "' -> '" + args.get(arg) + "'");
            }

            System.out.println("Flags: ");
            for (var flag : resolvedFlags.getKeys()) {
                var flagInputValue = resolvedFlags.getData(flag).orElse(null);
                System.out.println(" '" + flag + "' -> '" + flagInputValue + "'");
            }

        }
    }

}
