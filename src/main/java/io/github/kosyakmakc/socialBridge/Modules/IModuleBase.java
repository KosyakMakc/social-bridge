package io.github.kosyakmakc.socialBridge.Modules;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IModuleLoader;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public interface IModuleBase {
    Version getCompabilityVersion();
    UUID getId();
    String getName();

    CompletableFuture<Boolean> enable(ISocialBridge bridge);
    CompletableFuture<Boolean> disable();
    ISocialBridge getBridge();

    IModuleLoader getLoader();
}
