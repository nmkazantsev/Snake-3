package com.example.snake_3.game;

import com.example.snake_3.game.app.SnakeGameController;
import com.example.snake_3.game.core.command.GameCommand;
import com.example.snake_3.game.infra.SystemClock;
import com.example.snake_3.game.infra.UtilsRandomSource;
import com.example.snake_3.game.input.DesktopInputAdapter;
import com.example.snake_3.game.input.DesktopKeyboardFramePump;
import com.example.snake_3.game.input.TouchInputAdapter;
import com.example.snake_3.game.render.vm.GameViewModel;
import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.Platform;
import com.nikitos.utils.Utils;

public class MainRenderer extends GamePageClass {
    private final Shader shader;
    private final boolean desktopPlatform;
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;
    private final SnakeGameController controller;
    private final SnakeRenderer renderer;
    private final DesktopInputAdapter desktopInputAdapter;
    private final TouchInputAdapter touchInputAdapter;

    private Camera camera;

    public MainRenderer() {
        shader = new Shader(
                "vertex_shader_engine.glsl",
                "fragment_shader_engine.glsl",
                this,
                new MainShaderAdaptor()
        );

        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        desktopPlatform = CoreRenderer.engine.getPlatform() == Platform.DESKTOP;

        controller = new SnakeGameController(desktopPlatform, new SystemClock(), new UtilsRandomSource());
        renderer = new SnakeRenderer(this, desktopPlatform);

        desktopInputAdapter = desktopPlatform ? new DesktopInputAdapter(this, this::enqueueCommand) : null;
        touchInputAdapter = desktopPlatform ? null : new TouchInputAdapter(this, this::enqueueCommand);

        if (desktopInputAdapter != null) {
            desktopInputAdapter.install();
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        camera = new Camera(width, height);
        camera.resetFor2d();

        controller.onSurfaceChanged(width, height);
        GameViewModel viewModel = controller.getViewModel();
        if (viewModel != null) {
            renderer.onSurfaceChanged(viewModel);
        }

        if (touchInputAdapter != null) {
            touchInputAdapter.sync(controller.getTouchButtons());
        }
    }

    @Override
    public void draw() {
        if (desktopInputAdapter != null) {
            DesktopKeyboardFramePump.flushPendingEvents();
        }

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

        controller.tickFrame();
        if (touchInputAdapter != null) {
            touchInputAdapter.sync(controller.getTouchButtons());
        }

        renderer.render(controller.getViewModel());
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    private void enqueueCommand(GameCommand command) {
        controller.enqueue(command);
    }
}
