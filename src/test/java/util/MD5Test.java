package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MD5Test {

    private static final Random random = new Random();

    @Test
    public void md5() {
        for (int i = 0; i < 100; i++) {
            byte[] src = new byte[random.nextInt(100000)];
            for (int j = 0; j < src.length; j++) {
                src[j] = (byte) (random.nextInt(256) & 0xFF);
            }
            Assert.assertEquals(myMD5implement(src), jdkMD5implement(src));
        }
    }

    private String myMD5implement(byte[] src) {
        MD5 md5 = new MD5();
        md5.update(src);
        return md5.getDigest();
    }

    private String jdkMD5implement(byte[] bytes) {
        MessageDigest messagedigest = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Assert.fail(e.getMessage(), e);
        }
        messagedigest.update(bytes);
        byte[] digest = messagedigest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
