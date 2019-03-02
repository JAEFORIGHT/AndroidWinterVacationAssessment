package top.zennan.bihu.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import top.zennan.bihu.beans.User;
import top.zennan.bihu.R;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.ToastUtil;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText mAccountET;
    private EditText mPasswordET;
    private Button mLoginBtn;
    private TextView mRegisterTV;
    private String mPasswordText;
    private String mAccountText;
    private String mToken;
    private String mLoginInfo;
    private JSONObject mObject;
    private User mUser;
    private String mToken1;
    private String mAvatar;
    private String mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initView();
        initData();
        initClickListener();
    }

    /**
     * 注册成功跳转到登录界面时自动写好账号密码
     */
    private void initData() {
        Intent intent = getIntent();
        String accountValue = intent.getStringExtra("accountKey");
        String passwordValue = intent.getStringExtra("passwordKey");
        mAccountET.setText(accountValue);
        mPasswordET.setText(passwordValue);
    }

    /**
     * 处理点击事件
     */
    private void initClickListener() {
        //被点击时处理登录的信息
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordText = mPasswordET.getText().toString();
                mAccountText = mAccountET.getText().toString();
                if (mPasswordText.isEmpty()) {
                    ToastUtil.shortToast(
                            LoginActivity.this, "密码不能为空");
                } else if (mAccountText.isEmpty()) {
                    ToastUtil.shortToast(
                            LoginActivity.this, "用户名不能为空");
                } else {
                    login();
                }
            }
        });

        //被点击时跳转到注册界面
        mRegisterTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * 进行登录处理
     */
    private void login() {
        //判断都不为空后需要通过网络请求返回结果
        String url = "http://bihu.jay86.com/login.php";
        String param = "username=" + mAccountText + "&password=" + mPasswordText;
        HttpConnectUtil.doAsyncPost(url, param, new HttpConnectUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "登录返回的response是" + response);
                mToken1 = parseJsonData(response);
                Log.d(TAG, "在登录时获得的token为" + mToken1);
                parseInfoAndAvatar(response);
                mUser = new User();
                mUser.setToken(mToken1);
                mUser.setAvatar(mAvatar);
                mUser.setUserName(mUsername);
                judgeData();
            }
        });
    }

    /**
     * 对返回的数据进行处理
     */
    private void judgeData() {
        if ("success".equals(mLoginInfo)) {
            skipToMainActivity();
        } else {
            ToastUtil.shortToast(LoginActivity.this, "登录失败");
        }
    }

    /**
     * 解析info
     */
    private void parseInfoAndAvatar(String jsonData) {
        try {
            mObject = new JSONObject(jsonData);
            mLoginInfo = mObject.getString("info");
            JSONObject data = mObject.getJSONObject("data");
            mAvatar = data.getString("avatar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到主界面
     */
    private void skipToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user", mUser);
        startActivity(intent);
        finish();
    }

    /**
     * 解析登陆返回的json数据获得token
     */
    private String parseJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if ("200".equals(jsonObject.getString("status"))) {
                JSONObject data = jsonObject.getJSONObject("data");
                mToken = data.getString("token");
                mUsername = data.getString("username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mToken;
    }

    /**
     * 找到控件
     */
    private void initView() {
        mAccountET = this.findViewById(R.id.et_login_account);
        mPasswordET = this.findViewById(R.id.et_login_password);
        mLoginBtn = this.findViewById(R.id.btn_login);
        mRegisterTV = this.findViewById(R.id.tv_login_register);
    }
}
