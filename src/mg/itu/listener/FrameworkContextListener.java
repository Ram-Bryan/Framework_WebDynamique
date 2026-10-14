package mg.itu.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import mg.itu.model.ApplicationContext;
import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.utils.Utils;
import mg.itu.config.FrameworkConfig;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();

            String packageName = context.getInitParameter("package.controller");
            String viewPrefix = context.getInitParameter("view-prefix");
            String viewSuffix = context.getInitParameter("view-suffix");

            // le directory qu'on va uploader
            String uploadDir = context.getInitParameter("upload-dir");

            if (uploadDir != null && !uploadDir.isEmpty()) {
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                context.setAttribute("upload-dir", uploadDir);
                FrameworkConfig.setUploadDir(uploadDir);
            }

            // Creer le contenaire
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