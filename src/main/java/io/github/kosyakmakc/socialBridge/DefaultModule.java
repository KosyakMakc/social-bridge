package io.github.kosyakmakc.socialBridge;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.English;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.DefaultTranslations.Russian;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.Modules.SocialModule;
import java.util.UUID;

public class DefaultModule extends SocialModule {
    public static final UUID MODULE_ID = UUID.fromString("dcab3770-b24e-44bb-b9a9-19edf96b9986");
    public static final String MODULE_NAME = "socialbridge";

    public DefaultModule(IMinecraftPlatform loader) {
        super(loader, loader.getSocialBridgeVersion(), MODULE_ID, MODULE_NAME);
        addTranslationSource(new English());
        addTranslationSource(new Russian());
    }
}
