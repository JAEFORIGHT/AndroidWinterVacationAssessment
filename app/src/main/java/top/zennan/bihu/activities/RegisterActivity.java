package top.zennan.bihu.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONObject;

import top.zennan.bihu.R;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.ToastUtil;

public class RegisterActivity extends AppCompatActivity {

    private EditText mAccountET;
    private EditText mPasswordET;
    private Button mRegisterBtn;
    private ImageView mBackIV;
    private String mAccountText;
    private String mPasswordText;
    private String mUrl;
    private String mParam;
    private String mJsonDataValue;
    private String mRegisterInfo;
    private JSONObject mObject = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        initView();
        initOnClickListener();
    }

    /**
     * 监听注册按钮的点击事件
     */
    private void initOnClickListener() {
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取得输入的账号和密码
                mAccountText = mAccountET.getText().toString();
                mPasswordText = mPasswordET.getText().toString();
                //对数据进行判空
                if (mPasswordText.isEmpty()) {
                    ToastUtil.shortToast(
                            RegisterActivity.this, "密码不能为空");
                } else if (mAccountText.isEmpty()) {
                    ToastUtil.shortToast(
                            RegisterActivity.this, "用户名不能为空");
                } else {
                    register();
                }
            }
        });
    }

    /**
     * 注册
     */
    private void register() {
        mUrl = "http://bihu.jay86.com/register.php";
        mParam = "username=" + mAccountText + "&password=" + mPasswordText;
        HttpConnectUtil.doAsyncPost(
                mUrl, mParam, new HttpConnectUtil.CallBack() {
                    @Override
                    public void onResponse(String response) {
                        mJsonDataValue = response;

                        parseJsonData(mJsonDataValue);
                        judgeData();
                    }
                });


    }

    /**
     * 对请求返回的数据的不同做出不同的反应
     */
    private void judgeData() {
        if ("success".equals(mRegisterInfo)) {
            skipToLoginActivity();
        } else {
            ToastUtil.shortToast(RegisterActivity.this, "注册失败");
        }
    }

    /**
     * 解析json数据返回info
     *
     * @param jsonData
     */
    private void parseJsonData(String jsonData) {
        try {
            mObject = new JSONObject(jsonData);
            mRegisterInfo = mObject.getString("info");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册成功后返回登录界面
     */
    private void skipToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("accountKey", mAccountText);
        intent.putExtra("passwordKey", mPasswordText);
        startActivity(intent);
        finish();
    }


    /**
     * 找到控件
     */
    private void initView() {
        mBackIV = this.findViewById(R.id.iv_register_back);
        mAccountET = this.findViewById(R.id.et_register_account);
        mPasswordET = this.findViewById(R.id.et_register_password);
        mRegisterBtn = this.findViewById(R.id.btn_register);
        mBackIV.setImageResource(R.drawable.ic_back);
    }

    /**
     * 处理返回到登录界面的点击事件
     *
     * @param view
     */
    public void back(View view) {
        Intent intent = new Intent(
                RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
