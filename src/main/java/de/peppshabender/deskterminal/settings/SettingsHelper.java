package de.peppshabender.deskterminal.settings;

import com.jediterm.core.Color;
import de.peppshabender.deskterminal.utils.ColorUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * Utility class for serializing and deserializing {@link DeskterminalSettings}. Provides methods to save settings to a
 * file and load settings from a file.
 *
 * <p>The settings are stored in a key-value format, with support for primitive types and custom objects like
 * {@link Color}. This class uses reflection to dynamically access fields of the {@link DeskterminalSettings} class.
 *
 * <h2>Features:</h2>
 *
 * <ul>
 *   <li>Stores settings to a file in a readable key-value format.
 *   <li>Loads settings from a file and applies them to a {@link DeskterminalSettings} instance.
 *   <li>Handles custom parsing for {@link Color} fields and other data types.
 * </ul>
 */
@UtilityClass
class SettingsHelper {

    /**
     * Regular expression pattern for parsing RGBA color values. Matches strings in the format: `rgba(r, g, b, a)` or
     * `rgb(r, g, b)`.
     */
    private static final Pattern COLOR_RGX =
            Pattern.compile("rgb(a?)\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");

    /**
     * Stores the settings to the specified file.
     *
     * @param to The {@link Path} to the file where settings should be stored.
     * @param settings The {@link DeskterminalSettings} object to serialize.
     */
    public static void store(final Path to, final DeskterminalSettings settings) {
        final List<String> props = new ArrayList<>();

        final Field[] fields = DeskterminalSettings.class.getDeclaredFields();
        Class<?> prev = null;

        for (final Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue; // Skip static fields
            }

            if (!field.getType().equals(prev)) {
                if (prev != null) props.add(""); // Add a blank line for readability
                prev = field.getType();
            }

            try {
                field.setAccessible(true);

                Object value = field.get(settings);
                if (value == null) {
                    continue;
                }

                if (Color.class.equals(field.getType())) {
                    // Convert Color to rgba format
                    final Color color = (Color) value;
                    value = "rgba("
                            + color.getRed()
                            + ","
                            + color.getGreen()
                            + ","
                            + color.getBlue()
                            + ","
                            + color.getAlpha()
                            + ")";
                }

                props.add(field.getName() + "=" + value);
            } catch (IllegalAccessException e) {
                // Handle access exceptions silently
            } finally {
                field.setAccessible(false);
            }
        }

        store(to, props);
    }

    /**
     * Writes the serialized settings to the specified file.
     *
     * @param to The {@link Path} to the file.
     * @param props A list of strings representing serialized settings.
     */
    private static void store(final Path to, final List<String> props) {
        try (final OutputStream os = Files.newOutputStream(to)) {
            Files.writeString(to, "", StandardOpenOption.TRUNCATE_EXISTING); // Clear the file
            props.forEach(el -> {
                try {
                    os.write((el + "\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    // Handle write exceptions silently
                }
            });
            os.flush();
        } catch (IOException e) {
            // Handle file-related exceptions silently
        }
    }

    /**
     * Loads settings from the specified file and applies them to the given settings instance.
     *
     * @param settings The {@link DeskterminalSettings} instance to populate.
     * @param from The {@link Path} to the file containing the settings.
     */
    public static void load(final DeskterminalSettings settings, final Path from) {
        try (final InputStream is = Files.newInputStream(from)) {
            final Properties props = new Properties();
            props.load(is);
            props.forEach((k, v) -> setField(settings, k, v));
        } catch (final IOException e) {
            // Handle file read exceptions silently
        }
    }

    /**
     * Sets a field in the settings object using reflection.
     *
     * @param settings The {@link DeskterminalSettings} object to modify.
     * @param key The field name (key) from the properties file.
     * @param value The value to set for the field.
     */
    private static void setField(final DeskterminalSettings settings, final Object key, Object value) {
        try {
            final Field field = DeskterminalSettings.class.getDeclaredField(key.toString());
            if (Modifier.isStatic(field.getModifiers())) return; // Skip static fields

            field.setAccessible(true);

            // Handle field type conversion
            if (int.class.equals(field.getType())) {
                value = Integer.parseInt((String) value);
            } else if (Color.class.equals(field.getType())) {
                value = fromString((String) value);
            } else if (boolean.class.equals(field.getType())) {
                value = Boolean.valueOf((String) value);
            } else if (File.class.equals(field.getType())) {
                value = new File((String) value);
            }

            if (value != null) field.set(settings, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Handle reflection-related exceptions silently
            System.out.println("krhsgikh");
        }
    }

    private Color fromString(final String value) {
        if (value.startsWith("#")) {
            return ColorUtils.convert(java.awt.Color.decode(value));
        }

        final Matcher matcher = COLOR_RGX.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        final int r = Integer.parseInt(matcher.group(2));
        final int g = Integer.parseInt(matcher.group(3));
        final int b = Integer.parseInt(matcher.group(4));
        final int a = matcher.group(1).isEmpty() ? 255 : Integer.parseInt(matcher.group(5));

        return new Color(r, g, b, a);
    }
}
