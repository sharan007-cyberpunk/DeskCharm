package com.sharan.deskcharm;

import com.sharan.deskcharm.app.AppEnvironment;
import com.sharan.deskcharm.windows.SystemTrayManager;
import com.sharan.deskcharm.windows.WindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Builds exactly one {@link AppEnvironment}
 * (settings + charm library + physics), hands it to a {@link WindowManager}
 * that owns the overlay's lifecycle, and starts the system tray if the
 * platform supports it.
 */
public class DeskCharmApplication extends Application {

    private AppEnvironment environment;
    private WindowManager windowManager;
    private SystemTrayManager trayManager;

    @Override
    public void init() {
        // Keep the JavaFX runtime alive after the (invisible) primary stage
        // closes, since the real UI lives in windows WindowManager creates.
        Platform.setImplicitExit(false);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            environment = new AppEnvironment();
            windowManager = new WindowManager(environment);
            windowManager.launchOverlay();

            trayManager = new SystemTrayManager(environment, windowManager, this::shutdown);
            trayManager.installIfSupported();
        } catch (Exception e) {
            System.err.println("DeskCharm failed to start: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void shutdown() {
        if (windowManager != null) {
            windowManager.shutdown();
        }
        if (trayManager != null) {
            trayManager.uninstall();
        }
        if (environment != null) {
            environment.persistSettings();
        }
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void stop() {
        if (windowManager != null) {
            windowManager.shutdown();
        }
    }
}
