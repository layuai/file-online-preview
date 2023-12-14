package cn.keking.web.filter;


import cn.keking.web.controller.OnlinePreviewController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @date 2023/11/30
 */
public class UrlCheckFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(OnlinePreviewController.class);

    private String illegalRequest;

    @Override
    public void init(FilterConfig filterConfig) {
        ClassPathResource classPathResource = new ClassPathResource("web/illegalRequest.html");
        try {
            classPathResource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            this.illegalRequest = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();

        if(requestURI.contains("//") || requestURI.endsWith("/")) {
            String html = this.illegalRequest.replace("${request_path}", requestURI);
            response.getWriter().write(html);
            response.getWriter().close();
        }else {
            chain.doFilter(request, response);
        }
    }
}
