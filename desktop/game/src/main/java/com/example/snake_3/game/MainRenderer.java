package com.example.snake_3.game;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.utils.Utils;

public class MainRenderer extends GamePageClass {
    private Camera camera;
    private final Shader shader;
    private SnakeGame game;
    private SnakeRenderer renderer;
    private TouchProcessor[] buttonTouchProcessors = new TouchProcessor[0];

    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;

    public MainRenderer() {
        shader = new Shader(
                "vertex_shader_engine.glsl",
                "fragment_shader_engine.glsl",
                this,
                new MainShaderAdaptor()
        );

        // Engine GL bridge (requested: cached fields).
        // Note: CoreRenderer.engine is initialized by the platform launcher before pages are used.
        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        game = new SnakeGame();
        renderer = new SnakeRenderer(this);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        camera = new Camera(width, height);
        camera.resetFor2d();

        game.onSurfaceChanged(width, height);

        renderer.onSurfaceChanged(game);

        rebuildTouchProcessors();
    }

    @Override
    public void draw() {
        if (game == null || renderer == null) {
            Utils.background(0, 0, 0);
            CoreRenderer.engine.glClear();
            return;
        }

        Utils.background(0, 0, 0);
        CoreRenderer.engine.glClear();

        shader.apply();
        gl.glDisable(glc.GL_DEPTH_TEST()); // 2D sprites: rely on draw order, avoid depth-related invisibility
        gl.glDisable(glc.GL_CULL_FACE());
        gl.glBlendFunc(glc.GL_SRC_ALPHA(), glc.GL_ONE_MINUS_SRC_ALPHA());
        CoreRenderer.engine.enableBlend(); // required for sprites with transparent backgrounds (score/winner/explosions)
        camera.resetFor2d();
        camera.apply();
        Matrix.applyMatrix(Matrix.resetTranslateMatrix(new float[16]));

        // Match the original Processing order: render current state, then advance logic for the next frame.
        game.detectTimek();
        renderer.render(game);
        game.logic();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    private void rebuildTouchProcessors() {
        for (TouchProcessor tp : buttonTouchProcessors) {
            if (tp != null) tp.delete();
        }

        if (game == null) {
            buttonTouchProcessors = new TouchProcessor[0];
            return;
        }

        // 2 players * 4 buttons (touch behavior: same as Processing's touchStarted()).
        buttonTouchProcessors = new TouchProcessor[game.getPlayingUsers() * 4];
        int idx = 0;
        for (int snakeId = 0; snakeId < game.getPlayingUsers(); snakeId++) {
            for (int bid = 0; bid < 4; bid++) {
                TouchProcessor tp = getTouchProcessor(bid, snakeId);
                buttonTouchProcessors[idx++] = tp;
            }
        }
    }

    private TouchProcessor getTouchProcessor(int bid, int snakeId) {
        final int buttonId = bid;
        TouchProcessor tp = new TouchProcessor(
                p -> game.getSnakes()[snakeId].getButtons()[buttonId].checkTouch(p.touchX, p.touchY),
                p -> {
                    game.getSnakes()[snakeId].onButtonPressed(game, buttonId);
                    return null;
                },
                  p -> null,
                p -> null,
                this
        );
        tp.setPriority(-10); // leave room for debugger / other high-priority UI.
        return tp;
    }
}
