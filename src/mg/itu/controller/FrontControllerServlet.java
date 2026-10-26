package mg.itu.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import mg.itu.annotation.Auth;
import mg.itu.annotation.WebAPI;
import mg.itu.model.MapFile;
import mg.itu.model.ModelView;
import mg.itu.model.MySession;
import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.model.ApplicationContext;
import mg.itu.utils.Utils;
import mg.itu.config.*;

@MultipartConfig
public class FrontControllerServlet extends HttpServlet {

        private int checkAuth(HttpServletRequest request, java.lang.reflect.Method method) throws ServletException {
                Auth auth = method.getAnnotation(Auth.class);
                if (auth == null) {
                        return HttpServletResponse.SC_OK;
                }

                String authKey = (String) getServletContext().getAttribute("auth-session-key");
                if (authKey == null || authKey.isBlank()) {
                        throw new ServletException("@Auth utilisé mais 'auth-session-key' n'est pas configuré");
                }

                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute(authKey) == null) {
                        return HttpServletResponse.SC_UNAUTHORIZED;
                }

                String[] allowedRoles = auth.roles();
                if (allowedRoles == null || allowedRoles.length == 0) {
                        return HttpServletResponse.SC_OK;
                }

                String roleKey = (String) getServletContext().getAttribute("role-session-key");
                if (roleKey == null || roleKey.isBlank()) {
                        throw new ServletException("@Auth(roles) utilisé mais 'role-session-key' n'est pas configuré");
                }

                Object userRole = session.getAttribute(roleKey);
                if (userRole == null) {
                        return HttpServletResponse.SC_FORBIDDEN;
                }

                for (String role : allowedRoles) {
                        if (role.equals(userRole.toString())) {
                                return HttpServletResponse.SC_OK;
                        }
                }
                return HttpServletResponse.SC_FORBIDDEN;
        }

        private boolean isAuthenticated(HttpServletRequest request) throws ServletException {
                String key = (String) getServletContext().getAttribute("auth-session-key");
                if (key == null || key.isBlank()) {
                        throw new ServletException("@Auth utilisé mais 'auth-session-key' n'est pas configuré dans web.xml");
                }
                HttpSession session = request.getSession(false);
                return session != null && session.getAttribute(key) != null;
        }

        private static Map<String, List<MapFile>> extractFileMap(HttpServletRequest request) throws Exception {
                Map<String, List<MapFile>> files = new HashMap<>();

                String contentType = request.getContentType();
                if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
                        return files;
                }

                for (Part part : request.getParts()) {
                        String submittedFileName = part.getSubmittedFileName();
                        if (submittedFileName == null || submittedFileName.isEmpty()) {
                                continue;
                        }

                        if (part.getSize() > FrameworkConfig.getMaxFileSize()) {
                                throw new ServletException("Fichier trop volumineux: " + submittedFileName);
                        }

                        String extension = Utils.extractExtension(submittedFileName);
                        byte[] contenu = part.getInputStream().readAllBytes();
                        MapFile mapFile = new MapFile(submittedFileName, extension, contenu);

                        if (!Utils.checkProperties(mapFile)) {
                                throw new ServletException("Fichier trop volumineux: " + submittedFileName);
                        }

                        String fieldName = part.getName();
                        List<MapFile> fieldFiles = files.get(fieldName);
                        if (fieldFiles == null) {
                                fieldFiles = new ArrayList<>();
                                files.put(fieldName, fieldFiles);
                        }
                        fieldFiles.add(mapFile);
                }

                return files;
        }

        private void processRequest(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {

                request.setCharacterEncoding("UTF-8");
                response.setCharacterEncoding("UTF-8");
                String contextPath = request.getContextPath();
                String url = request.getRequestURI().substring(contextPath.length());

                Map<UrlMethod, UrlMappingModel> routes = (Map<UrlMethod, UrlMappingModel>) getServletContext()
                                .getAttribute("routes");
                String viewPrefix = (String) getServletContext().getAttribute("view-prefix");
                String viewSuffix = (String) getServletContext().getAttribute("view-suffix");
                ApplicationContext appContext = (ApplicationContext) getServletContext()
                                .getAttribute("applicationContext");

                String reqMethod = request.getMethod();
                UrlMethod urlMethod = new UrlMethod(url, reqMethod);

                if (routes.containsKey(urlMethod)) {

                        try {
                                UrlMappingModel mapping = routes.get(urlMethod);

                                int status = checkAuth(request, mapping.getMethod());
                                if (status != HttpServletResponse.SC_OK) {
                                        response.sendError(status, status == HttpServletResponse.SC_UNAUTHORIZED
                                                        ? "Authentification requise"
                                                        : "Accès refusé : rôle insuffisant");
                                        return;
                                }

                                String controllerName = mapping.getController().getSimpleName();
                                Object controller = appContext.getBean(controllerName);

                                if (controller == null) {
                                        throw new ServletException(
                                                        "Aucun bean trouvé pour le contrôleur: " + controllerName);
                                }

                                Map<String, String[]> parameterMap = request.getParameterMap();
                                Map<String, List<MapFile>> fileMap = extractFileMap(request);
                                HttpSession httpSession = null;
                                MySession mySession = null;

                                boolean besoinSession = Utils.besoinSession(mapping);
                                if (besoinSession) {
                                        httpSession = request.getSession(true);
                                        mySession = buildSession(httpSession);
                                }

                                Object[] args = Utils.resolveArguments(mapping.getMethod(), parameterMap, fileMap, mySession);
                                Object result = mapping.getMethod().invoke(controller, args);

                                if (besoinSession) {
                                        syncSession(httpSession, mySession);
                                }

                                if (mapping.getMethod().isAnnotationPresent(WebAPI.class)) {
                                        response.setContentType("application/json;charset=UTF-8");
                                        PrintWriter out = response.getWriter();

                                        if (result instanceof String) {
                                                out.print((String) result);
                                        } else {
                                                ObjectMapper mapper = new ObjectMapper();
                                                out.print(mapper.writeValueAsString(result));
                                        }
                                        return;
                                }

                                if (result instanceof ModelView) {

                                        ModelView mv = (ModelView) result;

                                        for (Map.Entry<String, Object> e : mv.getData().entrySet()) {
                                                request.setAttribute(e.getKey(), e.getValue());
                                        }

                                        String view = viewPrefix + mv.getUrl() + viewSuffix;

                                        request.getRequestDispatcher(view)
                                                        .forward(request, response);

                                        return;

                                } else if (result == null) {
                                        return;
                                } else {
                                        response.setContentType("text/plain;charset=UTF-8");
                                        PrintWriter out = response.getWriter();
                                        out.println(result.toString());
                                        return;
                                }

                        } catch (InvocationTargetException e) {
                                throw new ServletException(e.getCause());
                        } catch (Exception e) {
                                throw new ServletException(e);
                        }

                } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                        "No route found for " + url);
                        return;
                }
        }

        private MySession buildSession(HttpSession httpSession) {
                Map<String, Object> values = new HashMap<>();

                Enumeration<String> attributeNames = httpSession.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                        String attributeName = attributeNames.nextElement();
                        values.put(attributeName, httpSession.getAttribute(attributeName));
                }

                return new MySession(values);
        }

        private void syncSession(HttpSession httpSession, MySession mySession) {
                Map<String, Object> current = mySession.getAllSessions();

                for (String name : Collections.list(httpSession.getAttributeNames())) {
                        if (!current.containsKey(name)) {
                                httpSession.removeAttribute(name);
                        }
                }

                for (Map.Entry<String, Object> entry : current.entrySet()) {
                        httpSession.setAttribute(entry.getKey(), entry.getValue());
                }
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
                processRequest(request, response);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
                processRequest(request, response);
        }
}