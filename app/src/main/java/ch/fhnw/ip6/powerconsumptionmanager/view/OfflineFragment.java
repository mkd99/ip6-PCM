package ch.fhnw.ip6.powerconsumptionmanager.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.fhnw.ip6.powerconsumptionmanager.R;
import ch.fhnw.ip6.powerconsumptionmanager.activity.SplashScreenActivity;
import ch.fhnw.ip6.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;

/**
 * This fragment is shown when no connection to the PCM could be established and no initial data could be loaded.
 */
public class OfflineFragment extends Fragment {

    public static OfflineFragment newInstance() {
        return new OfflineFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offline, container, false);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.offline_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Try and restart the application
            case R.id.action_retry:
                // Reset online status
                PowerConsumptionManagerAppContext appContext = (PowerConsumptionManagerAppContext) getActivity().getApplicationContext();
                appContext.setOnline(true);

                Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
                intent.putExtra("status_info", getString(R.string.text_splash_retry_connecting));
                getActivity().startActivity(intent);
                getActivity().finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
