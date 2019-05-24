package blockchain;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Base58;
import com.google.common.primitives.Bytes;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.Arrays;

/**
 * @author xhldtc on 2019-05-21.
 */
public class EcdsaUtils {

    private static final String SHA_256 = "SHA-256";
    private static final String RIPEMD_160 = "RipeMD160";
    private static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private static final String EC_SECP256K1 = "secp256k1";


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair initKey() {
        return initKey(null);
    }

    // 初始化密钥对
    public static KeyPair initKey(String seed) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(EC_SECP256K1);
            SecureRandom secureRandom = seed == null ? new SecureRandom() : new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
            generator.initialize(ecGenParameterSpec, secureRandom);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String createAddress(byte[] src) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest sha256 = MessageDigest.getInstance(SHA_256);
        byte[] tmp = sha256.digest(src);
        MessageDigest rmd = MessageDigest.getInstance(RIPEMD_160, BOUNCY_CASTLE_PROVIDER);
        byte[] rmdHash = rmd.digest(tmp);
        return base58CheckEncode(rmdHash, (byte) 0x00);
    }

    public static String base58CheckEncode(byte[] hash, byte versionByte) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance(SHA_256);
        byte[] append = appendFirst(hash, new byte[]{versionByte});
        byte[] r1 = sha256.digest(append);
        byte[] r2 = sha256.digest(r1);
        byte[] checksum = Arrays.copyOfRange(r2, 0, 4);
        byte[] address = appendLast(append, checksum);
//        System.out.println(DatatypeConverter.printHexBinary(address).toLowerCase());
//        System.out.println(Base58.encode(address));
        return Base58.encode(address);
    }

    private static byte[] appendFirst(byte[] src, byte[] append) {
        byte[] result = Arrays.copyOf(append, src.length + append.length);
        System.arraycopy(src, 0, result, append.length, src.length);
        return result;
    }

    private static byte[] appendLast(byte[] src, byte[] append) {
        byte[] result = Arrays.copyOf(src, src.length + append.length);
        System.arraycopy(append, 0, result, src.length, append.length);
        return result;
    }

    private static String adjustTo64(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64 - s.length(); i++) {
            sb.append('0');
        }
        return sb.append(s).toString();
    }

    /**
     * WIF格式私钥转比特币地址，压缩和非压缩格式都支持
     *
     * @param wif
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws AddressFormatException
     */
    public static String walletImportFormatToBitcoinAddress(String wif) throws NoSuchAlgorithmException, NoSuchProviderException, AddressFormatException {
        if (!wif.startsWith("5") && !wif.startsWith("K") && !wif.startsWith("L")) {
            throw new IllegalArgumentException("Invalid WIF private key.");
        }
        boolean compressed = !wif.startsWith("5");
        byte[] decoded = Base58.decodeChecked(wif);
        if (decoded[0] != (byte) 0x80) {
            throw new IllegalArgumentException("Invalid WIF private key.");
        }
        if (compressed && decoded[decoded.length - 1] != 1) {
            throw new IllegalArgumentException("Invalid WIF private key.");
        }
        byte[] privateKey = Arrays.copyOfRange(decoded, 1, compressed ? decoded.length - 1 : decoded.length);

        //这里可以拿到64位字符格式的私匙
//        System.out.println("private key: " + adjustTo64(DatatypeConverter.printHexBinary(privateKey)));

        ECPoint ecPoint = toPublicKey(privateKey);
//        System.out.println("public key X: " + adjustTo64(ecPoint.getAffineX().toString(16)));
//        System.out.println("public key Y: " + adjustTo64(ecPoint.getAffineY().toString(16)));
        return publicKeyToBitcoinAddress(ecPoint, compressed);
    }

    private static String publicKeyToBitcoinAddress(ECPoint ecPoint, boolean compressed) throws NoSuchProviderException, NoSuchAlgorithmException {
        if (compressed) {
            String prefix = ecPoint.getAffineY().testBit(0) ? "03" : "02";
            String publicKeyX = ecPoint.getAffineX().toString(16);
            return createAddress(DatatypeConverter.parseHexBinary(prefix + adjustTo64(publicKeyX)));
        } else {
            String publicKeyX = ecPoint.getAffineX().toString(16);
            String publicKeyY = ecPoint.getAffineY().toString(16);
            return createAddress(DatatypeConverter.parseHexBinary("04" + adjustTo64(publicKeyX) + adjustTo64(publicKeyY)));
        }
    }

    /**
     * 私匙转WIF格式，
     *
     * @param privateKey
     * @param compressed true代表压缩格式的地址，false代表非压缩格式地址
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String privateKeyToWalletImportFormat(byte[] privateKey, boolean compressed) throws NoSuchAlgorithmException {
        return base58CheckEncode(compressed ? Bytes.concat(privateKey, new byte[]{0x01}) : privateKey, (byte) 0x80);
    }

    /**
     * 从私钥推导出公钥
     *
     * @return jdk版本的ECPoint，就是x,y坐标
     */
    public static ECPoint toPublicKey(String hexPrivateKey) {
        return toPublicKey(new BigInteger(hexPrivateKey, 16));
    }

    public static ECPoint toPublicKey(byte[] bytePrivateKey) {
        return toPublicKey(new BigInteger(1, bytePrivateKey));
    }

    public static ECPoint toPublicKey(BigInteger bigInteger) {
        X9ECParameters curve = SECNamedCurves.getByName(EC_SECP256K1);
        org.bouncycastle.math.ec.ECPoint Q = curve.getG().multiply(bigInteger);
        return new ECPoint(Q.getX().toBigInteger(), Q.getY().toBigInteger());
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, AddressFormatException {
        String seed = "any random seed you want to input here";
        KeyPair keyPair = initKey(seed);
        ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
        String privateKey = adjustTo64(ecPrivateKey.getS().toString(16));
        System.out.println("private key: " + privateKey);
//        privateKey = "3aba4162c7251c891207b747840551a71939b0de081f85c4e44cf7c13e41daa6";
        byte[] privateKeyByte = DatatypeConverter.parseHexBinary(privateKey);

        String wif = privateKeyToWalletImportFormat(privateKeyByte, true);
        String address = walletImportFormatToBitcoinAddress(wif);
        System.out.println("WIF compressed: " + wif);
        System.out.println("bitcoin address compressed: " + address);

        wif = privateKeyToWalletImportFormat(privateKeyByte, false);
        address = walletImportFormatToBitcoinAddress(wif);
        System.out.println("WIF not compressed: " + wif);
        System.out.println("bitcoin address not compressed: " + address);
    }
}
