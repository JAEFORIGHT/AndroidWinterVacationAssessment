package top.zennan.bihu.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import top.zennan.bihu.R;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideTheStatusBarAndTitleBar();
        setContentView(R.layout.activity_start);
        initImage();
        skipToHomePage();
    }

    /**
     * 隐藏状态栏和标题栏
     */
    private void hideTheStatusBarAndTitleBar() {
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 来显示三秒图片然后跳转到登陆界面
     */
    private void skipToHomePage() {
        final Intent intent = new Intent(this, LoginActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(intent);
            }
        };
        timer.schedule(task, 3 * 1000);
    }

    /**
     * 找控件并设置图片
     */
    private void initImage() {
        ImageView startIV = this.findViewById(R.id.iv_start);
        startIV.setImageResource(R.mipmap.ic_start);
    }

    /**
     * 跳转到登陆界面时关闭当前界面
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
