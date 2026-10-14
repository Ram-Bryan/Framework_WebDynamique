package mg.itu.config;

public final class FrameworkConfig {

    private static String uploadDir;

    private FrameworkConfig() {
    }

    public static void setUploadDir(String dir) {
        uploadDir = dir;
    }

    public static String getUploadDir() {
        return uploadDir;
    }
}
