package com.sharan.deskcharm.ui;

import com.sharan.deskcharm.app.AppEnvironment;
import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmImageProcessor;
import com.sharan.deskcharm.settings.AppSettings;
import com.sharan.deskcharm.windows.WindowManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Holds the behavior behind the settings window's controls. Kept separate
 * from {@link SettingsWindow} so the layout code and the "what happens when
 * a control changes" code aren't interleaved. Every mutating action goes
 * through {@link AppEnvironment}/{@link WindowManager} rather than touching
 * global state directly.
 */
public class SettingsController {

    private final AppEnvironment environment;
    private final WindowManager windowManager;
    private final CharmImageProcessor imageProcessor = new CharmImageProcessor();

    public SettingsController(AppEnvironment environment, WindowManager windowManager) {
        this.environment = environment;
        this.windowManager = windowManager;
    }

    public void onCharmSelected(Charm charm) {
        if (charm == null) {
            return;
        }
        environment.selectCharm(charm);
        windowManager.applySettingsChanges(false);
    }

    public void onMonitorSelected(int monitorIndex) {
        environment.getSettings().setSelectedMonitorIndex(monitorIndex);
        windowManager.applySettingsChanges(true);
    }

    public void onRopeLengthChanged(double newLength) {
        environment.getSettings().setRopeLength(newLength);
        windowManager.applySettingsChanges(true);
    }

    public void onGravityChanged(double newGravity) {
        environment.getSettings().setGravity(newGravity);
        windowManager.applySettingsChanges(true);
    }

    public void onDampingChanged(double newDamping) {
        environment.getSettings().setDamping(newDamping);
        windowManager.applySettingsChanges(true);
    }

    public void onBeadsVisibleChanged(boolean visible) {
        environment.getSettings().setBeadsVisible(visible);
        windowManager.applySettingsChanges(false);
    }

    public void onShadowsVisibleChanged(boolean visible) {
        environment.getSettings().setShadowsVisible(visible);
        windowManager.applySettingsChanges(false);
    }

    public void onSoundEnabledChanged(boolean enabled) {
        environment.getSettings().setSoundEnabled(enabled);
        windowManager.applySettingsChanges(false);
    }

    public void onAnimationEnabledChanged(boolean enabled) {
        environment.getSettings().setAnimationEnabled(enabled);
        windowManager.applySettingsChanges(false);
    }

    public void handleResetSettings() {
        AppSettings defaults = AppSettings.defaults();
        AppSettings current = environment.getSettings();
        current.setSelectedCharmId(defaults.getSelectedCharmId());
        current.setRopeLength(defaults.getRopeLength());
        current.setRopeThickness(defaults.getRopeThickness());
        current.setGravity(defaults.getGravity());
        current.setDamping(defaults.getDamping());
        current.setBeadsVisible(defaults.isBeadsVisible());
        current.setShadowsVisible(defaults.isShadowsVisible());
        current.setSoundEnabled(defaults.isSoundEnabled());
        current.setAnimationEnabled(defaults.isAnimationEnabled());
        current.setOverlayOffsetX(defaults.getOverlayOffsetX());
        current.setOverlayOffsetY(defaults.getOverlayOffsetY());
        windowManager.applySettingsChanges(true);
    }

    public void handleImportCustomCharm(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Custom Charm Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(ownerStage);
        if (selectedFile == null) {
            return; // User cancelled; nothing to do.
        }

        try {
            Path customCharmDirectory = environment.getSettingsStore().getAppDataDirectory().resolve("charms");
            Path storedImagePath = imageProcessor.importImage(selectedFile, customCharmDirectory);

            String newId = "custom-" + System.currentTimeMillis();
            String displayName = stripExtension(selectedFile.getName());
            Charm customCharm = Charm.fromImage(newId, displayName, storedImagePath.toString(), 48.0);

            environment.registerCustomCharm(customCharm);
            environment.selectCharm(customCharm);
            windowManager.applySettingsChanges(false);
        } catch (CharmImageProcessor.ImportException e) {
            showError("Could not import image", e.getMessage());
        }
    }

    public void handleExitApplication() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Exit DeskCharm completely?", ButtonType.YES, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.setTitle("Exit DeskCharm");
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                environment.persistSettings();
                windowManager.shutdown();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public List<Charm> getAvailableCharms() {
        return environment.getCharmLibrary().getAllCharms();
    }

    public List<javafx.stage.Screen> getAvailableScreens() {
        return windowManager.getAvailableScreens();
    }

    public AppEnvironment getEnvironment() {
        return environment;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
