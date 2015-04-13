package hk.amae.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kinka on 4/11/15.
 */
public class Command {
    byte Version = 1;

    private short crc16_ccitt(byte[] bytes, int end) {
        int crc = 0xFFFF;
        int temp;
        int crc_byte;

        for (int byte_index = 0; byte_index < end; byte_index++) {

            crc_byte = bytes[byte_index];

            for (int bit_index = 0; bit_index < 8; bit_index++) {

                temp = ((crc >> 15)) ^ ((crc_byte >> 7));

                crc <<= 1;
                crc &= 0xFFFF;

                if (temp > 0) {
                    crc ^= 0x1021;
                    crc &= 0xFFFF;
                }

                crc_byte <<=1;
                crc_byte &= 0xFF;

            }
        }

        return (short) crc;
    }
    private ByteBuffer build(byte version, int cmd, byte[] data) {
        if (data == null) data = new byte[0];

        ByteBuffer buf = ByteBuffer.allocate(3 + 1 + 2 + 2 + data.length + 2);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put((byte) 0xaa);
        buf.put((byte) 0xaf);
        buf.put((byte) 0xfa);
        buf.put(version);
        buf.putShort((short) cmd);
        buf.putShort((short) data.length);
        buf.put(data);

        short sum = crc16_ccitt(buf.array(), buf.capacity() - 2); // crc16-CCITT
        buf.putShort(sum);
        return buf;
    }

    public String Model = ""; // 型号
    public String SN = ""; // 序列号
    public ByteBuffer reqQueryModel() {
        String model = "EM-2008";
        String sn = "021547";
        ByteBuffer buffer = ByteBuffer.allocate(1 + model.length() + 1 + sn.length());
        buffer.put((byte) model.length());
        buffer.put(model.getBytes());
        buffer.put((byte) sn.length());
        buffer.put(sn.getBytes());
        return build(Version, 0x1, buffer.array());
    }
    public boolean parseQueryModel(ByteBuffer reply) {
        reply.order(ByteOrder.BIG_ENDIAN);

        reply.getShort();
        reply.get(); // == 0xfa?
        reply.get(); // version
        reply.getShort(); // cmd
        int len = reply.getShort(); // data len
        len = reply.get();
        byte[] data = new byte[len];
        reply.get(data);
        Model = new String(data);
        len = reply.get();
        data = new byte[len];
        reply.get(data);
        SN = new String(data);
        short sum = reply.getShort(); // sum
        // 校验sum
        short crc = crc16_ccitt(reply.array(), reply.capacity() - 2);
//        System.out.println("sum " + Integer.toHexString(sum & 0xffff));
        return crc == sum;
    }
    // todo 网络发送接收，取数据
}
