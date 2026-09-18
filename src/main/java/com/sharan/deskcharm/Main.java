package com.sharan.deskcharm;

import javafx.application.Application;

/**
 * Plain Java entry point that delegates to the JavaFX Application class.
 * Having a non-Application main class avoids classpath issues that can occur
 * when JavaFX modules are launched directly from a fat/shaded JAR, and is
 * required for the jpackage flow used later in the build.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(DeskCharmApplication.class, args);
    }
}