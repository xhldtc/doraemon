package util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhldtc
 * MD5摘要算法实现，参考自 https://zh.wikipedia.org/zh-cn/MD5 和 https://tools.ietf.org/html/rfc1321
 */
public class MD5 {

    private static int h0 = 0x67452301;
    private static int h1 = 0xEFCDAB89;
    private static int h2 = 0x98BADCFE;
    private static int h3 = 0x10325476;
    private static final int[] r = new int[64];
    private static final int[] k = new int[64];

    static {
        for (int i = 0; i < k.length; i++) {
            k[i] = (int) (((long) Math.abs((Math.sin(i + 1) * (1L << 32)))) & 0xFFFFFFFF);
        }
        for (int i = 0; i < 16; i += 4) {
            r[i] = 7;
            r[i + 1] = 12;
            r[i + 2] = 17;
            r[i + 3] = 22;
        }
        for (int i = 16; i < 32; i += 4) {
            r[i] = 5;
            r[i + 1] = 9;
            r[i + 2] = 14;
            r[i + 3] = 20;
        }
        for (int i = 32; i < 48; i += 4) {
            r[i] = 4;
            r[i + 1] = 11;
            r[i + 2] = 16;
            r[i + 3] = 23;
        }
        for (int i = 48; i < 64; i += 4) {
            r[i] = 6;
            r[i + 1] = 10;
            r[i + 2] = 15;
            r[i + 3] = 21;
        }
    }

    static int[] getWords(byte[] message) {
        long length = message.length;
        return appendLength(appendPaddingBits(message), length);
    }

    static byte[] appendPaddingBits(byte[] message) {
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

    static int[] appendLength(byte[] message, long length) {
        int[] ints = byte2int(message);
        ints[ints.length - 2] = (int) (length & 0xFFFFFFFF);
        ints[ints.length - 1] = (int) (length >>> 32);
        return ints;
    }

    static int[] byte2int(byte[] bytes) {
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < bytes.length; i += 4) {
            result[i / 4] = (0x000000FF & bytes[i + 3]) << 24 | (0x000000FF & bytes[i + 2]) << 16
                    | (0x000000FF & bytes[i + 1]) << 8 | (0x000000FF & bytes[i]);
        }
        return result;
    }

    static int F(int x, int y, int z) {
        return (x & y) | (~x & z);
    }

    static int G(int x, int y, int z) {
        return (x & z) | (y & ~z);
    }

    static int H(int x, int y, int z) {
        return x ^ y ^ z;
    }

    static int I(int x, int y, int z) {
        return y ^ (x | ~z);
    }

    static int leftrotate(int num, int count) {
        return (num << count) | (num >>> (32 - count));
    }

    static String int2byteLittleEndian(int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i += 8)
            sb.append(String.format("%02x", (num >>> i) & 0xFF));
        return sb.toString();
    }

    static void process(int[] array) {
        for (int j = 0; j < array.length; j += 16) {
            int a = h0, b = h1, c = h2, d = h3, f, g;
            for (int i = 0; i < 64; i++) {
                if (i < 16) {
                    f = F(b, c, d);
                    g = i;
                } else if (i < 32) {
                    f = G(b, c, d);
                    g = (5 * i + 1) % 16;
                } else if (i < 48) {
                    f = H(b, c, d);
                    g = (3 * i + 5) % 16;
                } else {
                    f = I(b, c, d);
                    g = (7 * i) % 16;
                }
                int temp = d;
                d = c;
                c = b;
                b = leftrotate(a + f + k[i] + array[j + g], r[i]) + b;
                a = temp;
            }
            h0 += a;
            h1 += b;
            h2 += c;
            h3 += d;
        }
    }

    static void update(byte[] message) {
        process(getWords(message));
    }

    static String getDigest() {
        return int2byteLittleEndian(h0) + int2byteLittleEndian(h1) + int2byteLittleEndian(h2)
                + int2byteLittleEndian(h3);
    }

    public static void main(String[] args) {
        update("".getBytes());
        System.out.println(getDigest());
    }
}
