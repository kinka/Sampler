package hk.amae.util;

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

        try {
            DatagramChannel channel = DatagramChannel.open();
            // todo timeout
            channel.socket().bind(new InetSocketAddress(localPort));
            channel.send(data, new InetSocketAddress(server, svrPort));

            channel.receive(recvData);
            recvData.flip();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recvData;
    }

    public static void main(String... args) {
        Command command = new Command();
        ByteBuffer buffer = command.reqModel();
        Deliver.printData(buffer.array());
        buffer.flip();
        System.out.println("verify " + command.resolveModel(buffer));
        System.out.println(command.Model + " " + command.SN);

        command = new Command();
        command.reqChannelState(Comm.Channel.A1);
        command.reqChannelState(Comm.Channel.ALL);
        command.reqChannelState(Comm.Channel.CH4);
/*        buffer = Deliver.send(command.reqQueryModel());
        byte[] arr = new byte[buffer.remaining()];
        buffer.mark(); // for reset
        buffer.get(arr);
        Deliver.printData(arr);
        buffer.reset();
        System.out.println(buffer.remaining());
        command.parseQueryModel(buffer);
        System.out.println(command.Model + " " + command.SN);*/
    }
}
