package io.github.kosyakmakc.socialBridge.TestEnvironment;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.ISocialCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.ITranslationSource;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IModuleLoader;
import io.github.kosyakmakc.socialBridge.Modules.IModule;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public class ModuleForTest implements IModule, AutoCloseable {
    public static final String DEFAULT_NAME = "DefaultEmptyName";
    private UUID moduleId = UUID.randomUUID();
    private Version version = HeadlessMinecraftPlatform.VERSION;

    private final IModuleLoader loader;
    @SuppressWarnings("rawtypes")
    private final HashMap<Class, ISocialCommand> socialCommands = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<Class, IMinecraftCommand> minecraftCommands = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<Class, ITranslationSource> translations = new HashMap<>();
    private ISocialBridge bridge;
    private String name = DEFAULT_NAME;

    public ModuleForTest() {
        this.loader = SocialBridge.INSTANCE.getMinecraftPlatform();
    }

    @Override
    public Version getCompabilityVersion() {
        return version;
    }

    public void setCompabilityVersion(Version version) {
        this.version = version;
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
    public Collection<ISocialCommand> getSocialCommands() {
        return socialCommands.values();
    }

    public void addSocialCommand(ISocialCommand socialCommand) {
        if (this.bridge != null) {
            throw new RuntimeException("TempModule already enabled and connected to SocialBridge, adding items disallowed now");
        }
        socialCommands.put(socialCommand.getClass(), socialCommand);
    }

    @SuppressWarnings("unchecked")
    public <T extends ISocialCommand> T getSocialCommand(Class<T> tClass) {
        var socialCommand = socialCommands.getOrDefault(tClass, null);
        if (socialCommand != null) {
            return (T) socialCommand;
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<IMinecraftCommand> getMinecraftCommands() {
        return minecraftCommands.values();
    }

    public void addMinecraftCommand(IMinecraftCommand minecraftCommand) {
        if (this.bridge != null) {
            throw new RuntimeException("TempModule already enabled and connected to SocialBridge, adding items disallowed now");
        }
        minecraftCommands.put(minecraftCommand.getClass(), minecraftCommand);
    }

    @SuppressWarnings("unchecked")
    public <T extends IMinecraftCommand> T getMinecraftCommand(Class<T> tClass) {
        var minecraftCommand = minecraftCommands.getOrDefault(tClass, null);
        if (minecraftCommand != null) {
            return (T) minecraftCommand;
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<ITranslationSource> getTranslations() {
        return translations.values();
    }

    public void addTranslation(ITranslationSource translationSource) {
        if (this.bridge != null) {
            throw new RuntimeException("TempModule already enabled and connected to SocialBridge, adding items disallowed now");
        }
        translations.put(translationSource.getClass(), translationSource);
    }

    @SuppressWarnings("unchecked")
    public <T extends ITranslationSource> T getTranslation(Class<T> tClass) {
        var translationSource = translations.getOrDefault(tClass, null);
        if (translationSource != null) {
            return (T) translationSource;
        }
        else {
            return null;
        }
    }

    @Override
    public IModuleLoader getLoader() {
        return loader;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getId() {
        return moduleId;
    }

    public void setId(UUID id) {
        this.moduleId = id;
    }

    @Override
    public void close() {
        SocialBridge.INSTANCE.disconnectModule(this);
    }
}
