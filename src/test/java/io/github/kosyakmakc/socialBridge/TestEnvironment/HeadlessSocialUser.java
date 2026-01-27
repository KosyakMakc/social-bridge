package io.github.kosyakmakc.socialBridge.TestEnvironment;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.Identifier;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.IdentifierType;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;

public class HeadlessSocialUser extends SocialUser {
    public static final HeadlessSocialUser Alex = new HeadlessSocialUser(HeadlessSocialPlatform.INSTANCE, "Alex");

    private static int HeadlessGlobalCounter = 1;
    private final Identifier id;
    private final String name;

    public HeadlessSocialUser(ISocialPlatform platform, String name) {
        super(platform);
        id = new Identifier(IdentifierType.Integer, HeadlessGlobalCounter);
        HeadlessGlobalCounter++;

        this.name = name;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocale() {
        return LocalizationService.defaultLocale;
    }

    @Override
    public CompletableFuture<Boolean> sendMessage(String message, HashMap<String, String> placeholders) {
        // TO DO build template
        Logger.getGlobal().info("[social message to: " + getName() + "] " + message);
        return CompletableFuture.completedFuture(true);
    }

}
