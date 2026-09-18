package com.sharan.deskcharm.windows;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmLibrary;
import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.physics.SimulationClock;
import com.sharan.deskcharm.physics.Vector2;
import com.sharan.deskcharm.rendering.BeadRenderer;
import com.sharan.deskcharm.rendering.CharmRenderer;
import com.sharan.deskcharm.rendering.RopeRenderer;
import com.sharan.deskcharm.settings.AppSettings;
import com.sharan.deskcharm.settings.SettingsStore;
import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class OverlayWindow {
    private final RopeSimulation simulation;
    private final CharmLibrary library;
    private final SettingsStore settingsStore;
    private AppSettings settings;
    private Stage stage;
    private Canvas canvas;
    private AnimationTimer timer;
    private Charm charm;

    public OverlayWindow(RopeSimulation simulation, CharmLibrary library,
                         AppSettings settings, SettingsStore settingsStore) {
        this.simulation = simulation;
        this.library = library;
        this.settings = settings;
        this.settingsStore = settingsStore;
        this.charm = library.find(settings.selectedCharm());
    }

    public void show() {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setTitle("DeskCharm");

        canvas = new Canvas(520, 760);
        Pane root = new Pane(canvas);
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root, 520, 760, Color.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        installMouseHandlers(scene);
        stage.setScene(scene);
        positionOnScreen();
        stage.show();

        simulation.setAnchor(new Vector2(260, 25));

        SimulationClock clock = new SimulationClock();
        timer = new AnimationTimer() {
            private long previous = -1;
            @Override public void handle(long now) {
                if (previous < 0) previous = now;
                double dt = (now - previous) / 1_000_000_000.0;
                previous = now;
                if (settings.animation()) clock.consume(dt, simulation);
                render();
            }
        };
        timer.start();
    }

    private void positionOnScreen() {
        Rectangle2D bounds = Screen.getScreens().get(Math.min(settings.screenIndex(), Screen.getScreens().size()-1)).getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - 520) / 2);
        stage.setY(bounds.getMinY());
    }

    private void installMouseHandlers(Scene scene) {
        scene.setOnMousePressed(e -> simulation.beginDrag(new Vector2(e.getX(), e.getY())));
        scene.setOnMouseDragged(e -> simulation.updateDrag(new Vector2(e.getX(), e.getY())));
        scene.setOnMouseReleased(e -> simulation.endDrag());
        scene.setOnMouseMoved(e -> {});
    }

    private void render() {
        var g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        new RopeRenderer().draw(g, simulation.getNodes());
        if (settings.beads()) new BeadRenderer().draw(g, simulation.getNodes());
        new CharmRenderer().draw(g, charm, simulation.getCharmPosition().x(),
                simulation.getCharmPosition().y(), null, settings.shadows());
    }

    public void setCharm(String id) {
        charm = library.find(id);
        settings = new AppSettings(id, settings.segments(), settings.segmentLength(),
                settings.gravity(), settings.damping(), settings.constraintIterations(),
                settings.maxStretch(), settings.beads(), settings.shadows(), settings.sound(),
                settings.animation(), settings.screenIndex());
        settingsStore.save(settings);
        simulation.wakeUp();
    }

    public void toggleVisible() {
        if (stage.isShowing()) stage.hide();
        else stage.show();
    }

    public void resetPosition() {
        positionOnScreen();
        simulation.reset(new Vector2(260, 25));
    }

    public void openSettings() {
        // Lightweight settings action: rotate to the next charm.
        int index = library.all().indexOf(charm);
        setCharm(library.all().get((index + 1) % library.all().size()).id());
    }

    public void close() {
        if (timer != null) timer.stop();
        if (stage != null) stage.close();
    }
}
