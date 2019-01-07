package nss.mobile.video.card.biometric;

public class ZKFaceDetectService
{
	static {
        System.loadLibrary("zkfacedetect");
    }
	
	public final static int PARAM_CODE_FACE_MIN_WIDTH = 1;			//根据实际摄像头尺寸设置
	public final static int PARAM_CODE_FACE_MAX_WIDTH = 2;			//根据实际摄像头尺寸设置
	public final static int PARAM_CODE_FACE_DETECT_THRESHOLD1 = 3;	//建议默认值0.6  (范围:0~0.9999,阈值越大，漏检的可能性就越高，反之越小，则误检的可能性就越高)
	public final static int PARAM_CODE_FACE_DETECT_THRESHOLD2 = 4;	//建议默认值0.6	 (范围:0~0.9999,阈值越大，漏检的可能性就越高，反之越小，则误检的可能性就越高)
	public final static int PARAM_CODE_FACE_DETECT_THRESHOLD3 = 5;	//建议默认值0.7  (范围:0~0.9999,阈值越大，漏检的可能性就越高，反之越小，则误检的可能性就越高)
	public final static int PARAM_CODE_FACE_SCALE_FACTOR = 6;		//建议默认值0.71 (范围:0.5~0.95,越小越快)
	public final static int PARAM_CODE_FACE_MAX_DETECT_COUNT = 7;	//最大探测人脸数(1~10)
	
	//创建一个实例
	public static native long createContext();
	//设置参数，参数代码如上，参数值字符串
	public static native int setParameter(long context, int code, String value);
	//暂不支持
	public static native String getParameter(long context, int code);
	//rgb为rgb 24数据， detectRect为指定探测区域，探测整张图像设置null(detectRect[0]~detecRect[3], x, y, width, height), detectFaceCount返回探测数
	public static native int detect(long context, byte[] bgr, int width, int height, int[] detectRect, int[] detectFaceCount);
	public static native long getFaceContext(long context, int faceIndex);
	public static native int closeFaceContext(long faceContext);
	
	//返回人脸坐标rect[0]~rect[3]：x,y,width, height
	public static native int getFaceRect(long faceContext, int[] rect);
	//获取人脸姿态，3个角度
	public static native int getFacePose(long faceContext, float[] yaw, float[] pitch, float[] roll);
	//获取人脸可靠性概率
	public static native int getFaceQuality(long faceContext, float[] quality);
	//加密数据，不能直接显示
	public static native int getFaceCropData(long faceContext, byte[] cropData, int[] size);
	
	//加密数据，不能直接显示,数据格式Base64
	public static native int getFaceCropDataBase64(long faceContext, byte[] cropData, int[] size);
	public static native int closeContext(long context);
}
