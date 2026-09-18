package com.sharan.deskcharm;

import com.sharan.deskcharm.app.AppEnvironment;
import javafx.application.Application;
import javafx.stage.Stage;

public final class DeskCharmApplication extends Application {
    private AppEnvironment environment;

    @Override
    public void start(Stage primaryStage) {
        environment = new AppEnvironment();
        environment.start();
    }

    @Override
    public void stop() {
        if (environment != null) {
            environment.stop();
        }
    }
}
