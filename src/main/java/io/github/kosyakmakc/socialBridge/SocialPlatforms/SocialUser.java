package io.github.kosyakmakc.socialBridge.SocialPlatforms;

import java.util.HashMap;

public abstract class SocialUser {
    private final ISocialPlatform platform;

    public SocialUser(ISocialPlatform platform) {
        this.platform = platform;
    }

    public ISocialPlatform getPlatform() {
        return platform;
    }

    public abstract Identifier getId();
    public abstract String getName();
    public abstract String getLocale();

    public abstract void sendMessage(String message, HashMap<String, String> placeholders);

}
