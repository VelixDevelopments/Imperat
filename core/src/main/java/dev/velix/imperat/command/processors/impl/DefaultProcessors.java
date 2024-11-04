package dev.velix.imperat.command.processors.impl;

import dev.velix.imperat.context.Source;

public final class DefaultProcessors {

    public static <S extends Source> UsageCooldownProcessor<S> preUsageCooldown() {
        return new UsageCooldownProcessor<>();
    }

    public static <S extends Source> UsagePermissionProcessor<S> preUsagePermission() {
        return new UsagePermissionProcessor<>();
    }
}
