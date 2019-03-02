package top.zennan.bihu.utils;

import android.accounts.NetworkErrorException;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpConnectUtil {

    private static final int TIMEOUT_IN_MILLIONS = 5 * 1000;
    private static String sData;
    private static Handler handler = new Handler();

    public interface CallBack {
        void onResponse(String response);
    }

    /**
     * 异步方式的网络请求
     *
     * @param urlString
     * @param param
     * @param callBack
     */
    public static void doAsyncPost(final String urlString, final String param, final CallBack callBack) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = doSyncPost(urlString, param);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onResponse(response);
                    }
                });
            }
        }).start();
    }


    /**
     * 同步方式的网络请求
     *
     * @param urlString 接口
     * @param param     参数
     * @return 返回获取到的数据
     */
    public static String doSyncPost(final String urlString, final String param) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_IN_MILLIONS);
            connection.setReadTimeout(TIMEOUT_IN_MILLIONS);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //POST模式不使用缓存
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            //自动重定向
            connection.setInstanceFollowRedirects(true);
            // 配置请求Content-Type
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=utf-8");
            //设置字符编码
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            //开始连接
            connection.connect();
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.write(param.getBytes());
            dos.flush();
            dos.close();
            //判断是否成功
            if (connection.getResponseCode() == 200) {
                //获取返回的输入流
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                sData = response.toString();
            } else {
                //请求失败
                throw new NetworkErrorException("response status is " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return sData;
    }
}
