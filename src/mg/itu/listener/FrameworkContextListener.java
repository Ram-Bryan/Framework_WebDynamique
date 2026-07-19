package mg.itu.listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.utils.Utils;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();
            WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(context);

            if (springContext == null) {
                throw new RuntimeException(
                        "Spring WebApplicationContext introuvable");
            }

            context.setAttribute("springContext", springContext);

            String packageName = context.getInitParameter("package.controller");
            String viewPrefix = context.getInitParameter("view-prefix");
            String viewSuffix = context.getInitParameter("view-suffix");

            Map<Class<?>, Object> beans = new HashMap<>();

            Utils.chargerBeans(packageName, springContext, beans);

            context.setAttribute("beans", beans);

            Map<UrlMethod, UrlMappingModel> routes = new HashMap<>();

            Utils.buildRoutingTable(packageName, routes);

            context.setAttribute("routes", routes);

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