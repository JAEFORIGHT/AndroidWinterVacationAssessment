package top.zennan.bihu.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtil {

    /**
     * 显示长时间的提示信息
     *
     * @param activity 需要在哪个活动显示该Toast
     * @param message  Toast显示的内容
     */
    public static void longToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 显示短时间的提示信息
     *
     * @param activity 需要在哪个活动显示该Toast
     * @param message  Toast显示的内容
     */
    public static void shortToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}
