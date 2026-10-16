package com.creamykeys.linux;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class Config {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("CreamyKeys Linux");
    private static final String DEFAULT_KEYBOARD = "cherrymx_red_abs";

    private boolean enabled;
    private String keyboardProfile;
    private String clickProfile;
    private float volume;
    private float pitch;
    private boolean softTail;

    private Config(
            boolean enabled,
            String keyboardProfile,
            String clickProfile,
            float volume,
            float pitch,
            boolean softTail
    ) {
        this.enabled = enabled;
        this.keyboardProfile = keyboardProfile;
        this.clickProfile = clickProfile;
        this.volume = volume;
        this.pitch = pitch;
        this.softTail = softTail;
    }

    static Config load(Logger logger) {
        Path path = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getConfigDir().resolve("creamykeys-linux.properties");
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            } catch (IOException exception) {
                logger.error("Could not read {}. Default CreamyKeys settings will be used.", path, exception);
                return defaults();
            }
        } else {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, """
                        # CreamyKeys Linux client settings
                        enabled=true
                        keyboardProfile=cherrymx_red_abs
                        clickProfile=random
                        volume=0.70
                        pitch=1.00
                        softTail=true
                        """);
                logger.info("Created CreamyKeys settings at {}", path);
            } catch (IOException exception) {
                logger.error("Could not create {}. Default CreamyKeys settings will be used.", path, exception);
            }
        }

        boolean enabled = parseBoolean(properties.getProperty("enabled", "true"), logger);
        String keyboard = validChoice(
                properties.getProperty("keyboardProfile", DEFAULT_KEYBOARD),
                CreamyKeysProfiles.KEYBOARDS,
                DEFAULT_KEYBOARD,
                "keyboardProfile",
                logger
        );
        String click = validChoice(
                properties.getProperty("clickProfile", "random"),
                CreamyKeysProfiles.CLICKS,
                "random",
                "clickProfile",
                logger
        );
        float volume = parseVolume(properties.getProperty("volume", "0.70"), logger);
        float pitch = parseFloat(properties.getProperty("pitch", "1.00"), 0.5F, 1.5F, 1.0F, "pitch", logger);
        boolean softTail = parseBoolean(properties.getProperty("softTail", "true"), logger);
        return new Config(enabled, keyboard, click, volume, pitch, softTail);
    }

    private static boolean parseBoolean(String value, Logger logger) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        logger.warn("Invalid enabled value '{}'; using true.", value);
        return true;
    }

    private static String validChoice(
            String value,
            java.util.List<String> choices,
            String defaultValue,
            String property,
            Logger logger
    ) {
        if (choices.contains(value)) {
            return value;
        }
        logger.warn("Invalid {} value '{}'; using '{}'.", property, value, defaultValue);
        return defaultValue;
    }

    private static float parseVolume(String value, Logger logger) {
        return parseFloat(value, 0.0F, 1.0F, 0.70F, "volume", logger);
    }

    private static float parseFloat(
            String value,
            float minimum,
            float maximum,
            float defaultValue,
            String property,
            Logger logger
    ) {
        try {
            float parsed = Float.parseFloat(value);
            if (Float.isFinite(parsed) && parsed >= minimum && parsed <= maximum) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
        }
        logger.warn("Invalid {} value '{}'; using {}.", property, value, defaultValue);
        return defaultValue;
    }

    private static Config defaults() {
        return new Config(true, DEFAULT_KEYBOARD, "random", 0.70F, 1.0F, true);
    }

    boolean enabled() {
        return enabled;
    }

    String keyboardProfile() {
        return keyboardProfile;
    }

    String clickProfile() {
        return clickProfile;
    }

    float volume() {
        return volume;
    }

    float pitch() {
        return pitch;
    }

    boolean softTail() {
        return softTail;
    }

    void cycleKeyboardProfile() {
        keyboardProfile = next(CreamyKeysProfiles.KEYBOARDS, keyboardProfile);
    }

    void cycleClickProfile() {
        clickProfile = next(CreamyKeysProfiles.CLICKS, clickProfile);
    }

    void toggleEnabled() {
        enabled = !enabled;
    }

    void setVolume(float volume) {
        if (!Float.isFinite(volume) || volume < 0.0F || volume > 1.0F) {
            throw new IllegalArgumentException("Volume must be between 0 and 1.");
        }
        this.volume = volume;
    }

    void setPitch(float pitch) {
        if (!Float.isFinite(pitch) || pitch < 0.5F || pitch > 1.5F) {
            throw new IllegalArgumentException("Pitch must be between 0.5 and 1.5.");
        }
        this.pitch = pitch;
    }

    void toggleSoftTail() {
        softTail = !softTail;
    }

    void save() {
        Path path = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getConfigDir().resolve("creamykeys-linux.properties");
        String contents = """
                # CreamyKeys Linux client settings
                enabled=%s
                keyboardProfile=%s
                clickProfile=%s
                volume=%.2f
                pitch=%.2f
                softTail=%s
                """.formatted(enabled, keyboardProfile, clickProfile, volume, pitch, softTail);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, contents);
        } catch (IOException exception) {
            LOGGER.error("Could not save CreamyKeys settings to {}.", path, exception);
        }
    }

    private static String next(java.util.List<String> choices, String selected) {
        int index = choices.indexOf(selected);
        return choices.get((index + 1) % choices.size());
    }
}
