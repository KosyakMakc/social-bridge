package io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands;

import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;

public class MinecraftCommandExecutionContext {
    private final MinecraftUser sender;
    private final String message;

    private MinecraftCommandExecutionContext(MinecraftUser sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public MinecraftUser getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
