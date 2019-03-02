package top.zennan.bihu.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import top.zennan.bihu.R;
import top.zennan.bihu.utils.UrlToBitmapUtil;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.InnerHolder> implements View.OnClickListener {

    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private final JSONArray mArray;
    private List<JSONObject> mAnswerList;

    public AnswerAdapter(Context context, JSONArray array) {
        this.mContext = context;
        this.mArray = array;
    }

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_answer, viewGroup, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder innerHolder, int i) {
        try {
            innerHolder.itemView.setTag(i);
            innerHolder.mAnswerExciting.setTag(i);
            innerHolder.mAnswerNaive.setTag(i);
            mAnswerList = new ArrayList<>();
            mAnswerList.clear();
            for (int j = 0; j < mArray.length(); j++) {
                mAnswerList.add((JSONObject) mArray.get(j));
            }
            Log.d("重组前的答案列表", mArray.toString());
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

            Log.d("重组后的答案列表", mAnswerList.toString());
            innerHolder.setData(mAnswerList.get(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        if (mArray != null) {
            return mArray.length();
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        if (mOnItemClickListener != null) {
            switch (v.getId()) {
                //点击条目中控件的情况
                case R.id.rv_question:
                    mOnItemClickListener.onItemClick(v, QuestionAdapter.ViewName.PRACTISE, position);
                    break;
                //点击条目的情况
                default:
                    mOnItemClickListener.onItemClick(v, QuestionAdapter.ViewName.ITEM, position);
                    break;
            }
        }
    }

    //自定义一个回调接口来实现Click事件
    public interface OnItemClickListener {
        void onItemClick(View v, QuestionAdapter.ViewName viewName, int position);
    }

    //定义方法并传给外面的使用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {

        private final ImageView mAnswerExciting;
        private final ImageView mAnswerNaive;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            //找到点赞 踩赞
            mAnswerExciting = itemView.findViewById(R.id.iv_discuss_exciting);
            mAnswerNaive = itemView.findViewById(R.id.iv_discuss_naive);
            //设置点击事件
            itemView.setOnClickListener(AnswerAdapter.this);
            mAnswerExciting.setOnClickListener(AnswerAdapter.this);
            mAnswerNaive.setOnClickListener(AnswerAdapter.this);
        }

        public void setData(JSONObject object) {
            ImageView avatarIV = itemView.findViewById(R.id.iv_discuss_avatar);
            TextView userNameTV = itemView.findViewById(R.id.tv_discuss_name);
            TextView contentTV = itemView.findViewById(R.id.tv_discuss_content);
            TextView timeTV = itemView.findViewById(R.id.tv_discuss_time);
            //找到点赞和踩赞的东西
            ImageView excitingIV = itemView.findViewById(R.id.iv_discuss_exciting);
            ImageView naiveIV = itemView.findViewById(R.id.iv_discuss_naive);
            try {
                String avatar = object.getString("authorAvatar");
                String authorName = object.getString("authorName");
                String content = object.getString("content");
                String date = object.getString("date");
                String excitingStatus = object.getString("is_exciting");
                String naiveStatus = object.getString("is_naive");

                if ("true".equals(excitingStatus)) {
                    excitingIV.setImageResource(R.mipmap.ic_like_fill);
                } else if ("false".equals(excitingStatus)) {
                    excitingIV.setImageResource(R.mipmap.ic_like);
                }
                if ("true".equals(naiveStatus)) {
                    naiveIV.setImageResource(R.mipmap.ic_unlike_fill);
                } else if ("false".equals(naiveStatus)) {
                    naiveIV.setImageResource(R.mipmap.ic_unlike);
                }
                UrlToBitmapUtil.setImageToImageView(avatarIV, avatar);
                userNameTV.setText(authorName);
                contentTV.setText(content);
                timeTV.setText("发布于" + date);
                Log.d("获取答案列表的数据", authorName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
