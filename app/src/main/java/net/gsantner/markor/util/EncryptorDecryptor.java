package net.gsantner.markor.util;

public interface EncryptorDecryptor {
    public String decrypt();

    public byte[] encrypt(String content);
}
