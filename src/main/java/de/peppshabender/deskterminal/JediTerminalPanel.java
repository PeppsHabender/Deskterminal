package de.peppshabender.deskterminal;

import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.TerminalActionProvider;
import com.jediterm.terminal.ui.TerminalPanel;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import de.peppshabender.deskterminal.settings.DeskterminalSettings;
import de.peppshabender.deskterminal.settings.DeskterminalSettingsEditor;
import de.peppshabender.deskterminal.utils.ColorUtils;
import de.peppshabender.deskterminal.utils.WindowsUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom implementation of {@link TerminalPanel} for the terminal widget.
 *
 * <p>This panel provides additional UI customization, such as hiding the context menu by default.
 */
class JediTerminalPanel extends TerminalPanel {
    private static final Logger LOG = LoggerFactory.getLogger(JediTerminalPanel.class);

    private final JediTerminal terminal;
    private Component[] mainFrameComponents = new Component[0];

    public JediTerminalPanel(
            JediTerminal terminal,
            @NotNull SettingsProvider settingsProvider,
            @NotNull TerminalTextBuffer terminalTextBuffer,
            @NotNull StyleState styleState) {
        super(settingsProvider, terminalTextBuffer, styleState);

        this.terminal = terminal;
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

        final JMenuItem configItem = menu.add("Edit Configuration");
        configItem.addActionListener(e -> toggleDecoration(this.terminal.getMainFrame()));
        addCustomItems(menu, configItem);

        return menu;
    }

    private void addCustomItems(final JPopupMenu menu, final JMenuItem... more) {
        if (!WindowsUtils.isAutoStart()) {
            final JCheckBoxMenuItem autoStartItem = new JCheckBoxMenuItem("Autostart");
            autoStartItem.setSelected(WindowsUtils.isAutoStart());
            menu.add(autoStartItem);
            autoStartItem.addActionListener(e -> {
                WindowsUtils.toggleAutoStart();
                autoStartItem.setSelected(WindowsUtils.isAutoStart());
            });
        }

        Arrays.stream(more).forEach(menu::add);

        menu.addSeparator();

        final JMenuItem exitItem = menu.add("Exit");
        exitItem.addActionListener(e -> System.exit(0));
    }

    private void toggleDecoration(final JFrame mainFrame) {
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
    }

    private void showTerminal(final JFrame mainFrame) {
        LOG.debug("Showing terminal...");
        mainFrame.setBackground(ColorUtils.withAlpha(mainFrame.getBackground(), 0));
        mainFrame.getContentPane().removeAll();
        Arrays.stream(this.mainFrameComponents).forEach(mainFrame.getContentPane()::add);
        this.mainFrameComponents = new Component[0];

        WindowsUtils.unstyleFrame(mainFrame);
    }

    private void hideTerminal(final JFrame mainFrame) {
        LOG.debug("Showing configuration editor...");
        this.mainFrameComponents = mainFrame.getContentPane().getComponents();
        mainFrame.getContentPane().removeAll();

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        final Border border = BorderFactory.createLineBorder(Color.WHITE, 3);
        panel.setBorder(border);

        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem saveConfig = saveConfigItem(mainFrame);

        final JMenuItem cancelItem = new JMenuItem("Cancel");
        cancelItem.addActionListener(e -> {
            DeskterminalSettings.reset();
            toggleDecoration(mainFrame);
        });
        addCustomItems(popupMenu, saveConfig, cancelItem);
        panel.setComponentPopupMenu(popupMenu);

        final DeskterminalSettingsEditor editor = new DeskterminalSettingsEditor();
        editor.setInheritsPopupMenu(true);
        panel.add(editor, BorderLayout.CENTER);

        mainFrame.getContentPane().add(panel);
    }

    private JMenuItem saveConfigItem(JFrame mainFrame) {
        final JMenuItem saveConfig = new JMenuItem("Save Configuration     ");
        saveConfig.addActionListener(e -> {
            // Save the window's new size and position
            final DeskterminalSettings settings = DeskterminalSettings.get();
            settings.setX(mainFrame.getX());
            settings.setY(mainFrame.getY());
            settings.setWidth(mainFrame.getWidth());
            settings.setHeight(mainFrame.getHeight());
            DeskterminalSettings.store();

            toggleDecoration(mainFrame);
        });

        return saveConfig;
    }
}
