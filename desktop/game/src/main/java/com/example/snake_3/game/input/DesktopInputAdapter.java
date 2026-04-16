package com.example.snake_3.game.input;

import com.example.snake_3.game.core.command.TurnSnakeCommand;
import com.nikitos.GamePageClass;
import com.nikitos.main.keyboard.KeyListener;

import java.util.function.Consumer;

public final class DesktopInputAdapter {
    private final GamePageClass page;
    private final Consumer<TurnSnakeCommand> commandConsumer;
    private KeyListener[] keyListeners = new KeyListener[0];

    public DesktopInputAdapter(GamePageClass page, Consumer<TurnSnakeCommand> commandConsumer) {
        this.page = page;
        this.commandConsumer = commandConsumer;
    }

    public void install() {
        dispose();
        keyListeners = new KeyListener[]{
                new KeyListener("W", key -> enqueue(0, 0), page),
                new KeyListener("D", key -> enqueue(0, 1), page),
                new KeyListener("S", key -> enqueue(0, 2), page),
                new KeyListener("A", key -> enqueue(0, 3), page),
                new KeyListener("UP", key -> enqueue(1, 0), page),
                new KeyListener("RIGHT", key -> enqueue(1, 1), page),
                new KeyListener("DOWN", key -> enqueue(1, 2), page),
                new KeyListener("LEFT", key -> enqueue(1, 3), page)
        };
    }

    public void dispose() {
        for (KeyListener keyListener : keyListeners) {
            if (keyListener != null) {
                keyListener.delete();
            }
        }
        keyListeners = new KeyListener[0];
    }

    private Void enqueue(int snakeId, int buttonIndex) {
        commandConsumer.accept(new TurnSnakeCommand(snakeId, buttonIndex));
        return null;
    }
}
