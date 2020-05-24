package net.gsantner.markor.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.gsantner.markor.R;

// the main purpose of the activity is
// to avoid multiple reloads of the current document in DocumentActivity.onResume
// while user confirms actions with OpenPGP provider for the first time
// it should not appear once use of keys is authorised properly
public class OpenPgpActivity extends AppCompatActivity {
    public static final String OPEN_PGP_INTENT = "OPEN_PGP_INTENT";
    public static final String OPEN_PGP_REQUEST = "OPEN_PGP_REQUEST";

    // make sure these don't clash with ShareUtil.REQUEST_* codes
    public static final int REQUEST_ENCRYPT = 4242;
    public static final int REQUEST_DECRYPT = 4243;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // at this stage we can retry encryption/decryption
        // but it doesn't seem worth it as the document is going to be reloaded in the caller activity anyway
        // also the caller can process the result and retry too if it wants to
        switch (requestCode) {
            case REQUEST_DECRYPT:
            case REQUEST_ENCRYPT:
                setResult(resultCode, data);
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_pgp__activity);

        Intent i = getIntent();
        PendingIntent pi = i.getParcelableExtra(OPEN_PGP_INTENT);
        try {
            startIntentSenderForResult(pi.getIntentSender(), i.getIntExtra(OPEN_PGP_REQUEST, 0), null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Toast.makeText(this, R.string.error_communicating_with_open_pgp_provider, Toast.LENGTH_LONG).show();
        }
    }
}
