package dev.padrewin.colddev.command.framework.command.sub;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.argument.ArgumentHandlers;
import dev.padrewin.colddev.command.framework.ArgumentsDefinition;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.command.framework.model.TestEnum;

public class TestSubCommand extends BaseColdCommand {

    public TestSubCommand(ColdPlugin ColdPlugin) {
        super(ColdPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        TestEnum input = context.get("arg1");

        context.getSender().sendMessage(input.name());
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("test")
                .arguments(ArgumentsDefinition.builder()
                        .required("arg1", ArgumentHandlers.forEnum(TestEnum.class))
                        .optionalSub("arg2",
                                new Option1(),
                                new Option2(),
                                new Option3()
                        ))
                .build();
    }

    private class Option1 extends BaseColdCommand {

        public Option1() {
            super(TestSubCommand.this.coldPlugin);
        }

        @ColdExecutable
        public void execute(CommandContext context) {
            TestSubCommand.this.execute(context);

            context.getSender().sendMessage("option1");

            String arg3 = context.get("arg3");
            if (arg3 != null)
                context.getSender().sendMessage(arg3);
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("option1")
                    .arguments(ArgumentsDefinition.builder()
                            .optional("arg3", ArgumentHandlers.STRING)
                            .build())
                    .build();
        }

    }

    private class Option2 extends BaseColdCommand {

        public Option2() {
            super(TestSubCommand.this.coldPlugin);
        }

        @ColdExecutable
        public void execute(CommandContext context) {
            TestSubCommand.this.execute(context);

            String arg3 = context.get("arg3");

            context.getSender().sendMessage("option2");
            context.getSender().sendMessage(arg3);
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("option2")
                    .arguments(ArgumentsDefinition.builder()
                            .required("arg3", ArgumentHandlers.STRING)
                            .build())
                    .build();
        }

    }

    private class Option3 extends BaseColdCommand {

        public Option3() {
            super(TestSubCommand.this.coldPlugin);
        }

        @ColdExecutable
        public void execute(CommandContext context) {
            TestSubCommand.this.execute(context);

            context.getSender().sendMessage("secret-option3");
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("secret-option3")
                    .arguments(ArgumentsDefinition.builder()
                            .required("arg3", ArgumentHandlers.BOOLEAN)
                            .build())
                    .build();
        }

    }

}
