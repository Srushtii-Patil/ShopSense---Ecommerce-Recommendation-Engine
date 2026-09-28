package com.shopsense.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties props = new Properties();
    private static String activeMode = "UNKNOWN";
    private static String activeUrl = "";
    private static String activeUser = "";
    private static String activePassword = "";
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        File file = new File("db.properties");
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("[ShopSense] Warning: Failed to read db.properties: " + e.getMessage());
            }
        } else {
            // Default properties
            props.setProperty("db.mode", "auto");
            props.setProperty("mysql.driver", "com.mysql.cj.jdbc.Driver");
            props.setProperty("mysql.url", "jdbc:mysql://localhost:3306/shopsense?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            props.setProperty("mysql.user", "root");
            props.setProperty("mysql.password", "root");
            props.setProperty("h2.driver", "org.h2.Driver");
            props.setProperty("h2.url", "jdbc:h2:./data/shopsense;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
            props.setProperty("h2.user", "sa");
            props.setProperty("h2.password", "");
            props.setProperty("server.port", "8080");
        }
    }

    public static synchronized void initialize() {
        if (initialized) return;

        String mode = props.getProperty("db.mode", "auto").trim().toLowerCase();

        if ("mysql".equals(mode)) {
            if (tryConnectMySQL()) {
                activeMode = "MYSQL";
            } else {
                System.err.println("[ShopSense] ERROR: db.mode is set to 'mysql' but connection to MySQL failed.");
                System.err.println("[ShopSense] Falling back to embedded engine to allow application startup.");
                initEmbedded();
            }
        } else if ("embedded".equals(mode)) {
            initEmbedded();
        } else {
            // 'auto' mode: try MySQL first
            System.out.println("[ShopSense] Testing MySQL connection (auto mode)...");
            if (tryConnectMySQL()) {
                activeMode = "MYSQL";
                System.out.println("[ShopSense] -> Successfully connected to MySQL database!");
            } else {
                System.out.println("[ShopSense] -> MySQL connection unavailable or credentials invalid.");
                System.out.println("[ShopSense] -> Seamlessly launching embedded database (MySQL-compatible mode)...");
                initEmbedded();
            }
        }

        initialized = true;
        // Initialize schema and sample seed data
        try (Connection conn = getConnection()) {
            DatabaseInitializer.initializeDatabase(conn);
        } catch (SQLException e) {
            System.err.println("[ShopSense] Warning: " + e.getMessage());
        }
    }

    private static boolean tryConnectMySQL() {
        String driver = props.getProperty("mysql.driver", "com.mysql.cj.jdbc.Driver");
        String url = props.getProperty("mysql.url");
        String user = props.getProperty("mysql.user", "root");
        String pass = props.getProperty("mysql.password", "root");

        try {
            Class.forName(driver);
            DriverManager.setLoginTimeout(2); // fast 2-second timeout
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                if (conn != null && !conn.isClosed()) {
                    activeUrl = url;
                    activeUser = user;
                    activePassword = pass;
                    return true;
                }
            }
        } catch (Throwable t) {
            // Failed to connect to MySQL
        }
        return false;
    }

    private static void initEmbedded() {
        String driver = props.getProperty("h2.driver", "org.h2.Driver");
        String url = props.getProperty("h2.url", "jdbc:h2:./data/shopsense;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        String user = props.getProperty("h2.user", "sa");
        String pass = props.getProperty("h2.password", "");

        try {
            Class.forName(driver);
            activeUrl = url;
            activeUser = user;
            activePassword = pass;
            activeMode = "EMBEDDED (H2 MySQL-Mode)";
            System.out.println("[ShopSense] -> Embedded MySQL-compatible database initialized at: " + url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize embedded database: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        if (!initialized) {
            initialize();
        }
        try {
            return DriverManager.getConnection(activeUrl, activeUser, activePassword);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain database connection: " + e.getMessage(), e);
        }
    }

    public static boolean switchConnection(String mode, String url, String user, String pass) {
        try {
            if ("MYSQL".equalsIgnoreCase(mode)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.h2.Driver");
            }
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                if (conn != null && !conn.isClosed()) {
                    activeMode = mode.toUpperCase();
                    activeUrl = url;
                    activeUser = user;
                    activePassword = pass;
                    DatabaseInitializer.initializeDatabase(conn);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[ShopSense] Failed to switch database: " + e.getMessage());
        }
        return false;
    }

    public static String getActiveMode() { return activeMode; }
    public static String getActiveUrl() { return activeUrl; }
    public static String getActiveUser() { return activeUser; }
    public static int getServerPort() {
        try {
            return Integer.parseInt(props.getProperty("server.port", "8080"));
        } catch (Exception e) {
            return 8080;
        }
    }
}
