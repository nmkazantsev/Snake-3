package com.example.snake_3.game;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.keyboard.KeyReleasedListener;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.Platform;
import com.nikitos.utils.Utils;

public final class SnakeIntroPage extends GamePageClass {
    private static final long ADVANCE_GUARD_MS = 250L;

    private final Shader shader;
    private final boolean desktopPlatform;
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;
    private final SnakeIntroRenderer renderer;

    private Camera camera;
    private KeyReleasedListener enterListener;
    private TouchProcessor touchProcessor;
    private SnakeIntroRenderer.Screen currentScreen = SnakeIntroRenderer.Screen.CONTROLS;
    private long lastAdvanceMs = -ADVANCE_GUARD_MS;

    public SnakeIntroPage() {
        shader = new Shader(
                "vertex_shader_engine.glsl",
                "fragment_shader_engine.glsl",
                this,
                new MainShaderAdaptor()
        );

        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        desktopPlatform = CoreRenderer.engine.getPlatform() == Platform.DESKTOP;
        renderer = new SnakeIntroRenderer(this, desktopPlatform);

        installInput();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        camera = new Camera(width, height);
        camera.resetFor2d();
        renderer.onSurfaceChanged(width, height);
    }

    @Override
    public void draw() {
        Utils.background(0, 0, 0);
        CoreRenderer.engine.glClear();

        if (camera == null) {
            return;
        }

        shader.apply();
        gl.glDisable(glc.GL_DEPTH_TEST());
        gl.glDisable(glc.GL_CULL_FACE());
        gl.glBlendFunc(glc.GL_SRC_ALPHA(), glc.GL_ONE_MINUS_SRC_ALPHA());
        CoreRenderer.engine.enableBlend();
        camera.resetFor2d();
        camera.apply();
        Matrix.applyMatrix(Matrix.resetTranslateMatrix(new float[16]));

        renderer.render();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    private void installInput() {
        if (desktopPlatform) {
            enterListener = new KeyReleasedListener("ENTER", key -> advance(), this);
            return;
        }

        touchProcessor = new TouchProcessor(
                point -> true,
                point -> advance(),
                point -> null,
                point -> null,
                this
        );
        touchProcessor.setPriority(-100);
    }

    private Void advance() {
        long pageMs = CoreRenderer.engine.pageMillis();
        if (pageMs - lastAdvanceMs < ADVANCE_GUARD_MS) {
            return null;
        }
        lastAdvanceMs = pageMs;

        if (currentScreen == SnakeIntroRenderer.Screen.CONTROLS) {
            currentScreen = SnakeIntroRenderer.Screen.RULES;
            renderer.setScreen(currentScreen);
            return null;
        }

        disposeInput();
        renderer.delete();
        CoreRenderer.engine.startNewPage(new MainRenderer());
        return null;
    }

    private void disposeInput() {
        if (enterListener != null) {
            enterListener.delete();
            enterListener = null;
        }
        if (touchProcessor != null) {
            touchProcessor.delete();
            touchProcessor = null;
        }
    }
}
