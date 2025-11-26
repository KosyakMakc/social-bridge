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
        class simpleBooleanCommand extends MinecraftCommandBase {
            private final boolean answer;
            public simpleBooleanCommand(boolean answer) {
                super("single argument", List.of(CommandArgument.ofBoolean("single argument")));
                this.answer = answer;
            }

            @Override
            public void execute(MinecraftUser sender, List<Object> args) {
                Assertions.assertEquals(answer, args.getFirst());
            }
        }

        HeadlessMinecraftPlatform.Init();
        try {
            var command = new simpleBooleanCommand(answer);
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
