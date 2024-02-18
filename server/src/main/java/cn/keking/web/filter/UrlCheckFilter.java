package cn.keking.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @date 2023/11/30
 */
public class UrlCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String servletPath = httpServletRequest.getServletPath();

        boolean redirect = false;

        // servletPath 中不能包含 //
        if (servletPath.contains("//")) {
            servletPath = servletPath.replaceAll("//+", "/");
            redirect = true;
        }

        // 不能以 / 结尾，同时考虑 **首页** 的特殊性
        if (servletPath.endsWith("/") && servletPath.length() > 1) {
            servletPath = servletPath.substring(0, servletPath.length() - 1);
            redirect = true;
        }
        if (redirect) {
            ((HttpServletResponse) response).sendRedirect(httpServletRequest.getContextPath() + servletPath + "?" + httpServletRequest.getQueryString());
        } else {
            chain.doFilter(request, response);
        }
    }
}
