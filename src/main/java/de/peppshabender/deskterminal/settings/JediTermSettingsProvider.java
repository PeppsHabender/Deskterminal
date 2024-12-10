package de.peppshabender.deskterminal.settings;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import de.peppshabender.deskterminal.utils.ColorUtils;
import java.awt.Font;
import org.jetbrains.annotations.NotNull;

/**
 * Provides custom terminal settings for the JediTerm terminal emulator. This class extends
 * {@link DefaultSettingsProvider} to override default configurations, integrating settings from
 * {@link DeskterminalSettings}.
 *
 * <p>Settings include:
 *
 * <ul>
 *   <li>Default text styles (foreground and background colors).
 *   <li>Color palette for terminal ANSI colors.
 *   <li>Selection colors.
 *   <li>Font settings (family and size).
 * </ul>
 */
public class JediTermSettingsProvider extends DefaultSettingsProvider {
    @Override
    public @NotNull TextStyle getDefaultStyle() {
        return new TextStyle(getDefaultForeground(), getDefaultBackground());
    }

    @NotNull
    @Override
    public TerminalColor getDefaultBackground() {
        return ColorUtils.toTerminalColor(DeskterminalSettings.get().getBackgroundColor());
    }

    @NotNull
    @Override
    public TerminalColor getDefaultForeground() {
        return ColorUtils.toTerminalColor(DeskterminalSettings.get().getForegroundColor());
    }

    @Override
    public ColorPalette getTerminalColorPalette() {
        return ColorUtils.extractPalette(DeskterminalSettings.get());
    }

    @Override
    public boolean useInverseSelectionColor() {
        return false;
    }

    @NotNull
    @Override
    public TextStyle getSelectionColor() {
        final DeskterminalSettings settings = DeskterminalSettings.get();

        return new TextStyle(
                settings.getSelectionForeground() == null
                        ? null
                        : ColorUtils.toTerminalColor(settings.getSelectionForeground()),
                ColorUtils.toTerminalColor(settings.getSelectionBackground()));
    }

    @Override
    public Font getTerminalFont() {
        return DeskterminalSettings.get().getFont();
    }

    @Override
    public float getTerminalFontSize() {
        return DeskterminalSettings.get().getFontSize();
    }
}
