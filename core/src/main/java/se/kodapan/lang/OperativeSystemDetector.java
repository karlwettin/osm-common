package se.kodapan.lang;

/**
 * @author kalle
 * @since 2013-10-16 20:19
 */
public class OperativeSystemDetector {

  public static boolean isAndroid() {
    return "http://www.android.com/".equals(System.getProperty("java.vendor.url"))
        || "http://www.android.com/".equals(System.getProperty("java.vm.vendor.url"))
        || "/system".equals(System.getProperty("java.home"))
        || "Dalvik".equals(System.getProperty("java.vm.name"))
        || "Android Runtime".equals(System.getProperty("java.runtime.name"))
        || "The Android Project".equals(System.getProperty("java.specification.vendor"))
        || "The Android Project".equals(System.getProperty("java.vm.specification.vendor"))
        || "The Android Project".equals(System.getProperty("java.vm.vendor"))
        || "true".equals(System.getProperty("android.vm.dexfile"))
        || "Dalvik Core Library".equals(System.getProperty("java.specification.name"))
        || "The Android Project".equals(System.getProperty("java.vendor"))
        || "Dalvik Virtual Machine Specification".equals(System.getProperty("java.vm.specification.name"))
        || "".equals(System.getProperty(""))
        || "".equals(System.getProperty(""))
        ;

  }

}
