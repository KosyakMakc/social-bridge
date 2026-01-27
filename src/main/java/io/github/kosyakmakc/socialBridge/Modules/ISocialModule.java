package io.github.kosyakmakc.socialBridge.Modules;

import java.util.Collection;

import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.ISocialCommand;

public interface ISocialModule extends IModuleBase {
    Collection<ISocialCommand> getSocialCommands();
}
