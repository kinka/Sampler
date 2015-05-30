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
    public static final String SP_SVR = "server";
    public static String server = "192.168.8.1";
    public static int svrPort = 36500;
    public static int localPort = 12346;
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
        server = Comm.getSP(SP_SVR);
        if (server == null || server.length() == 0)
            server = "192.168.8.1";
        if (server.contains(":")) {
            svrPort = Integer.valueOf(server.substring(server.indexOf(":")));
        }
        Comm.logI("connected server: " + server + ":" + svrPort);
    }
    public static synchronized ByteBuffer send(ByteBuffer data) {
        ByteBuffer recvData = ByteBuffer.allocate(1024*8);
        DatagramPacket packet = new DatagramPacket(recvData.array(), recvData.limit());

        try {
            DatagramChannel channel = DatagramChannel.open();
            DatagramSocket socket = channel.socket();
            socket.bind(null);
            channel.configureBlocking(true);
            data.flip();
            channel.send(data, new InetSocketAddress(server, svrPort));
            socket.setSoTimeout(1000);

            socket.receive(packet); // todo 考虑循环收包的问题
            recvData.limit(packet.getLength());
//            channel.receive(recvData);
//            recvData.flip();
        } catch (Exception e) {
            String msg = e.getCause().getMessage();
            Comm.logE("cause by " + msg);
            // todo 尝试重试一次
//            if (msg.contains("EAGAIN")) {
//                try {
//                    Thread.sleep(100);
//                } catch (Exception ex) {
//
//                }
//                return Deliver.send(data);
//            }
//            e.printStackTrace();
            recvData.limit(0);
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

    }
}
