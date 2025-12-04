package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.ConfigurationService;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.IDatabaseConsumer;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public interface ISocialBridge {
    Logger getLogger();
    Version getVersion();

    LocalizationService getLocalizationService();
    ConfigurationService getConfigurationService();
    <T> CompletableFuture<T> queryDatabase(IDatabaseConsumer<T> action);

    BridgeEvents getEvents();

    CompletableFuture<Boolean> connectSocialPlatform(ISocialPlatform socialPlatform);
    CompletableFuture<Void> disconnectSocialPlatform(ISocialPlatform socialPlatform);
    Collection<ISocialPlatform> getSocialPlatforms();
    <T extends ISocialPlatform> T getSocialPlatform(Class<T> tClass);

    IMinecraftPlatform getMinecraftPlatform();

    CompletableFuture<Boolean> connectModule(IBridgeModule module);
    CompletableFuture<Void> disconnectModule(IBridgeModule module);
    Collection<IBridgeModule> getModules();
    <T extends IBridgeModule> T getModule(Class<T> tClass);
}
