package top.zennan.bihu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import top.zennan.bihu.R;
import top.zennan.bihu.adapters.AnswerAdapter;
import top.zennan.bihu.adapters.QuestionAdapter;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.SpaceItemDecorationUtil;
import top.zennan.bihu.utils.ToastUtil;
import top.zennan.bihu.utils.UrlToBitmapUtil;

public class AnswerActivity extends AppCompatActivity {

    private Intent mIntent;
    private String mQuestionTitle;
    private String mQuestionAvatar;
    private String mQuestionUsername;
    private String mQuestionContent;
    private String mQuestionTime;
    private String mQuestionImage;
    private String mAnswerQid;
    private String mUserLoginToken;
    private TextView mBackTV;
    private TextView mTitleTV;
    private ImageView mAvatarIV;
    private TextView mUserNameTV;
    private TextView mContentTV;
    private TextView mTimeTV;
    private ImageView mImageIV;
    private RecyclerView mAnswerRV;
    private EditText mAnswerET;
    private Button mAnswerBtn;
    private JSONArray mAnswer;
    private List<JSONObject> mAnswerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        getData();
        initView();
        setData();
        initOnClickListener();
    }

    /**
     * 把获得到的数据更新到ui上
     */
    private void setData() {
        mTitleTV.setText(mQuestionTitle);
        UrlToBitmapUtil.setImageToImageView(mAvatarIV, mQuestionAvatar);
        mUserNameTV.setText(mQuestionUsername);
        mContentTV.setText(mQuestionContent);
        mTimeTV.setText("发布于" + mQuestionTime);
        UrlToBitmapUtil.setImageToImageView(mImageIV, mQuestionImage);
    }

    /**
     * 给点击事件设置监听
     */
    private void initOnClickListener() {
        //返回主页
        mBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //回答问题
        mAnswerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("回答的数据是", "qid=" + mAnswerQid + "&content=" + mAnswerET.getText().toString() + "&images=null&token=" + mUserLoginToken);
                HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/answer.php", "qid=" + mAnswerQid + "&content=" + mAnswerET.getText().toString() + "&images=null&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if ("200".equals(object.getString("status"))) {
                                ToastUtil.shortToast(AnswerActivity.this, "回答成功");
                                finish();
                            } else {
                                ToastUtil.shortToast(AnswerActivity.this, object.getString("info"));
                                Log.d("回答失败的原因是", response);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * 控件实例化
     */
    private void initView() {
        mBackTV = findViewById(R.id.tv_answer_quiz_back);
        mTitleTV = findViewById(R.id.tv_answer_title);
        mAvatarIV = findViewById(R.id.iv_answer_avatar);
        mUserNameTV = findViewById(R.id.tv_answer_name);
        mContentTV = findViewById(R.id.tv_answer_content);
        mTimeTV = findViewById(R.id.tv_answer_time);
        mImageIV = findViewById(R.id.iv_answer_image);
        mAnswerRV = findViewById(R.id.rv_answer);
        mAnswerET = findViewById(R.id.et_answer);
        mAnswerBtn = findViewById(R.id.btn_answer);
    }

    private AnswerAdapter.OnItemClickListener ClickListener = new AnswerAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, QuestionAdapter.ViewName viewName, final int position) {
            switch (v.getId()) {
                case R.id.iv_discuss_exciting:
                    //设置点赞和取消赞的操作
                    //先重新获取回答列表的数据
                    HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/getAnswerList.php", "page=null&count=100&qid=" + mAnswerQid + "&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if ("200".equals(object.getString("status"))) {
                                    JSONObject data = object.getJSONObject("data");
                                    JSONArray answers = data.getJSONArray("answers");
                                    mAnswerList = new ArrayList<>();
                                    mAnswerList.clear();
                                    for (int j = 0; j < answers.length(); j++) {
                                        mAnswerList.add((JSONObject) answers.get(j));
                                    }
                                    Collections.sort(mAnswerList, new Comparator<JSONObject>() {
                                        @Override
                                        public int compare(JSONObject o1, JSONObject o2) {
                                            try {
                                                return (Integer.parseInt(o1.getString("id")) - Integer.parseInt(o2.getString("id")));
                                            } catch (Exception e) {
                                                return 0;
                                            }
                                        }
                                    });
                                } else {
                                    ToastUtil.shortToast(AnswerActivity.this, object.getString("info"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    //如果是还未点赞的状态就点赞
                    try {
                        if ("false".equals(mAnswerList.get(position).getString("is_exciting"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/exciting.php", "id=" + mAnswerList.get(position).getString("id") + "&type=2&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject answerExcitingResult = new JSONObject(response);
                                        if ("200".equals(answerExcitingResult.getString("status"))) {
                                            ToastUtil.shortToast(AnswerActivity.this, "点赞成功");
                                            //进行换图片操作
                                            View itemView = mAnswerRV.getLayoutManager().findViewByPosition(position);
                                            ImageView favoriteIV = itemView.findViewById(R.id.iv_discuss_exciting);
                                            favoriteIV.setImageResource(R.mipmap.ic_like_fill);
                                        } else {
                                            ToastUtil.shortToast(AnswerActivity.this, answerExcitingResult.getString("info"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //如果是已经点赞了的状态就取消
                    try {
                        if ("true".equals(mAnswerList.get(position).getString("is_exciting"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/cancelExciting.php", "id=" + mAnswerList.get(position).getString("id") + "&type=2&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject answerCancelExcitingResult = new JSONObject(response);
                                        if ("200".equals(answerCancelExcitingResult.getString("status"))) {
                                            ToastUtil.shortToast(AnswerActivity.this, "取消点赞成功");
                                            //进行换图片操作
                                            View itemView = mAnswerRV.getLayoutManager().findViewByPosition(position);
                                            ImageView favoriteIV = itemView.findViewById(R.id.iv_discuss_exciting);
                                            favoriteIV.setImageResource(R.mipmap.ic_like);
                                        } else {
                                            ToastUtil.shortToast(AnswerActivity.this, answerCancelExcitingResult.getString("info"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.iv_discuss_naive:
                    //设置踩赞和取消踩赞的操作
                    //先重新获取回答列表的数据
                    HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/getAnswerList.php", "page=null&count=100&qid=" + mAnswerQid + "&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if ("200".equals(object.getString("status"))) {
                                    JSONObject data = object.getJSONObject("data");
                                    JSONArray answers = data.getJSONArray("answers");
                                    mAnswerList = new ArrayList<>();
                                    mAnswerList.clear();
                                    for (int j = 0; j < answers.length(); j++) {
                                        mAnswerList.add((JSONObject) answers.get(j));
                                    }
                                    Collections.sort(mAnswerList, new Comparator<JSONObject>() {
                                        @Override
                                        public int compare(JSONObject o1, JSONObject o2) {
                                            try {
                                                return (Integer.parseInt(o1.getString("id")) - Integer.parseInt(o2.getString("id")));
                                            } catch (Exception e) {
                                                return 0;
                                            }
                                        }
                                    });
                                } else {
                                    ToastUtil.shortToast(AnswerActivity.this, object.getString("info"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    //如果是还未踩赞就踩赞
                    try {
                        if ("false".equals(mAnswerList.get(position).getString("is_naive"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/naive.php", "id=" + mAnswerList.get(position).getString("id") + "&type=2&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject answerNaiveResult = new JSONObject(response);
                                        if ("200".equals(answerNaiveResult.getString("status"))) {
                                            ToastUtil.shortToast(AnswerActivity.this, "踩赞成功");
                                            //进行换图片操作
                                            View itemView = mAnswerRV.getLayoutManager().findViewByPosition(position);
                                            ImageView favoriteIV = itemView.findViewById(R.id.iv_discuss_naive);
                                            favoriteIV.setImageResource(R.mipmap.ic_unlike_fill);
                                        } else {
                                            ToastUtil.shortToast(AnswerActivity.this, answerNaiveResult.getString("info"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //如果是已经踩赞了的就取消踩赞
                    try {
                        if ("true".equals(mAnswerList.get(position).getString("is_naive"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/cancelNaive.php", "id=" + mAnswerList.get(position).getString("id") + "&type=2&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject answerCancelNaiveResult = new JSONObject(response);
                                        if ("200".equals(answerCancelNaiveResult.getString("status"))) {
                                            ToastUtil.shortToast(AnswerActivity.this, "取消踩赞成功");
                                            //进行换图片操作
                                            View itemView = mAnswerRV.getLayoutManager().findViewByPosition(position);
                                            ImageView favoriteIV = itemView.findViewById(R.id.iv_discuss_naive);
                                            favoriteIV.setImageResource(R.mipmap.ic_unlike);
                                        } else {
                                            ToastUtil.shortToast(AnswerActivity.this, answerCancelNaiveResult.getString("info"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获得通过intent传过来的数据
     */
    private void getData() {
        mIntent = getIntent();
        mQuestionTitle = mIntent.getStringExtra("questionTitleKey");
        mQuestionAvatar = mIntent.getStringExtra("questionAvatarKey");
        mQuestionUsername = mIntent.getStringExtra("questionUsernameKey");
        mQuestionContent = mIntent.getStringExtra("questionContentKey");
        mQuestionTime = mIntent.getStringExtra("questionTimeKey");
        mQuestionImage = mIntent.getStringExtra("questionImageKey");
        mAnswerQid = mIntent.getStringExtra("answerQidKey");
        mUserLoginToken = mIntent.getStringExtra("userLoginTokenKey");
        HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/getAnswerList.php", "page=null&count=20&qid=" + mAnswerQid + "&token=" + mUserLoginToken, new HttpConnectUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("返回的答案列表", response);
                    JSONObject object = new JSONObject(response);
                    JSONObject data = object.getJSONObject("data");
                    mAnswer = data.getJSONArray("answers");
                    LinearLayoutManager manager = new LinearLayoutManager(AnswerActivity.this);
                    mAnswerRV.setLayoutManager(manager);
                    AnswerAdapter adapter = new AnswerAdapter(AnswerActivity.this, mAnswer);
                    mAnswerRV.setAdapter(adapter);
                    adapter.setOnItemClickListener(ClickListener);
                    mAnswerRV.addItemDecoration(new SpaceItemDecorationUtil(20));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

}
