package com.greenaura.controller;

import com.greenaura.model.OrderHistoryItem;
import com.greenaura.model.User;
import com.greenaura.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OrdersController {
    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public ApiResponse<OrdersResponse> getOrders(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new OrdersResponse());
        }

        List<OrderHistoryItem> orders = orderService.getOrdersByUserId(loggedInUser.getId());
        int total = orders.size();
        int received = 0;
        int pending = 0;
        int cancelled = 0;

        for (OrderHistoryItem order : orders) {
            String status = String.valueOf(order.getStatus());
            if ("RECEIVED".equalsIgnoreCase(status)) {
                received += 1;
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                cancelled += 1;
            } else {
                pending += 1;
            }
        }

        OrdersResponse data = new OrdersResponse(orders, total, received, pending, cancelled);
        return new ApiResponse<>("success", "Orders loaded", data);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<OrdersResponse> cancelOrder(@PathVariable int orderId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new OrdersResponse());
        }

        boolean cancelled = orderService.cancelOrderIfAllowed(loggedInUser.getId(), orderId);
        if (!cancelled) {
            return new ApiResponse<>("error", "Order cannot be cancelled. Cancellation is allowed for pending orders within 30 minutes.", new OrdersResponse());
        }

        return getOrders(session);
    }

    @GetMapping("/staff/orders")
    public ApiResponse<OrdersResponse> getAllOrdersForStaff(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new OrdersResponse());
        }
        if (!isStaffUser(loggedInUser)) {
            return new ApiResponse<>("error", "Staff access required", new OrdersResponse());
        }
        return buildOrdersResponse(orderService.getAllOrders(), "Staff orders loaded");
    }

    @PostMapping("/staff/orders/{orderId}/received")
    public ApiResponse<OrdersResponse> markOrderReceived(@PathVariable int orderId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return new ApiResponse<>("error", "Not authenticated", new OrdersResponse());
        }
        if (!isStaffUser(loggedInUser)) {
            return new ApiResponse<>("error", "Staff access required", new OrdersResponse());
        }

        boolean updated = orderService.markOrderReceived(orderId);
        if (!updated) {
            return new ApiResponse<>("error", "Order cannot be marked as received. Only pending orders can be updated.", new OrdersResponse());
        }

        return buildOrdersResponse(orderService.getAllOrders(), "Order marked as received");
    }

    private ApiResponse<OrdersResponse> buildOrdersResponse(List<OrderHistoryItem> orders, String message) {
        int total = orders.size();
        int received = 0;
        int pending = 0;
        int cancelled = 0;

        for (OrderHistoryItem order : orders) {
            String status = String.valueOf(order.getStatus());
            if ("RECEIVED".equalsIgnoreCase(status)) {
                received += 1;
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                cancelled += 1;
            } else {
                pending += 1;
            }
        }

        OrdersResponse data = new OrdersResponse(orders, total, received, pending, cancelled);
        return new ApiResponse<>("success", message, data);
    }

    private boolean isStaffUser(User user) {
        if (user == null || user.getEmail() == null) {
            return false;
        }
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        String env = System.getenv("GREENAURA_STAFF_EMAILS");

        Set<String> staffEmails = new LinkedHashSet<>();
        if (env != null && !env.trim().isEmpty()) {
            staffEmails.addAll(Arrays.stream(env.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toSet()));
        }

        if (staffEmails.isEmpty()) {
            staffEmails.add("admin@greenaura.com");
            staffEmails.add("staff@greenaura.com");
        }

        return staffEmails.contains(normalizedEmail);
    }
}
