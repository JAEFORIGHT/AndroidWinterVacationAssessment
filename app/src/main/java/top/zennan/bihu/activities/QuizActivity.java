package top.zennan.bihu.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import top.zennan.bihu.beans.User;
import top.zennan.bihu.R;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.ToastUtil;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private EditText mQuizTitleET;
    private EditText mQuizContentTitleET;
    private Button mQuizBtn;
    private TextView mQuizBackTV;
    private TextView mQuizAddPicTV;
    private Intent mIntent;
    private String mTitleText;
    private String mContentText;
    private Uri mImageUri;
    private ImageView mQuizImageIV;
    private String mImgPath;
    private String mFileNameString;
    private final String DOMAIN_NAME = "http://pn09wx14o.bkt.clouddn.com";
    private String mImgUrl = null;
    private String mLoginToken;
    private String mUploadData;
    private String mQuizUrl;
    private String mZiLaiTokenUrl;
    private String mTokenGetParam;
    private String mQiniuTokenJsonDataValue;
    private String mToken;
    private Configuration mConfig;
    private JSONObject mObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        initView();
        initListener();

    }

    /**
     * 设置点击事件的监听
     */
    private void initListener() {
        //放弃提问返回主页
        mQuizBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //提交问题
        mQuizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImgPath != null) {
                    UploadPicsToQiNiuCloud();
                    Log.d(TAG, "开始上传图片到七牛云");
                } else {
                    submitQuestions();
                }


                Log.d(TAG, "开始提交问题");

            }
        });
        //添加图片
        mQuizAddPicTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "开始添加图片");
                pickImageFromAlbum();
            }
        });

    }

    /**
     * 上传文件到七牛云
     */
    private void UploadPicsToQiNiuCloud() {
        mConfig = new Configuration.Builder()
                .chunkSize(512 * 1024)
                .putThreshhold(1024 * 1024)
                .connectTimeout(10)
                .useHttps(true)
                .responseTimeout(60)
                .build();
        getQiNiuToken();
    }

    /**
     * 每次上传图片时都获取一次七牛token
     */
    private void getQiNiuToken() {
        mZiLaiTokenUrl = "http://zzzia.net:8080/qiniu/";
        mTokenGetParam = "accessKey=JSz5JslQIg5StvzaySYZGnRU85ZeeobYKBLjnbul" +
                "&secretKey=p67Ujpx4cHOFXPsJQWeXHupmHbFx3NhzahCbyFGM" +
                "&bucket=imgres";
        HttpConnectUtil.doAsyncPost(
                mZiLaiTokenUrl, mTokenGetParam, new HttpConnectUtil.CallBack() {
                    @Override
                    public void onResponse(String response) {
                        mQiniuTokenJsonDataValue = response;
                        Log.d(TAG, "获取token时的json数据是" + mQiniuTokenJsonDataValue);
                        //解析json
                        try {
                            JSONObject object = new JSONObject(mQiniuTokenJsonDataValue);
                            String info = object.getString("info");
                            if ("success".equals(info)) {
                                String token = object.getString("token");
                                if (token != null) {
                                    mToken = token;
                                    UploadManager uploadManager = new UploadManager(mConfig);
                                    Log.d(TAG, "上传图片的参数 ：路径====" + mImgPath + "token===" + mToken);
                                    uploadManager.put(mImgPath, null, mToken, new UpCompletionHandler() {

                                        @Override
                                        public void complete(String key, ResponseInfo info, JSONObject response) {
                                            if (info.isOK()) {
                                                Log.i(TAG, "上传成功");
                                                //上传成功后获取该图片
                                                try {
                                                    mFileNameString = response.getString("key");
                                                    mImgUrl = DOMAIN_NAME + "/" + mFileNameString;
                                                    submitQuestions();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Log.d(TAG, "上传失败");
                                            }
                                            Log.d(TAG, "返回的信息是" + response);

                                        }
                                    }, null);
                                }
                            } else {
                                ToastUtil.shortToast(QuizActivity.this, "您的网络可能不是很好");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

    }

    /**
     * 将图片的uri转换为路径
     */
    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * 获取选择图片的uri
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //当点击了取消之后
        if (requestCode == RESULT_CANCELED) {
            ToastUtil.shortToast(QuizActivity.this, "您真的不从相册选择图片吗？");
            return;
        }
        //成功了之后 获得图片的uri
        try {
            mImageUri = data.getData();
            mQuizImageIV.setImageURI(mImageUri);
            //把图片的uri转换成文件文件路径
            if (mImageUri != null) {
                mImgPath = getPath(mImageUri);
                Log.d(TAG, "提交问题时的文件路径为" + mImgPath);
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 从相册选择图片
     */
    private void pickImageFromAlbum() {
        mIntent = new Intent();
        mIntent.setAction(Intent.ACTION_PICK);
        mIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(mIntent, 1);
    }

    /**
     * 提交问题
     */
    private void submitQuestions() {
        mTitleText = mQuizTitleET.getText().toString();
        mContentText = mQuizContentTitleET.getText().toString();
        //通过接口开始提问
        Intent intent = getIntent();
        User user = intent.getParcelableExtra("user");
        mLoginToken = user.getToken();


        Log.d(TAG, "登录token是" + mLoginToken);
        Log.d(TAG, "图片的外链是" + mImgUrl);
        mQuizUrl = "http://bihu.jay86.com/question.php";
        mUploadData = "title=" + mTitleText + "&content=" + mContentText + "&images=" + mImgUrl + "&token=" + mLoginToken;
        Log.d(TAG, "提交问题的参数为" + "标题" + mTitleText + "内容" + mContentText + "图片地址" + mImgUrl + "token是" + mLoginToken);
        HttpConnectUtil.doAsyncPost(mQuizUrl, mUploadData, new HttpConnectUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                try {
                    mObject = new JSONObject(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "提交问题接口返回的json数据为" + response);

                try {
                    if ("200".equals(mObject.getString("status"))) {
                        //跳转到主页
                        skipToMainActivity();
                        ToastUtil.shortToast(QuizActivity.this, "提问成功");
                        Log.d(TAG, "提问成功");
                    } else {
                        ToastUtil.shortToast(QuizActivity.this, mObject.getString("info"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    /**
     * 跳转到主页
     */
    private void skipToMainActivity() {
        finish();
    }

    /**
     * 找到控件实例
     */
    private void initView() {
        mQuizTitleET = findViewById(R.id.et_quiz_title);
        mQuizContentTitleET = findViewById(R.id.et_quiz_content);
        mQuizBtn = findViewById(R.id.btn_quiz);
        mQuizBackTV = findViewById(R.id.tv_quiz_back);
        mQuizImageIV = findViewById(R.id.iv_quiz_image);
        mQuizAddPicTV = findViewById(R.id.tv_quiz_add_pictures);
    }
}
