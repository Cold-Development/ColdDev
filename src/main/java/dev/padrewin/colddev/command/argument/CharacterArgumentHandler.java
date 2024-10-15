package dev.padrewin.colddev.command.argument;

import dev.padrewin.colddev.command.framework.Argument;
import dev.padrewin.colddev.command.framework.ArgumentHandler;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.InputIterator;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;

public class CharacterArgumentHandler extends ArgumentHandler<Character> {

    protected CharacterArgumentHandler() {
        super(Character.class);
    }

    @Override
    public Character handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        if (input.length() != 1)
            throw new HandledArgumentException("argument-handler-character", StringPlaceholders.of("input", input));
        return input.charAt(0);
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return Collections.singletonList(argument.parameter());
    }

}
