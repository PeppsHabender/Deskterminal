package de.peppshabender.deskterminal;

import de.peppshabender.deskterminal.settings.DeskterminalSettings;
import de.peppshabender.deskterminal.utils.ColorUtils;
import de.peppshabender.deskterminal.utils.WinApiUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * A custom tray menu for interacting with the application, typically used with a system tray icon. This class allows
 * users to toggle window decorations (resize ability) and edit the terminal settings. It provides a right-click menu
 * with options that control the window's appearance and settings.
 */
public class TrayMenu extends PopupMenu {
    private static final String RESTART_STR = "Restart";

    private Component[] mainFrameComponents = new Component[0];
    private MenuItem restartItem;

    /**
     * Constructs a new {@link TrayMenu} for the given main window frame.
     *
     * <p>Initializes the menu with options to toggle window decoration and open the settings file.
     *
     * @param mainFrame The main {@link JFrame} of the application.
     */
    public TrayMenu(final JFrame mainFrame) {
        final MenuItem toggleDecoration = new MenuItem("Enable Resize");
        toggleDecoration.addActionListener(e -> toggleDecoration(toggleDecoration, mainFrame));

        final MenuItem editSettings = new MenuItem("Edit Settings");
        editSettings.addActionListener(e -> {
            try {
                Desktop.getDesktop().edit(DeskterminalSettings.SETTINGS_PATH.toFile());
            } catch (IOException ex) {
                // Handle error (no action specified in case of failure)
            }
        });

        final MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        add(toggleDecoration);
        add(editSettings);
        add(exit);
    }

    private void toggleDecoration(final MenuItem parent, final JFrame mainFrame) {
        // Store current window size and position
        mainFrame.dispose();

        mainFrame.setBackground(ColorUtils.withAlpha(mainFrame.getBackground(), 255));
        mainFrame.setUndecorated(!mainFrame.isUndecorated());
        mainFrame.setVisible(true);

        if (mainFrame.isUndecorated()) {
            showTerminal(mainFrame);
            parent.setLabel("Enable Resize");
        } else {
            hideTerminal(mainFrame);
            parent.setLabel("Save Position/Size");
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

        WinApiUtils.unstyleFrame(mainFrame);
    }

    private void hideTerminal(final JFrame mainFrame) {
        this.mainFrameComponents = mainFrame.getContentPane().getComponents();
        mainFrame.getContentPane().removeAll();

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        final Border border = BorderFactory.createLineBorder(Color.WHITE, 3);
        panel.setBorder(border);

        final ImageIcon img = new ImageIcon(TrayMenu.class.getResource("/deskterminal.png"));
        panel.add(new JLabel(img), BorderLayout.CENTER);

        mainFrame.getContentPane().add(panel);
    }
}
