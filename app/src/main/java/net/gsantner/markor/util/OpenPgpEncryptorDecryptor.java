package net.gsantner.markor.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import net.gsantner.markor.activity.AppActivityBase;
import net.gsantner.markor.activity.CryptoEnabledActivity;
import net.gsantner.markor.activity.OpenPgpActivity;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenPgpEncryptorDecryptor implements EncryptorDecryptor {
    static int globalCounter = 1;
    int counter = 1;

    public static boolean eligibleForProcessing(File file, Activity activity) {
        return activity instanceof AppActivityBase && ((CryptoEnabledActivity) activity).getCrypto().isConnected() && file.getName().endsWith(ENCRYPTION_EXTENSION);
    }

    @Override
    public String decrypt(Activity activity, byte[] encrypted) {
        Toast.makeText(activity, "decrypt" + counter++ + "/" + globalCounter++, Toast.LENGTH_SHORT).show();
        Intent data = new Intent(OpenPgpApi.ACTION_DECRYPT_VERIFY);
        OpenPgpApi api = new OpenPgpApi(activity, ((CryptoEnabledActivity) activity).getCrypto().getServiceConnection().getService());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = new ByteArrayInputStream(encrypted);
        Intent result = api.executeApi(data, is, os);
        switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            case OpenPgpApi.RESULT_CODE_SUCCESS:
                return os.toString();
            case OpenPgpApi.RESULT_CODE_ERROR: {
                OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                Toast.makeText(activity, "Error decrypting with OpenPGP: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return "";
            }
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
                PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                Intent i = new Intent(activity, OpenPgpActivity.class);
                i.putExtra(OpenPgpActivity.OPEN_PGP_INTENT, pi);
                i.putExtra(OpenPgpActivity.OPEN_PGP_REQUEST, OpenPgpActivity.REQUEST_DECRYPT);
                activity.startActivityForResult(i, OpenPgpActivity.REQUEST_DECRYPT);
                return null;
            }
            default:
                return "";
        }
    }

    @Override
    public byte[] encrypt(Activity activity, String content) {
        Toast.makeText(activity, "encrypt" + counter++ + "/" + globalCounter++, Toast.LENGTH_SHORT).show();
        Intent data = new Intent(OpenPgpApi.ACTION_ENCRYPT);
        OpenPgpApi api = new OpenPgpApi(activity, ((CryptoEnabledActivity) activity).getCrypto().getServiceConnection().getService());
        data.putExtra(OpenPgpApi.EXTRA_USER_IDS, AppSettings.get().getOpenPgpUserIds());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        Intent result = api.executeApi(data, is, os);
        switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            case OpenPgpApi.RESULT_CODE_SUCCESS:
                return os.toByteArray();
            case OpenPgpApi.RESULT_CODE_ERROR: {
                OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                Toast.makeText(activity, "Error encrypting with OpenPGP: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return new byte[0];
            }
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
                PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                Intent i = new Intent(activity, OpenPgpActivity.class);
                i.putExtra(OpenPgpActivity.OPEN_PGP_INTENT, pi);
                i.putExtra(OpenPgpActivity.OPEN_PGP_REQUEST, OpenPgpActivity.REQUEST_ENCRYPT);
                activity.startActivityForResult(i, OpenPgpActivity.REQUEST_ENCRYPT);
                return null;
            }
            default:
                return new byte[0];
        }
    }

    public static final String ENCRYPTION_EXTENSION = ".pgp";

    // current version of OpenPGP lib provides OpenPgpAppPreference (extends android.preference.DialogPreference)
    // incompatible with android.support.v7.preference.Preference
    // so let's implement selection ourselves
    private static final String PACKAGE_NAME_APG = "org.thialfihar.android.apg"; // according to OpenPGP lib, APG is broken
    private static final ArrayList<String> PROVIDER_BLACKLIST = new ArrayList<>();

    static {
        PROVIDER_BLACKLIST.add(PACKAGE_NAME_APG);
    }

    public static void getOpenPgpProviderPackages(Context context, List<String> providers, List<String> names) {
        Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
        PackageManager pkgManager = context.getPackageManager();
        List<ResolveInfo> resInfo = pkgManager.queryIntentServices(intent, 0);
        if (resInfo == null)
            return;

        for (ResolveInfo resolveInfo : resInfo) {
            if (resolveInfo.serviceInfo == null) {
                continue;
            }

            providers.add(resolveInfo.serviceInfo.packageName);
            names.add(String.valueOf(resolveInfo.serviceInfo.loadLabel(pkgManager)));
        }
    }
}