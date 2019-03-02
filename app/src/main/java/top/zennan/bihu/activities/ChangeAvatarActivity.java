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
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import top.zennan.bihu.R;
import top.zennan.bihu.beans.User;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.ToastUtil;

public class ChangeAvatarActivity extends AppCompatActivity {

    private static final String TAG = "ChangeAvatarActivity";
    private TextView mBackTV;
    private TextView mAddPicsTV;
    private Button mChangeAvatarBtn;
    private Intent mIntent;
    private ImageView mAvatarIV;
    private Uri mImageUri;
    private String mImgPath;
    private String mZiLaiTokenUrl;
    private String mTokenGetParam;
    private String mQiNiuToken;
    private String mFileNameString;
    private String mImgSite;
    private String mParam;
    private String mUrl;
    private final String DOMAIN_NAME = "http://pn09wx14o.bkt.clouddn.com/";
    private User mUser;
    private String mToken;
    private JSONObject mJsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_avatar);
        getLoginToken();
        initView();
        initOnClickListener();
    }

    /**
     * 获得登录时返回的token
     */
    private void getLoginToken() {
        Intent intent = getIntent();
        mUser = intent.getParcelableExtra("user");
        mToken = mUser.getToken();
    }

    private void initOnClickListener() {

        //返回主页
        mBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //选择图片
        mAddPicsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_PICK);
                mIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(mIntent, 2);
            }
        });

        //修改头像
        mChangeAvatarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "网络请求的线程是" + Thread.currentThread().getName());
                        //取得七牛token
                        getQiNiuToken();

                        //将图片上传到七牛云
                        UploadPicsToQiNiuCloud();

                    }
                }).start();
            }
        });

    }


    /**
     * 找到控件实例
     */
    private void initView() {
        mAvatarIV = findViewById(R.id.iv_change_avatar);
        mBackTV = findViewById(R.id.tv_change_avatar_back);
        mAddPicsTV = findViewById(R.id.tv_change_avatar_add_pictures);
        mChangeAvatarBtn = findViewById(R.id.btn_change_avatar);
    }

    /**
     * 跳转的结果
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
            ToastUtil.shortToast(ChangeAvatarActivity.this, "您真的不从相册选择图片吗？");
            return;
        }
        //成功了之后 获得图片的uri
        try {
            mImageUri = data.getData();
            mAvatarIV.setImageURI(mImageUri);
            //把图片的uri转换成文件文件路径
            if (mImageUri != null) {
                mImgPath = getPath(mImageUri);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

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
     * 每次上传图片时都获取一次七牛token
     */
    private void getQiNiuToken() {
        mZiLaiTokenUrl = "http://zzzia.net:8080/qiniu/";
        mTokenGetParam = "accessKey=JSz5JslQIg5StvzaySYZGnRU85ZeeobYKBLjnbul" +
                "&secretKey=p67Ujpx4cHOFXPsJQWeXHupmHbFx3NhzahCbyFGM" +
                "&bucket=imgres";
        String response = HttpConnectUtil.doSyncPost(mZiLaiTokenUrl, mTokenGetParam);
        Log.d(TAG, "获取token时的json数据是" + response);
        //解析json
        try {
            JSONObject object = new JSONObject(response);
            String info = object.getString("info");
            if ("success".equals(info)) {
                String token = object.getString("token");
                if (token != null) {
                    mQiNiuToken = token;
                }
            } else {
                ToastUtil.shortToast(ChangeAvatarActivity.this, "您的网络可能不是很好");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 上传文件到七牛云
     */
    private void UploadPicsToQiNiuCloud() {
        Configuration config = new Configuration.Builder()
                .chunkSize(512 * 1024)
                .putThreshhold(1024 * 1024)
                .connectTimeout(10)
                .useHttps(true)
                .responseTimeout(60)
                .build();
        UploadManager uploadManager = new UploadManager(config);
        uploadManager.put(mImgPath, null, mQiNiuToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (info.isOK()) {
                    Log.d(TAG, "上传成功");
                    //上传成功后获取该图片
                    try {
                        mFileNameString = response.getString("key");
                        if (!"null".equals(mFileNameString)) {
                            mImgSite = DOMAIN_NAME + mFileNameString;
                        }
                        Log.d(TAG, "图片的url为" + mImgSite);
                        mParam = "token=" + mToken + "&avatar=" + mImgSite;
                        mUrl = "http://bihu.jay86.com/modifyAvatar.php";
                        HttpConnectUtil.doAsyncPost(mUrl, mParam, new HttpConnectUtil.CallBack() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    mJsonObject = new JSONObject(response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "修改头像时的线程是" + Thread.currentThread().getName());
                                Log.d(TAG, "修改头像时的json数据是" + mJsonObject);
                                try {
                                    if ("200".equals(mJsonObject.getString("status"))) {
                                        ToastUtil.shortToast(
                                                ChangeAvatarActivity.this, "成功修改头像");
                                        finish();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

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


}
