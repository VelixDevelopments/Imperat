package dev.velix.imperat.command;

import org.jetbrains.annotations.Nullable;

public interface PermissionHolder {

    @Nullable String getPermission();

    void setPermission(String permission);
}
