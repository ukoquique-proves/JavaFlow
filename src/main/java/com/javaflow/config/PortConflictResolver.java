package com.javaflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.PortInUseException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;

/**
 * Resolves port conflicts by gracefully shutting down existing instances.
 * 
 * <p>This component checks if the configured port is in use before starting the application.
 * If a port conflict is detected, it attempts to gracefully shutdown the existing instance
 * via the Spring Boot Actuator shutdown endpoint.</p>
 * 
 * <p><strong>IMPORTANT:</strong> This feature is ONLY enabled in development profiles (dev, local).
 * In production environments, port conflicts should fail fast to allow orchestration systems
 * (like Kubernetes) to handle the issue properly.</p>
 * 
 * <p>This approach is superior to forceful process killing because:</p>
 * <ul>
 *   <li>Cross-platform (works on Windows, Linux, macOS)</li>
 *   <li>Graceful shutdown allows proper resource cleanup</li>
 *   <li>No race conditions or orphaned resources</li>
 *   <li>Respects application lifecycle hooks</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
public class PortConflictResolver implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final int MAX_SHUTDOWN_WAIT_SECONDS = 10;
    private static final int SHUTDOWN_CHECK_INTERVAL_MS = 500;
    private static final String[] DEVELOPMENT_PROFILES = {"dev", "local"};

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();
        
        // Only enable port conflict resolution in development profiles
        if (!isDevelopmentProfile(env)) {
            log.debug("Port conflict resolution disabled in production profile");
            return;
        }
        
        String portStr = env.getProperty("server.port", "8080");
        int port = Integer.parseInt(portStr);
        
        if (isPortInUse(port)) {
            log.warn("Port {} is already in use. Attempting graceful shutdown of existing instance...", port);
            
            if (shutdownExistingInstance(port)) {
                log.info("Successfully shut down existing instance on port {}", port);
            } else {
                log.error("Failed to gracefully shutdown existing instance on port {}. " +
                         "Please manually stop the running application or use a different port.", port);
                throw new PortInUseException(port);
            }
        }
    }

    /**
     * Checks if a port is currently in use.
     *
     * @param port The port to check
     * @return true if the port is in use, false otherwise
     */
    private boolean isPortInUse(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Attempts to gracefully shutdown an existing application instance via Actuator.
     *
     * @param port The port where the existing instance is running
     * @return true if shutdown was successful, false otherwise
     */
    private boolean shutdownExistingInstance(int port) {
        try {
            // Try to trigger shutdown via actuator endpoint
            String shutdownUrl = String.format("http://localhost:%d/actuator/shutdown", port);
            
            log.info("Sending shutdown request to existing instance at {}", shutdownUrl);
            
            HttpURLConnection connection = (HttpURLConnection) new URL(shutdownUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            if (responseCode == 200 || responseCode == 404) {
                // 200 = shutdown successful
                // 404 = actuator not enabled, but we can still wait for port to free
                return waitForPortToFree(port);
            }
            
            log.warn("Shutdown endpoint returned unexpected code: {}", responseCode);
            return false;
            
        } catch (IOException e) {
            log.warn("Could not connect to actuator shutdown endpoint: {}. " +
                    "The existing instance may not have actuator enabled.", e.getMessage());
            
            // If actuator is not available, we cannot gracefully shutdown
            // Log instructions for manual intervention
            log.error("Please manually stop the existing application instance on port {} or enable actuator shutdown endpoint", port);
            return false;
        }
    }

    /**
     * Waits for a port to become available after shutdown request.
     *
     * @param port The port to monitor
     * @return true if port became available, false if timeout occurred
     */
    private boolean waitForPortToFree(int port) {
        int attempts = (MAX_SHUTDOWN_WAIT_SECONDS * 1000) / SHUTDOWN_CHECK_INTERVAL_MS;
        
        for (int i = 0; i < attempts; i++) {
            if (!isPortInUse(port)) {
                log.info("Port {} is now available", port);
                return true;
            }
            
            try {
                Thread.sleep(SHUTDOWN_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        log.error("Timeout waiting for port {} to become available after {} seconds", 
                 port, MAX_SHUTDOWN_WAIT_SECONDS);
        return false;
    }
    
    /**
     * Checks if the application is running in a development profile.
     *
     * @param env The Spring environment
     * @return true if running in dev or local profile, false otherwise
     */
    private boolean isDevelopmentProfile(Environment env) {
        String[] activeProfiles = env.getActiveProfiles();
        
        if (activeProfiles.length == 0) {
            // No profile specified, assume development
            log.debug("No active profile specified, enabling port conflict resolution");
            return true;
        }
        
        boolean isDev = Arrays.stream(activeProfiles)
                .anyMatch(profile -> Arrays.asList(DEVELOPMENT_PROFILES).contains(profile));
        
        if (isDev) {
            log.info("Development profile detected ({}), port conflict resolution enabled", 
                    String.join(", ", activeProfiles));
        } else {
            log.info("Production profile detected ({}), port conflict resolution disabled for safety", 
                    String.join(", ", activeProfiles));
        }
        
        return isDev;
    }
}
