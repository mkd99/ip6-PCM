package ch.fhnw.ip6.powerconsumptionmanager.view.startup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.fhnw.ip6.powerconsumptionmanager.R;
import ch.fhnw.ip6.powerconsumptionmanager.network.DataLoader;
import ch.fhnw.ip6.powerconsumptionmanager.network.DataLoaderCallback;
import ch.fhnw.ip6.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;


public class SplashFragment extends Fragment {

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // When settings have changed edit info text that power consumption manager loads with new settings
        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null) {
            TextView loadingMsg = (TextView) view.findViewById(R.id.textLoadingMessage);
            loadingMsg.setText(extras.getString("settings_changed", getString(R.string.text_splash_info)));
        }

        PowerConsumptionManagerAppContext appContext = (PowerConsumptionManagerAppContext) getActivity().getApplicationContext();
        DataLoader loader = new DataLoader(appContext, (DataLoaderCallback) getActivity());

        // Web request to load the consumption and component data (load consumption data includes reading components)
        loader.loadConsumptionData("http://" + appContext.getIPAdress() + ":" + getString(R.string.webservice_getData));
        //loader.loadComponents("http://" + appContext.getIPAdress() + ":" + appContext.getString(R.string.webservice_getComponents));
    }
}