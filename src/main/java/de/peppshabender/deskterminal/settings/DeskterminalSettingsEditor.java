package de.peppshabender.deskterminal.settings;

import com.jediterm.core.Color;
import de.peppshabender.deskterminal.utils.ColorUtils;
import de.peppshabender.deskterminal.utils.swing.ColoredCircle;
import de.peppshabender.deskterminal.utils.swing.WrapLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.SneakyThrows;

/** A Swing-based panel for editing DeskterminalSettings dynamically using reflection. */
public class DeskterminalSettingsEditor extends JPanel {
    private final DeskterminalSettings settings = DeskterminalSettings.get();
    /** Constructs a settings editor panel. */
    @SneakyThrows
    public DeskterminalSettingsEditor() {
        setLayout(new BorderLayout());

        final JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));

        Field prev = null;
        JPanel currPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        // Reflectively analyze DeskterminalSettings fields
        for (final Field field : DeskterminalSettings.class.getDeclaredFields()) {
            if (prev == null) {
                prev = field;
            } else if (!prev.getType().equals(field.getType())) {
                editorPanel.add(currPanel);
                currPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));

                prev = field;
            }

            if (Modifier.isStatic(field.getModifiers())) {
                continue; // Ignore static fields
            }

            field.setAccessible(true);

            final JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            fieldPanel.add(new JLabel(field.getName() + ":"));

            final Component editorComponent = createEditorComponent(field, settings);
            fieldPanel.add(editorComponent);

            currPanel.add(fieldPanel);
        }

        editorPanel.add(currPanel);

        final JScrollPane scrollPane = new JScrollPane(
                editorPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setInheritsPopupMenu(true);

        add(scrollPane, BorderLayout.NORTH);
    }

    /** Creates an appropriate editor component for the field. */
    private Component createEditorComponent(Field field, DeskterminalSettings settings) throws IllegalAccessException {
        final Class<?> type = field.getType();
        final Object value = field.get(settings);

        if (int.class.equals(type)) {
            final JTextField textField = new JTextField(value.toString(), 10);
            textField.getDocument().addDocumentListener(new FieldChangeListener<>(field, textField, Integer::parseInt));

            return textField;
        } else if (String.class.equals(type)) {
            final JTextField textField = new JTextField(value.toString(), 20);
            textField
                    .getDocument()
                    .addDocumentListener(new FieldChangeListener<>(field, textField, Function.identity()));

            return textField;
        } else if (boolean.class.equals(type)) {
            final JCheckBox checkBox = new JCheckBox("", (boolean) value);
            checkBox.addActionListener(e -> setSettingsField(field, checkBox.isSelected()));

            return checkBox;
        } else if (Color.class.equals(type)) {
            return createColorPicker(field, ColorUtils.convert((Color) value));
        } else if (File.class.equals(type)) {
            return createFilePicker(
                    settings.getInitialDirectory() == null
                            ? new File("").getAbsolutePath()
                            : settings.getInitialDirectory(),
                    "...",
                    JFileChooser.DIRECTORIES_ONLY,
                    settings::setInitialDirectory);
        }

        throw new UnsupportedOperationException();
    }

    private Component createColorPicker(final Field field, final java.awt.Color base) {
        // Custom color picker circle panel
        final ColoredCircle circle = new ColoredCircle(base);

        // Open color chooser on click
        circle.addMouseListener(new MouseAdapter() {
            @Override
            @SneakyThrows
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                final java.awt.Color chosenColor = JColorChooser.showDialog(null, "Choose a Color", base);
                if (chosenColor != null) {
                    // Update settings and repaint the circle
                    setSettingsField(
                            field,
                            new Color(
                                    chosenColor.getRed(),
                                    chosenColor.getGreen(),
                                    chosenColor.getBlue(),
                                    chosenColor.getAlpha()));
                    circle.repaint(chosenColor);
                }
            }
        });

        return circle;
    }

    private JComponent createFilePicker(
            final String inDirectory, final String label, final int selectionMode, final Consumer<File> onSelect) {
        final JPanel panel = new JPanel();
        final JLabel jlabel = new JLabel(inDirectory);
        panel.add(jlabel);

        final JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(30, 30));
        panel.add(button);

        button.addActionListener(e -> {
            final JFileChooser chooser = new JFileChooser();
            if (settings.getInitialDirectory() != null) {
                chooser.setCurrentDirectory(new File(settings.getInitialDirectory()));
            }
            chooser.setFileSelectionMode(selectionMode);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                onSelect.accept(chooser.getSelectedFile());
                jlabel.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        return panel;
    }

    @SneakyThrows
    private void setSettingsField(final Field field, final Object value) {
        field.set(this.settings, value);
    }

    private final class FieldChangeListener<T> implements DocumentListener {
        private final Field field;
        private final JTextField parent;
        private final Function<String, T> converter;

        public FieldChangeListener(final Field field, final JTextField parent, final Function<String, T> converter) {
            this.field = field;
            this.parent = parent;
            this.converter = converter;
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            changeField();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            changeField();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            changeField();
        }

        private void changeField() {
            try {
                final T got = converter.apply(this.parent.getText());
                setSettingsField(this.field, got);
            } catch (final Throwable ignored) {
                //
            }
        }
    }
}
