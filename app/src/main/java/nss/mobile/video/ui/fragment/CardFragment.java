package nss.mobile.video.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortManager;
import cn.com.aratek.fp.FingerprintScanner;
import nss.mobile.video.MyApp;
import nss.mobile.video.R;
import nss.mobile.video.base.BaseFragment;
import nss.mobile.video.base.BindLayout;
import nss.mobile.video.base.bind.BindView;
import nss.mobile.video.bean.CardDetails;
import nss.mobile.video.card.aratek.SwitchUtil;
import nss.mobile.video.card.liveface.CardDevice;
import nss.mobile.video.card.liveface.ZKLiveFaceAnalyzer;
import nss.mobile.video.card.provider.AsyncParseSFZ;
import nss.mobile.video.card.provider.CardReceiverListener;
import nss.mobile.video.card.provider.CoreWise;
import nss.mobile.video.card.provider.ParseSFZAPI;
import nss.mobile.video.card.receiver.IDCardReceiver;
import nss.mobile.video.card.utils.FileUtils;
import nss.mobile.video.card.utils.ToastUtil;
import nss.mobile.video.ui.adapter.CardDetailsAdapter;
import nss.mobile.video.ui.adapter.ICardDetails;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/11/29
 *
 * @author ql
 */
@BindLayout(layoutRes = R.layout.frag_card, title = "身份证", bindTopBar = false)
public class CardFragment extends BaseFragment {
    @BindView(R.id.frag_card_rv)
    RecyclerView mCardDetailsRv;
    @BindView(R.id.frag_card_zhiwen_iv)
    ImageView mZhiwenIv;
    @BindView(R.id.frag_card_card_iv)
    ImageView mCardIv;
    @BindView(R.id.frag_card_check_photo_tv)
    View mCheckPhotoTv;
    @BindView(R.id.frag_card_check_user_tv)
    View mCheckUserTv;
    @BindView(R.id.frag_card_check_zhiwen_tv)
    View mCheckZhiwenTv;

    public ProgressDialog mProgressDialog;

    private AsyncParseSFZ asyncParseSFZ;
    private byte[] mPersonHead;
    private IDCardReceiver receiver;
    private FingerprintScanner mScanner;
    private Handler mHandler;
    private QMUIDialog.MessageDialogBuilder builder;
    private static final int ISVERIVY = 1234;
    private Vibrator mVibrator;

    private CardDetailsAdapter mCardDetailsAdapter = new CardDetailsAdapter();
    private List<ICardDetails> mCardDetails = new ArrayList<>();

    private ParseSFZAPI.People mOperatePeople;

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
        initRegCodeInfo();
        initHandler();
        initCardRecevier();
        mCheckPhotoTv.setOnClickListener(this);
        mCheckUserTv.setOnClickListener(this);
        mCheckZhiwenTv.setOnClickListener(this);

        builder = new QMUIDialog.MessageDialogBuilder(getContext());
        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        mCardDetailsRv.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mCardDetailsRv.setAdapter(mCardDetailsAdapter);
        initCardData();
        SwitchUtil.getInstance().openUSB();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SerialPortManager.getInstance().isOpen() && !SerialPortManager.getInstance().openSerialPort(CoreWise.type.sfz)) {
            Toast.makeText(getContext(), R.string.open_serial_fail, Toast.LENGTH_SHORT).show();
        }
        mScanner = new FingerprintScanner(getContext());
    }

    private void initAuthOption() {
        boolean isInit = ZKLiveFaceAnalyzer.getInstance(getContext()).isInit();
        if (false == isInit) {
            ToastUtil.showShort(getContext(), "请先导入授权!");
        } else {
//            Intent intent = new Intent(getActivity(), VerifyActivity.class);
//            intent.putExtra("mPersonHead", mPersonHead);
//            startActivity(intent);
//            SwipeCardActivity.this.finish();
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == mCheckZhiwenTv.getId()) {
            if (mOperatePeople == null) {
                displayMessageDialog("请先扫描身份证");
                return;
            }
            if (mOperatePeople.getWhichFinger() == null) {
                displayMessageDialog("当前身份证不包含指纹信息");
                return;
            }
            Message msg = Message.obtain();
            msg.obj = mOperatePeople;
            msg.what = ISVERIVY;
            mHandler.sendMessage(msg);
        } else if (v.getId() == mCheckUserTv.getId()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressDialog("正在读取数据...");
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    asyncParseSFZ.readSFZ(ParseSFZAPI.THIRD_GENERATION_CARD);
                }
            }).start();
        } else if (v.getId() == mCheckPhotoTv.getId()) {
            if (null == mPersonHead) {
                ToastUtil.showShort(getContext(), "请先读取身份证!");
                return;
            }
            showProgressDialog("获取人脸识别权限......");
            initAuthOption();
        }
    }

    /**
     * 生成注册码
     */
    private void initRegCodeInfo() {
        //获取设备指纹信息
        String defaultDevFpFilePath = "/sdcard/zklivefacedevfp.txt";
        String deviceFingeprint = ZKLiveFaceAnalyzer.getInstance(getContext()).getDeviceFingerprint();
        Log.i("liwei", "deviceFingeprint:" + deviceFingeprint);
        FileUtils.writeFile(defaultDevFpFilePath, deviceFingeprint);
//        mTvTip.setText("设备指纹信息文件路径:"+ defaultDevFpFilePath);
    }


    private void initCardRecevier() {
        receiver = CardDevice.initCardReceiver(getContext(), new CardReceiverListener() {
            @Override
            public void actionUsbDeviceAttached() {
                asyncParseSFZ.openFingerDevice(getContext(), mScanner);
            }

            @Override
            public void actionUsbDeviceDetached() {
                asyncParseSFZ.closeFingerDevice(getContext(), mScanner);
            }
        });
    }


    private void initCardData() {
        asyncParseSFZ = new AsyncParseSFZ(getActivity().getMainLooper(), getContext());
        asyncParseSFZ.setOnReadSFZListener(new AsyncParseSFZ.OnReadSFZListener() {
            @Override
            public void onReadSuccess(ParseSFZAPI.People people) {
                Log.i("liwei", "onReadSuccess");
                mVibrator.vibrate(60);
                cancleProgressDialog();
                updateInfo(people);
            }

            @Override
            public void onReadFail(int confirmationCode) {
                cancleProgressDialog();
                if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL) {
                    ToastUtil.showShort(getContext(), "未寻到卡,有返回数据");
                } else if (confirmationCode == ParseSFZAPI.Result.TIME_OUT) {
                    ToastUtil.showShort(getContext(), "未寻到卡,无返回数据，超时！！");
                } else if (confirmationCode == ParseSFZAPI.Result.OTHER_EXCEPTION) {
                    ToastUtil.showShort(getContext(), "可能是串口打开失败或其他异常");
                } else if (confirmationCode == ParseSFZAPI.Result.NO_THREECARD) {
                    ToastUtil.showShort(getContext(), "此二代证没有指纹数据");
                    mZhiwenIv.setImageResource(R.drawable.finger_gray);
                } else if (confirmationCode == ParseSFZAPI.Result.TIME_OUT) {
                    Toast.makeText(getContext(), "未寻到卡,无返回数据，超时！！(串口无数据)", Toast.LENGTH_SHORT).show();
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_8084) {
                    Toast.makeText(getContext(), "未寻到卡,有返回数据(80)", Toast.LENGTH_SHORT).show();
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_4145) {
                    Toast.makeText(getContext(), "未寻到卡,有返回数据(41)", Toast.LENGTH_SHORT).show();
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_other) {
                    Toast.makeText(getContext(), "未寻到卡,有返回数据(其他错误)", Toast.LENGTH_SHORT).show();
                } else if (confirmationCode == ParseSFZAPI.Result.FIND_FAIL_Length) {
                    Toast.makeText(getContext(), "未寻到卡,有返回数据(数据接收不完整)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateInfo(ParseSFZAPI.People people) {
//        mTvAddress.setText("地址:" + people.getPeopleAddress());
//        mTvCardNum.setText("身份证:" + people.getPeopleIDCode());
//        mTvBirth.setText("出生日期:" + people.getPeopleBirthday());
//        mTvName.setText("姓名:" + people.getPeopleName());
//        mTvNation.setText("民族:" + people.getPeopleNation());
//        mTvSex.setText("性别:" + people.getPeopleSex());
        mOperatePeople = people;
        mCardDetails.clear();
        mCardDetails.add(new CardDetails("姓名:", people.getPeopleName(), 0));
        mCardDetails.add(new CardDetails("性别:", people.getPeopleSex(), 1));
        mCardDetails.add(new CardDetails("出生日期:", people.getPeopleBirthday(), 2));
        mCardDetails.add(new CardDetails("地址:", people.getPeopleAddress(), 3));
        mCardDetails.add(new CardDetails("民族:", people.getPeopleNation(), 4));
        mCardDetails.add(new CardDetails("身份证:", people.getPeopleIDCode(), 5));
        mCardDetailsAdapter.notifyDataSetChanged();

        mPersonHead = people.getPhoto();

        if (mPersonHead != null) {
            Bitmap photo = BitmapFactory.decodeByteArray(mPersonHead, 0,
                    mPersonHead.length);
            mCardIv.setImageBitmap(photo);
        }

        if (people.getModel() != null) {
            mZhiwenIv.setImageResource(R.drawable.finger_green);
//            mTvFingerTip.setText("第一组指纹数据为:" + people.getWhichFinger()[0] + "\n第二组指纹数据为:" + people.getWhichFinger()[1] + "\n" + DataUtils.toHexString(people.getModel()));
//            Log.i("liwei", "--" + people.getWhichFinger()[0] + "--" + people.getWhichFinger()[1] + "----指纹数据长度-----" + mTvFingerTip.length());
            Message msg = Message.obtain();
            msg.obj = people;
            msg.what = ISVERIVY;
            mHandler.sendMessage(msg);
        } else {
//            mTvFingerTip.setText("无指纹信息");
            mZhiwenIv.setImageResource(R.drawable.finger_gray);
        }
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ISVERIVY://是否比对指纹
                        switch (CoreWise.getModel()) {
                            case CoreWise.device.CFON640:
                                break;
                            case CoreWise.device.A370:
                                break;
                            case CoreWise.device.U3_640:
                                final ParseSFZAPI.People people = (ParseSFZAPI.People) msg.obj;

                                builder.setTitle("是否比对指纹？")
                                        .addAction("比对", new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int index) {
                                                dialog.cancel();
                                                //采集指纹
                                                showProgressDialog("正在比对指纹");
                                                asyncParseSFZ.idCardVerify(mHandler, mScanner, people);
                                            }
                                        })
                                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                                            @Override
                                            public void onClick(QMUIDialog dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setMessage(people.getWhichFinger()[0] + "_" + people.getWhichFinger()[1]);
                                builder.show();
                                break;
                            case CoreWise.device.U3_A370:
                                break;
                        }
                        break;
                    case AsyncParseSFZ.IDCARD_IDCARDVERIFY_SUCCESS:
                        boolean result = (boolean) msg.obj;
                        if (result) {
                            Toast.makeText(getContext(), "比对成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "比对失败！", Toast.LENGTH_SHORT).show();
                        }
                        byte[] bitmapByte = MyApp.BitmapFinger;
                        if (null != bitmapByte) {
                            Bitmap image = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
                            mZhiwenIv.setImageBitmap(image);
                        }
                        cancleProgressDialog();
                        break;
                    case AsyncParseSFZ.IDCARD_EXTRACTFEATURE_ERROR:
                        Toast.makeText(getContext(), getString(R.string.enroll_failed_because_of_extract_feature), Toast.LENGTH_SHORT).show();
                        break;
                    case AsyncParseSFZ.IDCARD_MAKETEMPLATE_ERROR:
                        Toast.makeText(getContext(), getString(R.string.enroll_failed_because_of_make_template), Toast.LENGTH_SHORT).show();
                        break;

                    case AsyncParseSFZ.IDCARD_IDCARDVERIFY_ERROR:
                        Toast.makeText(getContext(), "指纹库比对失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }


    public void showProgressDialog(String message) {
        displayLoadingDialog(message);
    }

    public void cancleProgressDialog() {
        cancelLoadingDialog();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
        SwitchUtil.getInstance().closeUSB();
    }
}
