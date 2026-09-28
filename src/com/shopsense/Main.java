package com.shopsense;

import com.shopsense.cli.ShopSenseCLI;
import com.shopsense.config.DatabaseConfig;
import com.shopsense.server.ShopSenseWebServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   SHOPSENSE :: Personalized E-Commerce Recommendation Engine     ");
        System.out.println("       Powered by Java, JDBC & MySQL / Hybrid Database Engine     ");
        System.out.println("==================================================================");

        // 1. Initialize Database
        try {
            DatabaseConfig.initialize();
        } catch (Exception e) {
            System.err.println("[ShopSense] Fatal error during database startup: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        boolean cliOnly = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg)) {
                cliOnly = true;
                break;
            }
        }

        // 2. Start Web Server
        ShopSenseWebServer webServer = null;
        if (!cliOnly) {
            try {
                int port = DatabaseConfig.getServerPort();
                webServer = new ShopSenseWebServer(port);
                webServer.start();
            } catch (Exception e) {
                System.err.println("[ShopSense] Warning: Unable to start Web Server: " + e.getMessage());
            }
        }

        // 3. Launch CLI
        if (cliOnly || System.console() != null || (args.length > 0 && "--interactive".equalsIgnoreCase(args[0]))) {
            ShopSenseCLI cli = new ShopSenseCLI();
            cli.run();
        } else {
            // Keep server alive if started without console
            System.out.println("[ShopSense] Web Application is live at: http://localhost:" + (webServer != null ? webServer.getPort() : 8080));
            System.out.println("[ShopSense] Press Ctrl+C in console to stop.");
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                System.out.println("[ShopSense] Server shutting down.");
            }
        }
    }
}
