package hk.amae.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by kinka on 4/12/15.
 */
public class Deliver {
    public static final String DefaultSvr = "192.168.8.1:36500";
    public static final String SP_SVR = "server";

    // 丢包统计
    static private long total = 0;
    static private long lost = 0;

    static String svrHost = "192.168.8.1";
    static int svrPort = 36500;
    public static void printData(byte[] data) {
        String s = "0123456789abcdef";
        for (byte d:data) {
            int L = d & 0xf;
            int H = (d >> 4) & 0xf;
            System.out.print(s.charAt(H) + "" + s.charAt(L) + " ");
        }
        System.out.println();
    }
    public static void init() {
        String server = Comm.getSP(SP_SVR);
        if (server == null || server.length() == 0)
            server = DefaultSvr;

        if (server.contains(":")) {
            int pos = server.indexOf(":");
            svrHost = server.substring(0, pos);
            svrPort = Integer.valueOf(server.substring(pos + 1));
        }
        Comm.logI("connected server: " + svrHost + ":" + svrPort);
    }
    public static synchronized ByteBuffer send(ByteBuffer data, int timeout) {
        ByteBuffer recvData = ByteBuffer.allocate(1024*8);
        DatagramPacket packet = new DatagramPacket(recvData.array(), recvData.limit());

        try {
            total = (total + 1 ) % 10;
            if (total < lost) lost = 0;
            DatagramChannel channel = DatagramChannel.open();
            DatagramSocket socket = channel.socket();
            socket.bind(null);
            channel.configureBlocking(true);
            data.flip();
            channel.send(data, new InetSocketAddress(svrHost, svrPort));
            socket.setSoTimeout(timeout == 0 ? 1000 : timeout);

            socket.receive(packet); // todo 考虑循环收包的问题
            recvData.limit(packet.getLength());
//            channel.receive(recvData);
//            recvData.flip();
        } catch (Exception e) {
            if (e.getCause() != null) {
                String msg = e.getCause().getMessage();
                Comm.logE("cause by " + msg);
            } else {
                e.printStackTrace();
            }
            recvData.limit(0);
            lost++;
            double percent = (lost+1.0)/(total+1.0);
            if (total >= 9 && percent >= 0.5) {
                Comm.showTips("网络貌似不通，请检查网络状态");
                total = 0;
                lost = 0;
            }
            Comm.logI("lost " + lost + "/" + total + "=" + percent);
        }

        return recvData;
    }

    public static void main(String... args) {
        Command command = new Command(new Command.Once() {
            @Override
            public void done(boolean verify, Command cmd) {
                System.out.println(verify + " " + cmd.Model + " " + cmd.SN);
            }
        });
        ByteBuffer buffer = command.reqModel();
        printData(buffer.array());

//        System.out.println(String.format("%x", Command.crc16(new byte[]{0x12, 0x34, 0x56, 0x78})));
/*

        final int cnt = 0;

        for (int i=0; i<cnt; i++) {
            final int k = i;
            new Command(new Command.Once() {
                @Override
                public void done(boolean verify, Command cmd) {
                    if (k+1 == cnt)
                        Command.PacketLost();
                }
            }).reqDateTime();
        }
*/

    }
}
