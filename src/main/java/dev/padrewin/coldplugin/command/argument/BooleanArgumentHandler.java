package dev.padrewin.coldplugin.command.argument;

import dev.padrewin.coldplugin.command.framework.Argument;
import dev.padrewin.coldplugin.command.framework.ArgumentHandler;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.InputIterator;
import dev.padrewin.coldplugin.utils.StringPlaceholders;
import java.util.Arrays;
import java.util.List;

public class BooleanArgumentHandler extends ArgumentHandler<Boolean> {

    protected BooleanArgumentHandler() {
        super(Boolean.class);
    }

    @Override
    public Boolean handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        switch (input.toLowerCase()) {
            case "true": return true;
            case "false": return false;
            default: throw new HandledArgumentException("argument-handler-boolean", StringPlaceholders.of("input", input));
        }
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return Arrays.asList("true", "false");
    }

}
