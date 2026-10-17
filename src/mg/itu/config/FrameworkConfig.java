package mg.itu.config;

public final class FrameworkConfig {

    private static String uploadDir;
    private static long maxFileSize;
    private static long maxRequestSize;
    private static long fileSizeThreshold;

    private FrameworkConfig() {
    }

    public static void setUploadDir(String dir) {
        uploadDir = dir;
    }

    public static String getUploadDir() {
        return uploadDir;
    }

    public static void setMaxFileSize(long value) {
        maxFileSize = value;
    }

    public static long getMaxFileSize() {
        return maxFileSize;
    }

    public static void setMaxRequestSize(long value) {
        maxRequestSize = value;
    }

    public static long getMaxRequestSize() {
        return maxRequestSize;
    }

    public static void setFileSizeThreshold(long value) {
        fileSizeThreshold = value;
    }

    public static long getFileSizeThreshold() {
        return fileSizeThreshold;
    }

}
