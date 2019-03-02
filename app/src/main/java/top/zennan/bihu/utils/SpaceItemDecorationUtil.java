package top.zennan.bihu.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceItemDecorationUtil extends RecyclerView.ItemDecoration {
    int mSpace;

    /**
     * 给Recyclerview设置间隔的工具类
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = mSpace;
        }

    }

    public SpaceItemDecorationUtil(int space) {
        this.mSpace = space;
    }
}