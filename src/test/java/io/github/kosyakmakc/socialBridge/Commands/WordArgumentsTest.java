package io.github.kosyakmakc.socialBridge.Commands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestModule;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands.SimpleWordStringCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

public class WordArgumentsTest {
    @ParameterizedTest
    @CsvSource({
        "0, 0, false",
        "123, 123, false",
        "-123, -123, false",
        
        "0, 0 0, false",
        "123, 123 something, false",
        "-123, -123 -123, false",

        "asd, asd, false",
        "e0d, e0d, false",
        "100asd, 100asd, false",
        "asd100, asd100, false",

        "1e2, 1e2, false",
        "0xff, 0xff, false",
    })
    void simpleIntegerCheck(String answer, String raw, boolean isError) throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        try {
            var module = SocialBridge.INSTANCE.getModule(ArgumentsTestModule.class);
            var command = module.getSocialCommand(SimpleWordStringCommand.class);
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
