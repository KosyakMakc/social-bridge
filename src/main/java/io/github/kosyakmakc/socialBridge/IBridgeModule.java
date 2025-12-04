package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.ISocialCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IModuleLoader;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IBridgeModule {
    Version getCompabilityVersion();
    UUID getId();
    String getName();

    CompletableFuture<Boolean> enable(ISocialBridge bridge);
    CompletableFuture<Boolean> disable();
    ISocialBridge getBridge();
    
    Collection<ISocialCommand> getSocialCommands();
    Collection<IMinecraftCommand> getMinecraftCommands();
    Collection<ITranslationSource> getTranslations();
    
    IModuleLoader getLoader();
}
