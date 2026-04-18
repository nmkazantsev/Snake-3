package com.example.snake_3.game.input;

import com.nikitos.main.keyboard.KeyListener;
import com.nikitos.main.keyboard.KeyboardProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DesktopKeyboardFramePumpTest {
    @AfterEach
    void cleanupPressedKeys() {
        KeyboardProcessor.onKeyReleased("ENTER");
        KeyboardProcessor.processKeys();
    }

    @Test
    void keyPressedCallbacksAreQueuedUntilKeyboardProcessorRuns() {
        AtomicInteger callbackCount = new AtomicInteger();
        KeyListener listener = new KeyListener("ENTER", key -> {
            callbackCount.incrementAndGet();
            return null;
        }, null);

        try {
            KeyboardProcessor.onKeyPressed("ENTER");
            assertEquals(0, callbackCount.get(), "Engine queues key callbacks instead of invoking them immediately");

            DesktopKeyboardFramePump.flushPendingEvents();
            assertEquals(1, callbackCount.get(), "Flushing at frame start should expose the pending press before page logic");

            DesktopKeyboardFramePump.flushPendingEvents();
            assertEquals(1, callbackCount.get(), "Once the queue is drained, a second flush must not double-trigger the press");
        } finally {
            listener.delete();
        }
    }
}
