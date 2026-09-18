package com.sharan.deskcharm.windows;

import com.sharan.deskcharm.app.AppEnvironment;
import com.sharan.deskcharm.audio.SoundEffectPlayer;
import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.interaction.DragController;
import com.sharan.deskcharm.interaction.InteractionState;
import com.sharan.deskcharm.physics.Bead;
import com.sharan.deskcharm.physics.RopeNode;
import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.physics.SimulationClock;
import com.sharan.deskcharm.physics.Vector2;
import com.sharan.deskcharm.rendering.BeadRenderer;
import com.sharan.deskcharm.rendering.CharmRenderer;
import com.sharan.deskcharm.rendering.RopeRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * The transparent, undecorated, always-on-top overlay stage that hosts the
 * rope-and-charm animation. Only the region around the charm is meant to be
 * interactive; on Windows, {@link WindowsClickThrough} is used to make the
 * rest of the window's fully transparent area genuinely click-through at
 * the OS level (JavaFX alone cannot do this — see that class's Javadoc).
 *
 * The overlay is driven entirely by an {@link AppEnvironment}: rope length,
 * gravity, damping, and the selected charm's shape/size/color all come from
 * the loaded {@code AppSettings} and {@code CharmLibrary}, rather than being
 * hard-coded here.
 */
public class OverlayWindow {

    private static final double WINDOW_WIDTH = 260.0;
    private static final double TOP_MARGIN = 20.0;
    private static final double BOTTOM_MARGIN = 140.0;
    private static final double CHARM_HIT_RADIUS = 44.0;
    private static final double THROW_SPEED_THRESHOLD_PX_PER_SEC = 250.0;

    private final Stage stage;
    private final Canvas canvas;
    private final AppEnvironment environment;
    private final RopeSimulation ropeSimulation;
    private final List<Bead> beads = new ArrayList<>();

    private final RopeRenderer ropeRenderer;
    private final BeadRenderer beadRenderer = new BeadRenderer();
    private final CharmRenderer charmRenderer = new CharmRenderer();
    private final DragController dragController;
    private final SoundEffectPlayer soundEffectPlayer;

    private WindowsClickThrough clickThrough;
    private InteractionState previousState = InteractionState.IDLE;
    private long lastFrameNanos = -1;
    private AnimationTimer animationTimer;

    public OverlayWindow(Stage stage, AppEnvironment environment) {
        this.stage = stage;
        this.environment = environment;
        this.ropeSimulation = environment.getRopeSimulation();
        this.soundEffectPlayer = new SoundEffectPlayer(environment.getSettings().isSoundEnabled());

        double windowHeight = environment.getSettings().getRopeLength() + BOTTOM_MARGIN;
        Vector2 anchor = new Vector2(WINDOW_WIDTH / 2.0, TOP_MARGIN);
        ropeSimulation.setAnchor(anchor);

        this.ropeRenderer = new RopeRenderer(environment.getSettings().getRopeThickness());
        this.dragController = new DragController(ropeSimulation, CHARM_HIT_RADIUS);
        this.dragController.setOnStateChanged(this::handleInteractionStateChanged);

        if (environment.getSettings().isBeadsVisible()) {
            initializeBeads();
        }

        Screen targetScreen = resolveTargetScreen();
        double windowX = targetScreen.getVisualBounds().getMinX()
                + targetScreen.getVisualBounds().getWidth() / 2.0 - WINDOW_WIDTH / 2.0
                + environment.getSettings().getOverlayOffsetX();
        double windowY = targetScreen.getVisualBounds().getMinY()
                + environment.getSettings().getOverlayOffsetY();

        this.canvas = new Canvas(WINDOW_WIDTH, windowHeight);
        Pane root = new Pane(canvas);
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root, WINDOW_WIDTH, windowHeight);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setX(windowX);
        stage.setY(windowY);

        attachMouseHandlers(scene);
        startRenderLoop();
    }

    private Screen resolveTargetScreen() {
        List<Screen> screens = Screen.getScreens();
        int index = environment.getSettings().getSelectedMonitorIndex();
        if (index >= 0 && index < screens.size()) {
            return screens.get(index);
        }
        return Screen.getPrimary();
    }

    private void initializeBeads() {
        beads.add(new Bead(0.30, 5.0));
        beads.add(new Bead(0.55, 5.0));
        beads.add(new Bead(0.78, 5.0));
    }

    private void attachMouseHandlers(Scene scene) {
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    private void handleMouseMoved(MouseEvent event) {
        dragController.onMouseMoved(event.getX(), event.getY());
        updateCursorAndClickThrough(event.getX(), event.getY());
    }

    private void handleMousePressed(MouseEvent event) {
        dragController.onMousePressed(event.getX(), event.getY());
        updateCursorAndClickThrough(event.getX(), event.getY());
    }

    private void handleMouseDragged(MouseEvent event) {
        dragController.onMouseDragged(event.getX(), event.getY());
    }

    private void handleMouseReleased(MouseEvent event) {
        dragController.onMouseReleased(event.getX(), event.getY());
        updateCursorAndClickThrough(event.getX(), event.getY());
    }

    private void updateCursorAndClickThrough(double x, double y) {
        boolean interactive = dragController.hitTest(x, y);
        canvas.setCursor(interactive ? Cursor.HAND : Cursor.DEFAULT);
        if (clickThrough != null && clickThrough.isAvailable()) {
            if (interactive) {
                clickThrough.disableClickThrough();
            } else {
                clickThrough.enableClickThrough();
            }
        }
    }

    /**
     * Plays the appropriate sound effect for a grab, a gentle release, or a
     * fast throw, based on the interaction state transition and the resulting
     * charm speed. Sounds are entirely optional and silently no-op when
     * {@code AppSettings.isSoundEnabled()} is false.
     */
    private void handleInteractionStateChanged(InteractionState newState) {
        soundEffectPlayer.setEnabled(environment.getSettings().isSoundEnabled());
        if (newState == InteractionState.GRABBED) {
            soundEffectPlayer.playGrabbed();
        } else if (previousState == InteractionState.GRABBED) {
            RopeNode charmNode = ropeSimulation.getNodes().get(ropeSimulation.getNodes().size() - 1);
            double speedPxPerSec = charmNode.getImplicitVelocity().length() / SimulationClock.FIXED_TIMESTEP;
            if (speedPxPerSec >= THROW_SPEED_THRESHOLD_PX_PER_SEC) {
                soundEffectPlayer.playThrown();
            } else {
                soundEffectPlayer.playReleased();
            }
        }
        previousState = newState;
    }

    private void startRenderLoop() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!environment.getSettings().isAnimationEnabled()) {
                    lastFrameNanos = now;
                    render();
                    return;
                }
                if (lastFrameNanos < 0) {
                    lastFrameNanos = now;
                    return;
                }
                double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;

                ropeSimulation.update(deltaSeconds);
                render();
            }
        };
        animationTimer.start();
    }

    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ropeRenderer.render(gc, ropeSimulation.getNodes());

        if (environment.getSettings().isBeadsVisible()) {
            for (Bead bead : beads) {
                bead.sampleFrom(ropeSimulation.getNodes());
            }
            beadRenderer.render(gc, beads);
        }

        Charm selectedCharm = environment.getSelectedCharm();
        boolean hovered = dragController.getState() != InteractionState.IDLE;
        charmRenderer.render(gc, ropeSimulation.getCharmPosition(), selectedCharm, hovered);
    }

    /** Re-reads charm/rope settings and rebuilds render-affecting state without recreating the window. */
    public void refreshFromSettings() {
        beads.clear();
        if (environment.getSettings().isBeadsVisible()) {
            initializeBeads();
        }
        soundEffectPlayer.setEnabled(environment.getSettings().isSoundEnabled());
    }

    public void show() {
        stage.show();
        // The native window handle only exists once the stage is showing, so
        // click-through support is initialized here and starts in the
        // "capturing" (non-transparent) state until the first mouse-moved
        // event tells us the cursor isn't over the charm.
        Platform.runLater(() -> {
            clickThrough = new WindowsClickThrough(stage);
        });
    }

    public void hide() {
        stage.hide();
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        soundEffectPlayer.shutdown();
    }

    public RopeSimulation getRopeSimulation() {
        return ropeSimulation;
    }

    public Stage getStage() {
        return stage;
    }
}
