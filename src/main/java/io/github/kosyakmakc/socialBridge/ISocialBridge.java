package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.ConfigurationService;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;
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
    <T> CompletableFuture<T> doTransaction(ITransactionConsumer<T> action);

    BridgeEvents getEvents();

    CompletableFuture<Boolean> connectSocialPlatform(ISocialPlatform socialPlatform);
    CompletableFuture<Void> disconnectSocialPlatform(ISocialPlatform socialPlatform);
    Collection<ISocialPlatform> getSocialPlatforms();
    <T extends ISocialPlatform> T getSocialPlatform(Class<T> tClass);
    ISocialPlatform getSocialPlatform(UUID socialPlatformId);

    IMinecraftPlatform getMinecraftPlatform();

    CompletableFuture<Boolean> connectModule(IModuleBase module);
    CompletableFuture<Void> disconnectModule(IModuleBase module);
    Collection<IModuleBase> getModules();
    <T extends IModuleBase> T getModule(Class<T> tClass);
    IModuleBase getModule(UUID moduleId);
}
