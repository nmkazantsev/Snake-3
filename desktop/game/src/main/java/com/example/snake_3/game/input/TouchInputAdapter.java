package com.example.snake_3.game.input;

import com.example.snake_3.game.core.command.TurnSnakeCommand;
import com.nikitos.GamePageClass;
import com.nikitos.main.touch.TouchProcessor;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public final class TouchInputAdapter {
    private final GamePageClass page;
    private final Consumer<TurnSnakeCommand> commandConsumer;
    private TouchProcessor[] processors = new TouchProcessor[0];
    private TouchButtonRect[][] currentButtons = new TouchButtonRect[0][];

    public TouchInputAdapter(GamePageClass page, Consumer<TurnSnakeCommand> commandConsumer) {
        this.page = page;
        this.commandConsumer = commandConsumer;
    }

    public void sync(TouchButtonRect[][] newButtons) {
        if (sameLayout(newButtons)) {
            return;
        }
        rebuild(newButtons);
    }

    public void dispose() {
        for (TouchProcessor processor : processors) {
            if (processor != null) {
                processor.delete();
            }
        }
        processors = new TouchProcessor[0];
        currentButtons = new TouchButtonRect[0][];
    }

    private boolean sameLayout(TouchButtonRect[][] newButtons) {
        if (currentButtons.length != newButtons.length) {
            return false;
        }
        for (int index = 0; index < currentButtons.length; index++) {
            if (!Arrays.equals(currentButtons[index], newButtons[index])) {
                return false;
            }
        }
        return true;
    }

    private void rebuild(TouchButtonRect[][] newButtons) {
        dispose();
        currentButtons = Arrays.stream(newButtons).map(buttons -> buttons == null ? new TouchButtonRect[0] : Arrays.copyOf(buttons, buttons.length)).toArray(TouchButtonRect[][]::new);

        processors = Arrays.stream(currentButtons)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(this::createProcessor)
                .toArray(TouchProcessor[]::new);
    }

    private TouchProcessor createProcessor(TouchButtonRect button) {
        TouchProcessor processor = new TouchProcessor(
                point -> button.contains(point.touchX, point.touchY),
                point -> {
                    commandConsumer.accept(new TurnSnakeCommand(button.snakeId(), button.buttonIndex()));
                    return null;
                },
                point -> null,
                point -> null,
                page
        );
        processor.setPriority(-10);
        return processor;
    }
}
