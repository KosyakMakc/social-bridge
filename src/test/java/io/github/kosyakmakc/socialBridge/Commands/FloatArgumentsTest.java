package io.github.kosyakmakc.socialBridge.Commands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.MinecraftCommandBase;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;

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
        class simpleFloatCommand extends MinecraftCommandBase {
            private final float answer;
            public simpleFloatCommand(float answer) {
                super("single argument", List.of(CommandArgument.ofFloat("single argument")));
                this.answer = answer;
            }

            @Override
            public void execute(MinecraftUser sender, List<Object> args) {
                Assertions.assertEquals(answer, (float) args.getFirst());
            }
        }

        HeadlessMinecraftPlatform.Init();
        try {
            var command = new simpleFloatCommand(answer);
            command.init(SocialBridge.INSTANCE);
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
