package io.github.kosyakmakc.socialBridge.TestEnvironment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.ITransaction;
import io.github.kosyakmakc.socialBridge.Modules.ISocialModule;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.Identifier;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public class HeadlessSocialPlatform implements ISocialPlatform {
    public static final HeadlessSocialPlatform INSTANCE = new HeadlessSocialPlatform();

    private static final String PLATFORM_NAME = "headless";
    private static final UUID PLATFORM_ID = UUID.fromString("fef7e225-fc35-44db-a121-ed1ec0d03f57");
    private static final HashMap<Integer, HeadlessSocialUser> USER_DATABASE = new HashMap<>();

    private LinkedList<ISocialModule> connectedModules = new LinkedList<>();
    // private ISocialBridge socialBridge;

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public UUID getId() {
        return PLATFORM_ID;
    }

    @Override
    public Version getCompabilityVersion() {
        return HeadlessMinecraftPlatform.VERSION;
    }

    @Override
    public CompletableFuture<Void> connectModule(ISocialModule module) {
        connectedModules.add(module);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> disconnectModule(ISocialModule module) {
        connectedModules.remove(module);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> sendMessage(Identifier channelId, String message, HashMap<String, String> placeholders) {
        // TO DO build template
        Logger.getGlobal().info("[social message to channel: " + channelId.value().toString() + "] " + message);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<SocialUser> tryGetUser(Identifier id, ITransaction transaction) {
        var user = USER_DATABASE.getOrDefault((int) id.value(), null);
        return CompletableFuture.completedFuture(user);
    }

    @Override
    public CompletableFuture<Boolean> enable(ISocialBridge socialBridge) {
        // this.socialBridge = socialBridge;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Void> disable() {
        // this.socialBridge = null;
        return CompletableFuture.completedFuture(null);
    }
}
