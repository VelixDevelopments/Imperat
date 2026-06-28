package studio.mevera.imperat.command.cooldown;

import studio.mevera.imperat.context.CommandSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class DefaultCooldownHandler<S extends CommandSource> implements CooldownHandler<S> {

    public static final DefaultCooldownHandler<?> NOOP_INSTANCE = new DefaultCooldownHandler<>(null);

    private final Map<String, Instant> lastTimeExecuted = new HashMap<>();
    private final CooldownRecord cooldown;

    DefaultCooldownHandler(CooldownRecord cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public void registerExecutionMoment(S source) {
        if (cooldown != null) lastTimeExecuted.put(source.name(), Instant.now());
    }

    @Override
    public Optional<CooldownRecord> getUsageCooldown() {
        return Optional.ofNullable(cooldown);
    }

    @Override
    public void removeCooldown(S source) {
        if (cooldown != null) lastTimeExecuted.remove(source.name());
    }

    @Override
    public Optional<Instant> getLastTimeExecuted(S source) {
        return Optional.ofNullable(lastTimeExecuted.get(source.name()));
    }

}
