package nss.mobile.video.card.authentication.api.hxuhf;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.LooperBuffer;
import android_serialport_api.SerialPortManager;
import nss.mobile.video.card.authentication.CoreWise;
import nss.mobile.video.card.authentication.utils.Bytes;
import nss.mobile.video.card.authentication.utils.DataUtils;

/**
 * CoreWise HXUHF Api
 */
public class UHFHXAPI {

    private static final String TAG = "UHFHXAPI";

    private ExecutorService Executor = Executors.newSingleThreadExecutor();
    private boolean flag;
    private int isStart = 0;


    private static final byte[] U3_OPEN_COMMAND = {(byte) 0xca, (byte) 0xdf, 0x05, 0x36, 00, 01, 0x00, (byte) 0xe3};
    private static final byte[] U3_CLOSE_COMMAND = {(byte) 0xca, (byte) 0xdf, 0x05, 0x36, 00, 01, 0x01, (byte) 0xe3};


    private static final byte[] OPEN_COMMAND_370 = "D&C0004010O".getBytes();
    private static final byte[] CLOSE_COMMAND_370 = "D&C0004010P".getBytes();
    private static final byte[] OPEN_COMMAND_640 = "D&C0004010D".getBytes();
    private static final byte[] CLOSE_COMMAND_640 = "D&C0004010E".getBytes();


    private static final byte Preamble = (byte) 0xBB;

    private static final byte EndMask = (byte) 0x7E;

    private static final class MessageType {
        public static final byte Command = 0x00;
        public static final byte Response = 0x01;
        public static final byte Notification = 0x02;
    }

    private static final class MessageCode {
        public static final byte Set_Reader_Power_Control = 0x01;
        public static final byte Get_Reader_Information = 0x03;
        public static final byte Get_Region = 0x06;
        public static final byte Set_Region = 0x07;
        public static final byte Set_System_Reset = 0x08;
        public static final byte Get_Type_C_AI_Select_Parameters = 0x0B;
        public static final byte Set_Type_C_AI_Select_Parameters = 0x0C;
        public static final byte Get_Type_C_AI_Query_Related_Parameters = 0x0D;
        public static final byte Set_Type_C_AI_Query_Related_Parameters = 0x0E;
        public static final byte Get_current_RF_Channel = 0x11;
        public static final byte Set_current_RF_Channel = 0x12;
        public static final byte Get_FH_and_LBT_Parameters = 0x13;
        public static final byte Set_FH_and_LBT_Parameters = 0x14;
        public static final byte Set_Tx_Power_Level = 0x16;
        public static final byte RF_CW_signal_control = 0x17;

        public static final byte Get_Tx_Power_Level = (byte) 0x15;

        public static final byte Set_Antenna = 0x1B;

        public static final byte Read_Type_C_UII = 0x22;
        public static final byte Start_Auto_Read = 0x27;
        public static final byte Stop_Auto_Read = 0x28;
        public static final byte Read_Type_C_Tag_Data = 0x29;
        public static final byte Get_Frequency_Hopping_Table = 0x30;
        public static final byte Set_Frequency_Hopping_Table = 0x31;
        public static final byte Get_Modulation = 0x32;
        public static final byte Set_Modulation = 0x33;
        public static final byte Get_Anti_Collision_Mode = 0x34;
        public static final byte Set_Anti_Collision_Mode = 0x35;
        public static final byte Start_Auto_Read2 = 0x36;
        public static final byte Stop_Auto_Read2 = 0x37;
        public static final byte Write_Type_C_Tag_Data = 0x46;
        public static final byte BlockWrite_Type_C_Tag_Data = 0x47;
        public static final byte BlockErase_Type_C_Tag_Data = 0x48;
        public static final byte BlockPermalock_Type_C_Tag = (byte) 0x83;
        public static final byte Kill_Recom_Type_C_Tag = 0x65;
        public static final byte Lock_Type_C_Tag = (byte) 0x82;
        public static final byte Get_Temperature = (byte) 0xB7;
        public static final byte Get_RSSI = (byte) 0xC5;
        public static final byte Scan_RSSI = (byte) 0xC6;
        public static final byte Update_Registry = (byte) 0xD2;
        public static final byte Erase_Registry = (byte) 0xD3;
        public static final byte Get_Registry_Item = (byte) 0xD4;
        public static final byte Command_Failure = (byte) 0xFF;

        public static final byte Get_Tx_Power_Level_640 = (byte) 0xD4;
        public static final byte Get_Tx_Power_Level_370 = (byte) 0x15;
    }

    private class Packet {
        byte preamble = Preamble;
        byte messageType;
        byte messageCode;
        short payloadLength;
        byte[] payloadData;
        byte endMask = EndMask;
        byte[] crc16;

        public Packet(byte messageType, byte messageCode, byte[] payloadData) {
            this.messageType = messageType;
            this.messageCode = messageCode;
            this.payloadLength = (short) (payloadData == null ? 0 : payloadData.length);
            this.payloadData = payloadData;
            if (payloadData != null) {
                Log.i("whw", "payloadData=" + DataUtils.toHexString(payloadData));
            }
        }

        public byte[] getPacket() {
            byte[] commandPacket = null;
            if (payloadLength == 0) {
                commandPacket = Bytes.concat(new byte[][]{new byte[]{messageType}, new byte[]{messageCode},
                        DataUtils.short2byte(payloadLength), new byte[]{endMask}});
            } else {
                commandPacket = Bytes.concat(new byte[][]{new byte[]{messageType}, new byte[]{messageCode},
                        DataUtils.short2byte(payloadLength), payloadData, new byte[]{endMask}});
            }

            crc16 = getCRC(commandPacket);
            byte[] packet = Bytes.concat(new byte[][]{new byte[]{preamble}, commandPacket, crc16});
            Log.i("whw", "packet=" + DataUtils.toHexString(packet));
            return packet;
        }
    }

    public static class Response {
        public static final int TIME_OUT = -1;
        public static final int RESPONSE_PACKET = 0x01;
        public static final int COMMAND_FAILURE = 0x02;
        public int result;
        public byte[] data;
    }

    private byte[] buffer = new byte[1024];

    /**
     * Function Description: Open the UHF RFID module(打开超高频模组)
     *
     * @return Boolean
     */

    public boolean open() {

        // 0:A370 1:CFON640 2:其他机型,3:msm8953 for arm64
        switch (CoreWise.getAndroidVersion()) {

            case CoreWise.deviceSysVersion.O:
                int requestLength = 1;
                int len;

                if (CoreWise.getModel() == CoreWise.device.A370) {
                    SerialPortManager.getInstance().write(OPEN_COMMAND_370);
                    int a = SerialPortManager.getInstance().read(buffer, 3000, 100);
                    len = SerialPortManager.getInstance().readFixedLength(buffer, 3000, requestLength);
                    if (len > 0 && (buffer[0] == 'R' || buffer[0] == 'O')) {

                        return true;
                    }
                    return false;

                } else if (CoreWise.getModel() == CoreWise.device.CFON640) {
                    SerialPortManager.getInstance().write(OPEN_COMMAND_640);
                    requestLength = 1;
                    len = SerialPortManager.getInstance().readFixedLength(buffer, 3000, requestLength);
                    if (len > 0 && (buffer[0] == 'R' || buffer[0] == 'O')) {

                        return true;
                    }
                    return false;
                }
                break;
            case CoreWise.device.other:

                break;
            case CoreWise.deviceSysVersion.U:  //msm8953

                Log.i(TAG, "-------msm8953");
                SerialPortManager.getInstance().write(U3_OPEN_COMMAND);


                break;
        }

        //默认最大功率26
        String[] dbmStr = "26.0".split("\\.");
        int dbm = Integer.parseInt(dbmStr[0] + dbmStr[1]);
        byte[] data = DataUtils.int2Byte2(dbm);
        Log.d("jokey", "dbm  " + DataUtils.toHexString(data));
        //byte[] data = { 0x00, (byte) dbm };
        Response response = setTxPowerLevel(data);
        if (response.result == Response.RESPONSE_PACKET) {
            if (response.data[0] == 0x00) {
                Log.i("TTTTTT", "TxPower Update success!");
            }
        }

        return true;
    }

    /**
     * Function Description: Close the UHF RFID module(关闭超高频模块)
     *
     * @return
     */
    public void close() {
        SerialPortManager.getInstance().write(U3_CLOSE_COMMAND);
    }


    public int transceive(byte messageType, byte messageCode, byte[] data) {
        buffer = null;
        buffer = new byte[1024];
        Packet p = new Packet(messageType, messageCode, data);
        Log.d("jokey", "getPacket：" + DataUtils.toHexString(p.getPacket()));
        Log.d("jokey", "send：" + DataUtils.toHexString(topackage(p.getPacket())));
        SerialPortManager.getInstance().write(topackage(p.getPacket()));
        int len = SerialPortManager.getInstance().read(buffer, 3000, 100);
        if (len > 0) {
            byte[] temp = new byte[len];
            System.arraycopy(buffer, 0, temp, 0, temp.length);
            Log.i("zzd", "temp=" + DataUtils.toHexString(temp));
        }
        return len;
    }

    /**
     * Function Description: Set power mode（设置功率模式）
     *
     * @return
     */
    public void setReaderPowerMode(byte mode) {
        int length = transceive(MessageType.Command, MessageCode.Set_Reader_Power_Control, new byte[]{mode});
    }

    /**
     * Function Description: Get basic information from the reader.（获取模块基本信息）
     *
     * @param argument <br>
     *                 - Model (0x00)<br>
     *                 - S/N (0x01)<br>
     *                 - Manufacturer (0x02)<br>
     *                 - Frequency (0x03)<br>
     *                 - Tag Type (0x04)<br>
     * @return Example1) Manufacturer = PHYCHIPS.<br>
     * Example2) Tag Type = ISO 18000-6 Type B(0x01), ISO 18000-6 Type
     * C(0x02).
     */
    public Response getReaderInformation(int argument) {
        int length = transceive(MessageType.Command, MessageCode.Get_Reader_Information,
                new byte[]{(byte) argument});
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Reader_Information) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get the current region. PR9200 uses individual
     * channel table that depends on region. List of region code follows below.
     * （获取信道，PR9200信道取决于Region，以下是信道代码）
     *
     * @return - Korea (0x11)<br>
     * - US (0x21)<br>
     * - US2 (0x22)<br>
     * - Europe (0x31)<br>
     * - Japan (0x41)<br>
     * - China1 (0x51)<br>
     * - China2 (0x52)<br>
     */
    public Response getRegion() {
        int length = transceive(MessageType.Command, MessageCode.Get_Region, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Region) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set the current region. PR9200 uses individual
     * channel table that depends on region. List of region code follows below.
     * （设置信道）
     * <p>
     * <p>
     * - Korea (0x11)<br>
     * - US (0x21)<br>
     * - US2 (0x22)<br>
     * - Europe (0x31)<br>
     * - Japan (0x41)<br>
     * - China1 (0x51)<br>
     * - China2 (0x52)<br>
     *
     * @return Success (0x00)
     */
    public Response setRegion(int argument) {
        int length = transceive(MessageType.Command, MessageCode.Set_Region, new byte[]{(byte) argument});
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Region) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set the system level reset.
     * 函数描述: 初始化系统
     *
     * @return Success (0x00)
     */
    public Response setSystemReset() {
        int length = transceive(MessageType.Command, MessageCode.Set_System_Reset, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_System_Reset) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get 18000-6C air interface protocol command ‘Select’ parameters.
     * 函数描述: 获取18000-6C空中接口协议命令'选择'参数。
     *
     * @return - Target (3-bit): S0 (000), S1 (001), S2 (010), S3 (011), SL
     * (100)<br>
     * - Action (3-bit): Refer to ISO18000-6C.<br>
     * - Memory Bank (2-bit): 00 RFU, 01 EPC, 10 TID, 11 User<br>
     * - Pointer (32-bit): Starting mask address<br>
     * - Length (8-bit): mask length bits<br>
     * - Truncate (1-bit): Enable (1) and Disable (0)<br>
     * - Reserve (7-bit): Reserved 0000000 value should be placed here.
     * <br>
     * - Mask (0~255 bits): Mask value<br>
     * Example) Target=S0, Action=assert SL or inventoried - > A,
     * MB=User, Pointer = 0x000000FF,<br>
     * Length =0x20, T=0, Mask = 11111111111111110000000000000000
     */
    public Response getTypeCAISelectParameters() {
        int length = transceive(MessageType.Command, MessageCode.Get_Type_C_AI_Select_Parameters, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Type_C_AI_Select_Parameters) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set 18000-6C air interface protocol command ‘Select’ parameters.
     * 函数描述: 设置18000-6C空中接口协议命令'选择'参数。
     *
     * @param arguments - Target (3-bit): S0 (000), S1 (001), S2 (010), S3 (011), SL
     *                  (100)<br>
     *                  - Action (3-bit): Refer to ISO18000-6C.<br>
     *                  - Memory Bank (2-bit): RFU (00), EPC (01), TID (10), User (11)
     *                  <br>
     *                  - Pointer (32-bit): Starting mask address<br>
     *                  - Length (8-bit): mask length bits<br>
     *                  - Truncate (1-bit): Enable (1) and Disable (0)<br>
     *                  - Reserve (7-bit): Reserved 0000000 value should be placed
     *                  here.<br>
     *                  - Mask (0~255 bits): Mask value<br>
     *                  Example)<br>
     *                  Target=S0 where C, Action=assert SL ors inventoried - > A,
     *                  MB=User, Pointer = 0x000000FF<br>
     *                  Length=0x20, T=0, Mask=11111111111111110000000000000000<br>
     * @return Success (0x00)
     */
    public Response setTypeCAISelectParameters(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_Type_C_AI_Select_Parameters, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Type_C_AI_Select_Parameters) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get 18000-6C air interface protocol command ‘Query’ parameters.
     * 函数描述: 获取18000-6C空中接口协议命令'查询'参数。
     *
     * @return - DR (1-bit): DR=8 (0), DR=64/3 (1)<br>
     * - M (2-bit): M=1 (00), M=2 (01), M=4 (10), M=8 (11)<br>
     * - TRext (1-bit): No pilot tone (0), Use pilot tone (1)<br>
     * - Sel (2-bit): All (00 or 01), ~SL (10), SL (11)<br>
     * - Session (2-bit): S0 (00), S1 (01), S2 (10), S3 (11)<br>
     * - Target (1-bit): A (0), B (1)<br>
     * - Q (4-bit): 0-15; the number of slots in the round.<br>
     * Example) DR=8, M=1, TRext=Use pilot tone, Sel=All, Session=S0,
     * Target=A, Q=4, No change to Q
     */
    public Response getTypeCAIQueryParameters() {
        int length = transceive(MessageType.Command, MessageCode.Get_Type_C_AI_Query_Related_Parameters, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Type_C_AI_Query_Related_Parameters) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set 18000-6C air interface protocol command ‘Query’
     * parameters.
     * 函数描述: 设置18000-6C空中接口协议命令'查询'参数。
     *
     * @param arguments - DR (1-bit): DR=8 (0), DR=64/3 (1)<br>
     *                  - M (2-bit): M=1 (00), M=2 (01), M=4 (10), M=8 (11)<br>
     *                  - TRext (1-bit): No pilot tone (0), Use pilot tone (1)<br>
     *                  - Sel (2-bit): All (00 or 01), ~SL (10), SL (11)<br>
     *                  - Session (2-bit): S0 (00), S1 (01), S2 (10), S3 (11)<br>
     *                  - Target (1-bit): A (0), B (1)<br>
     *                  - Q (4-bit): 0-15; the number of slots in the round.<br>
     *                  Example) DR=8, M=1, TRext=Use pilot tone, Sel=All, Session=S0,
     *                  Target=A, Q=4, No change to Q<br>
     * @return Success (0x00)
     */
    public Response setTypeCAIQueryParameters(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_Type_C_AI_Query_Related_Parameters, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Type_C_AI_Query_Related_Parameters) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get RF channel. This command is valid only for non-FH mode.
     * 函数描述: 获取RF频道。这个指令只适用于non-FH模式
     *
     * @return - CN (8-bit): Channel Number. The range of channel number depends
     * on regional settings<br>
     * - CNO (8-bit): Channel number offset for miller subcarrier.<br>
     * Example) Channel Number = 10<br>
     */
    public Response getCurrentRFChannel() {
        int length = transceive(MessageType.Command, MessageCode.Get_current_RF_Channel, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_current_RF_Channel) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set RF channel. This command is valid only for non-FHSS mode.
     * 函数描述: 设置RF频道。这个指令只适用于non-FHSS模式
     *
     * @param arguments <br>
     *                  - CN (8-bit): Channel number. The range of channel number
     *                  depends on regional settings<br>
     *                  - CNO (8-bit): Channel number offset for miller subcarrier.
     *                  <br>
     *                  Example) Channel Number = 10, Channel Number Offset = 0<br>
     * @return Success (0x00)
     */
    public Response setCurrentRFChannel(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_current_RF_Channel, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_current_RF_Channel) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get FH and LBT control
     * 函数描述: 获取FH and LBT 参数
     *
     * @return - RT (16-bit): Read Time (1 = 1ms)<br>
     * - IT (16-bit): Idle Time (1 = 1ms)<br>
     * - CST (16-bit): Carrier Sense Time (1 = 1ms)<br>
     * - RFL (16-bit): Target RF power level (-dBm x 10)<br>
     * - FH (8-bit): enable (0x01 or over) / disable (0x00)<br>
     * - LBT (8-bit): enable (0x01 or over) / disable (0x00)<br>
     * - CW (8-bit): enable (0x01) / disable (0x00)<br>
     * Example) Success, FH disable, LBT enable, RT 400ms, IT 100ms, CST
     * 10ms, RFL -630 (-63.0 dBm)
     */
    public Response getFHAndLBTParameters() {
        int length = transceive(MessageType.Command, MessageCode.Get_FH_and_LBT_Parameters, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_FH_and_LBT_Parameters) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set FH and LBT Parameters
     * 函数描述: 获取FH and LBT 参数
     *
     * @param arguments - RT (16-bit): Read Time (1 = 1ms)<br>
     *                  - IT (16-bit): Idle Time (1 = 1ms)<br>
     *                  - CST (16-bit): Carrier Sense Time (1 = 1ms)<br>
     *                  - RFL (16-bit): Target RF power level (-dBm x 10)<br>
     *                  - FH (8-bit): enable (0x01 or over) / disable (0x00)<br>
     *                  - LBT (8-bit): enable (0x01 or over) / disable (0x00)<br>
     *                  - CW (8-bit): enable (0x01) / disable (0x00)<br>
     * @return Success (0x00)
     */
    public Response setFHAndLBTParameters(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_FH_and_LBT_Parameters, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_FH_and_LBT_Parameters) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get current Tx power level.
     * 函数描述: 获取天线功率
     *
     * @return PWR (16-bit): Tx Power. Example) PWR = 200 (20.0 dBm)
     */
    public Response getTxPowerLevel() {
        int length = transceive(MessageType.Command, MessageCode.Get_Tx_Power_Level, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Tx_Power_Level) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                Log.d("jokey", "buffer = " + DataUtils.toHexString(buffer));
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get current Tx power level.
     * 函数描述: 获取天线功率
     *
     * @return PWR (16-bit): Tx Power. Example) PWR = 200 (20.0 dBm)
     */
    public Response getTxPowerLevel2() {
        byte code;
        int length;
        if ((android.os.Build.MODEL).equals("A370")) {
            code = MessageCode.Get_Tx_Power_Level_370;
            length = transceive(MessageType.Command, code, null);
        } else {
            code = MessageCode.Get_Tx_Power_Level_640;
            length = transceive(MessageType.Command, code, new byte[]{0x00, 0x03});
        }

        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != code) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                if ((android.os.Build.MODEL).equals("A370")) {
                    short payloadLength = DataUtils.getShort(buffer[5], buffer[6]);
                    byte[] data = new byte[2];
                    System.arraycopy(buffer, 5, data, 0, data.length);
                    response.result = Response.RESPONSE_PACKET;
                    response.data = data;
                } else {
                    short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                    byte[] data = new byte[payloadLength];
                    System.arraycopy(buffer, 5, data, 0, data.length);
                    response.result = Response.RESPONSE_PACKET;
                    response.data = data;
                }
            }
        }
        return response;
    }

    /**
     * Function Description: Set current Tx power level.
     * 函数描述: 设置天线功率
     *
     * @param arguments - PWR (16-bit): Tx Power<br>
     *                  Example) PWR = 200 (20.0 dBm)<br>
     * @return Success (0x00)
     */
    public Response setTxPowerLevel(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_Tx_Power_Level, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Tx_Power_Level) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Turn the Continuous Wave (CW) signal on/off. This command packet is only valid for idle mode.
     * 函数描述:
     *
     * @param argument - On (0xFF)<br>
     *                 - Off (0x00)<br>
     * @return Success (0x00)
     */
    public Response RF_CW_SignalControl(int argument) {
        int length = transceive(MessageType.Command, MessageCode.RF_CW_signal_control, new byte[]{(byte) argument});
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.RF_CW_signal_control) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Read a EPC Block (PC + EPC)
     * 函数描述: 读取EPC块
     *
     * @return - EPC Block (PC + EPC)<br>
     * Example) PC = 0x3000, EPC = 0xE2003411B802011383258566
     */
    public Response readTypeCUII() {
        int length = transceive(MessageType.Command, MessageCode.Read_Type_C_UII, null);
        Log.i("whw", "readTypeCUII=" + DataUtils.toHexString(buffer));
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Read_Type_C_UII) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    public interface AutoRead {
        void start();

        void processing(byte[] data);

        void end();

        void timeout();
    }

    private LooperBuffer looperBuffer = new LoopBufferHX();

    /**
     * 盘点，搜索标签
     * Search Labels
     */
    public void startAutoRead(int commandCode, byte[] rc, final AutoRead autoRead) {

        Packet p = new Packet(MessageType.Command, MessageCode.Start_Auto_Read, Bytes.concat(new byte[][]{new byte[]{(byte) commandCode}, rc}));
        SerialPortManager.getInstance().setLoopBuffer(looperBuffer);
        SerialPortManager.getInstance().write(topackage(p.getPacket()));
        DataUtils.toHexString(topackage(p.getPacket()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStop = false;
                while (!isStop && autoRead != null) {
                    byte[] dataPacket = looperBuffer.getFullPacket();
                    if (dataPacket != null) {
                        int type = PacketType.getPacketType(dataPacket);
                        Log.i("whw", "type=" + type);
                        switch (type) {
                            case PacketType.Auto_Read_Start:
                                autoRead.start();
                                break;
                            case PacketType.Auto_Read_Processing:
                                autoRead.processing(getAutoReadData(dataPacket));
                                break;
                            case PacketType.Auto_Read_End:
                                SerialPortManager.getInstance().setLoopBuffer(null);
                                autoRead.end();
                                isStop = true;
                                Log.i("whw", "Auto_Read_End=");
                                break;
                            default:
                                break;
                        }
                    }
                    //SystemClock.sleep(30);
                }
            }

        }).start();
    }

    /**
     * 盘点标签，读到标签就停，并且返回需要读到的数据
     * Search Labels
     *
     * @param times 多少秒无数据停止
     * @param code  需要读的区域
     * @param pwd   标签访问密码
     * @param sa    偏移长度
     * @param dl    要读的长度
     */
    public void startAutoRead2C(final int times, final byte code, final String pwd, final short sa, final short dl,
                                final SearchAndRead Interface) {
        if (isStart == 0) {
            final long startTime = System.currentTimeMillis();
            isStart = 1;
            flag = true;
            Packet p = new Packet(MessageType.Command, MessageCode.Start_Auto_Read,
                    Bytes.concat(new byte[][]{{(byte) 0x22}, new byte[]{0x00, 0x01}}));
            SerialPortManager.getInstance().setLoopBuffer(looperBuffer);
            SerialPortManager.getInstance().write(topackage(p.getPacket()));

            Executor.execute(new Runnable() {

                @Override
                public void run() {
                    while (flag && (System.currentTimeMillis() - startTime) < times) {
                        byte[] dataPacket = looperBuffer.getFullPacket();
                        Log.d("jokey", "Satrting...");
                        if (dataPacket != null) {
                            int type = PacketType.getPacketType(dataPacket);
                            switch (type) {
                                case PacketType.Auto_Read_Start:
                                    Log.i("jokey", "Auto_Read_Start");
                                    break;
                                case PacketType.Auto_Read_Processing:
                                    Log.i("jokey", "Auto_Read_Processing");
                                    byte[] old = getAutoReadData(dataPacket);
                                    byte[] newdata = new byte[old.length - 2];
                                    System.arraycopy(old, 2, newdata, 0, newdata.length);
                                    String epc = DataUtils.toHexString(newdata);
                                    short epcLength = (short) (epc.length() / 2);
                                    byte mb = (byte) code;
                                    mb++;
                                    Response response = readTypeCTagData(arguments(pwd, epcLength, epc, mb, sa, dl));
                                    if (response.result == Response.RESPONSE_PACKET && response.data != null) {
                                        flag = false;
                                        isStart = 0;
                                        Interface.returnData(response.data);
                                    }
                                    break;
                                case PacketType.Auto_Read_End:
                                    Log.i("jokey", "Auto_Read_End");
                                    Packet p = new Packet(MessageType.Command, MessageCode.Start_Auto_Read,
                                            Bytes.concat(new byte[][]{{(byte) 0x22}, new byte[]{0x00, 0x01}}));
                                    SerialPortManager.getInstance().setLoopBuffer(looperBuffer);
                                    SerialPortManager.getInstance().write(topackage(p.getPacket()));
                                    break;
                                default:
                                    break;
                            }

                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (flag) {
                        flag = false;
                        isStart = 0;
                        Interface.readFail();
                    }
                }
            });
        } else {

        }
    }

    public Response getResponseData(Response response) {
        short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
        byte[] data = new byte[payloadLength];
        System.arraycopy(buffer, 5, data, 0, data.length);
        response.result = Response.RESPONSE_PACKET;
        response.data = data;
        return response;
    }

    public byte[] getAutoReadData(byte[] dataPacket) {
        short payloadLength = DataUtils.getShort(dataPacket[3], dataPacket[4]);
        byte[] data = new byte[payloadLength];
        System.arraycopy(dataPacket, 5, data, 0, data.length);
        return data;
    }

    /**
     * Function Description: Stop an automatic tag read operation.
     * 函数描述：停止自动搜索
     *
     * @return Success (0x00)
     */
    public Response stopAutoRead() {
        int length = transceive(MessageType.Command, MessageCode.Stop_Auto_Read, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Stop_Auto_Read) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Read Labels
     * 读取标签
     *
     * @param arguments 发送的指令
     * @return 读到的数据
     */
    public Response readTypeCTagData(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Read_Type_C_Tag_Data, arguments);
        Log.i("whw", "readTypeCTagData length=" + length);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Read_Type_C_Tag_Data) {

                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description:Get current frequency hopping table.
     * 函数描述：获取当前频率表
     *
     * @return - Table Size (8-bit)<br>
     * - Channel Number (variable)<br>
     * Example) Table Size = 6, channel numbers = 47, 19, 20, 23, 46, 16
     */
    public Response getFrequencyHoppingTable() {
        int length = transceive(MessageType.Command, MessageCode.Get_Frequency_Hopping_Table, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Frequency_Hopping_Table) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set current frequency hopping table.
     * 函数描述：设置当前频率跳频表
     *
     * @param arguments - Table Size (8-bit)<br>
     *                  - Channel Numbers (variable)<br>
     * @return Success (0x00)
     */
    public Response setFrequencyHoppingTable(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_Frequency_Hopping_Table, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Frequency_Hopping_Table) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get current modulation mode. The modulation mode is
     * combination Rx modulation type and BLF
     * 函数描述: 获取调制模式
     *
     * @return - BLF (16-bit): backscatter link frequency<br>
     * - RxMod (8-bit): data rate and modulation format<br>
     * - DR (8-bit): divide ratio<br>
     * Example) BLF = 160KHz, RxMod = M8, DR = 64/3
     */
    public Response getModulationMode() {

        int length = transceive(MessageType.Command, MessageCode.Get_Modulation, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Modulation) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set current modulation mode. The modulation mode is
     * combination Rx modulation type and BLF
     * 函数描述: 设置调制模式
     *
     * @param arguments - Modulation Mode (8-bit): High Sensitivity (0x00), High Speed
     *                  (0x01), Manual (0xFF)<br>
     *                  - BLF (16-bit), RxMod (8-bit), DR (8-bit): only available when
     *                  modulation mode is manual.<br>
     *                  Example) Normal mode(0x00)<br>
     *                  Example) Manual, BLF = 160KHz, RxMod = M8, DR = 64/3<br>
     * @return Success (0x00)
     */
    public Response setModulationMode(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Set_Modulation, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Modulation) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get Anti-collision algorithm. (TBD)
     * 函数描述: 获取防碰撞算法
     *
     * @return - Anti-collision Mode (8-bit)<br>
     * Example)(0x00)
     */
    public Response getAntiCollisionMode() {
        int length = transceive(MessageType.Command, MessageCode.Get_Anti_Collision_Mode, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Anti_Collision_Mode) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Set Anti-collision algorithm. (TBD)
     * 函数描述: 设置防碰撞算法
     *
     * @param argument - Anti-collision Mode (8-bit) Example)(0x00)
     * @return Success (0x00).
     */
    public Response setAntiCollisionMode(int argument) {
        int length = transceive(MessageType.Command, MessageCode.Set_Anti_Collision_Mode,
                new byte[]{(byte) argument});
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Set_Anti_Collision_Mode) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Start an automatic tag read operation, tag IDs are
     * send back to user through notification packet.
     * 函数描述: 自己盘点标签
     *
     * @return Response
     */
    public Response startAutoRead2() {
        int length = transceive(MessageType.Command, MessageCode.Start_Auto_Read2, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Start_Auto_Read2) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Stop an automatic read2 operation.
     * 函数描述: 停止盘点标签
     *
     * @return Success (0x00)
     */
    public Response stopAutoRead2() {
        int length = transceive(MessageType.Command, MessageCode.Stop_Auto_Read2, null);
        Log.i("whw", "stopAutoRead2=" + DataUtils.toHexString(buffer));
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Stop_Auto_Read2) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * 给标签写数据
     * Write data to Labels
     *
     * @param arguments 发送的指令
     * @return
     */
    public Response writeTypeCTagData(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Write_Type_C_Tag_Data, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Write_Type_C_Tag_Data) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Blockwrite type C tag data.
     * 函数描述: 写入
     *
     * @param arguments - AP (32-bit): Access Password if target memory bank was
     *                  password protected. Otherwise, set AP filed to 0x00000000. -
     *                  UL (16-bit): Target tag’s EPC length - EPC (variable): Target
     *                  tag’s EPC - MB (8-bit): Target memory bank; 0x00 Reserved,
     *                  0x01 EPC, 0x02 TID, 0x03 User - SA (16-bit): Starting Address
     *                  word pointer - DL (16-bit): Data Length to write (Word Count)
     *                  - DT (variable): Data to write Example) Access Password =
     *                  0x00000000, UL = 12 (0x0C), EPC = 0xE2003411B802011526370494,
     *                  Target memory bank = RFU, Start Address = 0x0000, Data Length
     *                  = 4 word, Data to write = 0x1234567800000000
     * @return Success (0x00)
     */
    public Response blockWriteTypeCTagData(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.BlockWrite_Type_C_Tag_Data, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.BlockWrite_Type_C_Tag_Data) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Block erases type C tag data.
     * 函数描述：擦除
     *
     * @param arguments - AP (32-bit): Access Password if target memory bank was
     *                  password protected. Otherwise, set AP filed to 0x00000000. -
     *                  UL (16-bit): Target tag’s EPC length - EPC (variable): Target
     *                  tag’s EPC - MB (8-bit): Target memory bank; 0x00 RFU, 0x01
     *                  EPC, 0x02 TID, 0x03 User - SA (16-bit): Starting Address word
     *                  pointer - DL (16-bit): Data Length (Word Count) Example)
     *                  Access Password = 0x00000000, UL = 12 (0x0C) byte, EPC =
     *                  0xE2003411B802011526370494, Target memory bank = RFU, Start
     *                  Address = 0x0000, Length = 4 word
     * @return Success (0x00).
     */
    public Response blockEraseTypeCTagData(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.BlockErase_Type_C_Tag_Data, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.BlockErase_Type_C_Tag_Data) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: BlockPermalock type C tag.
     * 函数描述: 锁住指定标签
     *
     * @param arguments - AP (32-bit): Access Password if target memory bank was
     *                  password protected. Otherwise, set AP filed to 0x00000000. -
     *                  UL (16-bit): Target tag's EPC length - EPC (variable): Target
     *                  tag's EPC - RFU (8-bit): 0x00 - R/L (8-bit): Read/Lock bit;
     *                  0x00 Read, 0x01 Permalock - MB (8-bit): Target memory bank;
     *                  0x00 Reserved, 0x01 EPC, 0x02 TID, 0x03 User - BP (16-bit):
     *                  Mask starting address, specified in units of 16 blocks - BR
     *                  (8-bit): Mask range, specified in units of 16 blocks - Mask
     *                  (variable): Mask value Example) Access Password = 0x11111111,
     *                  UL = 12 (0x0C), EPC = 0xE2003411B802011526370494, RFU = 0x00,
     *                  Read/Lock bit = Lock (0x01), Target memory bank = User memory
     *                  (0x03), Block Pointer = 0x0000, Block Range = 1, Mask value =
     *                  0xFFFF
     * @return Success (0x00).
     */
    public Response blockPermalockTypeCTag(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.BlockPermalock_Type_C_Tag, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.BlockPermalock_Type_C_Tag) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Kill a Tag.
     * 函数描述：杀死指定标签
     *
     * @param arguments - KP (32-bit): Kill Password. If KP filed set to 0x00000000,
     *                  ‘Kill Type C Tag’ command do not work. The target tag ignores
     *                  it. - UL (16-bit): Target tag’s EPC length - EPC (variable):
     *                  Target tag’s EPC - Recom (8-bit): Recommissioning bits
     *                  Example) Kill Password =0x87654321, UL = 12 (0x0C) byte, EPC =
     *                  0xE2003411B802011526370494, Recom = 0x00
     * @return Success (0x00);
     */
    public Response killTypeCTag(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Kill_Recom_Type_C_Tag, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Kill_Recom_Type_C_Tag) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Lock an indicated memory bank in the tag.
     * 函数描述: 锁定标签中指定的存储库。
     *
     * @param arguments - AP (32-bit): Access Password if memory bank was password
     *                  protected. Otherwise, set AP filed to 0x00000000. - UL
     *                  (16-bit): Target tag’s EPC length - EPC (variable): Target
     *                  tag’s EPC - LD (24-bit): Lock mask and action flags. Pad 4-bit
     *                  zeros (dummy) to the left of 20-bit lock mask and associated
     *                  action flags. Example) Access Password = 0x00000000, UL =
     *                  12(0x0C) byte, EPC = 0xE2003411B802011526370494, Lock mask and
     *                  action flags = 0x080200 {Binary: 0000 (dummy) + 1000000000
     *                  (mask) + 1000000000 (lock data)}
     * @return Success (0x00).
     */
    public Response lockTypeCTag(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Lock_Type_C_Tag, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Lock_Type_C_Tag) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get current temperature
     * 函数描述：获取设备温度
     *
     * @return - Temp (8-bit): Current temperature Example) 24 °C
     */
    public Response getTemperature() {
        int length = transceive(MessageType.Command, MessageCode.Get_Temperature, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Temperature) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Get RSSI level
     * 函数描述: 获取RSSI强度
     *
     * @return - RSSI (16-bit): RSSI level (-dBm x 10, decimal value) Example)
     * RSSI = 900 (-90.0 dBm)
     */
    public Response getRSSI() {
        int length = transceive(MessageType.Command, MessageCode.Get_RSSI, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_RSSI) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Scan RSSI level on all channels.
     * 函数描述: 扫描所有通道的RSSI级别
     *
     * @return - CHS (8-bit): Start channel number - CHE (8-bit): Stop channel
     * number - CHB (8-bit): Best channel (lowest RSSI channel) - RSSI1
     * (8-bit): RSSI level on CHS (-dBm) - RSSI2 (8-bit): RSSI level on
     * CHS + 1 (-dBm) …. - RSSI[N] (8-bit): RSSI level on CHE (-dBm)
     * Example) CHS = 7, CHE = 20, CHB = 7, RSSI0 = 90 (-90dBm), RSSI1 =
     * 87 (-87), …
     */
    public Response scanRSSI() {
        int length = transceive(MessageType.Command, MessageCode.Scan_RSSI, null);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Scan_RSSI) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Sets Registry Update function. - Arg (8-bit): Store
     * (0x01) Example) Store data into Registry
     * 函数描述: 更新寄存器
     *
     * @return Success (0x00).
     */
    public Response updateRegistry(int argument) {
        int length = transceive(MessageType.Command, MessageCode.Update_Registry, new byte[]{(byte) argument});
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Update_Registry) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description:Sets Registry Erase function.
     * 函数描述: 擦除寄存器
     *
     * @param argument - Arg (8-bit): Erase (0xFF)
     * @return Success (0x00).
     */
    public Response eraseRegistry(int argument) {
        int length = transceive(MessageType.Command, MessageCode.Erase_Registry, new byte[]{(byte) argument});
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Erase_Registry) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    /**
     * Function Description: Gets Registry items.
     * 函数描述：获取寄存器信息
     *
     * @param arguments - Registry Version (0x0000) - Firmware Date (0x0001) - Band
     *                  (0x0002) - Tx power (0x0003) - FH/LBT (0x0004) -
     *                  Anti-collision Mode (0x0005) - Modulation Mode (0x0006) -
     *                  Query(Q) (0x0007) - Frequency Hopping Table (0x0008) - Tx
     *                  Power Table (0x0009)
     * @return - Active (8-bit): Registry items status; Inactive (0x00),
     * Read-Only (0xBC), Active (0xA5)- Data (Variable) Example)
     * Registry Version = 1
     */
    public Response getRegistryItem(byte[] arguments) {
        int length = transceive(MessageType.Command, MessageCode.Get_Registry_Item, arguments);
        Response response = new Response();
        if (length == 0) {
            response.result = Response.TIME_OUT;
        } else {
            if (buffer[2] != MessageCode.Get_Registry_Item) {
                response.result = Response.COMMAND_FAILURE;
            } else {
                short payloadLength = DataUtils.getShort(buffer[3], buffer[4]);
                byte[] data = new byte[payloadLength];
                System.arraycopy(buffer, 5, data, 0, data.length);
                response.result = Response.RESPONSE_PACKET;
                response.data = data;
            }
        }
        return response;
    }

    private static byte[] getCRC(byte[] data) {
        int m_crc = 0xFFFF;
        m_crc = crc_sum(data, m_crc);
        byte[] crc = new byte[2];
        crc[0] = (byte) ((m_crc >>> 8) & 0xFF);
        crc[1] = (byte) (m_crc & 0x00FF);
        Log.i("whw", "getCRC=" + DataUtils.toHexString(crc));
        return crc;

    }

    private static int crc_sum(byte[] msg, int crc) {
        for (int i = 0; i < msg.length; i++)
            crc = crc_polynomial(msg[i], crc);
        return crc;
    }

    private static int crc_polynomial(byte b, int acc) {
        int bitA;
        int bitH = (acc >>> 8) & 0xFF;
        int bitL = (acc & 0xFF) << 8;
        bitH ^= (b & 0xFF);
        bitH ^= ((bitH >>> 4) & 0x0F);
        bitA = (bitH | bitL);
        bitA ^= (((bitH << 8) << 4) & 0xF000);
        bitA ^= ((bitH << 5) & 0x1FE0);
        acc = bitA;
        return acc;
    }

    private static boolean verifyCRC(byte[] packet) {
        byte[] inCRC = new byte[2];
        System.arraycopy(packet, packet.length - inCRC.length, inCRC, 0, inCRC.length);
        byte[] command = new byte[packet.length - 3];
        System.arraycopy(packet, 1, command, 0, command.length);
        byte[] outCRC = getCRC(command);
        Log.i("whw", "@@@@@@@@@@@@@CRC=" + DataUtils.toHexString(outCRC));
        if (outCRC[0] == inCRC[0] && outCRC[1] == inCRC[1]) {
            return true;
        }
        return false;
    }

    public static class LoopBufferHX implements LooperBuffer {
        private int packetStartIndex = 0;
        private int dataLength = 0;

        private byte[] mBuffer = new byte[1024 * 50];

        public synchronized void add(byte[] buf) {
            System.arraycopy(buf, 0, mBuffer, dataLength, buf.length);
            dataLength += buf.length;
        }

        public synchronized byte[] getFullPacket() {
            byte[] tempBuf = null;
            // byte[] temp = new byte[9];
            // System.arraycopy(mBuffer, 0, temp, 0, temp.length);
            // Log.i("whw", "getFullPacket="+DataUtils.toHexString(temp));
            if (packetStartIndex < dataLength) {
                if (mBuffer[packetStartIndex] == (byte) 0xBB
                        && (mBuffer[packetStartIndex + 1] == MessageType.Response
                        || mBuffer[packetStartIndex + 1] == MessageType.Notification)) {
                    short payloadLength = DataUtils.getShort(mBuffer[packetStartIndex + 3],
                            mBuffer[packetStartIndex + 4]);
                    // Log.i("whw", "**************=");
                    if (payloadLength >= 0 && mBuffer[packetStartIndex + 4 + payloadLength + 1] == 0x7E
                            && (packetStartIndex + 4 + payloadLength + 3) < dataLength) {
                        Log.i("whw", "!!!!!!!!!!!!!!!!!!!!!!!");
                        tempBuf = new byte[5 + payloadLength + 3];
                        System.arraycopy(mBuffer, packetStartIndex, tempBuf, 0, tempBuf.length);
                        Log.i("whw", "tempBuf=" + DataUtils.toHexString(tempBuf));
                        if (!verifyCRC(tempBuf)) {
                            packetStartIndex++;
                            return null;
                        }
                        byte[] lastBuffer = new byte[dataLength - tempBuf.length];
                        System.arraycopy(mBuffer, tempBuf.length, lastBuffer, 0, lastBuffer.length);
                        // Log.i("whw",
                        // "lastBuffer="+DataUtils.toHexString(lastBuffer));
                        System.arraycopy(lastBuffer, 0, mBuffer, 0, lastBuffer.length);
                        packetStartIndex = 0;
                        dataLength -= tempBuf.length;
                    } else {
                        packetStartIndex++;
                        Log.i("whw",
                                "&&&&&&&&&&&&&&packetStartIndex=" + packetStartIndex + "  dataLength=" + dataLength);
                    }
                } else {
                    packetStartIndex++;
                    Log.i("whw", "%%%%%%%%%%%packetStartIndex=" + packetStartIndex + "  dataLength=" + dataLength);
                }
            } else {
                // Log.i("whw",
                // "@@@@@@@@@@@@@@@@@packetStartIndex="+packetStartIndex+"
                // dataLength="+dataLength);
                packetStartIndex = 0;
                dataLength = 0;
            }

            return tempBuf;
        }
    }

    private static class PacketType {
        public static final int Auto_Read_Start = 1;
        public static final int Auto_Read_Processing = 2;
        public static final int Auto_Read_End = 3;

        public static int getPacketType(byte[] data) {
            if (data[1] == MessageType.Response && data[2] == MessageCode.Start_Auto_Read && data[5] == 0x00) {
                Log.i("whw", "Auto_Read_Start");
                return Auto_Read_Start;
            } else if (data[1] == MessageType.Notification && data[2] == MessageCode.Read_Type_C_UII) {
                Log.i("whw", "Auto_Read_Processing");
                return Auto_Read_Processing;
            } else if (data[1] == MessageType.Notification && data[2] == MessageCode.Start_Auto_Read
                    && data[5] == 0x1F) {
                Log.i("whw", "Auto_Read_End");
                return Auto_Read_End;
            }
            Log.i("whw", "Auto_Read_else -1");
            return -1;
        }
    }

    private byte[] topackage(byte[] old) {
        int len = old.length;
        byte[] heade = {(byte) 0xCA, (byte) 0xDF, (byte) 0x05, (byte) 0x35};
        byte[] cmd = new byte[heade.length + 2 + len + 1];
        System.arraycopy(heade, 0, cmd, 0, heade.length);
        cmd[heade.length] = (byte) 0;
        cmd[heade.length + 1] = (byte) len;
        System.arraycopy(old, 0, cmd, heade.length + 2, len);
        cmd[cmd.length - 1] = (byte) 0xE3;
        return cmd;
    }

    public interface SearchAndRead {
        void timeout();

        void returnData(byte[] data);

        void readFail();
    }

    private byte[] arguments(String pwd, short epcLength, String epc, byte mb, int sa, int dl) {
        return Bytes.concat(new byte[][]{DataUtils.hexStringTobyte(pwd), DataUtils.short2byte(epcLength),
                DataUtils.hexStringTobyte(epc), new byte[]{mb}, DataUtils.short2byte((short) sa),
                DataUtils.short2byte((short) dl)});
    }


}