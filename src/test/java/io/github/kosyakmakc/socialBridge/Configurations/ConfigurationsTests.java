package io.github.kosyakmakc.socialBridge.Configurations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.kosyakmakc.socialBridge.DefaultModule;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;

public class ConfigurationsTests {
    @ParameterizedTest
    @CsvSource({
        "__Test__Create1, 1",
        "__Test__Create2, a",
        "__Test__Create3, $",
    })
    void CheckCreates(String name, String value) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        var service = SocialBridge.INSTANCE.getConfigurationService();

        service.set(DefaultModule.MODULE_ID, name, value).join();
        Assertions.assertEquals(value, service.get(DefaultModule.MODULE_ID, name, "").join());
    }

    @ParameterizedTest
    @CsvSource({
        "__Test__Change1, 1",
        "__Test__Change1, a",
        "__Test__Change2, $",
        "__Test__Change2, $",
    })
    void CheckChanges(String name, String value) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        var service = SocialBridge.INSTANCE.getConfigurationService();

        service.set(DefaultModule.MODULE_ID, name, value).join();
        Assertions.assertEquals(value, service.get(DefaultModule.MODULE_ID, name, "").join());
    }

    @Test
    void CheckNotExisted() throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        var service = SocialBridge.INSTANCE.getConfigurationService();

        var defaultValue = UUID.randomUUID().toString();

        Assertions.assertEquals(defaultValue, service.get(DefaultModule.MODULE_ID, "__Test__" + UUID.randomUUID().toString(), defaultValue).join());
    }

    @Test
    void CheckDropEmptyParameterName() throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        var service = SocialBridge.INSTANCE.getConfigurationService();

        Assertions.assertThrows(RuntimeException.class, () -> {
            service.set(DefaultModule.MODULE_ID, "", "test").join();
        });
    }
}
