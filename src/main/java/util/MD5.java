package util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhldtc MD5摘要算法实现，参考自 https://zh.wikipedia.org/zh-cn/MD5 和
 *         https://tools.ietf.org/html/rfc1321
 *         实现的时候有几点需要非常注意！！！ 
 *         1. 在拼长度之前的byte数组转32位word时需要用小端法，
 *            即按数组顺序0xAB,0xCD,0xEF,0x12这四个字节拼成的int是 0x12EFCDAB
 *         2. 拼长度的时候，把64位long转成两个32位int,低字节优先，拼到words数组里，
 *            注意这时int不用再转成小端顺序，例 0xABABCDCDEFEF1212拆成
 *            0xEFEF1212,0xABABCDCD拼到后面
 *         3. 拿到32位words数组后计算按算法实现逻辑，这里没什么坑
 *         4. 最后需要注意！！得到的结果要转成小端顺序的int再输出
 */
public class MD5 {

    private int h0 = 0x67452301;
    private int h1 = 0xEFCDAB89;
    private int h2 = 0x98BADCFE;
    private int h3 = 0x10325476;
    private final int[] r = new int[64];
    private final int[] k = new int[64];

    {
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

    int[] getWords(byte[] message) {
        long length = message.length * 8;
        return appendLength(appendPaddingBits(message), length);
    }

    byte[] appendPaddingBits(byte[] message) {
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

    int[] appendLength(byte[] message, long length) {
        int[] ints = byte2int(message);
        ints[ints.length - 2] = (int) (length & 0xFFFFFFFF);
        ints[ints.length - 1] = (int) (length >>> 32);
        return ints;
    }

    int[] byte2int(byte[] bytes) {
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < bytes.length; i += 4) {
            result[i / 4] = (0xFF & bytes[i + 3]) << 24 | (0xFF & bytes[i + 2]) << 16 | (0xFF & bytes[i + 1]) << 8
                    | (0xFF & bytes[i]);
        }
        return result;
    }

    int F(int x, int y, int z) {
        return (x & y) | (~x & z);
    }

    int G(int x, int y, int z) {
        return (x & z) | (y & ~z);
    }

    int H(int x, int y, int z) {
        return x ^ y ^ z;
    }

    int I(int x, int y, int z) {
        return y ^ (x | ~z);
    }

    int leftrotate(int num, int count) {
        return (num << count) | (num >>> (32 - count));
    }

    String int2byteLittleEndian(int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i += 8)
            sb.append(String.format("%02x", (num >>> i) & 0xFF));
        return sb.toString();
    }

    void process(int[] array) {
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

    public void update(byte[] message) {
        process(getWords(message));
    }

    public String getDigest() {
        return int2byteLittleEndian(h0) + int2byteLittleEndian(h1) + int2byteLittleEndian(h2)
                + int2byteLittleEndian(h3);
    }
    /*
     * public static void main(String[] args) { update("".getBytes());
     * System.out.println(getDigest()); }
     */
}
