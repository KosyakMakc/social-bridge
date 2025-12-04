package io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandBase;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;


public class SimpleIntegerCommand extends SocialCommandBase {
    private int answer;
    
    public SimpleIntegerCommand() {
        super("IntegerTest", List.of(CommandArgument.ofInteger("single argument")));
    }
    
    public void prepareAnswer(int answer) {
        this.answer = answer;
    }

    @Override
    public void execute(SocialUser sender, List<Object> args) {
        Assertions.assertEquals(answer, (int) args.getFirst());
    }
}
