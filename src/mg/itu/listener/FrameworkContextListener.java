package mg.itu.listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;

import mg.itu.config.FrameworkConfig;
import mg.itu.model.ApplicationContext;
import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.utils.Utils;

public class FrameworkContextListener implements ServletContextListener {

    private static long parseLongProperty(Properties props, String key, long defaultValue) {
        String raw = props.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Valeur invalide pour '" + key + "': " + raw, e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();

            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (input == null) {
                    throw new RuntimeException("application.properties introuvable sur le classpath");
                }
                props.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Erreur de lecture de application.properties", e);
            }

            String uploadDir = props.getProperty("upload-dir");
            if (uploadDir == null || uploadDir.trim().isEmpty()) {
                throw new RuntimeException("Le paramètre 'upload-dir' est absent dans application.properties");
            }

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            context.setAttribute("upload-dir", uploadDir);
            FrameworkConfig.setUploadDir(uploadDir);

            long maxFileSize = parseLongProperty(props, "max-file-size", 10L * 1024L * 1024L);
            long maxRequestSize = parseLongProperty(props, "max-request-size", 50L * 1024L * 1024L);
            long fileSizeThreshold = parseLongProperty(props, "file-size-threshold", 1024L * 1024L);

            FrameworkConfig.setMaxFileSize(maxFileSize);
            FrameworkConfig.setMaxRequestSize(maxRequestSize);
            FrameworkConfig.setFileSizeThreshold(fileSizeThreshold);

            ServletRegistration registration = context.getServletRegistration("FrontControllerServlet");
            if (registration != null) {
                MultipartConfigElement multipartConfig = new MultipartConfigElement(
                        null,
                        FrameworkConfig.getMaxFileSize(),
                        FrameworkConfig.getMaxRequestSize(),
                        (int) FrameworkConfig.getFileSizeThreshold()
                );
                registration.setMultipartConfig(multipartConfig);
            }

            String packageName = context.getInitParameter("package.controller");
            String viewPrefix = context.getInitParameter("view-prefix");
            String viewSuffix = context.getInitParameter("view-suffix");

            ApplicationContext appContext = new ApplicationContext();

            Object springContext = Utils.getSpringWebApplicationContext(context);

            Utils.scanAndInstantiateBeans(packageName, appContext, springContext);

            Map<UrlMethod, UrlMappingModel> routes = new HashMap<>();

            Utils.buildRoutingTable(packageName, routes);

            context.setAttribute("routes", routes);
            context.setAttribute("applicationContext", appContext);

            context.setAttribute("view-prefix", viewPrefix);
            context.setAttribute("view-suffix", viewSuffix);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Framework initialization failed", e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}