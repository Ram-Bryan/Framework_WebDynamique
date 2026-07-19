package mg.itu.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mg.itu.annotation.Controller;
import mg.itu.annotation.UrlMapping;
import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;


import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class Utils {

    public static void findWithAnnotation(
            Class<? extends Annotation> annotation,
            String packageName,
            String niveau,
            List<Object> objects) throws Exception {

        if (niveau.equalsIgnoreCase("class")) {

            List<Class<?>> listClasses = new ArrayList<>();
            scanPackage(packageName, listClasses);

            for (Class<?> classe : listClasses) {
                if (classe.isAnnotationPresent(annotation)) {
                    objects.add(classe);
                }
            }

        } else if (niveau.equalsIgnoreCase("attribut")) {

        } else if (niveau.equalsIgnoreCase("method")) {

        } else {
            throw new Exception("Choose class, attribut or method");
        }
    }

    public static void getControllers(String packageName, List<Class<?>> controllers) {

        try {

            List<Object> objects = new ArrayList<>();

            findWithAnnotation(
                    Controller.class,
                    packageName,
                    "class",
                    objects);

            for (Object object : objects) {
                controllers.add((Class<?>) object);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void chargerBeans(
            String packageName,
            WebApplicationContext springContext,
            Map<Class<?>, Object> beans) {

        List<Class<?>> controllers = new ArrayList<>();

        getControllers(packageName, controllers);

        for (Class<?> controller : controllers) {

            try {

                Object instance = controller.getDeclaredConstructor().newInstance();

                springContext
                        .getAutowireCapableBeanFactory()
                        .autowireBean(instance);

                beans.put(controller, instance);

            } catch (Exception e) {
                throw new RuntimeException(
                        "Impossible de créer le bean : "
                                + controller.getName(),
                        e);
            }
        }
    }

    public static void buildRoutingTable(
            String packageName,
            Map<UrlMethod, UrlMappingModel> routes) {

        List<Class<?>> controllers = new ArrayList<>();

        getControllers(packageName, controllers);

        for (Class<?> controller : controllers) {

            for (Method method : controller.getMethods()) {

                if (!method.isAnnotationPresent(UrlMapping.class)) {
                    continue;
                }

                String url = method.getAnnotation(UrlMapping.class).url();
                String httpMethod = method.getAnnotation(UrlMapping.class)
                        .method()
                        .toUpperCase();

                UrlMethod urlMethod = new UrlMethod(url, httpMethod);

                UrlMappingModel mapping = new UrlMappingModel();
                mapping.setController(controller);
                mapping.setMethod(method);
                mapping.setUrl(url);

                if (routes.containsKey(urlMethod)) {
                    throw new RuntimeException(
                            "Duplicate URL and method mapping detected : "
                                    + url
                                    + " ["
                                    + httpMethod
                                    + "]");
                }

                routes.put(urlMethod, mapping);
            }
        }
    }

    public static void scanPackage(
            String packageName,
            List<Class<?>> classes) throws Exception {

        if (packageName == null) {
            throw new IllegalArgumentException(
                    "Package name cannot be null. Check your configuration.");
        }

        String path = packageName.replace('.', '/');

        URL resource = Utils.class.getClassLoader().getResource(path);

        if (resource == null) {
            return;
        }

        File directory = new File(resource.getFile());

        scanDirectory(directory, packageName, classes);
    }

    private static void scanDirectory(
            File directory,
            String packageName,
            List<Class<?>> classes)
            throws ClassNotFoundException {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                scanDirectory(
                        file,
                        packageName + "." + file.getName(),
                        classes);

            } else if (file.getName().endsWith(".class")) {

                String className = packageName + "."
                        + file.getName()
                                .substring(0, file.getName().length() - 6);

                classes.add(Class.forName(className));
            }
        }
    }

    public static void classesToString(
            List<Class<?>> classes,
            List<String> names) {

        for (Class<?> classe : classes) {
            names.add(classe.getSimpleName());
        }
    }
}