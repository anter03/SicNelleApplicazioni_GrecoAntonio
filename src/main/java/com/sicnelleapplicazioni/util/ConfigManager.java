package com.sicnelleapplicazioni.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final Properties properties = new Properties();
    private static final String PROPERTIES_FILE = "application.properties";

    static {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Impossibile trovare il file {0}", PROPERTIES_FILE);
            } else {
                properties.load(input);
                LOGGER.info("Configurazione caricata con successo.");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento delle properties", ex);
        }
    }

    // Metodo generico per leggere una proprietà
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Metodo con valore di default (utile per la programmazione difensiva)
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}