package net.gsantner.markor.util;

import android.app.Activity;

public class PassThroughEncryptorDecryptor implements EncryptorDecryptor {
    @Override
    public String decrypt(Activity activity, byte[] encrypted) {
        return new String(encrypted);
    }

    @Override
    public byte[] encrypt(Activity activity, String content) {
        return content.getBytes();
    }
}
