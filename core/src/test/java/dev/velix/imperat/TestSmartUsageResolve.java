package dev.velix.imperat;


import dev.velix.imperat.command.Command;
import dev.velix.imperat.command.CommandUsage;
import dev.velix.imperat.context.ArgumentQueue;
import dev.velix.imperat.context.Context;
import dev.velix.imperat.context.ResolvedContext;
import dev.velix.imperat.context.internal.ContextFactory;
import dev.velix.imperat.util.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static dev.velix.imperat.TestRun.IMPERAT;
import static dev.velix.imperat.TestRun.SOURCE;

public class TestSmartUsageResolve {

    TestSmartUsageResolve() {

    }

    private final static ContextFactory<TestSource> FACTORY = IMPERAT.getContextFactory();

    private static ResolvedContext<TestSource> inputResolve(String command, String... args) {
        ArgumentQueue queue = ArgumentQueue.parse(args);
        Command<TestSource> cmd = IMPERAT.getCommand(command);
        Assertions.assertNotNull(cmd);

        Context<TestSource> context = FACTORY.createContext(SOURCE, cmd, queue);
        CommandUsage<TestSource> usage = cmd.contextMatch(context).toUsage(cmd);
        Assertions.assertNotNull(usage);

        ResolvedContext<TestSource> resolvedContext = FACTORY.createResolvedContext(context, usage);
        Assertions.assertDoesNotThrow(resolvedContext::resolve);

        return resolvedContext;
    }

    private static void test(String cmdLine, ResolvedArgsData expected) {
        String[] split = cmdLine.split(" ");
        String[] raws = new String[split.length - 1];
        System.arraycopy(split, 1, raws, 0, split.length - 1);
        //System.arraycopy(split, 1, raws, 0, raws.length - 1);

        Assertions.assertTrue(expected
            .matches(
                ResolvedArgsData.of(
                    inputResolve(split[0], raws)
                )
            )
        );
    }

    @Test
    public void banWithSwitchFlag() {

        test(
            "ban mqzen",
            ResolvedArgsData.empty()
                .arg("player", "mqzen")
                .flag("silent", false)
                .arg("duration", null)
                .arg("reason", "Breaking server laws")
        );


        test("ban mqzen -s",
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
            /*System.out.println(this.args);
            System.out.println(other.args);
            System.out.println(this.resolvedFlags.getMap());
            System.out.println(other.resolvedFlags.getMap());
             */

            if (args.size() != other.args.size() || resolvedFlags.size() != other.resolvedFlags.size()) return false;

            for (var entry : this.args.entrySet()) {
                var thisObj = entry.getValue();
                var otherObj = other.args.get(entry.getKey());
                if (otherObj == null) break;
                if (!thisObj.equals(otherObj)) {
                    return false;
                }
            }

            return resolvedFlags.equals(other.resolvedFlags);
        }
    }

}
