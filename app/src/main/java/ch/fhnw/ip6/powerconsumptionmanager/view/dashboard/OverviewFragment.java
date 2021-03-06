package ch.fhnw.ip6.powerconsumptionmanager.view.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ch.fhnw.ip6.powerconsumptionmanager.R;
import ch.fhnw.ip6.powerconsumptionmanager.network.AsyncTaskCallback;
import ch.fhnw.ip6.powerconsumptionmanager.network.GetCurrentPCMDataAsyncTask;
import ch.fhnw.ip6.powerconsumptionmanager.util.helper.DashboardHelper;
import ch.fhnw.ip6.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;
import me.itangqi.waveloadingview.WaveLoadingView;

/**
 * Displays the consumption, autarchy and selfsupply in summary views / wave loading views and has a horizontal scroll view
 * that either displays the current state of components or the daily statistics to each component.
 */
public class OverviewFragment extends Fragment implements AsyncTaskCallback {
    private static final String TAG = "OverviewFragment";

    private enum Mode { DAILY, NOW }

    private PowerConsumptionManagerAppContext mAppContext;
    private DashboardHelper mDashboardHelper;
    private Handler mUpdateHandler;
    private Mode mMode;

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAppContext = (PowerConsumptionManagerAppContext) getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set mode from saved instance or new
        if(savedInstanceState != null) {
            mMode = (Mode) savedInstanceState.getSerializable("OVERVIEW_MODE");
        } else {
            mMode = Mode.NOW;
        }

        // Setup helper class for overview
        mDashboardHelper = DashboardHelper.getInstance();
        mDashboardHelper.initOverviewContext(getContext());

        // Add the summary views / wave loading views to the helper instance and modify them
        mDashboardHelper.addSummaryView(
            getString(R.string.text_autarchy),
            (WaveLoadingView) getView().findViewById(R.id.wlvAutarchy),
            mAppContext.getString(R.string.unit_percentage)
        );
        mDashboardHelper.addSummaryView(
            getString(R.string.text_selfsupply),
            (WaveLoadingView) getView().findViewById(R.id.wlvSelfsupply),
            mAppContext.getString(R.string.unit_percentage)
        );
        mDashboardHelper.addSummaryView(
            getString(R.string.text_consumption),
            (WaveLoadingView) getView().findViewById(R.id.wlvConsumption),
            mAppContext.getString(R.string.unit_kw)
        );

        mDashboardHelper.setSummaryRatio(getString(R.string.text_autarchy), (int) mAppContext.getPCMData().getAutarchy());
        mDashboardHelper.setSummaryRatio(getString(R.string.text_selfsupply), (int) mAppContext.getPCMData().getSelfsupply());
        mDashboardHelper.setSummaryRatio(
            getString(R.string.text_consumption),
            (int) mAppContext.getPCMData().getConsumption(),
            mAppContext.getPCMData().getMinScaleConsumption(),
            mAppContext.getPCMData().getMaxScaleConsumption()
        );

        // Load the correct fragment into the container
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if(mMode == Mode.NOW) {
            transaction.replace(R.id.dynamic_content_fragment, CurrentValuesFragment.newInstance());
        } else {
            transaction.replace(R.id.dynamic_content_fragment, DailyValuesFragment.newInstance());
        }
        transaction.commit();

        // Instantiate the update handler
        if(mAppContext.isUpdatingAutomatically()) {
            mUpdateHandler = new Handler();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overview_menu, menu);

        // Display the correct options menu depending on the mode
        if(mMode == Mode.DAILY) {
            MenuItem now = menu.findItem(R.id.action_now);
            now.setVisible(false);
        } else {
            MenuItem daily = menu.findItem(R.id.action_daily);
            daily.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        // Load the according fragment when an options menu item is pressed
        switch (item.getItemId()) {
            case R.id.action_daily:
                mMode = Mode.NOW;
                transaction.replace(R.id.dynamic_content_fragment, CurrentValuesFragment.newInstance());
                break;
            case R.id.action_now:
                mMode = Mode.DAILY;
                transaction.replace(R.id.dynamic_content_fragment, DailyValuesFragment.newInstance());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        transaction.commit();
        getActivity().invalidateOptionsMenu();

        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop data updater if instantiated
        if(mUpdateHandler != null) {
            mUpdateHandler.removeCallbacks(updateCurrentPCMData);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start data updater if instantiated
        if(mUpdateHandler != null) {
            mUpdateHandler.postDelayed(updateCurrentPCMData, mAppContext.getUpdateInterval() * 1000);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When screen rotates save the mode
        outState.putSerializable("OVERVIEW_MODE", mMode);
        super.onSaveInstanceState(outState);
    }

    /**
     * Return point when the current data of the PCM has finished loading from the webservice
     * and now can be displayed/rendered.
     * @param result Status if the data could be loaded successfully or not
     * @param opType Type of operation that has completed.
     */
    @Override
    public void asyncTaskFinished(boolean result, String opType) {
        if(OverviewFragment.this.isVisible()) {
            if(result) {
                // Update all UI elements currently displayed on the overview screen
                mDashboardHelper.updateOverview();
                if(mMode == Mode.NOW) {
                    mDashboardHelper.updateCurrentValues();
                } else {
                    mDashboardHelper.updateDailyValues();
                }
            } else {
                // Show an error message
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    getActivity(),
                                    mAppContext.getString(R.string.toast_dashboard_update_error),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                } catch (NullPointerException e) {
                    Log.e(TAG, "Activity/Fragment destroyed or changed while updating.");
                }
            }
        }
    }

    /**
     * Task to execute every few seconds (depending on settings) to load new current data.
     */
    private final Runnable updateCurrentPCMData = new Runnable() {
        public void run() {
        new GetCurrentPCMDataAsyncTask(
                mAppContext,
                getInstance()
        ).execute();
        mUpdateHandler.postDelayed(this, mAppContext.getUpdateInterval() * 1000);
        }
    };

    private OverviewFragment getInstance() { return this; }
}