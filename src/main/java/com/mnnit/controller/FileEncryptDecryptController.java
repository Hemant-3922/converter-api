package com.mnnit.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@RestController
@RequestMapping("/api/crypto")
public class FileEncryptDecryptController {

    // 🔑 Secret Key (for demo, static key; in production use secure key mgmt)
    private static final byte[] keyBytes;
    private static final SecretKey secretKey;

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128, new SecureRandom("WonderConverterSecret".getBytes())); // fixed seed for repeatability
            secretKey = keyGen.generateKey();
            keyBytes = secretKey.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Error generating key", e);
        }
    }

    private byte[] processFile(byte[] fileData, int mode) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, skeySpec);
        return cipher.doFinal(fileData);
    }

    @PostMapping("/encrypt")
    public ResponseEntity<byte[]> encryptFile(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] inputBytes = file.getBytes();
        byte[] encryptedBytes = processFile(inputBytes, Cipher.ENCRYPT_MODE);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encrypted_" + file.getOriginalFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(encryptedBytes);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<byte[]> decryptFile(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] inputBytes = file.getBytes();
        byte[] decryptedBytes = processFile(inputBytes, Cipher.DECRYPT_MODE);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=decrypted_" + file.getOriginalFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(decryptedBytes);
    }
}
