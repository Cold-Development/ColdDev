package dev.padrewin.colddev.command.framework.command.suggest;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.command.framework.handler.TestArgumentHandler;
import dev.padrewin.colddev.command.framework.model.TestEnum;

public class SuggestionCommand extends BaseColdCommand {

    public SuggestionCommand(ColdPlugin ColdPlugin) {
        super(ColdPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        throw new UnsupportedOperationException("Only for testing suggestions");
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .arguments(ArgumentsDefinition.builder()
                        .required("arg1", new TestArgumentHandler())
                        .required("arg2", ArgumentHandlers.forEnum(TestEnum.class))
                        .build())
                .build();
    }

}
