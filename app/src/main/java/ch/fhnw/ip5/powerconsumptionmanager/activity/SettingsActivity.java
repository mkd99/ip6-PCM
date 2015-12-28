package ch.fhnw.ip5.powerconsumptionmanager.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import ch.fhnw.ip5.powerconsumptionmanager.R;
import ch.fhnw.ip5.powerconsumptionmanager.network.DataLoader;
import ch.fhnw.ip5.powerconsumptionmanager.network.DataLoaderCallback;
import ch.fhnw.ip5.powerconsumptionmanager.util.IPSettingDialog;
import ch.fhnw.ip5.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;

public class SettingsActivity extends AppCompatActivity {
    // Flag if settings have changed
    public static boolean UPDATED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        UPDATED = false;

        // Load preferences in separate fragment
        getFragmentManager().beginTransaction().replace(R.id.ip_setting_fragment, new SettingsFragment()).commit();

        // Settings toolbar
        Toolbar tb = (Toolbar) findViewById(R.id.settings_toolbar);
        tb.setTitle("Settings");
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Close settings activity when finished with editing settings
            case android.R.id.home:
                if(SettingsActivity.UPDATED) {
                    Intent mainIntent = new Intent(SettingsActivity.this, SplashScreenActivity.class);
                    mainIntent.putExtra("settings_changed", getString(R.string.text_splash_settings_changed));
                    SettingsActivity.this.startActivity(mainIntent);
                    SettingsActivity.this.finish();
                } else {
                    Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    SettingsActivity.this.startActivity(mainIntent);
                    SettingsActivity.this.finish();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, DataLoaderCallback {
        private SharedPreferences mSettings;
        private DataLoaderCallback mContext;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            mSettings = getPreferenceScreen().getSharedPreferences();
            mContext = this;

            Preference syncPreference = (Preference) findPreference("syncPlan");

            syncPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity().getApplicationContext(), "Syncing...", Toast.LENGTH_SHORT).show();
                    PowerConsumptionManagerAppContext context = (PowerConsumptionManagerAppContext) getActivity().getApplicationContext();
                    DataLoader loader = new DataLoader(context, mContext);
                    loader.synchronizeChargingPlan("http://" + context.getIPAdress() + ":" + getString(R.string.webservice_putChargePlan));
                    return true;
                }
            });

            // Set present ip as summary of preference entry
            IPSettingDialog ipDialog = (IPSettingDialog) findPreference("IP");
            ipDialog.setSummary(mSettings.getString("IP", "192.168.0.1"));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Update summary of ip preference entry when ip changed
            Preference pref = findPreference(key);
            String newSummary = mSettings.getString("IP", "");
            pref.setSummary(newSummary);
        }

        @Override
        public void DataLoaderDidFinish() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Worked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void DataLoaderDidFail() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

