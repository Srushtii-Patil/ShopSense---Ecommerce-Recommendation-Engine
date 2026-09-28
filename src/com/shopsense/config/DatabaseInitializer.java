package com.shopsense.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    public static void initializeDatabase(Connection conn) {
        try {
            if (!isDatabaseInitialized(conn)) {
                System.out.println("[ShopSense] Initializing database schema...");
                executeSqlScript(conn, new File("sql/schema.sql"));

                System.out.println("[ShopSense] Populating sample e-commerce catalog and seed data...");
                executeSqlScript(conn, new File("sql/sample_data.sql"));
                System.out.println("[ShopSense] Database setup completed successfully.");
            } else {
                System.out.println("[ShopSense] Database schema and tables already exist.");
            }
        } catch (Exception e) {
            System.err.println("[ShopSense] Warning during database initialization: " + e.getMessage());
        }
    }

    private static boolean isDatabaseInitialized(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Check if products table exists and has rows
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            return false; // Table doesn't exist
        }
        return false;
    }

    public static void executeSqlScript(Connection conn, File scriptFile) throws Exception {
        if (!scriptFile.exists()) {
            System.err.println("[ShopSense] Script file not found: " + scriptFile.getAbsolutePath());
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // SQL standard comments start with --
                if (line.startsWith("--")) {
                    continue;
                }
                int commentIdx = line.indexOf("--");
                if (commentIdx > 0 && !line.contains("://")) {
                    line = line.substring(0, commentIdx).trim();
                }
                if (line.isEmpty()) {
                    continue;
                }
                sb.append(line).append(" ");
            }
        }

        String fullSql = sb.toString();
        List<String> statements = splitSqlStatements(fullSql);

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        System.err.println("[DatabaseInitializer] Statement warning/error: " + e.getMessage());
                        System.err.println("   Query: " + (trimmed.length() > 80 ? trimmed.substring(0, 80) + "..." : trimmed));
                    }
                }
            }
        }
    }

    private static List<String> splitSqlStatements(String sql) {
        List<String> list = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if ((c == '\'' || c == '"') && (i == 0 || sql.charAt(i - 1) != '\\')) {
                if (!inQuote) {
                    inQuote = true;
                    quoteChar = c;
                } else if (quoteChar == c) {
                    inQuote = false;
                }
            }

            if (c == ';' && !inQuote) {
                list.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0 && !current.toString().trim().isEmpty()) {
            list.add(current.toString());
        }
        return list;
    }
}
