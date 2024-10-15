package dev.padrewin.colddev.command.argument;

import dev.padrewin.colddev.command.framework.Argument;
import dev.padrewin.colddev.command.framework.ArgumentHandler;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.InputIterator;
import java.util.Collections;
import java.util.List;

public class StringArgumentHandler extends ArgumentHandler<String> {

    protected StringArgumentHandler() {
        super(String.class);
    }

    @Override
    public String handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        if (input.trim().isEmpty())
            throw new HandledArgumentException("argument-handler-string");
        return input;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return Collections.singletonList(argument.parameter());
    }

}
