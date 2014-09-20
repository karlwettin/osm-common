package se.kodapan.util;

/**
 * http://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l
 * <p/>
 * Created by kalle on 10/20/13.
 */
public class Hex {

  public static String encodeHexString(byte[] bytes) {
    char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for (int j = 0; j < bytes.length; j++) {
      v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v / 16];
      hexChars[j * 2 + 1] = hexArray[v % 16];
    }
    return new String(hexChars);
  }

}
