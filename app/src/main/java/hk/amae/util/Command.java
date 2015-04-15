package hk.amae.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        switch (cmd) {
            case 0x1:
                resolveModel(reply);
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
    public boolean resolveModel(ByteBuffer reply) {
        reply.order(ByteOrder.BIG_ENDIAN);

        reply.getShort();
        reply.get(); // == 0xfa?
        reply.get(); // version
        reply.getShort(); // cmd

        int len;
        byte[] data;
        reply.getShort(); // data len

        len = reply.get();
        data = new byte[len];
        reply.get(data);
        Model = new String(data);

        len = reply.get();
        data = new byte[len];
        reply.get(data);
        SN = new String(data);

        return true;
    }

    public ByteBuffer reqATM_TEMP() { // 查询大气压和温度
        return build(0x2, null);
    }

    public ByteBuffer reqDateTime() { // 查询日期和时间
        return build(0x3, null);
    }

    public ByteBuffer reqBattery() { // 查询充电状态和电量
        return build(0x4, null);
    }

    public ByteBuffer reqChannelState(Comm.Channel channel) { // 查询实时流量和已采样
        System.out.println((byte) channel.getValue());
        return build(0x5, new byte[] {(byte) channel.getValue()});
    }

    public ByteBuffer reqMachineState() { // 查询机器工作状态
        return build(0x6, null);
    }

    public ByteBuffer reqSampleState() { // 查询当前采样情况
        return build(0x7, null);
    }

    public ByteBuffer reqSampleHistory(int start, int end) { // 查询历史采样数据编号
        ByteBuffer buffer = ByteBuffer.allocate(2 + 2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) start);
        buffer.putShort((short) end);
        return build(0x8, null);
    }

    public ByteBuffer reqSampleData(String item) { // 查询编号对应数据
        ByteBuffer buffer = ByteBuffer.allocate(1 + item.length());
        buffer.put((byte) item.length());
        buffer.put(item.getBytes());
        return build(0x9, buffer.array());
    }

    public ByteBuffer reqSysInfo() { // 查询系统信息
        return build(0xa, null);
    }

    public ByteBuffer reqAutoSetting(int mode, Comm.Channel channel, int num) { // 查询定时(定容)设置
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1);
        buffer.put((byte) mode);
        buffer.put(channel.getValue());
        buffer.put((byte) num);
        return build(0xb, buffer.array());
    }

    // 设置采样参数(手动模式)
    public ByteBuffer setManualChannel(int operation, int mode, Comm.Channel channel, int speed, int cap) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + 4 + 4);
        buffer.put((byte) operation);
        buffer.put((byte) mode);
        buffer.put(channel.getValue());
        buffer.putInt(speed);
        buffer.putInt(cap); // 时长或者容量
        return  build(0x101, buffer.array());
    }

    // 设置采样参数(定时定容)
    public ByteBuffer setAutoChannel(boolean doSet, int mode, Comm.Channel channel, int num, int speed, int cap, String launchTime) {
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

    // 设置背光
    public ByteBuffer setBacklit(int normal, int saving) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) normal);
        buffer.put((byte) saving);
        return build(0x103, buffer.array());
    }

    // 设置时间
    public ByteBuffer setDateTime(String dateTime) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + dateTime.length());
        buffer.put((byte) dateTime.length());
        buffer.put(dateTime.getBytes());
        return build(0x104, buffer.array());
    }

    // 恢复出厂设置
    public ByteBuffer setRestore(boolean doSet) {
        return build(0x105, new byte[] {(byte) (doSet ? 1 : 2)});
    }

    // 清空采样数据
    public ByteBuffer setClearSample(boolean doSet) {
        return build(0x106, new byte[] {(byte) (doSet ? 1 : 2)});
    }

    // 流量校准
    public ByteBuffer setAdjust(Comm.Channel channel, int expectPressure, int targetSpeed) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 2);
        buffer.put(channel.getValue());
        buffer.putShort((short) expectPressure);
        buffer.putShort((short) targetSpeed);
        return build(0x107, buffer.array());
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
