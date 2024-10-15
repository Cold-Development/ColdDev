package dev.padrewin.colddev.command.framework.command.required;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;

public class NoArgsCommand extends BaseColdCommand {

    public static final String SUCCESS_OUTPUT = "success";

    public NoArgsCommand(ColdPlugin ColdPlugin) {
        super(ColdPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        context.getSender().sendMessage(SUCCESS_OUTPUT);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test").build();
    }

}
