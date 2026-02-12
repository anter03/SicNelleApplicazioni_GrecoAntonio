package com.sicnelleapplicazioni.util;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;

public class DbManager {

    private static final Properties properties = new Properties();
    private static final String PROPERTIES_FILE = "application.properties";

    private static String decryptedDbPassword;
    private static SecretKey masterKey;
    private static IvParameterSpec ivSpec;

    static {
        try (InputStream input = DbManager.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find " + PROPERTIES_FILE);
            }
            properties.load(input);
            Class.forName(properties.getProperty("db.driver"));

            // Lettura parametri dal file properties
            String jksPath = properties.getProperty("jks.path");
            String jksPassword = properties.getProperty("jks.password");
            String keyAlias = properties.getProperty("jks.key.alias");
            String keyPassword = properties.getProperty("jks.key.password"); // Nuova password specifica per la chiave
            String ivParam = properties.getProperty("jks.iv.param");

            if (jksPath == null || jksPassword == null || keyAlias == null || keyPassword == null || ivParam == null) {
                throw new RuntimeException("Missing JKS configuration in application.properties");
            }

            // Caricamento del KeyStore
            KeyStore keyStore = KeyStore.getInstance("JKS"); 
            try (FileInputStream fis = new FileInputStream(jksPath)) {
                keyStore.load(fis, jksPassword.toCharArray());
            }

            // Recupero della Master Key usando la password specifica della chiave
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                keyAlias, 
                new KeyStore.PasswordProtection(keyPassword.toCharArray()) 
            );

            if (secretKeyEntry == null) {
                throw new RuntimeException("Master Key not found in JKS for alias: " + keyAlias);
            }
            masterKey = secretKeyEntry.getSecretKey();

            // Ricostruzione IV e decrittazione password DB
            ivSpec = new IvParameterSpec(Base64.getDecoder().decode(ivParam));
            String encryptedDbPassword = properties.getProperty("db.password.encrypted");
            
            if (encryptedDbPassword == null) {
                throw new RuntimeException("Missing encrypted DB password in application.properties");
            }
            
            decryptedDbPassword = EncryptionUtil.decrypt(encryptedDbPassword, masterKey, ivSpec);

        } catch (Exception e) {
            // Stampo l'errore completo in console per debug, così se fallisce ancora capiamo perché
            e.printStackTrace();
            throw new RuntimeException("Error initializing DbManager", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                decryptedDbPassword
        );
    }
}