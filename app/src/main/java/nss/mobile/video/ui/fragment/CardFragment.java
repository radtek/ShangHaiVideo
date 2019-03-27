package nss.mobile.video.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android_serialport_api.SerialPortManager;
import nss.mobile.video.C;
import nss.mobile.video.R;
import nss.mobile.video.base.BaseFragment;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.CardDetails;
import nss.mobile.video.bean.MobileKeyBean;
import nss.mobile.video.card.authentication.CoreWise;
import nss.mobile.video.card.authentication.api.sfz.AsyncParseSFZ;
import nss.mobile.video.card.authentication.api.sfz.ParseSFZAPI;
import nss.mobile.video.card.authentication.aratek.SwitchUtil;
import nss.mobile.video.http.CardCheckHttp;
import nss.mobile.video.http.OkHttpHeader;
import nss.mobile.video.info.UrlApi;
import nss.mobile.video.ui.adapter.CardDetailsAdapter;
import nss.mobile.video.ui.adapter.ICardDetails;
import okhttp3.Call;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/29
 *
 * @author ql
 */
@BindLayout(layoutRes = R.layout.frag_card, title = "身份证", bindTopBar = false)
public class CardFragment extends BaseFragment {
    private static final int ISVERIVY = 1234;
    private static final String TAG = "CardFragment";


    @BindView(R.id.frag_card_fp_hint)
    TextView mFpHint;

    @BindView(R.id.frag_card_rv)
    RecyclerView mCardDetailsRv;
    @BindView(R.id.frag_card_zhiwen_iv)
    ImageView mZhiwenIv;
    @BindView(R.id.frag_card_zhiwen_check_iv)
    ImageView mZhiwenCheckIv;
    @BindView(R.id.frag_card_card_iv)
    ImageView mCardIv;
    @BindView(R.id.frag_card_check_photo_tv)
    View mCheckPhotoTv;
    @BindView(R.id.frag_card_check_user_tv)
    TextView mCheckUserTv;
    @BindView(R.id.frag_card_check_zhiwen_tv)
    View mCheckZhiwenTv;

    @BindView(R.id.hint)
    TextView mHintText;


    private IFingerPrint fingerPrint;


    private CardDetailsAdapter mCardDetailsAdapter = new CardDetailsAdapter();
    private List<ICardDetails> mCardDetails = new ArrayList<>();

    private ParseSFZAPI.People mOperatePeople;
    private Vibrator mVibrator;

    private IDCardReceiver receiver;
    private UsbManager usbManager;
    private SoundPool soundPool;
    private int load;
    private MediaPlayer mediaPlayer;
    private AsyncParseSFZ asyncParseSFZ;
    private boolean isSequentialRead;
    private IntentFilter filter;

    @Override
    protected void initData() {
        super.initData();
        mCardDetails.add(new CardDetails("姓名:", null, 0));
        mCardDetails.add(new CardDetails("性别:", null, 1));
        mCardDetails.add(new CardDetails("出生日期:", null, 2));
        mCardDetails.add(new CardDetails("地址:", null, 3));
        mCardDetails.add(new CardDetails("民族:", null, 4));
        mCardDetails.add(new CardDetails("身份证:", null, 5));
        mCardDetailsAdapter.setNewData(mCardDetails);


    }

    @Override
    protected void initWidget(View view) {
        super.initWidget(view);
        mCheckPhotoTv.setOnClickListener(this);
        mCheckUserTv.setOnClickListener(this);
        mCheckZhiwenTv.setOnClickListener(this);

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        mCardDetailsRv.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mCardDetailsRv.setAdapter(mCardDetailsAdapter);


        usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        load = soundPool.load(getContext(), R.raw.ok, 1);

        receiver = new IDCardReceiver();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getActivity().registerReceiver(receiver, filter);
        initCardData();
//避免刚打开usb指纹时，usb处于指纹模块时，导致的白屏
        SwitchUtil.getInstance().closeUSB();

        SwitchUtil.getInstance().openUSB();

    }


    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        if (v.getId() == mCheckUserTv.getId()) {
            readSFZ();
        } else if (v.getId() == mCheckZhiwenTv.getId()) {
            if (mOperatePeople == null) {
                return;
            }
            if (checkFingerInitFailedDev()) {
                return;
            }
            byte[] model = mOperatePeople.getModel();
            if (model == null) {
                displayMessageDialog("当前用户无指纹信息");
                return;
            }
            if (!fingerPrint.openFingerDev()) {
                displayMessageDialog("指纹设备初始化失败");
                return;
            }
            displayLoadingDialog("指纹比对中");
            fingerPrint.checkFinger(mOperatePeople);
        } else if (v.getId() == mCheckPhotoTv.getId()) {
            if (mOperatePeople == null) {
                displayMessageDialog("请先扫描身份证");
                return;
            }
            showCameraAction();
        }
    }

    private boolean checkFingerInitFailedDev() {
        if (fingerPrint == null) {
            displayMessageDialog("指纹设备初始化失败");
            return false;
        }
        return false;
    }


    @Override
    public void onCameraResult(File mTmpFile) {
        displayLoadingDialog("校验身份中");
        C.sTHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(mTmpFile.getAbsolutePath());
                Bitmap bitmap1 = CardCheckHttp.compressScale(bitmap);
                String encodeImgs = CardCheckHttp.encodeImage(bitmap1);
                Map<String, Object> map = new HashMap<>();
                map.put("capture-image", encodeImgs);
                map.put("face-time", System.currentTimeMillis());
                CardCheckHttp.checkDard(mOperatePeople.getPeopleName()
                        , mOperatePeople.getPeopleIDCode()
                        , encodeImgs
                        , new CardCheckHttp.OnCheckCardFaceListener() {
                            @Override
                            public void onFailed(String error) {
                                map.put("face-verify", 1);
                                uploadCheckDetails(map);
                                C.sHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        toast(error);
                                        displayMessageDialog(error);
                                        cancelLoadingDialog();
                                    }
                                });

                            }

                            @Override
                            public void onSuccess(Map o) {
                                map.put("face-verify", 0);
                                uploadCheckDetails(map);
                                C.sHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelLoadingDialog();
                                        displayTipDialogSuccess("验证成功");
                                        C.sHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                cancelTipDialogSuccess();
                                            }
                                        }, 2_000);
                                    }
                                });

                            }
                        });
            }
        });
    }

    /**
     * 读取身份证
     */
    private void readSFZ() {
        isSequentialRead = true;
        displayLoadingDialog("请扫描身份证信息");
        mCheckUserTv.setText(R.string.load_user_card);

        asyncParseSFZ.readSFZ(ParseSFZAPI.THIRD_GENERATION_CARD);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fingerPrint != null) {
            fingerPrint.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SerialPortManager.getInstance().isOpen() && !SerialPortManager.getInstance().openSerialPort(CoreWise.type.sfz)) {
            Toast.makeText(getContext(), R.string.open_serial_fail, Toast.LENGTH_SHORT).show();
        }
        getActivity().registerReceiver(receiver, filter);
        if (fingerPrint != null) {
            fingerPrint.onResume();
        }

    }

    //pause
    @Override
    public void onPause() {
        super.onPause();

    }


    private void initCardData() {

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.ok);
        asyncParseSFZ = new AsyncParseSFZ(getContext().getMainLooper(), getContext());
        asyncParseSFZ.setOnReadSFZListener(new AsyncParseSFZ.OnReadSFZListener() {

            @Override
            public void onReadSuccess(ParseSFZAPI.People people) {
                cancelLoadingDialog();
                mOperatePeople = people;
                resetSFZView(people);
                mVibrator.vibrate(60);
                soundPool.play(load, 1, 1, 0, 0, 1);
            }

            @Override
            public void onReadFail(int confirmationCode) {
                if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL) {
                    if (!isSequentialRead) {
                        cancelLoadingDialog();
                        displayMessageDialog("未寻到卡,有返回数据");
                    }
                } else if (confirmationCode == ParseSFZAPI.Result.TIME_OUT) {
                    if (!isSequentialRead) {
                        cancelLoadingDialog();
                        displayMessageDialog("未寻到卡,无返回数据，超时！！(串口无数据)");
                    }
                } else if (confirmationCode == ParseSFZAPI.Result.OTHER_EXCEPTION) {
                    if (!isSequentialRead) {
//                        Toast.makeText(getContext(), "可能是串口打开失败或其他异常", Toast.LENGTH_SHORT).show();
                        cancelLoadingDialog();

                        displayMessageDialog("可能是串口打开失败或其他异常");

                    }
                } else if (confirmationCode == ParseSFZAPI.Result.NO_THREECARD) {
                    if (!isSequentialRead) {
//                        Toast.makeText(getContext(), "此二代证没有指纹数据", Toast.LENGTH_SHORT).show();
                        cancelLoadingDialog();

                        displayMessageDialog("此二代证没有指纹数据");

                    }
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_8084) {
                    if (!isSequentialRead) {
                        cancelLoadingDialog();

                        displayMessageDialog("未寻到卡,有返回数据(80)");

                    }
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_4145) {
                    if (!isSequentialRead) {
//                        Toast.makeText(getContext(), "未寻到卡,有返回数据(41)", Toast.LENGTH_SHORT).show();
                        cancelLoadingDialog();

                        displayMessageDialog("未寻到卡,有返回数据(41)");

                    }
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_other) {
                    if (!isSequentialRead) {
//                        Toast.makeText(getContext(), "未寻到卡,有返回数据(其他错误)", Toast.LENGTH_SHORT).show();
                        cancelLoadingDialog();

                        displayMessageDialog("未寻到卡,有返回数据(其他错误)");

                    }
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_Length) {
                    if (!isSequentialRead) {
//                        Toast.makeText(getContext(), "未寻到卡,有返回数据(数据接收不完整)", Toast.LENGTH_SHORT).show();
                        cancelLoadingDialog();

                        displayMessageDialog("未寻到卡,有返回数据(数据接收不完整)");

                    }
                }

            }
        });

        asyncParseSFZ.setOnReadCardIDListener(new AsyncParseSFZ.OnReadCardIDListener() {

            @Override
            public void onReadSuccess(String id) {
                //moduleView.setText(id);
                if (!isSequentialRead) {

                }
            }

            @Override
            public void onReadFail() {
                Toast.makeText(getContext(), "读取卡号失败", Toast.LENGTH_SHORT).show();
                if (!isSequentialRead) {

                }
            }
        });
    }

    @Override
    public void displayMessageDialog(CharSequence msg) {
        super.displayMessageDialog(msg);
    }

    /**
     * 用于显示 用户信息
     *
     * @param people
     */
    private void resetSFZView(ParseSFZAPI.People people) {
        StringBuffer sb2 = new StringBuffer();

        mOperatePeople = people;
        mCardDetails.clear();
        mCardDetails.add(new CardDetails("姓名:", people.getPeopleName(), 0));
        mCardDetails.add(new CardDetails("性别:", people.getPeopleSex(), 1));
        mCardDetails.add(new CardDetails("出生日期:", people.getPeopleBirthday(), 2));
        mCardDetails.add(new CardDetails("地址:", people.getPeopleAddress(), 3));
        mCardDetails.add(new CardDetails("民族:", people.getPeopleNation(), 4));
        mCardDetails.add(new CardDetails("身份证:", people.getPeopleIDCode(), 5));
        mCheckUserTv.setText(R.string.user_card);
        Map<String, Object> map = new HashMap<>();
        MobileKeyBean last = MobileKeyBean.getLast();
        map.put("box-code", last.getMobileKey());
        map.put("identity", people.getPeopleIDCode());
        map.put("name", people.getPeopleName());
        map.put("birth-date", people.getPeopleBirthday());
        map.put("sex", people.getPeopleSex());
        map.put("adress", people.getPeopleAddress());
        map.put("nation", people.getPeopleNation());


        mCardDetailsAdapter.setNewData(mCardDetails);
        if (people.getPhoto() != null) {
            Bitmap photo = BitmapFactory.decodeByteArray(people.getPhoto(), 0,
                    people.getPhoto().length);
            mCardIv.setImageBitmap(photo);
        } else {
            displayMessageDialog("没有照片");
        }
        String[] whichFinger = people.getWhichFinger();
        if (whichFinger == null || whichFinger.length == 0 || whichFinger[0] == null) {
            mFpHint.setText("没有指纹");
        } else {
            StringBuffer sb = new StringBuffer();
            for (String s : whichFinger) {
                sb.append(s).append("+");
            }
            String substring = "";
            if (sb.length() > 0) {
                substring = sb.substring(0, sb.length() - 1);
            }
            mFpHint.setText(substring);
        }
        uploadCheckDetails(map);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
        }
        Log.i("whw", "SFZActivity onDestroy");
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
        if (checkFingerInitFailedDev()) {
            return;
        }
        fingerPrint.closeFingerDev();

    }

    private List<UsbDevice> getDeviceList() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        List<UsbDevice> usbDevices = new ArrayList<>();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            usbDevices.add(device);
            Log.e(TAG, "getDeviceList: " + device.getDeviceName());
        }
        return usbDevices;
    }

    //用来监听上层应用的广播,动态广播
    public class IDCardReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action) {

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:


                    List<UsbDevice> deviceList = getDeviceList();
                    if (deviceList.size() == 0) {
                        return;
                    }
                    UsbDevice device = deviceList.get(0);
                    String manufacturerName = device.getManufacturerName();
                    Log.i(TAG, "--device number: --" + deviceList.size() + "--device number: --" + deviceList.size() + "--ManufacturerName--" + manufacturerName);


                    switch (manufacturerName) {
                        case "USBKey Chip":
                            //晟元小指纹模块
                            //Toast.makeText(getContext(), "晟元小指纹模块", Toast.LENGTH_SHORT).show();
                            break;

                        case "Aratek     ":
                            //亚略特指纹模块
                            //Toast.makeText(getContext(), "亚略特指纹模块", Toast.LENGTH_SHORT).show();

                            //asyncParseSFZ.openFingerDevice(SFZActivity.this, mScanner);


                            break;

                        case "BHMDevice":
                            //比亚迪大指纹指纹模块
                            //Toast.makeText(getContext(), "比亚迪大指纹指纹模块", Toast.LENGTH_SHORT).show();
                            initFingerPrintSuccess();
                            break;
                        default:
                            break;
                    }


                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    break;

                case Intent.ACTION_SCREEN_ON:
                    Log.i(TAG, "亮屏了");


                    break;
                case Intent.ACTION_SCREEN_OFF:
                    Log.i(TAG, "息屏了");


                    //mHandler.removeCallbacks(task);


                    //息屏切回正常模式，省电
                    //SwitchUtil.getInstance().closeUSB();

                    break;
            }
        }
    }

    private void initFingerPrintSuccess() {
        fingerPrint = new FingerPrintBHMHelper(getContext());

        fingerPrint.setFingerListener(new IFingerPrint.IBHMFingerListener() {
            @Override
            public void checkSuccess() {
                cancelLoadingDialog();
                displayTipDialogSuccess("比对成功");
                mZhiwenCheckIv.setImageResource(R.drawable.select);
                Map<String, Object> map = new HashMap<>();
                MobileKeyBean last = MobileKeyBean.getLast();
                map.put("box-code", last.getMobileKey());
                map.put("identity", mOperatePeople.getPeopleIDCode());
                map.put("finger-time", System.currentTimeMillis());
                map.put("finger-verify", 0);
                uploadCheckDetails(map);
            }

            @Override
            public void checkFailed(String error) {
                cancelLoadingDialog();
                displayMessageDialog(error);
                mZhiwenCheckIv.setImageResource(R.drawable.warning);
                MobileKeyBean last = MobileKeyBean.getLast();
                Map<String, Object> map = new HashMap<>();
                map.put("box-code", last.getMobileKey());
                map.put("identity", mOperatePeople.getPeopleIDCode());
                map.put("finger-time", System.currentTimeMillis());
                map.put("finger-verify", 1);

                uploadCheckDetails(map);


            }

            @Override
            public void printFinger(Bitmap bitmap) {
                mZhiwenIv.setImageBitmap(bitmap);
            }
        });
        fingerPrint.onResume();


    }

    private void uploadCheckDetails(Map map) {
        OkHttpHeader.post(UrlApi.upload_check_id, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id, int code) {

            }
        });
    }

}
