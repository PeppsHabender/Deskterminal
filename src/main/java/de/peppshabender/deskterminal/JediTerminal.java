package de.peppshabender.deskterminal;

import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalActionProvider;
import com.jediterm.terminal.ui.TerminalPanel;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import de.peppshabender.deskterminal.settings.DeskterminalSettings;
import de.peppshabender.deskterminal.settings.JediTermSettingsProvider;
import de.peppshabender.deskterminal.utils.ColorUtils;
import de.peppshabender.deskterminal.utils.WindowsUtils;
import generated.r4j.MainResources;
import io.github.peppshabender.r4j.R4J;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
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
        return new JediTerminalPanel(settingsProvider, terminalTextBuffer, styleState);
    }

    /**
     * A custom implementation of {@link TerminalPanel} for the terminal widget.
     *
     * <p>This panel provides additional UI customization, such as hiding the context menu by default.
     */
    private class JediTerminalPanel extends TerminalPanel {

        private Component[] mainFrameComponents = new Component[0];

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
            final JPopupMenu menu = super.createPopupMenu(actionProvider);
            menu.addSeparator();
            addCustomItems(menu, "Move/Resize");

            return menu;
        }

        private void addCustomItems(final JPopupMenu menu, final String resizeLabel) {
            final JMenuItem resizeItem = menu.add(resizeLabel);
            resizeItem.addActionListener(e -> toggleDecoration(resizeItem, JediTerminal.this.mainFrame));

            final JMenuItem editItem = menu.add("Edit Settings");
            editItem.addActionListener(e -> {
                try {
                    Desktop.getDesktop().edit(DeskterminalSettings.SETTINGS_PATH.toFile());
                } catch (final IOException ignored) {
                }
            });

            final JCheckBoxMenuItem autoStartItem = new JCheckBoxMenuItem("Autostart");
            autoStartItem.setSelected(WindowsUtils.isAutoStart());
            menu.add(autoStartItem);
            autoStartItem.addActionListener(e -> {
                WindowsUtils.toggleAutoStart();
                autoStartItem.setSelected(WindowsUtils.isAutoStart());
            });

            menu.addSeparator();

            final JMenuItem exitItem = menu.add("Exit");
            exitItem.addActionListener(e -> System.exit(0));
        }

        private void toggleDecoration(final JMenuItem parent, final JFrame mainFrame) {
            // Store current window size and position
            mainFrame.dispose();

            mainFrame.setBackground(ColorUtils.withAlpha(mainFrame.getBackground(), 255));
            mainFrame.setUndecorated(!mainFrame.isUndecorated());
            mainFrame.setVisible(true);

            if (mainFrame.isUndecorated()) {
                showTerminal(mainFrame);
            } else {
                hideTerminal(mainFrame);
            }

            // Save the window's new size and position
            final DeskterminalSettings settings = DeskterminalSettings.get();
            settings.setX(mainFrame.getX());
            settings.setY(mainFrame.getY());
            settings.setWidth(mainFrame.getWidth());
            settings.setHeight(mainFrame.getHeight());
            DeskterminalSettings.store();
        }

        private void showTerminal(final JFrame mainFrame) {
            mainFrame.setBackground(ColorUtils.withAlpha(mainFrame.getBackground(), 0));
            mainFrame.getContentPane().removeAll();
            Arrays.stream(this.mainFrameComponents).forEach(mainFrame.getContentPane()::add);
            this.mainFrameComponents = new Component[0];

            WindowsUtils.unstyleFrame(mainFrame);
        }

        private void hideTerminal(final JFrame mainFrame) {
            this.mainFrameComponents = mainFrame.getContentPane().getComponents();
            mainFrame.getContentPane().removeAll();

            final JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            final Border border = BorderFactory.createLineBorder(Color.WHITE, 3);
            panel.setBorder(border);

            final ImageIcon img = new ImageIcon(R4J.asUrl(MainResources.DESKTERMINAL));
            panel.add(new JLabel(img), BorderLayout.CENTER);

            final JPopupMenu popupMenu = new JPopupMenu();
            addCustomItems(popupMenu, "Save Position/Size              ");
            panel.setComponentPopupMenu(popupMenu);

            mainFrame.getContentPane().add(panel);
        }
    }
}
