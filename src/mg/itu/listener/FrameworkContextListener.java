package mg.itu.listener;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.model.ApplicationContext;
import mg.itu.annotation.Controller;
import mg.itu.utils.Utils;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();

            String packageName = context.getInitParameter("package.controller");
            String viewPrefix = context.getInitParameter("view-prefix");
            String viewSuffix = context.getInitParameter("view-suffix");

            ApplicationContext appContext = new ApplicationContext();
            Utils.scanAndInstantiateBeans(packageName, appContext);

            Map<UrlMethod, UrlMappingModel> routes = new HashMap<>();

            Utils.buildRoutingTable(packageName, routes);

            context.setAttribute("routes", routes);
            context.setAttribute("applicationContext", appContext);

            context.setAttribute("view-prefix", viewPrefix);
            context.setAttribute("view-suffix", viewSuffix);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}