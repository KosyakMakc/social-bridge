package io.github.kosyakmakc.socialBridge.MinecraftPlatform;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class MinecraftUser {
    public abstract CompletableFuture<Boolean> HasPermission(String permission);
    public abstract void sendMessage(String message, HashMap<String, String> placeholders);
    public abstract String getName();
    public abstract String getLocale();
    public abstract UUID getId();
}
