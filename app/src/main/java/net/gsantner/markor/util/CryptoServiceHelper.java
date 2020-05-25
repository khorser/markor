package net.gsantner.markor.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.gsantner.markor.R;
import net.gsantner.opoc.util.Callback;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.File;
import java.io.InputStream;

public class CryptoServiceHelper {
    private OpenPgpServiceConnection _serviceConnection;
    private String _currentProvider = "";

    public void connect(Context context, Callback.a0 onConnect) {
        String provider = AppSettings.get().getOpenPgpProvider();
        if (!_currentProvider.equals(provider)) {
            disconnect();
            _currentProvider = provider;
            if (provider.equals("")) {
                if (onConnect != null)
                    onConnect.callback();
            } else {
                _serviceConnection = new OpenPgpServiceConnection(context, provider, new OpenPgpServiceConnection.OnBound() {
                    @Override
                    public void onBound(IOpenPgpService2 service) {
                        if (onConnect != null)
                            onConnect.callback();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, R.string.error_communicating_with_open_pgp_provider, Toast.LENGTH_LONG).show();
                        if (onConnect != null)
                            onConnect.callback();
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
        Intent result = api.executeApi(data, (InputStream) null, null);
        return result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_SUCCESS;
    }

    public OpenPgpServiceConnection getServiceConnection() {
        return _serviceConnection;
    }

    public static EncryptorDecryptor getByExtension(Activity activity, String filename) {
        if (JavaPasswordEncryptorDecryptor.eligibleForProcessing(activity, filename))
            return new JavaPasswordEncryptorDecryptor();
        else if (OpenPgpEncryptorDecryptor.eligibleForProcessing(activity, filename))
            return new OpenPgpEncryptorDecryptor();
        else
            return new PassThroughEncryptorDecryptor();
    }
}