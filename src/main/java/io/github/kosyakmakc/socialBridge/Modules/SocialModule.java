package io.github.kosyakmakc.socialBridge.Modules;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.ISocialCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IModuleLoader;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public class SocialModule implements IModule {
    private final LinkedList<ISocialCommand> socialCommands = new LinkedList<>();
    private final LinkedList<IMinecraftCommand> minecraftCommands = new LinkedList<>();
    private final LinkedList<ITranslationSource> translationSources = new LinkedList<>();

    private Version compabilityVersion;
    private UUID moduleId;
    private String moduleName;
    private IModuleLoader moduleLoader;

    private ISocialBridge bridge;

    public SocialModule(IModuleLoader moduleLoader, Version compabilityVersion, UUID moduleId, String moduleName) {
        this.compabilityVersion = compabilityVersion;
        this.moduleId = moduleId;
        this.moduleLoader = moduleLoader;
        this.moduleName = moduleName;
    }

    @Override
    public Version getCompabilityVersion() {
        return compabilityVersion;
    }

    @Override
    public UUID getId() {
        return moduleId;
    }

    @Override
    public String getName() {
        return moduleName;
    }

    @Override
    public CompletableFuture<Boolean> enable(ISocialBridge bridge) {
        this.bridge = bridge;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> disable() {
        bridge = null;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public ISocialBridge getBridge() {
        return bridge;
    }

    @Override
    public IModuleLoader getLoader() {
        return moduleLoader;
    }

    @Override
    public Collection<ITranslationSource> getTranslations() {
        return List.copyOf(translationSources);
    }

    @Override
    public Collection<ISocialCommand> getSocialCommands() {
        return List.copyOf(socialCommands);
    }

    @Override
    public Collection<IMinecraftCommand> getMinecraftCommands() {
        return List.copyOf(minecraftCommands);
    }

    protected boolean isConnected() {
        return bridge != null;
    }

    private void throwIfConnected() {
        if (isConnected()) {
            throw new RuntimeException("Please fill this module before connecting to SocialBridge (or temporaly disconnect module for changing");
        }
    }

    protected void addMinecraftCommand(IMinecraftCommand minecraftCommand) {
        throwIfConnected();

        minecraftCommands.add(minecraftCommand);
    }

    protected void removeMinecraftCommand(IMinecraftCommand minecraftCommand) {
        throwIfConnected();

        minecraftCommands.remove(minecraftCommand);
    }

    protected void clearMinecraftCommands() {
        throwIfConnected();

        minecraftCommands.clear();
    }

    protected void addSocialCommand(ISocialCommand socialCommand) {
        throwIfConnected();

        socialCommands.add(socialCommand);
    }

    protected void removeSocialCommand(ISocialCommand socialCommand) {
        throwIfConnected();

        socialCommands.remove(socialCommand);
    }

    protected void clearSocialCommands() {
        throwIfConnected();

        socialCommands.clear();
    }

    protected void addTranslationSource(ITranslationSource translationSource) {
        throwIfConnected();

        translationSources.add(translationSource);
    }

    protected void removeTranslationSource(ITranslationSource translationSource) {
        throwIfConnected();

        translationSources.remove(translationSource);
    }

    protected void clearTranslationSources() {
        throwIfConnected();

        translationSources.clear();
    }
}
