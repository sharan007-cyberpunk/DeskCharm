package com.sharan.deskcharm.windows;

import com.sharan.deskcharm.charm.CharmLibrary;
import com.sharan.deskcharm.settings.SettingsStore;
import javafx.application.Platform;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class SystemTrayManager {
    private final OverlayWindow overlay;
    private final CharmLibrary library;
    private final SettingsStore settingsStore;
    private TrayIcon trayIcon;

    public SystemTrayManager(OverlayWindow overlay, CharmLibrary library, SettingsStore settingsStore) {
        this.overlay = overlay;
        this.library = library;
        this.settingsStore = settingsStore;
    }

    public void install() {
        if (!SystemTray.isSupported()) return;

        PopupMenu menu = new PopupMenu();

        MenuItem show = new MenuItem("Show / Hide");
        show.addActionListener(e -> Platform.runLater(overlay::toggleVisible));

        Menu charmMenu = new Menu("Choose Charm");
        library.all().forEach(charm -> {
            MenuItem item = new MenuItem(charm.name());
            item.addActionListener(e -> Platform.runLater(() -> overlay.setCharm(charm.id())));
            charmMenu.add(item);
        });

        MenuItem reset = new MenuItem("Reset Position");
        reset.addActionListener(e -> Platform.runLater(overlay::resetPosition));

        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> Platform.exit());

        menu.add(show);
        menu.add(charmMenu);
        menu.add(reset);
        menu.addSeparator();
        menu.add(exit);

        Image image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ((BufferedImage) image).createGraphics();
        g.setColor(new java.awt.Color(245, 190, 60));
        g.fillOval(7, 7, 18, 18);
        g.dispose();

        trayIcon = new TrayIcon(image, "DeskCharm", menu);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException ignored) {
        }
    }

    public void uninstall() {
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}
