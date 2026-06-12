package com.pachasuite.api.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;

@Component
public class CachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest  httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        if (httpReq.getMethod().equals("POST") &&
                httpReq.getRequestURI().contains("/api/reservas")) {

            ContentCachingResponseWrapper wrapper =
                    new ContentCachingResponseWrapper(httpRes);
            chain.doFilter(req, wrapper);
            wrapper.copyBodyToResponse();
        } else {
            chain.doFilter(req, res);
        }
    }
}