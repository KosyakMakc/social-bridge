package io.github.kosyakmakc.socialBridge.TestEnvironment.ArgumentsTestCommands;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandBase;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;

public class SimpleFloatCommand extends SocialCommandBase {
    private float answer;
    
    public SimpleFloatCommand() {
        super("FloatTest", List.of(CommandArgument.ofFloat("single argument")));
    }
    
    public void prepareAnswer(float answer) {
        this.answer = answer;
    }

    @Override
    public void execute(SocialUser sender, List<Object> args) {
        Assertions.assertEquals(answer, (float) args.getFirst());
    }
}