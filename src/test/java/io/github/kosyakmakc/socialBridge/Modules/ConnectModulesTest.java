package io.github.kosyakmakc.socialBridge.Modules;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.MinecraftCommandBase;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandBase;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ModuleForTest;
import io.github.kosyakmakc.socialBridge.Utils.Version;

public class ConnectModulesTest {
    @ParameterizedTest
    @CsvSource({
        // Environment with SocialBridge(0.3.0)
        "0.3.0, false", // is the same version
        "0.3.1, false", // module just have a little bug-fix or new functionality
        "0.3.2, false", // module just have a little bug-fix or new functionality
        "0.3.20, false", // module just have a little bug-fix or new functionality
    })
    void checkModuleVersionCompability(String moduleVersion) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.setCompabilityVersion(new Version(moduleVersion));
            
            var isConnected = SocialBridge.INSTANCE.connectModule(module).join();
            
            if (isConnected) {
                SocialBridge.INSTANCE.disconnectModule(module).join();
            }
            
            Assertions.assertTrue(isConnected);
        }
    }
    @ParameterizedTest
    @CsvSource({
        // Environment with SocialBridge(0.3.0)
        "0.4.0", // module have new breaking-changes api, DON'T CONNECT
        "0.4.1", // module have new breaking-changes api, DON'T CONNECT
        "0.4.99", // module have new breaking-changes api, DON'T CONNECT
        "0.2.0", // module outdated to SocialBridge breaking-change api, DON'T CONNECT
        "0.2.1", // module outdated to SocialBridge breaking-change api, DON'T CONNECT
        "0.2.99", // module outdated to SocialBridge breaking-change api, DON'T CONNECT

        "1.0.0", // module have new breaking-changes api, DON'T CONNECT
        "1.0.1", // module have new breaking-changes api, DON'T CONNECT
        "1.1.0", // module have new breaking-changes api, DON'T CONNECT
        "1.1.1", // module have new breaking-changes api, DON'T CONNECT
        "1.2.0", // module have new breaking-changes api, DON'T CONNECT
        "1.2.1", // module have new breaking-changes api, DON'T CONNECT
        "1.3.0", // module have new breaking-changes api, DON'T CONNECT
        "1.3.1", // module have new breaking-changes api, DON'T CONNECT
        "1.4.0", // module have new breaking-changes api, DON'T CONNECT
        "1.4.1", // module have new breaking-changes api, DON'T CONNECT
    })
    void checkModuleVersionUncompability(String moduleVersion) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.setCompabilityVersion(new Version(moduleVersion));
            
            var isConnected = SocialBridge.INSTANCE.connectModule(module).join();
            
            Assertions.assertFalse(isConnected);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "JustName1",
        "Just_Name2",
        "Just#Name3",
        "JustName4!",
        "$JustName5",
        "\\'JustName6\\'",
        "\"JustName7\"",
        "`JustName8`",
    })
    void checkPassingValidModuleNames(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.setName(moduleName);
            
            var isConnected = SocialBridge.INSTANCE.connectModule(module).join();
            
            Assertions.assertTrue(isConnected);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "Just-Name1",
        "Just Name2",
        "Just\tName3",
        // "\"Just\nName4\"", // in unit test \n not working properly
        // "\"Just\rName5\"", // in unit test \r not working properly
        "Just.Name6",
    })
    void checkDropInvalidModuleNames(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.setName(moduleName);
            
            Assertions.assertThrows(RuntimeException.class, () -> {
                SocialBridge.INSTANCE.connectModule(module).join();
            });
        }
    }

    @ParameterizedTest
    @CsvSource({
        "JustName1",
        "Just_Name2",
        "Just#Name3",
        "JustName4!",
        "$JustName5",
        "\\'JustName6\\'",
        "\"JustName7\"",
        "`JustName8`",
        "Just-Name9",
        "Just.Name10",
    })
    void checkPassingValidMinecraftCommandLiteral(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.addMinecraftCommand(new MinecraftCommandBase(moduleName) {
                @Override
                public void execute(MinecraftUser sender, List<Object> args) { }
                
            });
            
            var isConnected = SocialBridge.INSTANCE.connectModule(module).join();
            
            Assertions.assertTrue(isConnected);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "Just Name1",
        "Just\tName2",
        // "\"Just\nName3\"", // in unit test \n not working properly
        // "\"Just\rName4\"", // in unit test \r not working properly
    })
    void checkPassingInvalidMinecraftCommandLiteral(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.addMinecraftCommand(new MinecraftCommandBase(moduleName) {
                @Override
                public void execute(MinecraftUser sender, List<Object> args) { }
                
            });
            
            Assertions.assertThrows(RuntimeException.class, () -> {
                SocialBridge.INSTANCE.connectModule(module).join();
            });
        }
    }

    @ParameterizedTest
    @CsvSource({
        "JustName1",
        "Just_Name2",
        "Just#Name3",
        "JustName4!",
        "$JustName5",
        "\\'JustName6\\'",
        "\"JustName7\"",
        "`JustName8`",
        "Just-Name9",
        "Just.Name10",
    })
    void checkPassingValidSocialCommandLiteral(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.addSocialCommand(new SocialCommandBase(moduleName) {
                @Override
                public void execute(SocialUser sender, List<Object> args) { }
                
            });
            
            var isConnected = SocialBridge.INSTANCE.connectModule(module).join();
            
            Assertions.assertTrue(isConnected);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "Just Name1",
        "Just\tName2",
        // "\"Just\nName3\"", // in unit test \n not working properly
        // "\"Just\rName4\"", // in unit test \r not working properly
    })
    void checkPassingInvalidSocialCommandLiteral(String moduleName) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();

        try (var module = new ModuleForTest()) {
            module.addSocialCommand(new SocialCommandBase(moduleName) {
                @Override
                public void execute(SocialUser sender, List<Object> args) { }
                
            });
            
            Assertions.assertThrows(RuntimeException.class, () -> {
                SocialBridge.INSTANCE.connectModule(module).join();
            });
        }
    }
}
