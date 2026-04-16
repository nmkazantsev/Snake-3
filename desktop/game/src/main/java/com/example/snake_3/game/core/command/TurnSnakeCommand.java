package com.example.snake_3.game.core.command;

public record TurnSnakeCommand(int snakeId, int buttonIndex) implements GameCommand {
}
