package dev.padrewin.coldplugin.command.framework.command.suggest;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.argument.ArgumentHandlers;
import dev.padrewin.coldplugin.command.framework.ArgumentsDefinition;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.CommandInfo;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.command.framework.handler.TestArgumentHandler;
import dev.padrewin.coldplugin.command.framework.model.TestEnum;

public class SuggestionCommand extends BaseColdCommand {

    public SuggestionCommand(ColdPlugin coldPlugin) {
        super(coldPlugin);
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
