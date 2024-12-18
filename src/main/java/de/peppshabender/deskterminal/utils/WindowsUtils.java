package de.peppshabender.deskterminal.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import java.awt.Window;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFrame;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import mslinks.ShellLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for interacting with the Windows API using JNA (Java Native Access). Provides methods for manipulating
 * Swing windows (e.g., unstyling a frame or moving it to the background).
 */
@UtilityClass
public class WindowsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsUtils.class);

    /** JNA interface for interacting with the Windows `User32` library. */
    private static final User32 USER_32 = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

    private static final Path STARTUP_PATH = Path.of(System.getProperty("user.home"))
            .resolve("AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/Deskterminal.lnk");
    private static final String APP_PATH = "Deskterminal.exe";

    private static final int GWL_EXSTYLE = -20; // Extended window style index.
    private static final int WS_EX_TOOLWINDOW = 0x00000080; // Tool window style (excludes window from the taskbar).
    private static final int SWP_NOMOVE = 0x0002; // Do not move the window.
    private static final int SWP_NOSIZE = 0x0001; // Do not resize the window.
    private static final int SWP_NOACTIVATE = 0x0010; // Do not activate the window.
    private static final int SWP_SHOWWINDOW = 0x0040; // Show the window.
    private static final Pointer HWND_BOTTOM =
            Pointer.createConstant(1); // Position the window at the bottom of the z-order.

    /**
     * Applies the "tool window" style to a {@link JFrame}, removing it from the taskbar and sets it as a child of
     * progman.
     *
     * @param frame The {@link JFrame} to style as a tool window.
     */
    public static void unstyleFrame(final JFrame frame) {
        LOG.debug("Unstyling window...");

        final Pointer hwnd = getHWND(frame);
        final Pointer progman = USER_32.FindWindowA("Progman", null);

        USER_32.SetParent(hwnd, progman);
        USER_32.SetWindowLong(hwnd, GWL_EXSTYLE, USER_32.GetWindowLong(hwnd, GWL_EXSTYLE) | WS_EX_TOOLWINDOW);

        LOG.debug("Unstyled window!");
    }

    /**
     * Moves a {@link JFrame} to the background in the z-order of windows.
     *
     * <p>This ensures the frame does not interfere with other applications.
     *
     * @param frame The {@link JFrame} to send to the background.
     */
    public static void moveToBackground(final JFrame frame) {
        USER_32.SetWindowPos(
                getHWND(frame),
                HWND_BOTTOM,
                frame.getX(),
                frame.getY(),
                frame.getWidth(),
                frame.getHeight(),
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE | SWP_SHOWWINDOW);

        LOG.debug("Moved window to the background");
    }

    /**
     * Retrieves the native window handle (HWND) for a {@link java.awt.Window}.
     *
     * @param window The {@link java.awt.Window} whose handle is to be retrieved.
     * @return A {@link Pointer} representing the native HWND of the window.
     */
    public static Pointer getHWND(Window window) {
        return Native.getComponentPointer(window);
    }

    /** @return true when the app is in the auto start folder, false otherwise */
    public static boolean isAutoStart() {
        return Files.exists(STARTUP_PATH);
    }

    /**
     * Copies the application to the windows auto start folder or deletes it vice-versa.
     *
     * <p>This requires the application to not have been renamed!
     */
    @SneakyThrows
    public static void toggleAutoStart() {
        final Path appPath = Path.of(APP_PATH);
        if (!Files.exists(appPath) || !Files.exists(STARTUP_PATH.getParent())) {
            return;
        }

        if (Files.exists(STARTUP_PATH)) {
            Files.delete(STARTUP_PATH);
        }

        ShellLink.createLink(
                appPath.toAbsolutePath().toString(),
                STARTUP_PATH.toAbsolutePath().toString());
        LOG.info("Moved application to startup directory");
    }

    /** Interface for the Windows `User32` library, providing access to window manipulation functions. */
    private interface User32 extends Library {

        int GetWindowLong(Pointer hWnd, int nIndex);

        int SetWindowLong(Pointer hWnd, int nIndex, int dwNewLong);

        boolean SetWindowPos(Pointer hWnd, Pointer hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);

        Pointer SetParent(Pointer hWndChild, Pointer hWndNewParent);

        Pointer FindWindowA(String lpClassName, String lpWindowName);
    }
}
