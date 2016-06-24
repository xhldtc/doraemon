package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SHA1Test {

    private static final Random random = new Random();

    @Test
    public void sha1() {
        for (int i = 0; i < 100; i++) {
            byte[] src = new byte[random.nextInt(100000)];
            for (int j = 0; j < src.length; j++) {
                src[j] = (byte) (random.nextInt(256) & 0xFF);
            }
            Assert.assertEquals(mySHA1implement(src), jdkSHA1implement(src));
        }
    }

    private String mySHA1implement(byte[] src) {
        SHA1 sha1 = new SHA1();
        sha1.update(src);
        return sha1.getDigest();
    }

    private String jdkSHA1implement(byte[] bytes) {
        MessageDigest messagedigest = null;
        try {
            messagedigest = MessageDigest.getInstance("SHA-1");
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
