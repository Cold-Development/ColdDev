package dev.padrewin.colddev.command.argument;

import dev.padrewin.colddev.command.framework.Argument;
import dev.padrewin.colddev.command.framework.ArgumentHandler;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.InputIterator;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;

public class DoubleArgumentHandler extends ArgumentHandler<Double> {

    protected DoubleArgumentHandler() {
        super(Double.class);
    }

    @Override
    public Double handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-double", StringPlaceholders.of("input", input));
        }
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return Collections.singletonList(argument.parameter());
    }

}
