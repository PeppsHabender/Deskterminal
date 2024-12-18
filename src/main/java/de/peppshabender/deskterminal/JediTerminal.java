package de.peppshabender.deskterminal;

import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalPanel;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import de.peppshabender.deskterminal.settings.JediTermSettingsProvider;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import org.jetbrains.annotations.NotNull;

/**
 * A custom implementation of {@link JediTermWidget} for creating a specialized terminal widget. This class customizes
 * the appearance and behavior of the terminal, such as hiding the scroll bar and providing a simplified context menu.
 */
public class JediTerminal extends JediTermWidget {

    private final JFrame mainFrame;

    /** Constructs a new instance of {@link JediTerminal}, initializing it with a custom settings provider. */
    public JediTerminal(final JFrame mainFrame) {
        super(new JediTermSettingsProvider());

        this.mainFrame = mainFrame;
        this.mainFrame.getContentPane().add(this);
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
        return new JediTerminalPanel(this, settingsProvider, terminalTextBuffer, styleState);
    }

    JFrame getMainFrame() {
        return this.mainFrame;
    }
}
