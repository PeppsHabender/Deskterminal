package de.peppshabender.deskterminal.settings;

import com.jediterm.core.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for the Deskterminal. Provides default values for terminal settings such as position, size, font,
 * colors, and command to execute. Settings are loaded from or saved to a configuration file.
 *
 * <p>The settings file is located at {@code <user.home>/deskterminal.ini}. The class provides a singleton instance to
 * manage settings globally within the application.
 */
@Data
@NoArgsConstructor
public class DeskterminalSettings {

    /** The path to the settings file in the user's home directory. */
    public static final Path SETTINGS_PATH = Path.of(System.getProperty("user.home"), "deskterminal.ini");

    /** Singleton instance of the settings. */
    private static DeskterminalSettings INSTANCE;

    /** X-coordinate of the terminal window's position. */
    private int x = 0;
    /** Y-coordinate of the terminal window's position. */
    private int y = 0;
    /** Width of the terminal window in pixels. */
    private int width = 600;
    /** Height of the terminal window in pixels. */
    private int height = 800;
    /** Font size used in the terminal. */
    private int fontSize = 14;

    /** Lets the terminal exit, when e.g. an 'exit' cmd is typed */
    private boolean exitOnExit = false;

    /** The default command to execute in the terminal. */
    private String command = "powershell.exe";

    /** The font family used for terminal text. */
    private String fontFamily = "Consolas";

    /** Background color of the terminal. */
    private Color backgroundColor = new Color(0, 0, 0, 1);
    /** Foreground color of the terminal. */
    private Color foregroundColor = new Color(0xffffff);
    /** Background color for selected text. */
    private Color selectionBackground = new Color(0, 0, 0, 100);
    /** Foreground color for selected text. */
    private Color selectionForeground = null;

    /** ANSI black color. */
    private Color black = new Color(0x000000);
    /** ANSI red color. */
    private Color red = new Color(0x800000);
    /** ANSI green color. */
    private Color green = new Color(0x008000);
    /** ANSI yellow color. */
    private Color yellow = new Color(0x808000);
    /** ANSI blue color. */
    private Color blue = new Color(0x000080);
    /** ANSI magenta color. */
    private Color magenta = new Color(0x800080);
    /** ANSI cyan color. */
    private Color cyan = new Color(0x008080);
    /** ANSI white color. */
    private Color white = new Color(0xc0c0c0);

    /** Bright ANSI black color. */
    private Color brightBlack = new Color(0x808080);
    /** Bright ANSI red color. */
    private Color brightRed = new Color(0xff0000);
    /** Bright ANSI green color. */
    private Color brightGreen = new Color(0x00ff00);
    /** Bright ANSI yellow color. */
    private Color brightYellow = new Color(0xffff00);
    /** Bright ANSI blue color. */
    private Color brightBlue = new Color(0x4682b4);
    /** Bright ANSI magenta color. */
    private Color brightMagenta = new Color(0xff00ff);
    /** Bright ANSI cyan color. */
    private Color brightCyan = new Color(0x00ffff);
    /** Bright ANSI white color. */
    private Color brightWhite = new Color(0xffffff);

    public Font getFont() {
        return new Font(this.fontFamily, Font.PLAIN, this.fontSize);
    }

    /**
     * Retrieves the singleton instance of the DeskterminalSettings. If the settings file exists, the settings are
     * loaded from it. Otherwise, default settings are created and saved to the file.
     *
     * @return The singleton instance of {@link DeskterminalSettings}.
     */
    public static DeskterminalSettings get() {
        if (INSTANCE != null) return INSTANCE;

        INSTANCE = new DeskterminalSettings();
        if (Files.exists(SETTINGS_PATH)) {
            SettingsHelper.load(INSTANCE, SETTINGS_PATH);
        } else {
            try {
                SettingsHelper.store(Files.createFile(SETTINGS_PATH), INSTANCE);
            } catch (IOException e) {
                // Handle exception silently
            }
        }

        return INSTANCE;
    }

    /** Saves the current settings to the configuration file. If no instance exists, this method does nothing. */
    public static void store() {
        if (INSTANCE == null) return;

        SettingsHelper.store(SETTINGS_PATH, get());
    }
}
