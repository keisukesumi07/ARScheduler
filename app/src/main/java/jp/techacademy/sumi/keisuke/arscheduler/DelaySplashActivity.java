package jp.techacademy.sumi.keisuke.arscheduler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class DelaySplashActivity extends AppCompatActivity {

    private final static int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delay_splash);
        ImageView imageView = findViewById(R.id.splash);

        if (!CameraPermission.hasCameraPermission(this)) {
            CameraPermission.requestCameraPermission(this);
            return;
        }

        Animation animation= AnimationUtils.loadAnimation(this,
                R.anim.alpha_fadein);
        imageView.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(DelaySplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);

    }

    /*バックキー無効にし強制スプラッシュ*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermission.hasCameraPermission(this)) {
            Toast.makeText(this, "カメラの権限がないためアプリを終了します。", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermission.shouldShowRequestPermissionRationale(this)) {
                CameraPermission.launchPermissionSettings(this);
            }
            finish();
        }
        ImageView imageView = findViewById(R.id.splash);
        Animation animation= AnimationUtils.loadAnimation(this,
                R.anim.alpha_fadein);
        imageView.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(DelaySplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);

    }
}