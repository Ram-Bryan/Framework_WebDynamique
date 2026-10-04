package mg.itu.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mg.itu.annotation.Controller;
import mg.itu.annotation.UrlMapping;
import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.model.ApplicationContext;

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

    public static Object getSpringWebApplicationContext(Object servletContext) {
        try {
            Class<?> contextClass = Class.forName("org.springframework.web.context.WebApplicationContext");
            Class<?> utilsClass = Class.forName("org.springframework.web.context.support.WebApplicationContextUtils");
            Object methodResult = utilsClass
                    .getMethod("getWebApplicationContext", Class.forName("jakarta.servlet.ServletContext"))
                    .invoke(null, servletContext);

            return contextClass.cast(methodResult);
        } catch (Exception e) {
            return null;
        }
    }

    public static void scanAndInstantiateBeans(
            String packageName,
            ApplicationContext appContext,
            Object springContext) {

        List<Class<?>> controllers = new ArrayList<>();
        getControllers(packageName, controllers);

        for (Class<?> controller : controllers) {
            try {
                Object instance = controller.getDeclaredConstructor().newInstance();

                if (springContext != null) {
                    try {
                        Object autowireCapableBeanFactory = springContext.getClass()
                                .getMethod("getAutowireCapableBeanFactory")
                                .invoke(springContext);
                        autowireCapableBeanFactory.getClass()
                                .getMethod("autowireBean", Object.class)
                                .invoke(autowireCapableBeanFactory, instance);
                    } catch (Exception ignored) {
                        // Spring may not be available or may not be configured in this context.
                    }
                }

                appContext.addBean(controller.getSimpleName(), instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate bean: " + controller.getName(), e);
            }
        }
    }

    public static Object[] resolveArguments(Method method, Map<String, String[]> parameterMap) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            String name = params[i].getName();
            String[] values = parameterMap.get(name);
            String rawValue = (values != null && values.length > 0) ? values[0] : null;
            args[i] = convert(rawValue, params[i].getType());
        }
        return args;
    }

    public static Object convert(String rawValue, Class<?> targetType) {
        if (rawValue == null) {
            if (targetType == String.class) {
                return null;
            }
            if (targetType == Integer.class || targetType == int.class) {
                return targetType == int.class ? 0 : null;
            }
            if (targetType == Long.class || targetType == long.class) {
                return targetType == long.class ? 0L : null;
            }
            if (targetType == Double.class || targetType == double.class) {
                return targetType == double.class ? 0.0d : null;
            }
            if (targetType == Float.class || targetType == float.class) {
                return targetType == float.class ? 0.0f : null;
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return targetType == boolean.class ? false : null;
            }
            if (targetType == Short.class || targetType == short.class) {
                return targetType == short.class ? (short) 0 : null;
            }
            if (targetType == Byte.class || targetType == byte.class) {
                return targetType == byte.class ? (byte) 0 : null;
            }
            if (targetType == Character.class || targetType == char.class) {
                return targetType == char.class ? '\u0000' : null;
            }
            if (targetType == BigDecimal.class) {
                return null;
            }
            if (targetType == LocalDate.class) {
                return null;
            }
            if (targetType == LocalDateTime.class) {
                return null;
            }
            return null;
        }

        if (targetType == String.class) {
            return rawValue;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.valueOf(rawValue);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.valueOf(rawValue);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.valueOf(rawValue);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.valueOf(rawValue);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(rawValue);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.valueOf(rawValue);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.valueOf(rawValue);
        }
        if (targetType == char.class || targetType == Character.class) {
            return rawValue.charAt(0);
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(rawValue);
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(rawValue, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(rawValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}