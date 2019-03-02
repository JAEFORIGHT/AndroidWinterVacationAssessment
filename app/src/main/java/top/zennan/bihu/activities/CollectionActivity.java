package top.zennan.bihu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import top.zennan.bihu.R;
import top.zennan.bihu.adapters.CollectionAdapter;
import top.zennan.bihu.beans.User;
import top.zennan.bihu.utils.HttpConnectUtil;
import top.zennan.bihu.utils.SpaceItemDecorationUtil;

public class CollectionActivity extends AppCompatActivity {

    private TextView mBackTV;
    private RecyclerView mCollectionRV;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        initView();
        getIntentData();
        initOnClickListener();
        setDataIntoRecyclerView();
    }

    private void setDataIntoRecyclerView() {
        HttpConnectUtil.doAsyncPost("http://bihu.jay86.com/getFavoriteList.php", "page=null&count=100&token=" + mUser.getToken(), new HttpConnectUtil.CallBack() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    if ("200".equals(responseObject.getString("status"))) {
                        JSONObject data = responseObject.getJSONObject("data");
                        JSONArray array = data.getJSONArray("questions");
                        List<JSONObject> list = new ArrayList<>();
                        for (int j = 0; j < array.length(); j++) {
                            list.add((JSONObject) array.get(j));
                        }
                        //集合按id排序
                        Collections.sort(list, new Comparator<JSONObject>() {
                            @Override
                            public int compare(JSONObject o1, JSONObject o2) {
                                try {
                                    return (Integer.parseInt(o1.getString("id")) - Integer.parseInt(o2.getString("id")));
                                } catch (Exception e) {
                                    return 0;
                                }
                            }
                        });
                        Log.d("点击收藏列表时的集合数据是", list.toString());
                        LinearLayoutManager manager = new LinearLayoutManager(CollectionActivity.this);
                        mCollectionRV.setLayoutManager(manager);
                        CollectionAdapter adapter = new CollectionAdapter(CollectionActivity.this, mUser, list);
                        mCollectionRV.setAdapter(adapter);
                        mCollectionRV.addItemDecoration(new SpaceItemDecorationUtil(20));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initOnClickListener() {
        mBackTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mUser = intent.getParcelableExtra("user");
    }

    private void initView() {
        mBackTV = findViewById(R.id.tv_collection_back);
        mCollectionRV = findViewById(R.id.rv_collection);
    }
}
