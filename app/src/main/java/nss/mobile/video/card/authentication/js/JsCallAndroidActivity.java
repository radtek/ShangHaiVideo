package nss.mobile.video.card.authentication.js;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import android_serialport_api.SerialPortManager;
import nss.mobile.video.R;
import nss.mobile.video.card.authentication.CoreWise;
import nss.mobile.video.card.authentication.api.hxuhf.UHFHXAPI;
import nss.mobile.video.card.authentication.utils.DataUtils;
import nss.mobile.video.card.authentication.utils.ToastUtil;

/**
 * 作者：李阳
 * 时间：2018/11/21
 * 描述：
 */
public class JsCallAndroidActivity extends AppCompatActivity {

    private static final String TAG = "JsCallAndroidActivity";

    private WebView mWebView = null;

    public UHFHXAPI api = new UHFHXAPI();


    //private String url = "file:///android_asset/index.html";
    private String url = "http://mastermes.com/scan.html";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1234:
                    mWebView.loadUrl("javascript:returnResult('" + msg.obj + "')");
                    //mWebView.loadUrl("javascript:callJS()");

                    //Toast.makeText(JsCallAndroidActivity.this, "data: "+msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SerialPortManager.getInstance().openSerialPort(CoreWise.type.uhf)) {
            ToastUtil.showToast(this, R.string.open_serial_fail);
        } else {
            ToastUtil.showToast(this, R.string.open_serial_success);
        }



        mWebView = new WebView(this);
        WebSettings webSettings = mWebView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //在类里实现javascript想调用的方法，并将其实例化传入webview, "hello"这个字串告诉javascript调用哪个实例的方法
        mWebView.addJavascriptInterface(new CwJSInterface(), "cw");
        // 先载入JS代码
        // 格式规定为:file:///android_asset/文件名.html
        mWebView.loadUrl(url);
        setContentView(mWebView);


        // 由于设置了弹窗检验调用结果,所以需要支持js对话框
        // webview只是载体，内容的渲染需要使用webviewChromClient类去实现
        // 通过设置WebChromeClient对象处理JavaScript的对话框
        //设置响应js 的Alert()函数
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(JsCallAndroidActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        api.open();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        api.close();
        SerialPortManager.getInstance().closeSerialPort();
    }

    class CwJSInterface {


        @JavascriptInterface
        public void scanUHF() {
            Log.i(TAG, "扫描超高频标签了！");

            int times = 5000;// 默认超时5秒
            byte code = 1;// 默认读取epc区域 0:读取EPC,1:读取TID
            short sa = 0;// 默认偏移从0开始
            short dl = 5;// 默认数据长度5
            String pwd = "00000000";// 默认访问密码00000000

            final Message msg = Message.obtain();
            msg.what = 1234;


            api.startAutoRead2C(times, code, pwd, sa, dl, new UHFHXAPI.SearchAndRead() {
                @Override
                public void timeout() {
                    Log.e(TAG, "timeout");
                    msg.obj = "-1";
                    mHandler.sendMessage(msg);
                }

                @Override
                public void returnData(final byte[] data) {
                    Log.e(TAG + "JS", "Js data:" + DataUtils.toHexString(data));

                    msg.obj = DataUtils.toHexString(data);
                    mHandler.sendMessage(msg);

                }

                @Override
                public void readFail() {
                    Log.e(TAG, "readfail");
                    msg.obj = "-2";
                    mHandler.sendMessage(msg);
                }
            });
        }


    }
}
