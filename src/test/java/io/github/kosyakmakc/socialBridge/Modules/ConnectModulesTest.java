package io.github.kosyakmakc.socialBridge.Modules;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.TestEnvironment.VersioningModuleTest;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public class ConnectModulesTest {
    @ParameterizedTest
    @CsvSource({
        // Environment with SocialBridge(0.3.0)
        "0.3.0, false", // is the same version
        "0.3.1, false", // module just have a little bug-fix or new functionality
        "0.3.2, false", // module just have a little bug-fix or new functionality
        "0.3.20, false", // module just have a little bug-fix or new functionality

        "0.4.0, true", // module have new breaking-changes api, DON'T CONNECT
        "0.4.1, true", // module have new breaking-changes api, DON'T CONNECT
        "0.4.99, true", // module have new breaking-changes api, DON'T CONNECT
        "0.2.0, true", // module outdated to SocialBridge breaking-change api, DON'T CONNECT
        "0.2.1, true", // module outdated to SocialBridge breaking-change api, DON'T CONNECT
        "0.2.99, true", // module outdated to SocialBridge breaking-change api, DON'T CONNECT

        "1.0.0, true", // module have new breaking-changes api, DON'T CONNECT
        "1.0.1, true", // module have new breaking-changes api, DON'T CONNECT
        "1.1.0, true", // module have new breaking-changes api, DON'T CONNECT
        "1.1.1, true", // module have new breaking-changes api, DON'T CONNECT
        "1.2.0, true", // module have new breaking-changes api, DON'T CONNECT
        "1.2.1, true", // module have new breaking-changes api, DON'T CONNECT
        "1.3.0, true", // module have new breaking-changes api, DON'T CONNECT
        "1.3.1, true", // module have new breaking-changes api, DON'T CONNECT
        "1.4.0, true", // module have new breaking-changes api, DON'T CONNECT
        "1.4.1, true", // module have new breaking-changes api, DON'T CONNECT
    })
    void checkModuleVersionCompability(String moduleVersion, boolean isError) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        var loader = SocialBridge.INSTANCE.getMinecraftPlatform();
        var module = new VersioningModuleTest("checkModuleVersionCompability", new Version(moduleVersion), loader);
        
        var isConnected = SocialBridge.INSTANCE.connectModule(module).join();

        if (isConnected) {
            SocialBridge.INSTANCE.disconnectModule(module).join();
        }

        if (isConnected && isError) {
            Assertions.fail("MUST failed | " + moduleVersion + " | " + isError);
        }
        if (!isConnected && !isError) {
            Assertions.fail("MUST passing | " + moduleVersion + " | " + isError);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "JustName1",
        "Just_Name2",
        "Just#Name3",
        "JustName4!",
        "$JustName5",
    })
    void checkPassingValidModuleNames(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        var loader = SocialBridge.INSTANCE.getMinecraftPlatform();
        var module = new VersioningModuleTest(moduleName, HeadlessMinecraftPlatform.VERSION, loader);

        var isConnected = SocialBridge.INSTANCE.connectModule(module).join();

        if (isConnected) {
            SocialBridge.INSTANCE.disconnectModule(module).join();
        }

        Assertions.assertTrue(isConnected);
    }

    @ParameterizedTest
    @CsvSource({
        "Just-Name1",
        "Just.Name2",
        "Just Name3",
        "\\'JustName4\\'",
        "\"JustName5\"",
        "`JustName6`",
    })
    void checkDropInvalidModuleNames(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        var loader = SocialBridge.INSTANCE.getMinecraftPlatform();
        var module = new VersioningModuleTest(moduleName, HeadlessMinecraftPlatform.VERSION, loader);

        Assertions.assertThrows(RuntimeException.class, () -> {
            var isConnected = SocialBridge.INSTANCE.connectModule(module).join();
    
            if (isConnected) {
                SocialBridge.INSTANCE.disconnectModule(module).join();
            }
        });
    }
}
