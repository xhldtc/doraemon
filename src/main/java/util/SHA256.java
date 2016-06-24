package util;

import static util.ByteUtils.*;


/**
 * @author xhldtc
 *  SHA256算法实现，参考自https://en.wikipedia.org/wiki/SHA-2 ,注意有两个地方是rightshift!!!
 *  不全是rightrotate
 */
public class SHA256 {

    private int h0, h1, h2, h3, h4, h5, h6, h7;
    private final int k[] = { 0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4,
            0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152,
            0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138,
            0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70,
            0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
            0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa,
            0xa4506ceb, 0xbef9a3f7, 0xc67178f2 };

    public SHA256() {
        h0 = 0x6a09e667;
        h1 = 0xbb67ae85;
        h2 = 0x3c6ef372;
        h3 = 0xa54ff53a;
        h4 = 0x510e527f;
        h5 = 0x9b05688c;
        h6 = 0x1f83d9ab;
        h7 = 0x5be0cd19;
    }

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
            int[] w = new int[64];
            System.arraycopy(array, j, w, 0, 16);
            for (int i = 16; i < 64; i++) {
                int s0 = rightrotate(w[i - 15], 7) ^ rightrotate(w[i - 15], 18) ^ rightshift(w[i - 15], 3);
                int s1 = rightrotate(w[i - 2], 17) ^ rightrotate(w[i - 2], 19) ^ rightshift(w[i - 2], 10);
                w[i] = w[i - 16] + s0 + w[i - 7] + s1;
            }
            int a = h0, b = h1, c = h2, d = h3, e = h4, f = h5, g = h6, h = h7;
            for (int i = 0; i < 64; i++) {
                int S1 = rightrotate(e, 6) ^ rightrotate(e, 11) ^ rightrotate(e, 25);
                int ch = (e & f) ^ (~e & g);
                int temp1 = h + S1 + ch + k[i] + w[i];
                int S0 = rightrotate(a, 2) ^ rightrotate(a, 13) ^ rightrotate(a, 22);
                int maj = (a & b) ^ (a & c) ^ (b & c);
                int temp2 = S0 + maj;
                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;
            }
            h0 += a;
            h1 += b;
            h2 += c;
            h3 += d;
            h4 += e;
            h5 += f;
            h6 += g;
            h7 += h;
        }
    }

    public String getDigest() {
        return String.format("%08x", h0) + String.format("%08x", h1) + String.format("%08x", h2)
                + String.format("%08x", h3) + String.format("%08x", h4) + String.format("%08x", h5)
                + String.format("%08x", h6) + String.format("%08x", h7);
    }

    public static void main(String[] args) {
        SHA256 sha1 = new SHA256();
        sha1.update("hello".getBytes());
        System.out.println(sha1.getDigest());
    }
}
