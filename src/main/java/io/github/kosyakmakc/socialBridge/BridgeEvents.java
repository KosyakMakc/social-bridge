package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialPlatform;
import io.github.kosyakmakc.socialBridge.Utils.AsyncEvent;

public class BridgeEvents {
    public final AsyncEvent<IModuleBase> moduleConnect = new AsyncEvent<>();
    public final AsyncEvent<IModuleBase> moduleDisconnect = new AsyncEvent<>();

    public final AsyncEvent<ISocialPlatform> socialPlatformConnect = new AsyncEvent<>();
    public final AsyncEvent<ISocialPlatform> socialPlatformDisconnect = new AsyncEvent<>();
}
