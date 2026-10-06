package com.sliit.vrs.config;

import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

// Shared / minor function: simple RBAC (Role-Based Access Control) gatekeeper.
//
// Step 1 - Authentication: every protected request must have a logged-in
// user in session, otherwise we redirect to /login.
//
// Step 2 - Authorization: staff-only areas (fleet, bookings management,
// returns, maintenance, driver allocation, the admin panel) are blocked
// for CUSTOMER accounts, and the customer-only areas (my bookings, profile,
// checkout) are blocked for staff accounts. Each is redirected to the
// dashboard that *is* meant for their role, with a friendly explanation,
// rather than a raw 403 page.
public class SessionInterceptor implements HandlerInterceptor {

    private static final String[] STAFF_ONLY_PREFIXES = {
            "/admin", "/vehicles", "/reservations", "/returns", "/maintenance", "/drivers"
    };
    private static final String[] CUSTOMER_ONLY_PREFIXES = {
            "/my-bookings", "/profile", "/book"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        User loggedInUser = session != null ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        String path = request.getRequestURI();
        boolean isCustomer = loggedInUser.getRole() == Role.CUSTOMER;

        if (isCustomer && startsWithAny(path, STAFF_ONLY_PREFIXES)) {
            response.sendRedirect("/dashboard?denied=staffArea");
            return false;
        }
        if (!isCustomer && startsWithAny(path, CUSTOMER_ONLY_PREFIXES)) {
            response.sendRedirect("/admin?denied=customerArea");
            return false;
        }

        return true;
    }

    private boolean startsWithAny(String path, String[] prefixes) {
        for (String prefix : prefixes) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
