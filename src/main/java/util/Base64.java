package util;


/**
 * @author xhldtc
 *	Base64 encode算法，详见https://zh.wikipedia.org/wiki/Base64
 */
public class Base64 {

	public static final int mask = 0x3f;
	public static final char[] BASE64_INDEX = new char[64];

	static String source = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
	static String target = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";

	static {
		for (int i = 0; i < 26; i++)
			BASE64_INDEX[i] = (char) ('A' + i);
		for (int i = 26; i < 52; i++)
			BASE64_INDEX[i] = (char) ('a' + i - 26);
		for (int i = 52; i < 62; i++)
			BASE64_INDEX[i] = (char) ('0' + i - 52);
		BASE64_INDEX[62] = '+';
		BASE64_INDEX[63] = '/';
	}

	public static void main(String[] args) {
		System.out.println(target.equals(encodeBase64String(source)));
	}

	public static String encodeBase64String(String s) {
		byte[] data = s.getBytes();
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i + 2 < data.length; i += 3)
			sb.append(encodeThreeByteString(data[i], data[i + 1], data[i + 2]));
		if (i + 2 == data.length)
			sb.append(encodeTwoByteString(data[i], data[i + 1]));
		else if (i + 1 == data.length)
			sb.append(encodeOneByteString(data[i]));
		return sb.toString();
	}

	public static String encodeThreeByteString(byte a, byte b, byte c) {
		int i = (a << 16) | (b << 8) | c;
		char[] ch = new char[4];
		ch[0] = BASE64_INDEX[(i >>> 18) & mask];
		ch[1] = BASE64_INDEX[(i >>> 12) & mask];
		ch[2] = BASE64_INDEX[(i >>> 6) & mask];
		ch[3] = BASE64_INDEX[i & mask];
		return new String(ch);
	}

	public static String encodeTwoByteString(byte a, byte b) {
		int i = (a << 10) | (b << 2);
		char[] ch = new char[4];
		ch[0] = BASE64_INDEX[(i >>> 12) & mask];
		ch[1] = BASE64_INDEX[(i >>> 6) & mask];
		ch[2] = BASE64_INDEX[i & mask];
		ch[3] = '=';
		return new String(ch);
	}

	public static String encodeOneByteString(byte a) {
		int i = a << 4;
		char[] ch = new char[4];
		ch[0] = BASE64_INDEX[(i >>> 6) & mask];
		ch[1] = BASE64_INDEX[i & mask];
		ch[2] = '=';
		ch[3] = '=';
		return new String(ch);
	}
}
