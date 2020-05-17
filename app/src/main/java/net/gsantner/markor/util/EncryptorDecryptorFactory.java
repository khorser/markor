package net.gsantner.markor.util;

import android.content.Context;

import java.io.File;

public class EncryptorDecryptorFactory {
    public static EncryptorDecryptor getByExtension(File file, Context context) {
        if (JavaPasswordEncryptorDecryptor.eligibleForProcessing(file, context))
            return new JavaPasswordEncryptorDecryptor(file, context);
        else
            return new PassThroughEncryptorDecryptor(file);
    }

    public static EncryptorDecryptor getApplicable(boolean cryptographic, File file, Context context) {
        if (cryptographic && JavaPasswordEncryptorDecryptor.cryptographyAvailable())
            return new JavaPasswordEncryptorDecryptor(file, context);
        else
            return new PassThroughEncryptorDecryptor(file);
    }
}
