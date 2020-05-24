package net.gsantner.markor.util;

import android.app.Activity;

public interface EncryptorDecryptor {
    public String decrypt(Activity activity, byte[] encrypted);

    public byte[] encrypt(Activity activity, String content);
}
