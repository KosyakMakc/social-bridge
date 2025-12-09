package io.github.kosyakmakc.socialBridge.Commands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ModuleForTest;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands.SimpleFloatCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

public class FloatArgumentsTest {
    @ParameterizedTest
    @CsvSource({
        "0, 0, false",
        "123, 123, false",
        "-123, -123, false",

        "100, 1e2, false",
        
        "0, 0 0, false",
        "123, 123 something, false",
        "-123, -123 -123, false",

        "0, asd, true",
        "0, e0d, true",
        "0, 100asd, true",
        "0, asd100, true",

        "0, 0.0, false",
        "123.456, 123.456, false",
        "-123.456, -123.456, false",

        "255, 0xff, true", // :(
    })
    void simpleIntegerCheck(float answer, String raw, boolean isError) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        try (var module = new ModuleForTest()) {
            try {
                var command = new SimpleFloatCommand();
                module.addSocialCommand(command);
                SocialBridge.INSTANCE.connectModule(module);
                
                command.prepareAnswer(answer);
                command.handle(null, new StringReader(raw));
                
                if (isError) {
                    Assertions.fail("MUST failed | " + answer + " | " + raw + " | " + isError);
                }
            } catch (ArgumentFormatException e) {
                if (!isError) {
                    Assertions.fail("MUST passing | " + answer + " | " + raw + " | " + isError);
                }
            }
        }
    }
}
