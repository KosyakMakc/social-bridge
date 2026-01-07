package io.github.kosyakmakc.socialBridge.MinecraftPlatform;

import io.github.kosyakmakc.socialBridge.ISocialModule;
import io.github.kosyakmakc.socialBridge.IConfigurationService;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public interface IMinecraftPlatform extends IConfigurationService, IModuleLoader {
    String getPlatformName();
    UUID getId();
    UUID getInstanceId();

    java.nio.file.Path getDataDirectory() throws IOException;
    Version getSocialBridgeVersion();

    Logger getLogger();

    CompletableFuture<MinecraftUser> tryGetUser(UUID minecraftId);

    CompletableFuture<Void> connectModule(ISocialModule module);
}
