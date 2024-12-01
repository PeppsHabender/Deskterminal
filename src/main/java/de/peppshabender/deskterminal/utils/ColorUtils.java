package de.peppshabender.deskterminal.utils;

import com.jediterm.core.Color;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.emulator.ColorPalette;
import de.peppshabender.deskterminal.settings.DeskterminalSettings;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for handling color-related operations and conversions. Provides methods for converting between various
 * color types, managing transparency, and extracting a terminal color palette from {@link DeskterminalSettings}.
 */
@UtilityClass
public class ColorUtils {

    /**
     * Extracts a terminal color palette from the provided settings.
     *
     * @param settings The {@link DeskterminalSettings} instance containing the color configuration.
     * @return A {@link ColorPalette} object populated with the terminal's color palette.
     */
    public static ColorPalette extractPalette(final DeskterminalSettings settings) {
        final ColorPaletteImpl palette = new ColorPaletteImpl();
        palette.colors[0] = settings.getBlack();
        palette.colors[1] = settings.getRed();
        palette.colors[2] = settings.getGreen();
        palette.colors[3] = settings.getYellow();
        palette.colors[4] = settings.getBlue();
        palette.colors[5] = settings.getMagenta();
        palette.colors[6] = settings.getCyan();
        palette.colors[7] = settings.getWhite();
        palette.colors[8] = settings.getBrightBlack();
        palette.colors[9] = settings.getBrightRed();
        palette.colors[10] = settings.getBrightGreen();
        palette.colors[11] = settings.getBrightYellow();
        palette.colors[12] = settings.getBrightBlue();
        palette.colors[13] = settings.getBrightMagenta();
        palette.colors[14] = settings.getBrightCyan();
        palette.colors[15] = settings.getBrightWhite();

        return palette;
    }

    /**
     * Creates a new {@link java.awt.Color} object with the specified alpha value.
     *
     * @param color The original {@link java.awt.Color}.
     * @param alpha The alpha value (transparency) to apply.
     * @return A new {@link java.awt.Color} with the updated alpha value.
     */
    public static java.awt.Color withAlpha(final java.awt.Color color, final int alpha) {
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Converts a {@link java.awt.Color} to a {@link com.jediterm.core.Color}.
     *
     * @param c The {@link java.awt.Color} to convert.
     * @return A {@link com.jediterm.core.Color} equivalent to the input color.
     */
    public static Color convert(final java.awt.Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    /**
     * Converts a {@link com.jediterm.core.Color} to a {@link java.awt.Color}.
     *
     * @param c The {@link com.jediterm.core.Color} to convert.
     * @return A {@link java.awt.Color} equivalent to the input color.
     */
    public static java.awt.Color convert(final Color c) {
        return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    /**
     * Converts a {@link java.awt.Color} to a terminal-compatible {@link TerminalColor}.
     *
     * @param color The {@link java.awt.Color} to convert.
     * @return A {@link TerminalColor} wrapping the input color.
     */
    public static TerminalColor toTerminalColor(final java.awt.Color color) {
        return new TerminalColor(() -> convert(color));
    }

    /**
     * Converts a {@link com.jediterm.core.Color} to a terminal-compatible {@link TerminalColor}.
     *
     * @param color The {@link com.jediterm.core.Color} to convert.
     * @return A {@link TerminalColor} wrapping the input color.
     */
    public static TerminalColor toTerminalColor(final Color color) {
        return new TerminalColor(() -> color);
    }

    /** Private implementation of {@link ColorPalette} for managing a terminal color palette. */
    private static final class ColorPaletteImpl extends ColorPalette {
        private final Color[] colors = new Color[16]; // Stores 16 ANSI colors.

        /**
         * Retrieves the foreground color by its index in the color palette.
         *
         * @param colorIndex The index of the color (0-15).
         * @return The corresponding {@link Color}.
         */
        @Override
        protected @NotNull Color getForegroundByColorIndex(int colorIndex) {
            return this.colors[colorIndex];
        }

        /**
         * Retrieves the background color by its index in the color palette.
         *
         * @param colorIndex The index of the color (0-15).
         * @return The corresponding {@link Color}.
         */
        @NotNull
        @Override
        protected Color getBackgroundByColorIndex(int colorIndex) {
            return this.colors[colorIndex];
        }
    }
}
