package io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandBase;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandExecutionContext;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

public class SimpleBooleanCommand extends SocialCommandBase {
    public static final String NAME = "BooleanTest";

    private boolean answer;

    public SimpleBooleanCommand() {
        super(NAME, MessageKey.EMPTY, List.of(CommandArgument.ofBoolean("single argument")));
    }

    public void prepareAnswer(boolean answer) {
        this.answer = answer;
    }

    @Override
    public void execute(SocialCommandExecutionContext context, List<Object> args) {
        Assertions.assertEquals(answer, args.getFirst());
    }
}