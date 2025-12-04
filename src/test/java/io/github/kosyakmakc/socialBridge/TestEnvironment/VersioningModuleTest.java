package io.github.kosyakmakc.socialBridge.TestEnvironment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.IBridgeModule;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.ISocialCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IModuleLoader;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public class VersioningModuleTest implements IBridgeModule {

    public static final UUID MODULE_ID = UUID.randomUUID();

    private final String debugName;
    private final Version version;
    private final IModuleLoader loader;
    private final List<ISocialCommand> socialCommands = List.of();
    private final List<IMinecraftCommand> minecraftCommands = List.of();
    private final List<ITranslationSource> translationSources = List.of();

    private ISocialBridge bridge;

    public VersioningModuleTest(String debugName, Version version, IModuleLoader loader) {
        this.debugName = debugName;
        this.version = version;
        this.loader = loader;
    }

    @Override
    public Version getCompabilityVersion() {
        return version;
    }

    @Override
    public UUID getId() {
        return MODULE_ID;
    }

    @Override
    public String getName() {
        return debugName;
    }

    @Override
    public CompletableFuture<Boolean> enable(ISocialBridge bridge) {
        this.bridge = bridge;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> disable() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public ISocialBridge getBridge() {
        return bridge;
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
    public IModuleLoader getLoader() {
        return loader;
    }

}
