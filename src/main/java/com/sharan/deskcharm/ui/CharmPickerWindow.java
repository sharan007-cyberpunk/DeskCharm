package com.sharan.deskcharm.ui;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmLibrary;
import com.sharan.deskcharm.rendering.CharmRenderer;
import com.sharan.deskcharm.windows.OverlayWindow;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Locale;

public final class CharmPickerWindow {

    private final CharmLibrary library;
    private final OverlayWindow overlay;
    private final CharmRenderer renderer = new CharmRenderer();

    private Stage stage;
    private FlowPane charmGrid;
    private TextField searchField;

    public CharmPickerWindow(CharmLibrary library, OverlayWindow overlay) {
        this.library = library;
        this.overlay = overlay;
    }

    public void show() {
        if (stage != null && stage.isShowing()) {
            stage.toFront();
            return;
        }

        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setTitle("DeskCharm — Charm Collection");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setStyle("""
            -fx-background-color:
                linear-gradient(to bottom right,
                    rgba(17,20,29,0.98),
                    rgba(8,11,18,0.99));
            -fx-background-radius: 22;
            -fx-border-color: rgba(255,255,255,0.12);
            -fx-border-width: 1;
            -fx-border-radius: 22;
            -fx-effect:
                dropshadow(gaussian, rgba(0,0,0,0.60), 28, 0.35, 0, 10);
        """);

        root.setTop(createHeader());

        charmGrid = new FlowPane();
        charmGrid.setHgap(12);
        charmGrid.setVgap(12);
        charmGrid.setPadding(new Insets(14, 2, 8, 2));
        charmGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(charmGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(650);
        scrollPane.setPrefViewportHeight(470);
        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-control-inner-background: transparent;
            -fx-border-color: transparent;
        """);

        root.setCenter(scrollPane);
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 690, 650);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        refreshCards("");
        centreOnScreen();
        stage.show();
    }

    private VBox createHeader() {
        Label title = new Label("DESKCHARM");
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-letter-spacing: 3px;
        """);

        Label subtitle = new Label("Premium Charm Collection");
        subtitle.setStyle("""
            -fx-text-fill: rgba(255,255,255,0.55);
            -fx-font-size: 12px;
        """);

        searchField = new TextField();
        searchField.setPromptText("Search charms...");
        searchField.setPrefWidth(260);
        searchField.setStyle("""
            -fx-background-color: rgba(255,255,255,0.07);
            -fx-background-radius: 12;
            -fx-border-color: rgba(255,255,255,0.10);
            -fx-border-radius: 12;
            -fx-text-fill: white;
            -fx-prompt-text-fill: rgba(255,255,255,0.35);
            -fx-padding: 10 14 10 14;
        """);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshCards(newValue));

        VBox titleBox = new VBox(3, title, subtitle);
        HBox row = new HBox(20, titleBox, searchField);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(10, row, createNameDangleRow(), createSizeRow(), createRopeLengthRow());
        header.setPadding(new Insets(2, 2, 12, 2));
        return header;
    }

    private static final String FIELD_STYLE = """
        -fx-background-color: rgba(255,255,255,0.07);
        -fx-background-radius: 12;
        -fx-border-color: rgba(255,255,255,0.10);
        -fx-border-radius: 12;
        -fx-text-fill: white;
        -fx-prompt-text-fill: rgba(255,255,255,0.35);
        -fx-padding: 8 14 8 14;
    """;

    /** "Name Dangle": type any text and it becomes a charm, engraved on a gold plaque. */
    private HBox createNameDangleRow() {
        Label label = new Label("Name Dangle");
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px; -fx-font-weight: bold;");
        label.setMinWidth(90);

        TextField nameField = new TextField();
        nameField.setPromptText("Type a name…");
        nameField.setPrefWidth(220);
        nameField.setStyle(FIELD_STYLE);

        Button create = new Button("Create");
        create.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #E8CB86, #B8860B);
            -fx-background-radius: 10;
            -fx-text-fill: #241705;
            -fx-font-weight: bold;
            -fx-padding: 8 16 8 16;
            -fx-cursor: hand;
        """);
        Runnable createDangle = () -> {
            if (!nameField.getText().isBlank()) {
                overlay.setNameDangle(nameField.getText());
                close();
            }
        };
        create.setOnAction(e -> createDangle.run());
        nameField.setOnAction(e -> createDangle.run());

        HBox row = new HBox(10, label, nameField, create);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Global charm size control (0.5x - 2.0x), applied live to whichever charm is active. */
    private HBox createSizeRow() {
        Label label = new Label("Charm Size");
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px; -fx-font-weight: bold;");
        label.setMinWidth(90);

        Slider slider = new Slider(0.5, 2.0, overlay.getCharmScale());
        slider.setPrefWidth(220);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> overlay.setCharmScale(newVal.doubleValue()));

        Label percent = new Label(Math.round(overlay.getCharmScale() * 100) + "%");
        percent.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px;");
        percent.setMinWidth(40);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> percent.setText(Math.round(newVal.doubleValue() * 100) + "%"));

        HBox row = new HBox(10, label, slider, percent);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Total rope length control (120-900 px), applied live and persisted. */
    private HBox createRopeLengthRow() {
        Label label = new Label("Rope Length");
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px; -fx-font-weight: bold;");
        label.setMinWidth(90);

        Slider slider = new Slider(120, 900, overlay.getRopeLength());
        slider.setPrefWidth(220);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> overlay.setRopeLength(newVal.doubleValue()));

        Label pixels = new Label(Math.round(overlay.getRopeLength()) + " px");
        pixels.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px;");
        pixels.setMinWidth(50);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> pixels.setText(Math.round(newVal.doubleValue()) + " px"));

        HBox row = new HBox(10, label, slider, pixels);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox createFooter() {
        Label info = new Label(library.all().size() + " premium charms");
        info.setStyle("""
            -fx-text-fill: rgba(255,255,255,0.40);
            -fx-font-size: 11px;
        """);

        Button close = new Button("Close");
        close.setStyle("""
            -fx-background-color: rgba(255,255,255,0.08);
            -fx-background-radius: 10;
            -fx-text-fill: white;
            -fx-padding: 8 18 8 18;
            -fx-cursor: hand;
        """);
        close.setOnAction(e -> close());

        HBox footer = new HBox(10, info, close);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 2, 0, 2));
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        return footer;
    }

    private void refreshCards(String filter) {
        if (charmGrid == null) return;
        charmGrid.getChildren().clear();

        String query = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);

        for (Charm charm : library.all()) {
            if (query.isEmpty()
                    || charm.name().toLowerCase(Locale.ROOT).contains(query)
                    || charm.id().toLowerCase(Locale.ROOT).contains(query)) {
                charmGrid.getChildren().add(createCharmCard(charm));
            }
        }
    }

    private VBox createCharmCard(Charm charm) {
        Canvas canvas = new Canvas(120, 115);
        canvas.setMouseTransparent(true);

        renderer.draw(canvas.getGraphicsContext2D(), charm, 60, 55, null, true);

        Label name = new Label(charm.name());
        name.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
        """);
        name.setMaxWidth(130);
        name.setAlignment(Pos.CENTER);

        Label type = new Label(charm.type().name().replace('_', ' '));
        type.setStyle("""
            -fx-text-fill: rgba(255,255,255,0.38);
            -fx-font-size: 9px;
        """);
        type.setMaxWidth(130);
        type.setAlignment(Pos.CENTER);

        VBox card = new VBox(3, canvas, name, type);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(145, 160);
        card.setMaxSize(145, 160);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.045);
            -fx-background-radius: 16;
            -fx-border-color: rgba(255,255,255,0.09);
            -fx-border-radius: 16;
            -fx-cursor: hand;
        """);

        card.setOnMouseEntered(e -> card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.085);
            -fx-background-radius: 16;
            -fx-border-color: rgba(255,215,130,0.42);
            -fx-border-radius: 16;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(255,210,120,0.18), 18, 0.2, 0, 0);
        """));

        card.setOnMouseExited(e -> card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.045);
            -fx-background-radius: 16;
            -fx-border-color: rgba(255,255,255,0.09);
            -fx-border-radius: 16;
            -fx-cursor: hand;
        """));

        card.setOnMouseClicked(e -> {
            overlay.setCharm(charm.id());
            close();
        });

        return card;
    }

    private void centreOnScreen() {
        Rectangle2DHelper helper = new Rectangle2DHelper(Screen.getPrimary().getVisualBounds());
        stage.setX(helper.x() + (helper.width() - stage.getWidth()) / 2.0);
        stage.setY(helper.y() + (helper.height() - stage.getHeight()) / 2.0);
    }

    public void close() {
        if (stage != null) stage.close();
    }

    private record Rectangle2DHelper(double x, double y, double width, double height) {
        Rectangle2DHelper(javafx.geometry.Rectangle2D bounds) {
            this(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
    }
}
