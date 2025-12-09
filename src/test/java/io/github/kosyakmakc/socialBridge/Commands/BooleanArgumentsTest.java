package io.github.kosyakmakc.socialBridge.Commands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ModuleForTest;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands.SimpleBooleanCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

public class BooleanArgumentsTest {
    @ParameterizedTest
    @CsvSource({
        "true, true, false",
        "true, TRUE, false",
        "true, True, false",

        "false, false, false",
        "false, FALSE, false",
        "false, False, false",

        "false, foo, false",
        "false, bar, false",
        "false, falsee, false",
        "false, truee, false",

        "false, 0, false", // :(
        "false, 1, false", // :(
    })
    void simpleIntegerCheck(boolean answer, String raw, boolean isError) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        try (var module = new ModuleForTest()) {
            try {

                var command = new SimpleBooleanCommand();
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
