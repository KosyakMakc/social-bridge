package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.ISocialCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.English;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IModuleLoader;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultModule implements IBridgeModule {
    public static final UUID MODULE_ID = UUID.fromString("dcab3770-b24e-44bb-b9a9-19edf96b9986");
    public static final String MODULE_NAME = "SocialBridge";
    public static final List<ITranslationSource> translationSources = List.of(new English());
    public static final List<IMinecraftCommand> minecraftCommands = List.of();
    public static final List<ISocialCommand> socialCommands = List.of();
    
    public final Version compabilityVersion;
    private final IModuleLoader loader;

    private ISocialBridge bridge;

    public DefaultModule(IMinecraftPlatform loader) {
        this.loader = loader;
        this.compabilityVersion = loader.getSocialBridgeVersion();
    }

    @Override
    public IModuleLoader getLoader() {
        return loader;
    }

    @Override
    public Version getCompabilityVersion() {
        return compabilityVersion;
    }

    @Override
    public Collection<ISocialCommand> getSocialCommands() {
        return socialCommands;
    }

    @Override
    public Collection<IMinecraftCommand> getMinecraftCommands() {
        return minecraftCommands;
    }

    @Override
    public Collection<ITranslationSource> getTranslations() {
        return translationSources;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public CompletableFuture<Boolean> enable(ISocialBridge bridge) {
        this.bridge = bridge;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> disable() {
        this.bridge = null;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public ISocialBridge getBridge() {
        return bridge;
    }

    @Override
    public UUID getId() {
        return MODULE_ID;
    }
}
