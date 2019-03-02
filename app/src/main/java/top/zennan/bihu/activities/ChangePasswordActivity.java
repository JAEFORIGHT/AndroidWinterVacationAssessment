package top.zennan.bihu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import top.zennan.bihu.beans.User;
import top.zennan.bihu.R;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.ToastUtil;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";
    private TextView mBackTV;
    private EditText mNewET;
    private Button mChangeBtn;

    private String mNewPassword;
    private String mChangePasswordJson = null;
    private String mStatus;
    private String mInfo;
    private String mUrl;
    private String mParam;
    private User mUser;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initView();
        initOnClickListener();
    }

    /**
     * 处理点击事件
     */
    private void initOnClickListener() {
        mBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                mUser = intent.getParcelableExtra("user");
                String token = mUser.getToken();
                Log.d(TAG, "改变密码的token为" + token);
                mUrl = "http://bihu.jay86.com/changePassword.php";
                mNewPassword = mNewET.getText().toString();
                mParam = "password=" + mNewPassword + "&token=" + token;
                Log.d(TAG, "EditText获取到的密码为" + mNewPassword);
                HttpConnectUtil.doAsyncPost(mUrl, mParam, new HttpConnectUtil.CallBack() {
                    @Override
                    public void onResponse(String response) {
                        mChangePasswordJson = response;
                        Log.d(TAG, "更改密码时返回的json为" + mChangePasswordJson);

                        try {
                            JSONObject object = new JSONObject(mChangePasswordJson);
                            mStatus = object.getString("status");
                            mInfo = object.getString("info");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if ("200".equals(mStatus)) {
                            //成功了之后获取新的token
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/login.php", "username=" + mUser.getUserName() + "&password=" + mNewPassword, new HttpConnectUtil.CallBack() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "修改密码后重新登录返回的json数据是" + response);
                                    try {
                                        JSONObject loginResult = new JSONObject(response);
                                        JSONObject object = loginResult.getJSONObject("data");
                                        mToken = object.getString("token");
                                        Log.d(TAG, "修改密码后重新登录返回的token数据是" + mToken);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            mUser.setToken(mToken);
                            Intent intent1 = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                            intent1.putExtra("user", mUser);
                            startActivity(intent1);
                            finish();
                            ToastUtil.shortToast(ChangePasswordActivity.this, "更改成功");
                        } else {
                            ToastUtil.shortToast(ChangePasswordActivity.this, mInfo);
                        }
                    }
                });
            }
        });
    }

    /**
     * 实例化控件
     */
    private void initView() {
        mBackTV = findViewById(R.id.tv_change_password_back);
        mNewET = findViewById(R.id.et_change_password_new);
        mChangeBtn = findViewById(R.id.btn_change_password);
    }
}
