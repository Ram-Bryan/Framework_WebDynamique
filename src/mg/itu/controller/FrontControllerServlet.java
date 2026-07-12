package mg.itu.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.itu.model.ModelView;
import mg.itu.model.UrlMappingModel;
import mg.itu.model.UrlMethod;
import mg.itu.utils.Utils;

public class FrontControllerServlet extends HttpServlet {

        private void processRequest(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {

                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                String urlMain = request.getRequestURL().toString();
                String contextPath = request.getContextPath();
                String url = request.getRequestURI().substring(contextPath.length());

                Map<UrlMethod, UrlMappingModel> routes = (Map<UrlMethod, UrlMappingModel>) getServletContext()
                                .getAttribute("routes");
                String viewPrefix = (String) getServletContext().getAttribute("view-prefix");
                String viewSuffix = (String) getServletContext().getAttribute("view-suffix");

                out.println("<h2>FrontController servlet</h2>");
                out.println("<p><strong>Current URL:</strong> " + urlMain + "</p>");

                String reqMethod = request.getMethod();
                UrlMethod urlMethod = new UrlMethod(url, reqMethod);

                if (routes.containsKey(urlMethod)) {

                        try {
                                UrlMappingModel mapping = routes.get(urlMethod);
                                Object controller = mapping.getController()
                                                .getDeclaredConstructor()
                                                .newInstance();
                                Object result = mapping.getMethod()
                                                .invoke(controller);

                                if (result instanceof ModelView) {

                                        ModelView mv = (ModelView) result;

                                        for (Map.Entry<String, Object> e : mv.getData().entrySet()) {
                                                request.setAttribute(e.getKey(), e.getValue());
                                        }

                                        String view = viewPrefix + mv.getUrl() + viewSuffix;

                                        request.getRequestDispatcher(view)
                                                        .forward(request, response);

                                        return;
                                }

                        } catch (Exception e) {
                                throw new ServletException(e);
                        }

                } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                        "No route found for " + url);
                        return;
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