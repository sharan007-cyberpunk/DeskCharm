package com.sharan.deskcharm.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WindowsClickThrough {
    private static final Logger LOGGER = Logger.getLogger(WindowsClickThrough.class.getName());
    private static final int GWL_EXSTYLE = -20;
    private static final int WS_EX_LAYERED = 0x00080000;
    private static final int WS_EX_TRANSPARENT = 0x00000020;

    private interface ExtendedUser32 extends StdCallLibrary {
        ExtendedUser32 INSTANCE = Native.load("user32", ExtendedUser32.class, W32APIOptions.DEFAULT_OPTIONS);
        int GetWindowLongA(HWND hWnd, int nIndex);
        int SetWindowLongA(HWND hWnd, int nIndex, int dwNewLong);
    }

    private final HWND windowHandle;
    private final boolean available;
    private boolean currentlyTransparent = false;

    public WindowsClickThrough(Stage stage) {
        HWND resolvedHandle = null;
        boolean resolvedAvailable = false;
        try {
            if (isWindows()) {
                resolvedHandle = resolveHwnd(stage);
                resolvedAvailable = resolvedHandle != null;
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Could not resolve native window handle for click-through support", t);
        }
        this.windowHandle = resolvedHandle;
        this.available = resolvedAvailable;
        if (!available) {
            LOGGER.info("Native click-through is unavailable (not on Windows, or the handle could not be resolved); the overlay will remain fully click-capturing across its whole bounding box.");
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        return osName.contains("win");
    }

    private HWND resolveHwnd(Stage stage) throws Exception {
        Object tkStage = invoke(invoke(stage, "impl_getPeer"), "getPlatformWindow");
        if (tkStage == null) {
            tkStage = invoke(invoke(stage, "getPeer"), "getPlatformWindow");
        }
        Long nativeHandle = (Long) invoke(tkStage, "getNativeHandle");
        return nativeHandle == null ? null : new HWND(Pointer.createConstant(nativeHandle));
    }

    private Object invoke(Object target, String methodName) throws Exception {
        if (target == null) return null;
        Method method = target.getClass().getMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    public boolean isAvailable() { return available; }
    public void enableClickThrough() { setTransparentStyle(true); }
    public void disableClickThrough() { setTransparentStyle(false); }

    private void setTransparentStyle(boolean transparent) {
        if (!available || transparent == currentlyTransparent) return;
        try {
            int currentStyle = ExtendedUser32.INSTANCE.GetWindowLongA(windowHandle, GWL_EXSTYLE);
            int newStyle = transparent
                    ? (currentStyle | WS_EX_LAYERED | WS_EX_TRANSPARENT)
                    : (currentStyle & ~WS_EX_TRANSPARENT);
            ExtendedUser32.INSTANCE.SetWindowLongA(windowHandle, GWL_EXSTYLE, newStyle);
            currentlyTransparent = transparent;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Failed to toggle native click-through", t);
        }
    }
}
