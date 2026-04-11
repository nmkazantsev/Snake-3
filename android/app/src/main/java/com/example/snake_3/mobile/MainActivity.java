package com.example.snake_3.mobile;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.nikitos.Engine;
import com.nikitos.main.touch.TouchProcessor;
import com.seal.gl_engine.platform.AndroidLauncher;
import com.seal.gl_engine.platform.AndroidLauncherParams;
import com.seal.gl_engine.touch.AndroidMotionEventAdapter;

import com.example.snake_3.game.MainRenderer;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private Engine engine;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock orientation for this game.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        AndroidLauncherParams androidLauncherParams = new AndroidLauncherParams(getApplicationContext())
                .setDebug(false)
                .setStartPage(unused -> new MainRenderer())
                .setMSAA(true);

        AndroidLauncher androidLauncher = new AndroidLauncher(androidLauncherParams);
        engine = androidLauncher.getEngine();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(uiOptions);

        GLSurfaceView surfaceView = androidLauncher.launch();
        setContentView(surfaceView);
        surfaceView.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        engine.onResume();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        TouchProcessor.onTouch(new AndroidMotionEventAdapter(event));
        return true;
    }
}
