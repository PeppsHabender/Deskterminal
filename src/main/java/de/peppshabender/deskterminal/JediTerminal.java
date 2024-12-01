package de.peppshabender.deskterminal;

import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalActionProvider;
import com.jediterm.terminal.ui.TerminalPanel;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import de.peppshabender.deskterminal.settings.JediTermSettingsProvider;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import org.jetbrains.annotations.NotNull;

/**
 * A custom implementation of {@link JediTermWidget} for creating a specialized terminal widget. This class customizes
 * the appearance and behavior of the terminal, such as hiding the scroll bar and providing a simplified context menu.
 */
public class JediTerminal extends JediTermWidget {

    /** Constructs a new instance of {@link JediTerminal}, initializing it with a custom settings provider. */
    public JediTerminal() {
        super(new JediTermSettingsProvider());
    }

    /**
     * Creates a scroll bar for the terminal widget.
     *
     * <p>Overrides the default behavior to hide the scroll bar from view.
     *
     * @return A {@link JScrollBar} instance that is hidden by default.
     */
    @Override
    protected JScrollBar createScrollBar() {
        final JScrollBar scrollBar = new JScrollBar();
        scrollBar.setVisible(false);
        return scrollBar;
    }

    /**
     * Creates a custom {@link TerminalPanel} for rendering terminal content.
     *
     * <p>The custom panel allows for additional UI customizations, such as modifying the context menu behavior.
     *
     * @param settingsProvider The settings provider for terminal configuration.
     * @param styleState The state of styles used in the terminal.
     * @param terminalTextBuffer The text buffer that stores terminal content.
     * @return A {@link TerminalPanel} instance with custom behavior.
     */
    @Override
    protected TerminalPanel createTerminalPanel(
            @NotNull SettingsProvider settingsProvider,
            @NotNull StyleState styleState,
            @NotNull TerminalTextBuffer terminalTextBuffer) {
        return new JediTerminalPanel(settingsProvider, terminalTextBuffer, styleState);
    }

    /**
     * A custom implementation of {@link TerminalPanel} for the terminal widget.
     *
     * <p>This panel provides additional UI customization, such as hiding the context menu by default.
     */
    private static class JediTerminalPanel extends TerminalPanel {

        /**
         * Constructs a new {@link JediTerminalPanel} with the given settings, text buffer, and style state.
         *
         * @param settingsProvider The settings provider for terminal configuration.
         * @param terminalTextBuffer The text buffer that stores terminal content.
         * @param styleState The state of styles used in the terminal.
         */
        public JediTerminalPanel(
                @NotNull SettingsProvider settingsProvider,
                @NotNull TerminalTextBuffer terminalTextBuffer,
                @NotNull StyleState styleState) {
            super(settingsProvider, terminalTextBuffer, styleState);
        }

        /**
         * Creates a context menu for the terminal panel.
         *
         * <p>Overrides the default behavior to initially hide the context menu and then calls the superclass
         * implementation.
         *
         * @param actionProvider The action provider for the terminal.
         * @return A {@link JPopupMenu} instance for the context menu.
         */
        @Override
        protected @NotNull JPopupMenu createPopupMenu(@NotNull TerminalActionProvider actionProvider) {
            final JPopupMenu menu = new JPopupMenu();
            menu.setVisible(false);
            return super.createPopupMenu(actionProvider);
        }
    }
}
