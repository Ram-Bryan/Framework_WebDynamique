package mg.itu.listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.model.ApplicationContext;
import mg.itu.utils.Utils;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();

            String packageName = context.getInitParameter("package.controller");
            String viewPrefix = context.getInitParameter("view-prefix");
            String viewSuffix = context.getInitParameter("view-suffix");

            System.out.println("[FRAMEWORK] Initializing FrameworkContextListener...");
            System.out.println("[FRAMEWORK] Controller package: " + packageName);
            System.out.println("[FRAMEWORK] View prefix: " + viewPrefix);
            System.out.println("[FRAMEWORK] View suffix: " + viewSuffix);

            // Create ApplicationContext (the container)
            ApplicationContext appContext = new ApplicationContext();
            System.out.println("[FRAMEWORK] ApplicationContext created");

            // If a Spring application context is already configured, reuse it.
            // This keeps the framework in control of controllers while allowing Spring
            // to manage repository/service beans when they exist.
            Object springContext = Utils.getSpringWebApplicationContext(context);
            if (springContext != null) {
                System.out.println("[FRAMEWORK] Spring ApplicationContext found and will be used");
            } else {
                System.out.println("[FRAMEWORK] No Spring ApplicationContext found");
            }

            // Scan and instantiate all @Controller beans
            System.out.println("[FRAMEWORK] Scanning for @Controller beans...");
            Utils.scanAndInstantiateBeans(packageName, appContext, springContext);
            System.out.println("[FRAMEWORK] Found " + appContext.getAllBeans().size() + " controller(s)");

            Map<UrlMethod, UrlMappingModel> routes = new HashMap<>();

            System.out.println("[FRAMEWORK] Building routing table...");
            Utils.buildRoutingTable(packageName, routes);
            System.out.println("[FRAMEWORK] Found " + routes.size() + " route(s):");
            for (UrlMethod method : routes.keySet()) {
                System.out.println("  - " + method.getMethod() + " " + method.getUrl());
            }

            context.setAttribute("routes", routes);
            context.setAttribute("applicationContext", appContext);

            context.setAttribute("view-prefix", viewPrefix);
            context.setAttribute("view-suffix", viewSuffix);

            System.out.println("[FRAMEWORK] FrameworkContextListener initialized successfully!");

        } catch (Exception e) {

            System.err.println("[FRAMEWORK] ERROR during initialization:");
            e.printStackTrace();
            throw new RuntimeException("Framework initialization failed", e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}