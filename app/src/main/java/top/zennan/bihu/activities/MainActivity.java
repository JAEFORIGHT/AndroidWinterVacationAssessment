package top.zennan.bihu.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import top.zennan.bihu.R;
import top.zennan.bihu.adapters.QuestionAdapter;
import top.zennan.bihu.beans.User;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.SpaceItemDecorationUtil;
import top.zennan.bihu.utils.ToastUtil;
import top.zennan.bihu.utils.UrlToBitmapUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ActionBar mActionBar;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private Intent mIntent;
    private MenuItem mQuizItem;
    private MenuItem mCollectionItem;
    private MenuItem mChangeAvatarItem;
    private MenuItem mChangePasswordItem;
    private MenuItem mExitItem;
    private User mUser;
    private CircleImageView mAvatar;
    private RecyclerView mQuestionRV;
    private String mParam;
    private String mGetQuestionListUrl;
    private JSONArray mQuestionsArray;
    private QuestionAdapter mQuestionAdapter;
    private String mIntentTitle;
    private String mIntentAvatar;
    private String mIntentUsername;
    private String mIntentContent;
    private String mIntentTime;
    private String mIntentImage;
    private String mIntentQid;
    private List<JSONObject> mExcitingList;
    private List<JSONObject> mQuestionList;
    private List<JSONObject> mNaiveList;
    private List<JSONObject> mCollectList;
    private LinearLayoutManager mLinearLayoutManager;
    private Bitmap mBitmap;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getUser();
        initView();
        initListener();
        getQuestionList();
        setCircleImage();
    }

    /**
     * 取得问题列表
     */
    private void getQuestionList() {
        //获取数据
        mGetQuestionListUrl = "http://bihu.jay86.com/getQuestionList.php";

        mParam = "page=null&count=500&token=" + mUser.getToken();

        HttpConnectUtil.doAsyncPost(mGetQuestionListUrl, mParam, new HttpConnectUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    Log.d(TAG, "问题列表的json数据为" + response);
                    if ("200".equals(object.getString("status"))) {
                        JSONObject dataObject = object.getJSONObject("data");
                        mQuestionsArray = dataObject.getJSONArray("questions");
                        mQuestionList = new ArrayList();
                        mQuestionList.clear();
                        for (int j = mQuestionsArray.length(); j > 0; j--) {
                            for (int k = mQuestionsArray.length(); k > 0; k--) {
                                if (Integer.parseInt(mQuestionsArray.getJSONObject(k - 1).getString("id")) == j) {
                                    mQuestionList.add(mQuestionsArray.getJSONObject(k - 1));
                                }
                            }
                        }
                        setDataToRecycleView();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void setDataToRecycleView() {
        //拿到数据后更新ui,mQuestionsArray就是拿到的数据数组
        mLinearLayoutManager = new LinearLayoutManager(MainActivity.this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mQuestionRV.setLayoutManager(mLinearLayoutManager);
        mQuestionAdapter = new QuestionAdapter(MainActivity.this, mQuestionsArray);
        mQuestionAdapter.setOnItemClickListener(myItemClickListener);
        mQuestionRV.setAdapter(mQuestionAdapter);

    }

    /**
     * item和itemView的点击事件的设置
     */
    private QuestionAdapter.OnItemClickListener myItemClickListener = new QuestionAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, QuestionAdapter.ViewName viewName, final int position) {
            switch (v.getId()) {
                //点赞与取消赞
                case R.id.iv_question_exciting:
                    //每次点击按钮时都重新获取一次问题列表的数据
                    HttpConnectUtil.doAsyncPost(mGetQuestionListUrl, mParam, new HttpConnectUtil.CallBack() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject object = new JSONObject(response);
                                Log.d(TAG, "问题列表的json数据为" + response);
                                if ("200".equals(object.getString("status"))) {
                                    JSONObject dataObject = object.getJSONObject("data");
                                    mQuestionsArray = dataObject.getJSONArray("questions");
                                    mExcitingList = new ArrayList();
                                    mExcitingList.clear();
                                    for (int j = mQuestionsArray.length(); j > 0; j--) {
                                        for (int k = mQuestionsArray.length(); k > 0; k--) {
                                            if (Integer.parseInt(mQuestionsArray.getJSONObject(k - 1).getString("id")) == j) {
                                                mExcitingList.add(mQuestionsArray.getJSONObject(k - 1));
                                            }
                                        }
                                    }
                                    Log.d("重排的列表是", mExcitingList.toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    //如果原来是还未点赞的状态 那么点击之后就是点赞
                    try {
                        if ("false".equals(mExcitingList.get(position).getString("is_exciting"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/exciting.php",
                                    "id=" + mExcitingList.get(position).getString("id") + "&type=1&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject excitingResult = new JSONObject(response);
                                                if ("200".equals(excitingResult.getString("status"))) {
                                                    ToastUtil.shortToast(MainActivity.this, "点赞成功");
                                                    View itemView = mQuestionRV.getLayoutManager().findViewByPosition(position);
                                                    ImageView favoriteIV = itemView.findViewById(R.id.iv_question_exciting);
                                                    favoriteIV.setImageResource(R.mipmap.ic_like_fill);
                                                    Log.d("点赞操作的id和位置分别是", mExcitingList.get(position).getString("id") + "和" + position);
                                                } else {
                                                    ToastUtil.shortToast(MainActivity.this,
                                                            excitingResult.getString("info"));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                        //如果原来是已经点赞的状态 那么点击之后就是取消点赞
                        else if ("true".equals(mExcitingList.get(position).getString("is_exciting"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/cancelExciting.php",
                                    "id=" + mExcitingList.get(position).getString("id") + "&type=1&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject cancelExcitingResult = new JSONObject(response);
                                                if ("200".equals(cancelExcitingResult.getString("status"))) {
                                                    ToastUtil.shortToast(MainActivity.this, "取消点赞成功");
                                                    View itemView = mQuestionRV.getLayoutManager().findViewByPosition(position);
                                                    ImageView favoriteIV = itemView.findViewById(R.id.iv_question_exciting);
                                                    favoriteIV.setImageResource(R.mipmap.ic_like);
                                                    Log.d("取消点赞操作的id和位置分别是", mExcitingList.get(position).getString("id") + "和" + position);
                                                } else {
                                                    ToastUtil.shortToast(MainActivity.this,
                                                            cancelExcitingResult.getString("info"));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                        } else {
                            ToastUtil.shortToast(MainActivity.this, "不是成功也不是失败的操作");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                //踩赞与取消踩赞
                case R.id.iv_question_naive:
                    //每次点击按钮时都重新获取一次问题列表的数据
                    HttpConnectUtil.doAsyncPost(mGetQuestionListUrl, mParam, new HttpConnectUtil.CallBack() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject object = new JSONObject(response);
                                Log.d(TAG, "问题列表的json数据为" + response);
                                if ("200".equals(object.getString("status"))) {
                                    JSONObject dataObject = object.getJSONObject("data");
                                    mQuestionsArray = dataObject.getJSONArray("questions");
                                    mNaiveList = new ArrayList();
                                    mNaiveList.clear();
                                    for (int j = mQuestionsArray.length(); j > 0; j--) {
                                        for (int k = mQuestionsArray.length(); k > 0; k--) {
                                            if (Integer.parseInt(mQuestionsArray.getJSONObject(k - 1).getString("id")) == j) {
                                                mNaiveList.add(mQuestionsArray.getJSONObject(k - 1));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    //如果原来是还未踩赞的状态 那么点击之后就是踩赞
                    try {
                        if ("false".equals(mNaiveList.get(position).getString("is_naive"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/naive.php",
                                    "id=" + mNaiveList.get(position).getString("id") + "&type=1&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject naiveResult = new JSONObject(response);
                                                if ("200".equals(naiveResult.getString("status"))) {
                                                    ToastUtil.shortToast(MainActivity.this, "踩赞成功");
                                                    View itemView = mQuestionRV.getLayoutManager().findViewByPosition(position);
                                                    ImageView favoriteIV = itemView.findViewById(R.id.iv_question_naive);
                                                    favoriteIV.setImageResource(R.mipmap.ic_unlike_fill);
                                                } else {
                                                    ToastUtil.shortToast(MainActivity.this, naiveResult.getString("info"));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                        //如果是踩赞的状态  那么点击之后就是取消踩赞
                        else if ("true".equals(mNaiveList.get(position).getString("is_naive"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/cancelNaive.php",
                                    "id=" + mNaiveList.get(position).getString("id") + "&type=1&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject cancelNaiveResult = new JSONObject(response);
                                                if ("200".equals(cancelNaiveResult.getString("status"))) {
                                                    ToastUtil.shortToast(MainActivity.this, "取消踩赞成功");
                                                    View itemView = mQuestionRV.getLayoutManager().findViewByPosition(position);
                                                    ImageView favoriteIV = itemView.findViewById(R.id.iv_question_naive);
                                                    favoriteIV.setImageResource(R.mipmap.ic_unlike);
                                                } else {
                                                    ToastUtil.shortToast(MainActivity.this,
                                                            cancelNaiveResult.getString("info"));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //收藏与取消收藏
                case R.id.iv_question_favorite:
                    try {
                        //每次点击按钮时都重新获取一次问题列表的数据
                        HttpConnectUtil.doAsyncPost(mGetQuestionListUrl, mParam, new HttpConnectUtil.CallBack() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    Log.d(TAG, "问题列表的json数据为" + response);
                                    if ("200".equals(object.getString("status"))) {
                                        JSONObject dataObject = object.getJSONObject("data");
                                        mQuestionsArray = dataObject.getJSONArray("questions");
                                        mCollectList = new ArrayList();
                                        mCollectList.clear();
                                        for (int j = mQuestionsArray.length(); j > 0; j--) {
                                            for (int k = mQuestionsArray.length(); k > 0; k--) {
                                                if (Integer.parseInt(mQuestionsArray.getJSONObject(k - 1).getString("id")) == j) {
                                                    mCollectList.add(mQuestionsArray.getJSONObject(k - 1));
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //如果原来是还未收藏的状态 那么点击之后就是收藏
                        if ("false".equals(mCollectList.get(position).getString("is_favorite"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/favorite.php",
                                    "qid=" + mCollectList.get(position).getString("id") + "&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject collectResult = new JSONObject(response);
                                                if ("200".equals(collectResult.getString("status"))) {
                                                    ToastUtil.shortToast(MainActivity.this, "收藏成功");
                                                    View itemView = mQuestionRV.getLayoutManager().findViewByPosition(position);
                                                    ImageView favoriteIV = itemView.findViewById(R.id.iv_question_favorite);
//                                                    View view = mLinearLayoutManager.findViewByPosition(position);
//                                                    LinearLayout layout = (LinearLayout) view;
//                                                    ImageView favoriteIV = layout.findViewById(R.id.iv_question_favorite);
                                                    favoriteIV.setImageResource(R.mipmap.ic_heart_fill);
                                                } else {
                                                    ToastUtil.shortToast(MainActivity.this,
                                                            collectResult.getString("info"));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                        //如果原来就是已经收藏的状态了 那么点击之后就是取消收藏
                        else if ("true".equals(mCollectList.get(position).getString("is_favorite"))) {
                            HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/cancelFavorite.php",
                                    "qid=" + mCollectList.get(position).getString("id") + "&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject cancelCollectResult = new JSONObject(response);
                                                if ("200".equals(cancelCollectResult.getString("status"))) {
                                                    ToastUtil.shortToast(MainActivity.this, "取消收藏成功");
                                                    View itemView = mQuestionRV.getLayoutManager().findViewByPosition(position);
                                                    ImageView favoriteIV = itemView.findViewById(R.id.iv_question_favorite);


                                                    favoriteIV.setImageResource(R.mipmap.ic_heart);
                                                } else {
                                                    ToastUtil.shortToast(MainActivity.this,
                                                            cancelCollectResult.getString("info"));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        } else {
                            ToastUtil.shortToast(MainActivity.this, "奇怪的错误");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;


                //回答
                case R.id.iv_question_answer:
                    //点击回答要通过intent传输的数据先在这里获取
                    try {
                        JSONObject questionsData = mQuestionList.get(position);
                        mIntentTitle = questionsData.getString("title");
                        mIntentAvatar = questionsData.getString("authorAvatar");
                        mIntentUsername = questionsData.getString("authorName");
                        mIntentContent = questionsData.getString("content");
                        mIntentTime = questionsData.getString("date");
                        mIntentImage = questionsData.getString("images");
                        mIntentQid = questionsData.getString("id");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(MainActivity.this, AnswerActivity.class);
                    intent.putExtra("questionTitleKey", mIntentTitle);
                    intent.putExtra("questionAvatarKey", mIntentAvatar);
                    intent.putExtra("questionUsernameKey", mIntentUsername);
                    intent.putExtra("questionContentKey", mIntentContent);
                    intent.putExtra("questionTimeKey", mIntentTime);
                    intent.putExtra("questionImageKey", mIntentImage);
                    intent.putExtra("answerQidKey", mIntentQid);
                    intent.putExtra("userLoginTokenKey", mUser.getToken());
                    startActivity(intent);
                    break;
                //item
                default:
//                    Toast.makeText(MainActivity.this,
//                            "你点击了item按钮" + (position + 1), Toast.LENGTH_SHORT).show();
                    //点击回答要通过intent传输的数据先在这里获取
                    try {
                        JSONObject questionsData = mQuestionList.get(position);
                        mIntentTitle = questionsData.getString("title");
                        mIntentAvatar = questionsData.getString("authorAvatar");
                        mIntentUsername = questionsData.getString("authorName");
                        mIntentContent = questionsData.getString("content");
                        mIntentTime = questionsData.getString("date");
                        mIntentImage = questionsData.getString("images");
                        mIntentQid = questionsData.getString("id");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent1 = new Intent(MainActivity.this, AnswerActivity.class);
                    intent1.putExtra("questionTitleKey", mIntentTitle);
                    intent1.putExtra("questionAvatarKey", mIntentAvatar);
                    intent1.putExtra("questionUsernameKey", mIntentUsername);
                    intent1.putExtra("questionContentKey", mIntentContent);
                    intent1.putExtra("questionTimeKey", mIntentTime);
                    intent1.putExtra("questionImageKey", mIntentImage);
                    intent1.putExtra("answerQidKey", mIntentQid);
                    intent1.putExtra("userLoginTokenKey", mUser.getToken());
                    startActivity(intent1);
                    break;
            }
        }

    };


    /**
     * 设置头像
     */
    private void setCircleImage() {
        Log.d("头像地址是", mUser.getAvatar());
        if (mUser.getAvatar() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                    Thread.sleep(2000);
                        mBitmap = UrlToBitmapUtil.getBitmap(mUser.getAvatar());
                        if (mBitmap != null) {
                            mAvatar.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAvatar.setImageBitmap(mBitmap);
                                }
                            });
                        } else {
                            ToastUtil.shortToast(MainActivity.this, "头像资源获取失败...");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
//        //还是显示不出来
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (!"null".equals(mUser.getAvatar())) {
//                        UrlToBitmapUtil.setImageToImageView(mAvatar, mUser.getAvatar());
//                    }
//                } catch (Exception e) {
//                    e.getStackTrace();
//                }
//            }
//        }).start();
    }

    /**
     * 得到登录时的user对象，从而获得token
     */
    private void getUser() {
        Intent intent = getIntent();
        mUser = intent.getParcelableExtra("user");
    }

    /**
     * 设置对滑动菜单内的item的点击事件的监听
     */
    private void initListener() {
        //对发表问题这个item的点击事件的监听
        mQuizItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mIntent = new Intent(MainActivity.this, QuizActivity.class);
                mIntent.putExtra("user", mUser);
                startActivity(mIntent);

                return true;
            }
        });
        //对查看收藏这个item点击事件的监听
        mCollectionItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mIntent = new Intent(MainActivity.this, CollectionActivity.class);
                mIntent.putExtra("user", mUser);
                startActivity(mIntent);

                return true;
            }
        });
        //对更改头像这个item点击事件的监听
        mChangeAvatarItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mIntent = new Intent(MainActivity.this, ChangeAvatarActivity.class);
                mIntent.putExtra("user", mUser);
                startActivity(mIntent);

                return true;
            }
        });
        //对更改密码这个item点击事件的监听
        mChangePasswordItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mIntent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                mIntent.putExtra("user", mUser);
                startActivity(mIntent);
                return true;
            }
        });
        //对退出登录这个item点击事件的监听
        mExitItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(mIntent);
                finish();
                return true;
            }
        });

    }

    /**
     * 获得控件的实例并设置一些与滑动菜单有关的东西
     */
    private void initView() {
        //获得实例
        mToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.dl_main);
        mActionBar = getSupportActionBar();
        mNavigationView = findViewById(R.id.nav_main);
        mQuizItem = mNavigationView.getMenu().findItem(R.id.nav_quiz);
        mCollectionItem = mNavigationView.getMenu().findItem(R.id.nav_see_collection);
        mChangeAvatarItem = mNavigationView.getMenu().findItem(R.id.nav_change_avatar);
        mChangePasswordItem = mNavigationView.getMenu().findItem(R.id.nav_change_password);
        mExitItem = mNavigationView.getMenu().findItem(R.id.nav_exit);
        mAvatar = findViewById(R.id.civ_avatar);
        mQuestionRV = findViewById(R.id.rv_question);
        mQuestionRV.addItemDecoration(new SpaceItemDecorationUtil(30));
        mSwipeRefreshLayout = findViewById(R.id.srl_question);

        //设置图标
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        //设置默认选中返回主页
        mNavigationView.setCheckedItem(R.id.nav_main);
        //设置当选中其他item时关闭NavigationView
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
        //设置下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //模拟网络请求需要3000毫秒，请求完成，设置setRefreshing 为false
                getQuestionList();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
    }

    /**
     * 对导航菜单按钮点击返回主页事件的处理
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }


}