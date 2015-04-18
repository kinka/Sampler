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
    public static String server = "127.0.0.1";
    public static int svrPort = 12345;
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
    public static ByteBuffer send(ByteBuffer data) {
        ByteBuffer recvData = ByteBuffer.allocate(1024);
        DatagramPacket packet = new DatagramPacket(recvData.array(), recvData.limit());

        try {
            DatagramChannel channel = DatagramChannel.open();
            DatagramSocket socket = channel.socket();
            socket.bind(new InetSocketAddress(localPort));
            channel.configureBlocking(true);
            channel.send(data, new InetSocketAddress(server, svrPort));
            socket.setSoTimeout(1000);

            socket.receive(packet);
            recvData.limit(packet.getLength());
//            channel.receive(recvData);
//            recvData.flip();
        } catch (Exception e) {
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

        System.out.println("req ");
        Deliver.printData(buffer.array());
    }
}
