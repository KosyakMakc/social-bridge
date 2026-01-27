package io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandBase;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandExecutionContext;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

public class SimpleGreedyStringCommand extends SocialCommandBase {
    public static final String NAME = "GreedyStringTest";

    private String answer;

    public SimpleGreedyStringCommand() {
        super(NAME, MessageKey.EMPTY, List.of(CommandArgument.ofGreedyString("single argument")));
    }

    public void prepareAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public void execute(SocialCommandExecutionContext context, List<Object> args) {
        Assertions.assertEquals(answer, args.getFirst());
    }
}