package util;

import java.util.ArrayList;
import java.util.List;

public class ByteUtils {

    public static byte[] appendPaddingBits(byte[] message) {
        List<Byte> paddings = new ArrayList<Byte>();
        paddings.add((byte) 0x80);
        while ((message.length + paddings.size()) * 8 % 512 != 448) {
            paddings.add((byte) 0);
        }
        byte[] result = new byte[message.length + paddings.size() + 8];
        System.arraycopy(message, 0, result, 0, message.length);
        for (int i = message.length; i < message.length + paddings.size(); i++)
            result[i] = paddings.get(i - message.length);
        return result;
    }

    public static int leftrotate(int num, int count) {
        return (num << count) | (num >>> (32 - count));
    }

    public static int rightrotate(int num, int count) {
        return (num >>> count) | (num << (32 - count));
    }

    public static int rightshift(int num, int count) {
        return (num >>> count);
    }

    public static int[] byte2int(byte[] bytes, boolean bigEndian) {
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < bytes.length; i += 4) {
            if (bigEndian) {
                result[i / 4] = (0xFF & bytes[i]) << 24 | (0xFF & bytes[i + 1]) << 16 | (0xFF & bytes[i + 2]) << 8
                        | (0xFF & bytes[i + 3]);
            } else {
                result[i / 4] = (0xFF & bytes[i + 3]) << 24 | (0xFF & bytes[i + 2]) << 16 | (0xFF & bytes[i + 1]) << 8
                        | (0xFF & bytes[i]);
            }
        }
        return result;
    }
}
