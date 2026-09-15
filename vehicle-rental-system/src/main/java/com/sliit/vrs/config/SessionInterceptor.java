package com.sliit.vrs.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

// Shared / minor function: simple RBAC gatekeeper. Every request that is
// not the login/register/home page must have a logged-in user in session,
// otherwise we redirect back to /login. This is the "security checkpoint"
// mentioned in the proposal's Minor Functions section.
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("loggedInUser") != null);
        if (!loggedIn) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
