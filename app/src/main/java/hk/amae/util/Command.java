package hk.amae.util;

import android.widget.Spinner;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import hk.amae.sampler.HardwareAct;
import hk.amae.util.Comm.Channel;

/**
 * Created by kinka on 4/11/15.
 */
public class Command {
    byte Version = 1;

    // http://introcs.cs.princeton.edu/java/51data/CRC16CCITT.java.html
    private short crc16_ccitt(byte[] bytes, int end) {
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        for (int k=0; k<end; k++) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((bytes[k]   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;

        return (short) crc;
    }
    private String getString(ByteBuffer reply) {
        int len = reply.get();
        byte[] data = new byte[len];
        reply.get(data);
        return new String(data);
    }
    private ByteBuffer build(int cmd, byte[] data) {
        if (data == null) data = new byte[0];

        ByteBuffer buf = ByteBuffer.allocate(3 + 1 + 2 + 2 + data.length + 2);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put((byte) 0xaa);
        buf.put((byte) 0xaf);
        buf.put((byte) 0xfa);
        buf.put(Version);
        buf.putShort((short) cmd);
        buf.putShort((short) data.length);
        buf.put(data);

        short sum = crc16_ccitt(buf.array(), buf.capacity() - 2); // crc16-CCITT
        buf.putShort(sum);

        ByteBuffer reply = Deliver.send(buf);
        reply.order(ByteOrder.BIG_ENDIAN);

        reply.getShort();
        reply.get(); // == 0xfa?
        reply.get(); // version
        reply.getShort(); // cmd

        int len = reply.getShort();
        switch (cmd) {
            case 0x1:
                resolveModel(reply);
                break;
            case 0x2:
                resolveATM_TEMP(reply);
                break;
            case 0x3:
            case 0x104:
                resolveDateTime(reply);
                break;
            case 0x4:
                resolveBattery(reply);
                break;
            case 0x5:
                resolveChannelState(reply);
                break;
            case 0x6:
                resolveMachineState(reply);
                break;
            case 0x7:
                resolveSampleState(reply);
                break;
            case 0x8:
                resolveSampleHistory(reply);
                break;
            case 0x9:
                resolveSampleData(reply);
                break;
            case 0xa:
                resolveSysInfo(reply);
                break;
            case 0xb:
                resolveAutoSetting(reply);
                break;
            case 0x101:
                resolveManualChannel(reply);
                break;
            case 0x102:
                resolveAutoChannel(reply);
                break;
            case 0x103:
                resolveBacklit(reply);
                break;
            case 0x105:
                resolveRestore(reply);
                break;
            case 0x106:
                resolveClearSample(reply);
                break;
            case 0x107:
                resolveAdjust(reply);
                break;
        }

        // 校验sum
        sum = reply.getShort(); // sum
        short crc = crc16_ccitt(reply.array(), reply.arrayOffset());
        System.out.println("checksum " + (crc == sum));

        return buf;
    }

    public String Model = ""; // 型号
    public String SN = ""; // 序列号
    public ByteBuffer reqModel() { // 查询型号
/*        String model = "EM-2008";
        String sn = "021547";
        ByteBuffer buffer = ByteBuffer.allocate(1 + model.length() + 1 + sn.length());
        buffer.put((byte) model.length());
        buffer.put(model.getBytes());
        buffer.put((byte) sn.length());
        buffer.put(sn.getBytes());
        return build(Version, 0x1, buffer.array());*/
        return build(0x1, null);
    }
    public void resolveModel(ByteBuffer reply) {
        int len;
        byte[] data;
        reply.getShort(); // data len

        Model = getString(reply);
        SN = getString(reply);
    }

    public int ATM, TEMP;
    public ByteBuffer reqATM_TEMP() { // 查询大气压和温度
        return build(0x2, null);
    }
    public void resolveATM_TEMP(ByteBuffer reply) {

        ATM = reply.getInt();
        TEMP = reply.getShort();
    }

    public String DateTime;
    public ByteBuffer reqDateTime() { // 查询日期和时间
        return build(0x3, null);
    }
    public void resolveDateTime(ByteBuffer reply) {

        DateTime = getString(reply);
    }

    public boolean Charging = false;
    public int Power = 0;
    public ByteBuffer reqBattery() { // 查询充电状态和电量
        return build(0x4, null);
    }
    public void resolveBattery(ByteBuffer reply) {
        Charging = (reply.get() == 1);
        Power = reply.get();
    }

    public int ChannelState;
    public Channel Channel;
    public int Speed;
    public int Volume;
    public ByteBuffer reqChannelState(Channel channel) { // 查询实时流量和已采样
        System.out.println((byte) channel.getValue());
        return build(0x5, new byte[] {(byte) channel.getValue()});
    }
    public void resolveChannelState(ByteBuffer reply) {
        ChannelState = reply.get();
        Channel = Comm.Channel.init(reply.get());
        Speed = reply.getInt();
        Volume = reply.getInt();
    }

    public byte[] MachineState = new byte[8];
    public ByteBuffer reqMachineState() { // 查询机器工作状态
        return build(0x6, null);
    }
    public void resolveMachineState(ByteBuffer reply) {
        for (int i=0; i<MachineState.length; i++)
            MachineState[i] = reply.get();
    }

    public String SampleID; // 采样编号
    public int TargetSpeed; // 设定流量/流速
    public int SampleMode; // 采样模式(定时or定容)
    public int SampleVolume; // 累计标体(临时命名)
    public byte Progress; // 采样进度
    public int Elapse; // 已采样时间
    public int TargetDuration; // 设定时间
    public boolean Manual; // 手动/定时
    public ByteBuffer reqSampleState() { // 查询当前采样情况
        return build(0x7, null);
    }
    public void resolveSampleState(ByteBuffer reply) {
        int len = reply.get();
        byte[] data = new byte[len];
        reply.get(data);
        SampleID = new String(data);
        Speed = reply.getInt();
        TargetSpeed = reply.getInt();
        Volume = reply.getShort();
        SampleVolume = reply.getShort();
        SampleMode = reply.get();
        DateTime = getString(reply);
        ATM = reply.getInt();
        TEMP = reply.getShort();
        Progress = reply.get();
        Elapse = reply.getInt();
        TargetDuration = reply.getShort();
        Channel = Comm.Channel.init(reply.get());
        Manual = reply.get() == 0;
    }

    public String[] History;
    public ByteBuffer reqSampleHistory(int start, int end) { // 查询历史采样数据编号
        ByteBuffer buffer = ByteBuffer.allocate(2 + 2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) start);
        buffer.putShort((short) end);
        return build(0x8, null);
    }
    public void resolveSampleHistory(ByteBuffer reply) {
        int len = reply.getShort();
        History = new String[len];
        for (int i=0; i<History.length; i++) {
            History[i] = getString(reply);
        }
    }

    public ByteBuffer reqSampleData(String item) { // 查询编号对应数据
        ByteBuffer buffer = ByteBuffer.allocate(1 + item.length());
        buffer.put((byte) item.length());
        buffer.put(item.getBytes());
        return build(0x9, buffer.array());
    }
    public void resolveSampleData(ByteBuffer reply) {
        resolveSampleState(reply);
    }

    public String SoftwareVer; // 软件
    public String HardwareVer; // 硬件版本
    public String ChannelCount; // 8通道
    public String ChannelCap; // 通道容量
    public String Storage; // 存储容量
    public ByteBuffer reqSysInfo() { // 查询系统信息
        return build(0xa, null);
    }
    public void resolveSysInfo(ByteBuffer reply) {
        Model = getString(reply);
        SoftwareVer = getString(reply);
        ChannelCount = getString(reply);
        ChannelCap = getString(reply);
        Storage = getString(reply);
        HardwareVer = getString(reply);
    }

    public int SettingNum; // 第几条设置
    public int TargetVolume;
    public ByteBuffer reqAutoSetting(int mode, Channel channel, int num) { // 查询定时(定容)设置
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1);
        buffer.put((byte) mode);
        buffer.put(channel.getValue());
        buffer.put((byte) num);
        return build(0xb, buffer.array());
    }
    public void resolveAutoSetting(ByteBuffer reply) {

        SampleMode = reply.get();
        Channel = Comm.Channel.init(reply.get());
        SettingNum = reply.get();
        TargetSpeed = reply.getInt();
        TargetVolume = TargetDuration = reply.getInt(); // 根据SampleMode 再去作区分吧
        DateTime = getString(reply);
    }

    // 设置采样参数(手动模式)
    public ByteBuffer setManualChannel(int operation, int mode, Channel channel, int speed, int cap) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + 4 + 4);
        buffer.put((byte) operation);
        buffer.put((byte) mode);
        buffer.put(channel.getValue());
        buffer.putInt(speed);
        buffer.putInt(cap); // 时长或者容量
        return  build(0x101, buffer.array());
    }
    public void resolveManualChannel(ByteBuffer reply) {
        resolveChannelState(reply);
    }

    // 设置采样参数(定时定容)
    public ByteBuffer setAutoChannel(boolean doSet, int mode, Channel channel, int num, int speed, int cap, String launchTime) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + 4 + 4 + 1 + launchTime.length());
        buffer.put((byte) (doSet ? 1 : 2));
        buffer.put((byte) mode);
        buffer.put(channel.getValue());
        buffer.put((byte) num);
        buffer.putInt(speed);
        buffer.putInt(cap);
        buffer.put((byte) launchTime.length());
        buffer.put(launchTime.getBytes());
        return build(0x102, buffer.array());
    }
    public boolean DoSet;
    public void resolveAutoChannel(ByteBuffer reply) {

        DoSet = reply.get() == 1;
        SampleMode = reply.get();
        Channel = Comm.Channel.init(reply.get());
        SettingNum = reply.get();
        Speed = reply.getInt();
        TargetVolume = TargetDuration = reply.getInt();
        DateTime = getString(reply);
    }

    // 设置背光
    public ByteBuffer setBacklit(int normal, int saving) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) normal);
        buffer.put((byte) saving);
        return build(0x103, buffer.array());
    }
    public byte NormalBacklit;
    public byte SavingBacklit;
    public void resolveBacklit(ByteBuffer reply) {
        NormalBacklit = reply.get();
        SavingBacklit = reply.get();
    }

    // 设置时间
    public ByteBuffer setDateTime(String dateTime) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + dateTime.length());
        buffer.put((byte) dateTime.length());
        buffer.put(dateTime.getBytes());
        return build(0x104, buffer.array());
    }
    // see resolveDateTime

    // 恢复出厂设置
    public ByteBuffer setRestore(boolean doSet) {
        return build(0x105, new byte[] {(byte) (doSet ? 1 : 2)});
    }
    public void resolveRestore(ByteBuffer reply) {
        DoSet = reply.get() == 1; // 恢复命令 or 查询恢复进度
        Progress = reply.get();
    }

    // 清空采样数据
    public ByteBuffer setClearSample(boolean doSet) {
        return build(0x106, new byte[] {(byte) (doSet ? 1 : 2)});
    }
    public void resolveClearSample(ByteBuffer reply) {
        DoSet = reply.get() == 1; // 清空命令 or 查询清空进度
        Progress = reply.get();
    }

    // 流量校准
    public ByteBuffer setAdjust(Channel channel, int expectPressure, int targetSpeed) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 2);
        buffer.put(channel.getValue());
        buffer.putShort((short) expectPressure);
        buffer.putShort((short) targetSpeed);
        return build(0x107, buffer.array());
    }
    public int OutputPower; // 动力输出
    public int DutyCycle; // 占空比
    public int PickPower; // 采集压力
    public int PickVoltage; // 电压
    public void resolveAdjust(ByteBuffer reply) {
        Channel = Comm.Channel.init(reply.get());
        OutputPower = reply.getShort();
        DutyCycle = reply.getInt();
        PickPower = reply.getShort();
        PickVoltage = reply.getShort();
    }

    // 锁定界面
    public ByteBuffer setScreenLock() {
        return build(0x108, null);
    }

    // 打印某一条数据
    public ByteBuffer printSample(String sampleId) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + sampleId.length());
        buffer.put((byte) sampleId.length());
        buffer.put(sampleId.getBytes());
        return build(0x109, buffer.array());
    }

    // 定时关机
    public ByteBuffer setShutdown(String dateTime) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + dateTime.length());
        buffer.put((byte) dateTime.length());
        buffer.put(dateTime.getBytes());
        return build(0x10a, buffer.array());
    }

    // 清洗
    public ByteBuffer setClean() {
        return build(0x10b, null);
    }
}
