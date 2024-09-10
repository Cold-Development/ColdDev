package dev.padrewin.coldplugin.command.argument;

import dev.padrewin.coldplugin.command.framework.Argument;
import dev.padrewin.coldplugin.command.framework.ArgumentHandler;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.InputIterator;
import dev.padrewin.coldplugin.utils.StringPlaceholders;
import java.util.Collections;
import java.util.List;

public class FloatArgumentHandler extends ArgumentHandler<Float> {

    protected FloatArgumentHandler() {
        super(Float.class);
    }

    @Override
    public Float handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        try {
            return Float.parseFloat(input);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-float", StringPlaceholders.of("input", input));
        }
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return Collections.singletonList(argument.parameter());
    }

}
