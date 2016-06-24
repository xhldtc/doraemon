package util;

/**
 * @author xhldtc SHA1摘要算法实现，参考自 https://zh.wikipedia.org/wiki/SHA家族
 */
public class SHA1 {

    private int h0 = 0x67452301;
    private int h1 = 0xEFCDAB89;
    private int h2 = 0x98BADCFE;
    private int h3 = 0x10325476;
    private int h4 = 0xC3D2E1F0;

    int[] appendLength(byte[] message, long length) {
        int[] ints = ByteUtils.byte2int(message, true);
        ints[ints.length - 2] = (int) (length >>> 32);
        ints[ints.length - 1] = (int) (length & 0xFFFFFFFF);
        return ints;
    }
    
    int[] getWords(byte[] message) {
        long length = message.length * 8;
        return appendLength(ByteUtils.appendPaddingBits(message), length);
    }

    public void update(byte[] message) {
        process(getWords(message));
    }

    void process(int[] array) {
        for (int j = 0; j < array.length; j += 16) {
            int[] w = new int[80];
            System.arraycopy(array, j, w, 0, 16);
            for (int i = 16; i < 80; i++)
                w[i] = ByteUtils.leftrotate(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 1);

            int a = h0, b = h1, c = h2, d = h3, e = h4, f, k;
            for (int i = 0; i < 80; i++) {
                if (i < 20) {
                    f = (b & c) | (~b & d);
                    k = 0x5A827999;
                } else if (i < 40) {
                    f = b ^ c ^ d;
                    k = 0x6ED9EBA1;
                } else if (i < 60) {
                    f = (b & c) | (b & d) | (c & d);
                    k = 0x8F1BBCDC;
                } else {
                    f = b ^ c ^ d;
                    k = 0xCA62C1D6;
                }
                int temp = ByteUtils.leftrotate(a, 5) + f + e + k + w[i];
                e = d;
                d = c;
                c = ByteUtils.leftrotate(b, 30);
                b = a;
                a = temp;
            }
            h0 += a;
            h1 += b;
            h2 += c;
            h3 += d;
            h4 += e;
        }
    }

    public String getDigest() {
        return String.format("%08x", h0) + String.format("%08x", h1) + String.format("%08x", h2)
                + String.format("%08x", h3) + String.format("%08x", h4);
    }

    public static void main(String[] args) {
        SHA1 sha1 = new SHA1();
        sha1.update("hello".getBytes());
        System.out.println(sha1.getDigest());
    }
}
