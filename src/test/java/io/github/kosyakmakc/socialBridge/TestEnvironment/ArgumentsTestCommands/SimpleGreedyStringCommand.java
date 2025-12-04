package io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandBase;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;

public class SimpleGreedyStringCommand extends SocialCommandBase {
    private String answer;
    
    public SimpleGreedyStringCommand() {
        super("GreedyStringTest", List.of(CommandArgument.ofGreedyString("single argument")));
    }
    
    public void prepareAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public void execute(SocialUser sender, List<Object> args) {
        Assertions.assertEquals(answer, args.getFirst());
    }
}