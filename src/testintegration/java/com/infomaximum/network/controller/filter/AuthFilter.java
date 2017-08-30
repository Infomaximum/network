package com.infomaximum.network.controller.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by kris on 12.03.15.
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String auth = request.getParameter("auth");
        if(!"xxx".equals(auth)) {
            //Не пришел "секретный" код доступа
            response.sendError(401);
        } else {
            //Все хорошо - запрос прокидываем дальше
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {}
}
