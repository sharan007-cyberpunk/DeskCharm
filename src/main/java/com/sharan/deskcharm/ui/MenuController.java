package com.sharan.deskcharm.ui;

import com.sharan.deskcharm.app.AppEnvironment;
import com.sharan.deskcharm.windows.WindowManager;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

/**
 * Builds the settings window's top menu bar. Kept separate from
 * {@link SettingsController} so the "what actions exist" wiring (also reused
 * conceptually by the system tray's popup menu) isn't tangled up with the
 * slider/checkbox layout code.
 */
public class MenuController {

    private final AppEnvironment environment;
    private final WindowManager windowManager;
    private final SettingsController settingsController;

    public MenuController(AppEnvironment environment, WindowManager windowManager, SettingsController settingsController) {
        this.environment = environment;
        this.windowManager = windowManager;
        this.settingsController = settingsController;
    }

    public MenuBar buildMenuBar(Stage ownerStage) {
        Menu fileMenu = new Menu("File");

        MenuItem importItem = new MenuItem("Import Custom Charm...");
        importItem.setOnAction(e -> settingsController.handleImportCustomCharm(ownerStage));

        MenuItem resetItem = new MenuItem("Reset Settings to Defaults");
        resetItem.setOnAction(e -> settingsController.handleResetSettings());

        MenuItem exitItem = new MenuItem("Exit DeskCharm");
        exitItem.setOnAction(e -> settingsController.handleExitApplication());

        fileMenu.getItems().addAll(importItem, resetItem, new SeparatorMenuItem(), exitItem);

        Menu viewMenu = new Menu("View");
        MenuItem closeSettingsItem = new MenuItem("Close Settings Window");
        closeSettingsItem.setOnAction(e -> ownerStage.close());
        viewMenu.getItems().add(closeSettingsItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, viewMenu);
        return menuBar;
    }
}
