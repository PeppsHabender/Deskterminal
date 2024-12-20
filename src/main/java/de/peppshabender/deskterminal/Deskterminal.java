package de.peppshabender.deskterminal;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import de.peppshabender.deskterminal.settings.DeskterminalSettings;
import de.peppshabender.deskterminal.utils.WindowsUtils;
import generated.r4j.MainResources;
import io.github.peppshabender.r4j.R4J;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main class for initializing and running Deskterminal. */
public class Deskterminal {
    private static final Logger LOG = LoggerFactory.getLogger(Deskterminal.class);

    /** Main application frame used to host the terminal. */
    private final JFrame mainFrame = new JFrame();

    /** The terminal widget used to interact with the terminal. */
    private JediTerminal terminal;

    /**
     * Private constructor for initializing the application. Sets up the look and feel, main frame, terminal, and system
     * tray.
     */
    private Deskterminal() {
        LOG.debug("Initializing Deskterminal...");
        LafManager.installTheme(new OneDarkTheme()); // Install the OneDark theme
        initMainFrame(); // Initialize the main frame
        initTerminal(); // Initialize the terminal
        LOG.info("Initialized Deskterminal!");
    }

    /**
     * Initializes the main application window (JFrame). Configures window settings such as size, position,
     * transparency, and event listeners.
     */
    @SneakyThrows
    private void initMainFrame() {
        LOG.debug("Initializing main frame...");
        this.mainFrame.setIconImage(ImageIO.read(R4J.asUrl(MainResources.DESKTERMINAL)));

        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setUndecorated(true); // Make the window undecorated
        this.mainFrame.setBackground(new Color(0, 0, 0, 0));

        final DeskterminalSettings settings = DeskterminalSettings.get();
        this.mainFrame.setSize(settings.getWidth(), settings.getHeight());
        this.mainFrame.setLocation(settings.getX(), settings.getY());

        // Add a listener to move the window to the background when activated
        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                WindowsUtils.moveToBackground(Deskterminal.this.mainFrame);
                Deskterminal.this.terminal.requestFocus(); // Focus the terminal
            }
        });
        LOG.debug("Initialized main frame!");
    }

    /**
     * Initializes the terminal component. Configures the terminal to use a pseudo-terminal and sets its visual
     * appearance.
     */
    private void initTerminal() {
        LOG.debug("Initializing jediterm...");
        this.terminal = new JediTerminal(this.mainFrame);
        this.terminal.setTtyConnector(createTtyConnector()); // Set the terminal's TTY connector
        this.terminal.setOpaque(false); // Set the terminal to be transparent
        this.terminal.setBackground(new Color(0, 0, 0, 0)); // Set the background to transparent
        LOG.debug("Initialized jediterm!");
    }

    /**
     * Creates a TTY connector for the terminal using a specified command. This method creates a pseudo-terminal (PTY)
     * process and connects it to the terminal.
     *
     * @return The {@link TtyConnector} instance used to communicate with the terminal process.
     */
    private TtyConnector createTtyConnector() {
        return createTtyConnector(DeskterminalSettings.get().getCommand().split(" "));
    }

    /**
     * Creates a TTY connector for the terminal using a specified command. This method creates a pseudo-terminal (PTY)
     * process and connects it to the terminal.
     *
     * @param command Command to start the connector with
     * @return The {@link TtyConnector} instance used to communicate with the terminal process.
     */
    @SneakyThrows
    private TtyConnector createTtyConnector(final String[] command) {
        final DeskterminalSettings settings = DeskterminalSettings.get();
        final FontMetrics font = this.terminal.getFontMetrics(settings.getFont());
        final PtyProcessBuilder processBuilder = new PtyProcessBuilder()
                .setCommand(command)
                // Roughly approximate the column and row size here without any padding so we don't overshoot
                .setInitialColumns(DeskterminalSettings.get().getWidth() / font.charWidth('M') - 1)
                .setInitialRows(DeskterminalSettings.get().getHeight() / font.getHeight() - 1)
                .setWindowsAnsiColorEnabled(true)
                .setEnvironment(System.getenv());
        if (settings.getInitialDirectory() != null) {
            processBuilder.setDirectory(settings.getInitialDirectory());
        }

        try {
            final PtyProcess process = processBuilder.start();
            // Start a separate thread to wait for the process to exit
            new Thread(() -> waitFor(process)).start();

            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            final String[] cmd = new String[] {"cmd.exe"};
            if (Arrays.equals(cmd, command)) {
                System.exit(1);
            }

            LOG.error("Failed to create pty process, falling back to cmd.exe...", e);
            return createTtyConnector(cmd);
        }
    }

    @SneakyThrows
    private void waitFor(final Process process) {
        process.waitFor();

        if (DeskterminalSettings.get().isExitOnExit()) {
            LOG.info("Process ended.. Exiting gracefully.");
            System.exit(0);
            return;
        }

        LOG.info("Process ended.. Creating new one");
        this.terminal.getTerminal().reset(true);

        final TtyConnector connector = createTtyConnector();
        this.terminal.setTtyConnector(connector);
        this.terminal.start();
    }

    /**
     * Starts the application by making the main frame visible and starting the terminal. Also ensures that the window
     * is styled correctly using the Windows API.
     */
    public void run() {
        LOG.info("Running deskterminal...");
        this.mainFrame.setVisible(true);

        WindowsUtils.unstyleFrame(this.mainFrame); // Unstyle the window (remove border and other styles)
        WindowsUtils.moveToBackground(this.mainFrame); // Move the window to the background

        this.terminal.start();
    }

    /**
     * Main entry point of the application. Initializes and runs the Deskterminal application.
     *
     * @param args Command-line arguments (not used in this case).
     */
    public static void main(String[] args) {
        new Deskterminal().run(); // Run the application
    }
}
