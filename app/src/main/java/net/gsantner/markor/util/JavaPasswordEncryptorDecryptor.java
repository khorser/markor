package net.gsantner.markor.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import net.gsantner.markor.R;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import other.de.stanetz.jpencconverter.JavaPasswordbasedCryption;
import other.de.stanetz.jpencconverter.PasswordStore;

public class JavaPasswordEncryptorDecryptor implements EncryptorDecryptor {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String decrypt(Activity activity, byte[] encrypted) {
        try {
            if (encrypted.length > JavaPasswordbasedCryption.Version.NAME_LENGTH) {
                return JavaPasswordbasedCryption.getDecryptedText(encrypted, getPassword(activity));
            } else {
                return new String(encrypted, StandardCharsets.UTF_8);
            }
        } catch (JavaPasswordbasedCryption.EncryptionFailedException | IllegalArgumentException e) {
            Toast.makeText(activity, R.string.could_not_decrypt_file_content_wrong_password_or_is_the_file_maybe_not_encrypted, Toast.LENGTH_LONG).show();
            Log.e(JavaPasswordEncryptorDecryptor.class.getName(), "loadDocument:  decrypt failed. " + e.getMessage(), e);
            return "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public byte[] encrypt(Activity activity, String content) {
        try {
            return new JavaPasswordbasedCryption(JavaPasswordbasedCryption.Version.V001, new SecureRandom()).encrypt(content, getPassword(activity));
        } catch (JavaPasswordbasedCryption.EncryptionFailedException e) {
            Log.e(JavaPasswordEncryptorDecryptor.class.getName(), "loadDocument:  encrypt failed. " + e.getMessage(), e);
            Toast.makeText(activity, R.string.could_not_encrypt_file_content_the_file_was_not_saved, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static boolean eligibleForProcessing(File file, Context context) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
                && file.getName().endsWith(JavaPasswordbasedCryption.DEFAULT_ENCRYPTION_EXTENSION)
                && getPassword(context) != null;
    }

    public static boolean cryptographyAvailable() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static char[] getPassword(Context context) {
        final PasswordStore securityStore = new PasswordStore(context);
        final char[] pw = securityStore.loadKey(R.string.pref_key__default_encryption_password);
        if (pw == null || pw.length == 0) {
            final String warningText = context.getString(R.string.no_password_set_cannot_encrypt_decrypt);
            Toast.makeText(context, warningText, Toast.LENGTH_LONG).show();
            Log.w(JavaPasswordEncryptorDecryptor.class.getName(), warningText);
            return null;
        }
        return pw;
    }
}
