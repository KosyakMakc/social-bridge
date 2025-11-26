package io.github.kosyakmakc.socialBridge.TestEnvironment;

import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.Utils.Version;
import io.github.kosyakmakc.socialBridge.SocialBridge;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class HeadlessMinecraftPlatform implements IMinecraftPlatform {
    @Override
    public void setAuthBridge(ISocialBridge authBridge) {

    }

    @Override
    public Path getDataDirectory() {
        return Path.of(System.getProperty("java.io.tmpdir"), "social-bridge", UUID.randomUUID().toString());
    }

    @Override
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    @Override
    public MinecraftUser getUser(UUID minecraftId) {
        return null;
    }

    @Override
    public String get(String parameter, String defaultValue) {
        if (Objects.equals(parameter, "connectionString")) {
            return "jdbc:h2:mem:account";
            // return "jdbc:sqlite:social-bridge.sqlite";
        }
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public boolean set(String parameter, String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'set'");
    }

    private static boolean isInited = false;
    public static void Init() throws SQLException, IOException {
        if (isInited) {
            return;
        }

        SocialBridge.Init(new HeadlessMinecraftPlatform());
        isInited = true;
    }

    @Override
    public Version getSocialBridgeVersion() {
        return new Version("0.2.1");
    }
}
