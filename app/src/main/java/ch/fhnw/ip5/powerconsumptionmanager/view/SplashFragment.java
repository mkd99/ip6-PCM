package ch.fhnw.ip5.powerconsumptionmanager.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.fhnw.ip5.powerconsumptionmanager.R;
import ch.fhnw.ip5.powerconsumptionmanager.network.DataLoader;
import ch.fhnw.ip5.powerconsumptionmanager.network.DataLoaderCallback;
import ch.fhnw.ip5.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;


public class SplashFragment extends Fragment {
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final PowerConsumptionManagerAppContext context = (PowerConsumptionManagerAppContext) getActivity().getApplicationContext();

        final DataLoader loader = new DataLoader(context, (DataLoaderCallback) getActivity());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loader.loadComponents("http://" + context.getIPAdress() + ":" + getString(R.string.webservice_getComponents));
                loader.loadConsumptionData("http://" + context.getIPAdress() + ":" + getString(R.string.webservice_getData));
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}