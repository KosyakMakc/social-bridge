package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.ConfigurationService;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.IDatabaseConsumer;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public interface ISocialBridge {
    Version getVersion();

    Logger getLogger();
    ILocalizationService getLocalizationService();
    ConfigurationService getConfigurationService();
    <T> CompletableFuture<T> queryDatabase(IDatabaseConsumer<T> action);
    <T> CompletableFuture<T> queryDatabase(IDatabaseConsumer<T> action, ITransaction transaction);

    BridgeEvents getEvents();

    CompletableFuture<Boolean> connectSocialPlatform(ISocialPlatform socialPlatform);
    CompletableFuture<Void> disconnectSocialPlatform(ISocialPlatform socialPlatform);
    Collection<ISocialPlatform> getSocialPlatforms();
    <T extends ISocialPlatform> T getSocialPlatform(Class<T> tClass);
    ISocialPlatform getSocialPlatform(UUID socialPlatformId);

    IMinecraftPlatform getMinecraftPlatform();

    CompletableFuture<Boolean> connectModule(ISocialModule module);
    CompletableFuture<Void> disconnectModule(ISocialModule module);
    Collection<ISocialModule> getModules();
    <T extends ISocialModule> T getModule(Class<T> tClass);
    ISocialModule getModule(UUID moduleId);
}
