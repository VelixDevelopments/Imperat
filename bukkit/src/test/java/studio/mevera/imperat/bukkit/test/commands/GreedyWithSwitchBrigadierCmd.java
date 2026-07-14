package studio.mevera.imperat.bukkit.test.commands;

import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Named;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Suggest;
import studio.mevera.imperat.annotations.types.Switch;

/**
 * Regression shape for a subcommand-scoped greedy positional followed by a
 * {@code @Switch}. On modern Paper the previous flags-as-argument-node design
 * emitted a cyclic {@code <flag>} node beside the {@code greedyString} node,
 * a shape Paper's client mirror silently dropped — killing ALL suggestions
 * (targets AND switch) for the scope. Flags are now folded into the greedy
 * node's suggester and the cyclic node is skipped for greedy scopes.
 */
@RootCommand("greedysw")
public final class GreedyWithSwitchBrigadierCmd {

    @SubCommand("migrate")
    public void migrate(
            BukkitCommandSource source,
            @Named("target") @Suggest({"alpha", "beta", "gamma"}) @Greedy String target,
            @Switch("shallow") boolean shallow
    ) {
    }
}
