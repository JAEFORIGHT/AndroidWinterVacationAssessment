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

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import top.zennan.bihu.R;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.InnerHolder> implements View.OnClickListener {

    private Context mContext;
    private JSONArray mData;
    private String mDate;
    //声明自定义的接口
    private OnItemClickListener mOnItemClickListener;
    private String mImages;
    private String mIsExcitingStatus;
    private String mIsNaiveStatus;
    private String mIsFavoriteStatus;
    private List mList;


    public QuestionAdapter(Context context, JSONArray data) {
        this.mData = data;
        Log.d("QuestionAdapter", "data是" + mData);
        this.mContext = context;
    }

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_question, viewGroup, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder innerHolder, int i) {
        innerHolder.itemView.setTag(i);
        innerHolder.mAnswerIV.setTag(i);
        innerHolder.mExcitingIV.setTag(i);
        innerHolder.mNaiveIV.setTag(i);
        innerHolder.mFavoriteIV.setTag(i);
        //把json数据按照id顺序重排列
        mList = new ArrayList();
        mList.clear();
        try {
//            //从小到大的顺序
//            for (int j = 0; j < mData.length(); j++) {
//                for (int k = 0; k < mData.length(); k++) {
//                    if (Integer.parseInt(mData.getJSONObject(k).getString("id")) == (j + 1)) {
//                        mList.add(mData.getJSONObject(k));
//                    }
//                }
//            }

            //从大到小的顺序 使最新发布的内容在最上方
            for (int j = mData.length(); j > 0; j--) {
                for (int k = mData.length(); k > 0; k--) {
                    if (Integer.parseInt(mData.getJSONObject(k - 1).getString("id")) == j) {
                        mList.add(mData.getJSONObject(k - 1));
                    } else {

                    }
                }
            }
            innerHolder.setData(mList.get(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.length();
        }
        return 0;
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        if (mOnItemClickListener != null) {
            switch (v.getId()) {
                //点击条目中控件的情况
                case R.id.rv_question:
                    mOnItemClickListener.onItemClick(v, ViewName.PRACTISE, position);
                    break;
                //点击条目的情况
                default:
                    mOnItemClickListener.onItemClick(v, ViewName.ITEM, position);
                    break;
            }
        }
    }

    public class InnerHolder extends RecyclerView.ViewHolder {

        private String mAuthorName;
        private String mAvatar;
        private String mContent;
        private String mTitle;
        private final ImageView mAnswerIV;
        private final ImageView mExcitingIV;
        private final ImageView mNaiveIV;
        private final ImageView mFavoriteIV;


        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            //找到点赞 评论 收藏 踩赞
            mAnswerIV = itemView.findViewById(R.id.iv_question_answer);
            mExcitingIV = itemView.findViewById(R.id.iv_question_exciting);
            mNaiveIV = itemView.findViewById(R.id.iv_question_naive);
            mFavoriteIV = itemView.findViewById(R.id.iv_question_favorite);

            //给itemView 还有上面四个图标设置点击事件
            itemView.setOnClickListener(QuestionAdapter.this);
            mAnswerIV.setOnClickListener(QuestionAdapter.this);
            mExcitingIV.setOnClickListener(QuestionAdapter.this);
            mNaiveIV.setOnClickListener(QuestionAdapter.this);
            mFavoriteIV.setOnClickListener(QuestionAdapter.this);
        }


        public void setData(Object o) {
            //找到各个控件

            //头像
            ImageView avatarIV = itemView.findViewById(R.id.iv_question_avatar);
            //用户名
            TextView authorNameTV = itemView.findViewById(R.id.tv_question_name);
            //内容
            TextView contentTV = itemView.findViewById(R.id.tv_question_content);
            //标题
            TextView titleTV = itemView.findViewById(R.id.tv_question_title);
            //发布时间
            TextView timeTV = itemView.findViewById(R.id.tv_question_time);
            //内容图片
            ImageView imageIV = itemView.findViewById(R.id.iv_question_image);
            //点赞状态
            ImageView excitingIV = itemView.findViewById(R.id.iv_question_exciting);
            //踩赞状态
            ImageView naiveIV = itemView.findViewById(R.id.iv_question_naive);
            //收藏状态
            ImageView favoriteIV = itemView.findViewById(R.id.iv_question_favorite);
            JSONObject object = (JSONObject) o;
            try {

                mAuthorName = object.getString("authorName");
                mAvatar = object.getString("authorAvatar");
                mContent = object.getString("content");
                mTitle = object.getString("title");
                mDate = object.getString("date");
                mImages = object.getString("images");
                mIsExcitingStatus = object.getString("is_exciting");
                mIsNaiveStatus = object.getString("is_naive");
                mIsFavoriteStatus = object.getString("is_favorite");
                authorNameTV.setText(mAuthorName);
                contentTV.setText(mContent);
                titleTV.setText(mTitle);
                timeTV.setText(mDate + "时发布");
                Picasso.with(itemView.getContext()).load(mAvatar).into(avatarIV);
                Picasso.with(itemView.getContext()).load(mImages).into(imageIV);
                //对图片有无进行判断
                if ("null".equals(mImages)) {
                    imageIV.setVisibility(View.GONE);
                }
                //对点赞状态进行判断
                if ("true".equals(mIsExcitingStatus)) {
                    excitingIV.setImageResource(R.mipmap.ic_like_fill);
                } else if ("false".equals(mIsExcitingStatus)) {
                    excitingIV.setImageResource(R.mipmap.ic_like);
                }
                //对踩赞状态进行判断
                if ("true".equals(mIsNaiveStatus)) {
                    naiveIV.setImageResource(R.mipmap.ic_unlike_fill);
                } else if ("false".equals(mIsNaiveStatus)) {
                    naiveIV.setImageResource(R.mipmap.ic_unlike);
                }
                //对收藏状态进行判断
                if ("true".equals(mIsFavoriteStatus)) {
                    favoriteIV.setImageResource(R.mipmap.ic_heart_fill);
                } else if ("false".equals(mIsFavoriteStatus)) {
                    favoriteIV.setImageResource(R.mipmap.ic_heart);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //item里面有多个控件可以点击（item+item内部控件）
    public enum ViewName {
        ITEM,
        PRACTISE
    }

    //自定义一个回调接口来实现Click事件
    public interface OnItemClickListener {
        void onItemClick(View v, ViewName viewName, int position);
    }

    //定义方法并传给外面的使用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


}
