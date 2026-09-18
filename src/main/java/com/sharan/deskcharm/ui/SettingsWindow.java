package com.sharan.deskcharm.ui;

import com.sharan.deskcharm.app.AppEnvironment;
import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.settings.AppSettings;
import com.sharan.deskcharm.windows.WindowManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * The JavaFX settings window: charm selection, rope length/gravity/damping
 * sliders, visibility/sound/animation checkboxes, and reset/import/exit
 * buttons — deliberately a separate {@link Stage} from the transparent
 * overlay, since it needs normal window chrome and doesn't need to be
 * always-on-top or click-through.
 */
public class SettingsWindow {

    private final AppEnvironment environment;
    private final SettingsController controller;
    private final WindowManager windowManager;
    private final Stage stage = new Stage();

    public SettingsWindow(AppEnvironment environment, WindowManager windowManager) {
        this.environment = environment;
        this.windowManager = windowManager;
        this.controller = new SettingsController(environment, windowManager);
        buildScene();
    }

    private void buildScene() {
        AppSettings settings = environment.getSettings();

        MenuController menuController = new MenuController(environment, windowManager, controller);
        BorderPane root = new BorderPane();
        root.setTop(menuController.buildMenuBar(stage));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        int row = 0;

        grid.add(new Label("Charm:"), 0, row);
        ComboBox<Charm> charmCombo = new ComboBox<>();
        charmCombo.getItems().addAll(controller.getAvailableCharms());
        charmCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Charm charm) {
                return charm == null ? "" : charm.getDisplayName();
            }

            @Override
            public Charm fromString(String string) {
                return null; // Combo is not editable; conversion back from text is never needed.
            }
        });
        environment.getCharmLibrary().findById(settings.getSelectedCharmId())
                .ifPresent(charmCombo.getSelectionModel()::select);
        charmCombo.setOnAction(e -> controller.onCharmSelected(charmCombo.getValue()));
        grid.add(charmCombo, 1, row++);

        row = addSliderRow(grid, row, "Rope length:", 60, 500, settings.getRopeLength(), controller::onRopeLengthChanged);
        row = addSliderRow(grid, row, "Gravity:", 50, 3000, settings.getGravity(), controller::onGravityChanged);
        row = addSliderRow(grid, row, "Damping:", 0.80, 1.0, settings.getDamping(), controller::onDampingChanged);

        grid.add(new Label("Monitor:"), 0, row);
        ComboBox<javafx.stage.Screen> monitorCombo = new ComboBox<>();
        monitorCombo.getItems().addAll(controller.getAvailableScreens());
        monitorCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(javafx.stage.Screen screen) {
                if (screen == null) {
                    return "";
                }
                int index = controller.getAvailableScreens().indexOf(screen);
                return "Monitor " + (index + 1)
                        + " (" + (int) screen.getBounds().getWidth() + "x" + (int) screen.getBounds().getHeight() + ")";
            }

            @Override
            public javafx.stage.Screen fromString(String string) {
                return null;
            }
        });
        int savedMonitorIndex = settings.getSelectedMonitorIndex();
        if (savedMonitorIndex >= 0 && savedMonitorIndex < monitorCombo.getItems().size()) {
            monitorCombo.getSelectionModel().select(savedMonitorIndex);
        } else if (!monitorCombo.getItems().isEmpty()) {
            monitorCombo.getSelectionModel().select(0);
        }
        monitorCombo.setOnAction(e -> controller.onMonitorSelected(monitorCombo.getSelectionModel().getSelectedIndex()));
        grid.add(monitorCombo, 1, row++);

        row = addCheckBoxRow(grid, row, "Show beads", settings.isBeadsVisible(), controller::onBeadsVisibleChanged);
        row = addCheckBoxRow(grid, row, "Show shadows", settings.isShadowsVisible(), controller::onShadowsVisibleChanged);
        row = addCheckBoxRow(grid, row, "Sound effects", settings.isSoundEnabled(), controller::onSoundEnabledChanged);
        row = addCheckBoxRow(grid, row, "Animation enabled", settings.isAnimationEnabled(), controller::onAnimationEnabledChanged);

        Button importButton = new Button("Import Custom Charm...");
        importButton.setOnAction(e -> controller.handleImportCustomCharm(stage));
        grid.add(importButton, 0, row, 2, 1);
        row++;

        Button resetButton = new Button("Reset Settings");
        resetButton.setOnAction(e -> controller.handleResetSettings());
        grid.add(resetButton, 0, row);

        Button exitButton = new Button("Exit Application");
        exitButton.setOnAction(e -> controller.handleExitApplication());
        grid.add(exitButton, 1, row);

        root.setCenter(grid);

        Scene scene = new Scene(root, 420, 500);
        String stylesheet = getClass().getResource("/styles/settings.css") == null
                ? null
                : getClass().getResource("/styles/settings.css").toExternalForm();
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        }

        stage.setTitle("DeskCharm Settings");
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    private int addSliderRow(GridPane grid, int row, String label, double min, double max,
                              double initial, java.util.function.DoubleConsumer onChanged) {
        grid.add(new Label(label), 0, row);
        Slider slider = new Slider(min, max, initial);
        slider.setShowTickLabels(false);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> onChanged.accept(newVal.doubleValue()));
        grid.add(slider, 1, row);
        return row + 1;
    }

    private int addCheckBoxRow(GridPane grid, int row, String label, boolean initial,
                                java.util.function.Consumer<Boolean> onChanged) {
        CheckBox checkBox = new CheckBox(label);
        checkBox.setSelected(initial);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> onChanged.accept(newVal));
        grid.add(checkBox, 0, row, 2, 1);
        return row + 1;
    }
}
