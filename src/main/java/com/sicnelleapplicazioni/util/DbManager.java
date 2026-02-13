package com.sicnelleapplicazioni.util;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;

public class DbManager {

    private static String decryptedDbPassword;
    private static SecretKey masterKey;
    private static IvParameterSpec ivSpec;

    static {
        try {
            // Registrazione del driver caricato tramite ConfigManager
            Class.forName(ConfigManager.getProperty("db.driver"));

            // Lettura parametri tramite il nuovo ConfigManager
            String jksPath = ConfigManager.getProperty("jks.path");
            String jksPassword = ConfigManager.getProperty("jks.password");
            String keyAlias = ConfigManager.getProperty("jks.key.alias");
            String keyPassword = ConfigManager.getProperty("jks.key.password");
            String ivParam = ConfigManager.getProperty("jks.iv.param");
            String encryptedDbPassword = ConfigManager.getProperty("db.password.encrypted");

            // Controllo presenza configurazioni (Programmazione Difensiva 3.7)
            if (jksPath == null || jksPassword == null || keyAlias == null || keyPassword == null || ivParam == null) {
                throw new RuntimeException("Missing JKS configuration in application.properties");
            }

            // Caricamento del KeyStore (Gestione sicura chiavi 3.5)
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

            if (encryptedDbPassword == null) {
                throw new RuntimeException("Missing encrypted DB password in application.properties");
            }

            decryptedDbPassword = EncryptionUtil.decrypt(encryptedDbPassword, masterKey, ivSpec);

        } catch (Exception e) {
            // Stampo l'errore completo in console per debug
            e.printStackTrace();
            throw new RuntimeException("Error initializing DbManager", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConfigManager.getProperty("db.url"),
                ConfigManager.getProperty("db.user"),
                decryptedDbPassword
        );
    }
}