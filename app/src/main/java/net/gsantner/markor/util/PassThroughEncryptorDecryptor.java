package net.gsantner.markor.util;

import net.gsantner.opoc.util.FileUtils;

import java.io.File;

public class PassThroughEncryptorDecryptor implements EncryptorDecryptor {
    File _file;

    public PassThroughEncryptorDecryptor(File file) {
        _file = file;
    }

    @Override
    public String decrypt() {
        return FileUtils.readTextFileFast(_file);
    }

    @Override
    public byte[] encrypt(String content) {
        return content.getBytes();
    }
}
