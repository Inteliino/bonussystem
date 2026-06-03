package lv.janis.bonussystem;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        if (uri.equals("/login")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("loggedUser") != null) {
            return true;
        }

        response.sendRedirect("/login");
        return false;
    }
}