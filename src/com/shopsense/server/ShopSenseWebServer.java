package com.shopsense.server;

import com.shopsense.config.DatabaseConfig;
import com.shopsense.model.*;
import com.shopsense.service.ShopSenseService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ShopSenseWebServer {
    private final ShopSenseService service = new ShopSenseService();
    private HttpServer server;
    private int port;

    public ShopSenseWebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            // If port is busy, try port + 1
            port = port + 1;
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }

        // Static files handler
        server.createContext("/", new StaticFileHandler());

        // API Endpoints
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/user/budget", new UpdateBudgetHandler());
        server.createContext("/api/user/preferences", new UpdatePreferencesHandler());
        server.createContext("/api/products", new ProductsHandler());
        server.createContext("/api/recommendations", new RecommendationsHandler());
        server.createContext("/api/cart", new CartHandler());
        server.createContext("/api/cart/add", new AddToCartHandler());
        server.createContext("/api/cart/update", new UpdateCartHandler());
        server.createContext("/api/cart/remove", new RemoveFromCartHandler());
        server.createContext("/api/cart/clear", new ClearCartHandler());
        server.createContext("/api/cart/suggestions", new CartSuggestionsHandler());
        server.createContext("/api/checkout", new CheckoutHandler());
        server.createContext("/api/orders", new OrdersHandler());
        server.createContext("/api/db-status", new DbStatusHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("[ShopSense] ===================================================");
        System.out.println("[ShopSense] Web Application running at: http://localhost:" + port);
        System.out.println("[ShopSense] Database Engine: " + DatabaseConfig.getActiveMode());
        System.out.println("[ShopSense] ===================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }

    // ==================== HTTP HANDLERS ====================

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("web/index.html");
            }

            if (!file.exists()) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) {
                contentType = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript; charset=UTF-8";
            } else if (path.endsWith(".json")) {
                contentType = "application/json; charset=UTF-8";
            } else if (path.endsWith(".png")) {
                contentType = "image/png";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (path.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            List<User> users = service.getAllUsers();
            String json = JsonHelper.usersToJson(users);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    private class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(params.getOrDefault("id", "1"));
            User user = service.getUser(userId);

            if (user != null) {
                sendResponse(exchange, 200, "application/json; charset=UTF-8", JsonHelper.userToJson(user));
            } else {
                sendResponse(exchange, 404, "application/json; charset=UTF-8", "{\"error\":\"User not found\"}");
            }
        }
    }

    private class UpdateBudgetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int userId = Integer.parseInt(body.getOrDefault("userId", "1"));
            double budget = Double.parseDouble(body.getOrDefault("budget", "500"));

            boolean success = service.updateUserBudget(userId, budget);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + ",\"budget\":" + budget + "}");
        }
    }

    private class UpdatePreferencesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int userId = Integer.parseInt(body.getOrDefault("userId", "1"));
            String categories = body.getOrDefault("categories", "");
            String brands = body.getOrDefault("brands", "");
            String sensitivity = body.getOrDefault("priceSensitivity", "BALANCED");

            boolean success = service.updateUserPreferences(userId, categories, brands, sensitivity);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + "}");
        }
    }

    private class ProductsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            List<Product> products = service.getAllProducts();
            String json = JsonHelper.productsToJson(products);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    private class RecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(params.getOrDefault("userId", "1"));
            int limit = Integer.parseInt(params.getOrDefault("limit", "10"));

            List<Recommendation> recs = service.getPersonalizedRecommendations(userId, limit);
            String json = JsonHelper.recommendationsToJson(recs);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    private class CartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(params.getOrDefault("userId", "1"));

            List<CartItem> items = service.getUserCart(userId);
            double total = service.getCartTotal(userId);
            User user = service.getUser(userId);
            double remainingBudget = user != null ? Math.max(0.0, user.getBudget() - total) : 0.0;

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"userId\":").append(userId).append(",");
            sb.append("\"total\":").append(total).append(",");
            sb.append("\"remainingBudget\":").append(remainingBudget).append(",");
            sb.append("\"userBudget\":").append(user != null ? user.getBudget() : 0.0).append(",");
            sb.append("\"items\":").append(JsonHelper.cartItemsToJson(items));
            sb.append("}");

            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString());
        }
    }

    private class AddToCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int userId = Integer.parseInt(body.getOrDefault("userId", "1"));
            int productId = Integer.parseInt(body.getOrDefault("productId", "0"));
            int quantity = Integer.parseInt(body.getOrDefault("quantity", "1"));

            boolean success = service.addToCart(userId, productId, quantity);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + "}");
        }
    }

    private class UpdateCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int cartItemId = Integer.parseInt(body.getOrDefault("cartItemId", "0"));
            int quantity = Integer.parseInt(body.getOrDefault("quantity", "1"));

            boolean success = service.updateCartQuantity(cartItemId, quantity);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + "}");
        }
    }

    private class RemoveFromCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int cartItemId = Integer.parseInt(body.getOrDefault("cartItemId", "0"));

            boolean success = service.removeFromCart(cartItemId);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + "}");
        }
    }

    private class ClearCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int userId = Integer.parseInt(body.getOrDefault("userId", "1"));

            boolean success = service.clearCart(userId);
            sendResponse(exchange, 200, "application/json", "{\"success\":" + success + "}");
        }
    }

    private class CartSuggestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(params.getOrDefault("userId", "1"));

            List<CartSuggestion> suggestions = service.getDynamicCartSuggestions(userId);
            String json = JsonHelper.cartSuggestionsToJson(suggestions);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    private class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> body = parseFormData(exchange);
            int userId = Integer.parseInt(body.getOrDefault("userId", "1"));

            Order order = service.checkout(userId);
            if (order != null) {
                sendResponse(exchange, 200, "application/json; charset=UTF-8",
                        "{\"success\":true,\"orderId\":" + order.getOrderId() + ",\"total\":" + order.getTotalAmount() + "}");
            } else {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"error\":\"Cart is empty or checkout failed\"}");
            }
        }
    }

    private class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(params.getOrDefault("userId", "1"));

            List<Order> orders = service.getUserOrders(userId);
            String json = JsonHelper.ordersToJson(orders);
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    private class DbStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            String json = "{" +
                    "\"mode\":\"" + JsonHelper.escape(DatabaseConfig.getActiveMode()) + "\"," +
                    "\"url\":\"" + JsonHelper.escape(DatabaseConfig.getActiveUrl()) + "\"," +
                    "\"user\":\"" + JsonHelper.escape(DatabaseConfig.getActiveUser()) + "\"," +
                    "\"serverPort\":" + port +
                    "}";
            sendResponse(exchange, 200, "application/json; charset=UTF-8", json);
        }
    }

    // ==================== HELPER METHODS ====================

    private void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    map.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } else if (idx < 0) {
                    map.put(URLDecoder.decode(pair, "UTF-8"), "");
                }
            } catch (Exception e) {}
        }
        return map;
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        // Read body
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        String body = new String(baos.toByteArray(), StandardCharsets.UTF_8).trim();

        // Check if JSON body or form-urlencoded
        if (body.startsWith("{") && body.endsWith("}")) {
            // Simple naive JSON key-value extraction for string and numeric values
            String stripped = body.substring(1, body.length() - 1);
            String[] tokens = stripped.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String token : tokens) {
                int colon = token.indexOf(":");
                if (colon > 0) {
                    String k = token.substring(0, colon).trim().replaceAll("^\"|\"$", "");
                    String v = token.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
                    map.put(k, v);
                }
            }
        } else if (!body.isEmpty()) {
            map.putAll(parseQueryParams(body));
        }

        // Also merge query parameters if present
        if (exchange.getRequestURI().getQuery() != null) {
            map.putAll(parseQueryParams(exchange.getRequestURI().getQuery()));
        }

        return map;
    }
}
