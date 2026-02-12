package com.sicnelleapplicazioni.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE_BITS = 256; // AES-256


    
    public static void main(String[] args) throws Exception {
        // 1. Configurazione Percorsi e Password
        // Usiamo .p12 che è lo standard per PKCS12
        String keystorePath = "src/main/resources/keystore.p12"; 
        String keystorePassword = "PasswordKeyStore110226!!"; 
        String keyAlias = "master_key_alias";                 
        String keyPassword = "PasswordJksKeyAlias_110226!!";  

        // 2. Generazione Chiave AES e IV
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        // 3. Cifra la password del DB
        String dbPasswordReale = "SqlServerMio160625"; 
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] encryptedPw = cipher.doFinal(dbPasswordReale.getBytes());

        // 4. Creazione del Keystore PKCS12
        // CAMBIO FONDAMENTALE: Da "JKS" a "PKCS12"
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);

        // Salvataggio della chiave nel keystore
        KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyPassword.toCharArray());
        ks.setEntry(keyAlias, skEntry, protParam);

        // 5. Scrittura fisica del file .p12 su disco
        java.io.File file = new java.io.File(keystorePath);
        file.getParentFile().mkdirs(); 
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            ks.store(fos, keystorePassword.toCharArray());
        }

        // 6. Output per il file properties
        System.out.println("\n--- NUOVA CONFIGURAZIONE PKCS12 (.p12) ---");
        // Ti stampo il path assoluto così lo incolli e siamo sicuri che Tomcat lo veda
        System.out.println("jks.path=" + file.getAbsolutePath().replace("\\", "\\\\"));
        System.out.println("jks.password=" + keystorePassword);
        System.out.println("jks.key.alias=" + keyAlias);
        System.out.println("jks.key.password=" + keyPassword);
        System.out.println("jks.iv.param=" + Base64.getEncoder().encodeToString(iv));
        System.out.println("db.password.encrypted=" + Base64.getEncoder().encodeToString(encryptedPw));
    }
    
    
    
    // Generates a new random AES key
    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(AES_KEY_SIZE_BITS, SecureRandom.getInstanceStrong()); // Use a strong secure random
        return keyGen.generateKey();
    }

    // Generates a new random IV
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16]; // 16 bytes for AES (128 bits)
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Encrypts plain text using a secret key and IV
    public static String encrypt(String plainText, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    // Decrypts cipher text using a secret key and IV
    public static String decrypt(String cipherText, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    // Helper to convert byte[] key to SecretKey
    public static SecretKey convertBytesToSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
