package com.sharan.deskcharm.windows;

import com.sharan.deskcharm.app.AppEnvironment;
import com.sharan.deskcharm.charm.Charm;
import javafx.application.Platform;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps {@code java.awt.SystemTray} to provide a Windows tray icon with a
 * right-click menu (show/hide, open settings, charm submenu, toggle
 * animation, reset position, exit). If the platform reports no tray support
 * (unusual on Windows but possible in some sandboxed/CI environments), this
 * class fails gracefully: {@link #installIfSupported()} simply does nothing
 * and logs a warning, and the application remains usable via the settings
 * window's own Exit button.
 */
public class SystemTrayManager {

    private static final Logger LOGGER = Logger.getLogger(SystemTrayManager.class.getName());

    private final AppEnvironment environment;
    private final WindowManager windowManager;
    private final Runnable onExitRequested;

    private TrayIcon trayIcon;
    private CheckboxMenuItem animationToggleItem;

    public SystemTrayManager(AppEnvironment environment, WindowManager windowManager, Runnable onExitRequested) {
        this.environment = environment;
        this.windowManager = windowManager;
        this.onExitRequested = onExitRequested;
    }

    public boolean isSupported() {
        return SystemTray.isSupported();
    }

    /** Installs the tray icon if the platform supports it; otherwise logs and returns false. */
    public boolean installIfSupported() {
        if (!isSupported()) {
            LOGGER.warning("java.awt.SystemTray is not supported on this platform; "
                    + "the tray menu will be unavailable. Use the settings window to open settings or exit.");
            return false;
        }
        try {
            PopupMenu popupMenu = buildPopupMenu();
            trayIcon = new TrayIcon(renderTrayIconImage(), "DeskCharm", popupMenu);
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
            return true;
        } catch (AWTException e) {
            LOGGER.log(Level.WARNING, "Failed to install the DeskCharm tray icon", e);
            return false;
        }
    }

    public void uninstall() {
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    private PopupMenu buildPopupMenu() {
        PopupMenu menu = new PopupMenu();

        MenuItem showHideItem = new MenuItem("Show/Hide Charm");
        showHideItem.addActionListener(e -> Platform.runLater(windowManager::toggleOverlayVisibility));
        menu.add(showHideItem);

        MenuItem openSettingsItem = new MenuItem("Open Settings...");
        openSettingsItem.addActionListener(e -> Platform.runLater(this::openSettingsWindow));
        menu.add(openSettingsItem);

        menu.add(buildCharmSubmenu());

        animationToggleItem = new CheckboxMenuItem(
                "Animation Enabled", environment.getSettings().isAnimationEnabled());
        animationToggleItem.addItemListener(e -> Platform.runLater(() -> {
            environment.getSettings().setAnimationEnabled(animationToggleItem.getState());
            environment.persistSettings();
        }));
        menu.add(animationToggleItem);

        MenuItem resetPositionItem = new MenuItem("Reset Position");
        resetPositionItem.addActionListener(e -> Platform.runLater(windowManager::resetOverlayPosition));
        menu.add(resetPositionItem);

        menu.addSeparator();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> Platform.runLater(onExitRequested));
        menu.add(exitItem);

        return menu;
    }

    private Menu buildCharmSubmenu() {
        Menu charmMenu = new Menu("Select Charm");
        for (Charm charm : environment.getCharmLibrary().getAllCharms()) {
            MenuItem item = new MenuItem(charm.getDisplayName());
            item.addActionListener(e -> Platform.runLater(() -> {
                environment.selectCharm(charm);
                windowManager.applySettingsChanges(false);
            }));
            charmMenu.add(item);
        }
        return charmMenu;
    }

    private void openSettingsWindow() {
        // The settings window itself (ui.SettingsWindow) is opened from here once
        // that stage's JavaFX Stage/Scene is constructed; deferred to keep this
        // class focused purely on tray/menu wiring.
        new com.sharan.deskcharm.ui.SettingsWindow(environment, windowManager).show();
    }

    /**
     * Builds a small original tray icon image programmatically (a filled
     * circle with a simple highlight) rather than shipping a bitmap asset,
     * so the tray icon can never be mistaken for Hangly's branding.
     */
    private Image renderTrayIconImage() {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(230, 110, 150));
        g.fillOval(1, 1, size - 2, size - 2);
        g.setColor(new Color(255, 255, 255, 160));
        g.fillOval(3, 3, size / 3, size / 3);
        g.dispose();
        return image;
    }
}
