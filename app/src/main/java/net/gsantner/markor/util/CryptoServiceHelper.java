package net.gsantner.markor.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.gsantner.markor.activity.CryptoEnabledActivity;
import net.gsantner.markor.activity.OpenPgpActivity;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class CryptoServiceHelper {
    private OpenPgpServiceConnection _serviceConnection;
    private String _currentProvider = "";

    public void connect(Context context, Runnable onConnect) {
        String provider = AppSettings.get().getOpenPgpProvider();
        if (!_currentProvider.equals(provider)) {
            disconnect();
            _currentProvider = provider;
            if (provider.equals("")) {
                onConnect.run();
            }
            else {
                _serviceConnection = new OpenPgpServiceConnection(context, provider, new OpenPgpServiceConnection.OnBound() {
                    @Override
                    public void onBound(IOpenPgpService2 service) {
                        onConnect.run();
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
                _serviceConnection.bindToService();
            }
        }
    }

    public void disconnect() {
        if (_serviceConnection != null)
            _serviceConnection.unbindFromService();
        _serviceConnection = null;
    }

    public boolean isConnected() {
        return _serviceConnection != null && _serviceConnection.isBound();
    }

    public boolean areUserIdsValid(Activity activity, String[] userIds) {
        Intent data = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
        OpenPgpApi api = new OpenPgpApi(activity, _serviceConnection.getService());
        data.putExtra(OpenPgpApi.EXTRA_USER_IDS, userIds);
        Intent result = api.executeApi(data, (InputStream)null, null);
        switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
            case OpenPgpApi.RESULT_CODE_SUCCESS:
                return true;
            case OpenPgpApi.RESULT_CODE_ERROR: {
                return false;
            }
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
            }
            return false;
        }
        return false;
    }

    public OpenPgpServiceConnection getServiceConnection() {
        return _serviceConnection;
    }

    public static EncryptorDecryptor getByExtension(File file, Activity activity) {
        if (JavaPasswordEncryptorDecryptor.eligibleForProcessing(file, activity))
            return new JavaPasswordEncryptorDecryptor();
        else if (OpenPgpEncryptorDecryptor.eligibleForProcessing(file, activity))
            return new OpenPgpEncryptorDecryptor();
        else
            return new PassThroughEncryptorDecryptor();
    }

    public static EncryptorDecryptor getApplicable(boolean cryptographic, Activity activity) {
        if (cryptographic && JavaPasswordEncryptorDecryptor.cryptographyAvailable())
            return new JavaPasswordEncryptorDecryptor();
        else
            return new PassThroughEncryptorDecryptor();
    }
}