package io.github.kosyakmakc.socialBridge.Commands.SocialCommands;

import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialMessage;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;

public abstract class SocialCommandExecutionContext {
    private ISocialMessage message;

    public SocialCommandExecutionContext (ISocialMessage message) {
        this.message = message;
    }

    public ISocialMessage getSocialMessage() {
        return message;
    }
    

    public abstract String getFullMessage();

    public abstract String getMessage();

    public SocialUser getSender() {
        return message.getAuthor();
    }
}
