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
import com.sharan.deskcharm.ui.CharmPickerWindow;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class OverlayWindow {

    private static final double WINDOW_WIDTH = 520;
    private static final double WINDOW_HEIGHT = 760;
    private static final double ANCHOR_X = WINDOW_WIDTH / 2.0;
    private static final double ANCHOR_Y = 25.0;
    private static final double CHARM_HIT_RADIUS = 55.0;

    private final RopeSimulation simulation;
    private final CharmLibrary library;
    private final SettingsStore settingsStore;
    private final CharmRenderer charmRenderer = new CharmRenderer();
    private final RopeRenderer ropeRenderer = new RopeRenderer();
    private final BeadRenderer beadRenderer = new BeadRenderer();

    private AppSettings settings;
    private Stage stage;
    private Canvas canvas;
    private AnimationTimer timer;
    private Charm charm;
    private CharmPickerWindow pickerWindow;
    private boolean draggingCharmHorizontally;

    public OverlayWindow(RopeSimulation simulation, CharmLibrary library,
                         AppSettings settings, SettingsStore settingsStore) {
        this.simulation = simulation;
        this.library = library;
        this.settings = settings;
        this.settingsStore = settingsStore;

        // Name Dangle charms aren't part of the static library, so if the last session had one
        // active, recreate it from the saved text before looking up the selected charm.
        if ("name-dangle".equals(settings.selectedCharm()) && settings.dangleText() != null
                && !settings.dangleText().isBlank()) {
            library.add(new Charm("name-dangle", settings.dangleText(),
                    com.sharan.deskcharm.charm.CharmType.DANGLE_NAME, 38, null, settings.dangleText()));
        }

        Charm initial = library.find(settings.selectedCharm());
        this.charm = initial.withRadius(initial.radius() * settings.charmScale());
    }

    public void show() {
        if (stage != null && stage.isShowing()) {
            stage.toFront();
            return;
        }

        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setTitle("DeskCharm");

        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        Pane root = new Pane(canvas);
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        installMouseHandlers(scene);
        stage.setScene(scene);

        positionOnScreen();
        stage.show();

        simulation.setAnchor(new Vector2(ANCHOR_X, ANCHOR_Y));

        SimulationClock clock = new SimulationClock();
        timer = new AnimationTimer() {
            private long previous = -1;

            @Override
            public void handle(long now) {
                if (previous < 0) previous = now;
                double dt = (now - previous) / 1_000_000_000.0;
                previous = now;
                dt = Math.min(dt, 0.05);

                if (settings.animation()) {
                    clock.consume(dt, simulation);
                }
                render();
            }
        };
        timer.start();
        render();
    }

    private void positionOnScreen() {
        int screenIndex = Math.max(0, Math.min(settings.screenIndex(), Screen.getScreens().size() - 1));
        Rectangle2D bounds = Screen.getScreens().get(screenIndex).getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - WINDOW_WIDTH) / 2.0);
        stage.setY(bounds.getMinY());
    }

    private enum DragMode { NONE, DESKTOP_POSITION, ROPE_PHYSICS }

    private DragMode dragMode = DragMode.NONE;
    private double desktopDragOffsetX;

    private void installMouseHandlers(Scene scene) {
        scene.setOnMousePressed(event -> {
            Vector2 point = new Vector2(event.getX(), event.getY());
            Vector2 charmPosition = simulation.getCharmPosition();
            double distanceToCharm = point.distance(charmPosition);

            // Right-click on the charm: open the premium collection.
            if (event.getButton() == MouseButton.SECONDARY
                    && distanceToCharm <= CHARM_HIT_RADIUS) {
                dragMode = DragMode.NONE;
                simulation.endDrag();
                openCharmPicker();
                event.consume();
                return;
            }

            // Double-click on the charm: open the premium collection.
            if (event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2
                    && distanceToCharm <= CHARM_HIT_RADIUS) {
                dragMode = DragMode.NONE;
                simulation.endDrag();
                openCharmPicker();
                event.consume();
                return;
            }

            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            /*
             * TWO DIFFERENT INTERACTIONS:
             *
             * 1. Grab the charm itself -> move the whole DeskCharm window
             *    horizontally along the top of the desktop. The rope keeps
             *    its physics in local coordinates.
             *
             * 2. Grab the rope/body -> engage the original rope physics drag.
             *    The endpoint jumps toward the pointer and the rope bends,
             *    stretches and swings naturally when released.
             */
            if (distanceToCharm <= CHARM_HIT_RADIUS) {
                dragMode = DragMode.DESKTOP_POSITION;
                desktopDragOffsetX = event.getScreenX() - stage.getX();
                event.consume();
                return;
            }

            dragMode = DragMode.ROPE_PHYSICS;
            simulation.beginDrag(point);
            event.consume();
        });

        scene.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) {
                return;
            }

            if (dragMode == DragMode.DESKTOP_POSITION) {
                moveOverlayAlongDesktop(event.getScreenX());
                event.consume();
                return;
            }

            if (dragMode == DragMode.ROPE_PHYSICS) {
                simulation.updateDrag(new Vector2(event.getX(), event.getY()));
                event.consume();
            }
        });

        scene.setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (dragMode == DragMode.ROPE_PHYSICS) {
                simulation.endDrag();
            }

            dragMode = DragMode.NONE;
            event.consume();
        });
    }

    private void moveOverlayAlongDesktop(double screenMouseX) {
        if (stage == null) {
            return;
        }

        Screen screen = findScreenForX(screenMouseX);
        Rectangle2D bounds = screen.getVisualBounds();

        // Keep the charm at the top while allowing it to travel from
        // the left edge to the right edge of the usable desktop.
        double desiredX = screenMouseX - desktopDragOffsetX;

        double minimumX = bounds.getMinX() - ANCHOR_X;
        double maximumX = bounds.getMaxX() - ANCHOR_X;

        stage.setY(bounds.getMinY());
        stage.setX(Math.max(minimumX, Math.min(maximumX, desiredX)));
    }

    private Screen findScreenForX(double x) {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            if (x >= bounds.getMinX() && x <= bounds.getMaxX()) {
                return screen;
            }
        }

        int index = Math.max(
                0,
                Math.min(settings.screenIndex(), Screen.getScreens().size() - 1)
        );
        return Screen.getScreens().get(index);
    }

    private void render() {
        if (canvas == null) return;

        var graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ropeRenderer.draw(graphics, simulation.getNodes(), charm);

        if (settings.beads()) {
            beadRenderer.draw(graphics, simulation.getNodes(), charm);
        }

        Vector2 charmPosition = simulation.getCharmPosition();
        charmRenderer.draw(graphics, charm, charmPosition.x(), charmPosition.y(), null, settings.shadows());
    }

    public void setCharm(String id) {
        Charm selected = library.find(id);
        if (selected == null) return;

        charm = selected.withRadius(baseRadiusFor(selected) * settings.charmScale());
        settings = settings.withSelectedCharm(id);

        settingsStore.save(settings);
        simulation.setAnchor(new Vector2(ANCHOR_X, ANCHOR_Y));
        simulation.wakeUp();
        render();
    }

    /** Creates (or updates) a Name Dangle charm from typed text and makes it the active charm.
     *  The dangle is added to the library under a fixed id, so re-typing a new name just replaces
     *  the previous dangle rather than piling up duplicate library entries. */
    public void setNameDangle(String text) {
        String label = text == null ? "" : text.trim();
        if (label.isEmpty()) return;

        Charm dangle = new Charm("name-dangle", label, com.sharan.deskcharm.charm.CharmType.DANGLE_NAME,
                38, null, label);
        library.add(dangle);
        settings = settings.withDangleText(label);
        setCharm("name-dangle");
    }

    /** Applies the size-control slider: rescales whichever charm is currently active, without
     *  changing which charm is selected. */
    public void setCharmScale(double scale) {
        settings = settings.withCharmScale(scale);
        settingsStore.save(settings);
        charm = charm.withRadius(baseRadiusFor(library.find(settings.selectedCharm())) * scale);
        simulation.wakeUp();
        render();
    }

    public double getCharmScale() {
        return settings.charmScale();
    }

    /** Applies a live total rope-length change and persists it for the next launch. */
    public void setRopeLength(double totalLength) {
        double clamped = Math.max(120, Math.min(900, totalLength));
        simulation.setRopeLength(clamped);
        settings = settings.withRopeLength(clamped);
        settingsStore.save(settings);
        simulation.setAnchor(new Vector2(ANCHOR_X, ANCHOR_Y));
        simulation.wakeUp();
        render();
    }

    public double getRopeLength() {
        return simulation.getRopeLength();
    }

    private double baseRadiusFor(Charm libraryCharm) {
        return libraryCharm.radius();
    }

    public void toggleVisible() {
        if (stage == null) {
            show();
            return;
        }

        if (stage.isShowing()) {
            stage.hide();
        } else {
            stage.show();
            stage.toFront();
            render();
        }
    }

    public void resetPosition() {
        if (stage == null) return;

        positionOnScreen();
        simulation.reset(new Vector2(ANCHOR_X, ANCHOR_Y));
        simulation.setAnchor(new Vector2(ANCHOR_X, ANCHOR_Y));
        simulation.wakeUp();
        render();
    }

    public void openSettings() {
        openCharmPicker();
    }

    private void openCharmPicker() {
        if (pickerWindow == null) {
            pickerWindow = new CharmPickerWindow(library, this);
        }
        pickerWindow.show();
    }

    public void close() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        if (pickerWindow != null) {
            pickerWindow.close();
        }

        if (stage != null) {
            stage.close();
            stage = null;
        }
    }
}
