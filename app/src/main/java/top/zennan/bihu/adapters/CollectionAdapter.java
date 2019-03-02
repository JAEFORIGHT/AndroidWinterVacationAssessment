package top.zennan.bihu.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

import top.zennan.bihu.R;
import top.zennan.bihu.beans.User;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.InnerHolder> {

    private final Context mCollectContext;
    private final List<JSONObject> mDataList;

    public CollectionAdapter(Context context, User user, List<JSONObject> list) {
        mCollectContext = context;
        mDataList = list;
    }

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCollectContext).inflate(R.layout.item_collection, viewGroup, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder innerHolder, int i) {
        innerHolder.setData(mDataList.get(i));
    }

    @Override
    public int getItemCount() {
        if (mDataList != null) {
            return mDataList.size();
        }
        return 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setData(JSONObject jsonObject) {
            //找到各个控件
            TextView titleTV = itemView.findViewById(R.id.tv_collection_title);
            ImageView avatarIV = itemView.findViewById(R.id.iv_collection_avatar);
            TextView userNameTV = itemView.findViewById(R.id.tv_collection_name);
            TextView contentTV = itemView.findViewById(R.id.tv_collection_content);
            ImageView imageResIV = itemView.findViewById(R.id.iv_collection_image);
            //取数据
            try {
                String title = jsonObject.getString("title");
                String avatar = jsonObject.getString("authorAvatar");
                String authorName = jsonObject.getString("authorName");
                String content = jsonObject.getString("content");
                String images = jsonObject.getString("images");

                titleTV.setText(title);
                userNameTV.setText(authorName);
                contentTV.setText(content);
                Picasso.with(itemView.getContext()).load(avatar).into(avatarIV);
                Picasso.with(itemView.getContext()).load(images).into(imageResIV);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
