package org.freenetproject.mobile.ui.main.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.freenetproject.mobile.BuildConfig;
import org.freenetproject.mobile.R;
import org.freenetproject.mobile.services.node.Manager;
import org.freenetproject.mobile.ui.about.activity.AboutActivity;
import org.freenetproject.mobile.ui.acknowledgement.activity.AcknowledgementActivity;
import org.freenetproject.mobile.ui.acknowledgement.activity.AcknowledgementFragment;
import org.freenetproject.mobile.ui.main.viewmodel.MainViewModel;
import org.freenetproject.mobile.ui.settings.activity.SettingsActivity;

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        SharedPreferences prefs = getContext().getSharedPreferences(
                getContext().getPackageName(), Context.MODE_PRIVATE
        );

        if (!prefs.getBoolean(AcknowledgementFragment.ACKNOWLEDGEMENT_KEY, false)) {
            startActivity(new Intent(getContext(), AcknowledgementActivity.class));
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        Manager manager = Manager.getInstance();
        updateSharedPreferences(manager, view);
        updateSettings(manager, view);
        updateAbout(manager, view);
        updateControls(manager, view);
        updateStatus(manager, view);
        updateStatusDetail(manager, view);
    }

    private void updateControls(Manager m, View view) {
        Button controlButton = view.findViewById(R.id.control_button);
        controlButton.setOnClickListener(view1 -> {
            new Thread(() -> {
                // Return right aways if for some reason it is still working (either
                // starting up or stopping).
                if (m.isTransitioning()) {
                    return;
                }

                // When running or paused the node can be shutdown,but it can not
                // be paused or started.
                if (m.isRunning() || m.isPaused()) {
                    m.stopService(view.getContext());
                } else {
                    m.startService(view.getContext());
                }
            }).start();
        });

        m.getStatus().observe(getViewLifecycleOwner(), status -> {
            controlButton
                .setEnabled(
                    !m.isTransitioning()
                );
            controlButton.setBackgroundResource(
                m.isRunning() || m.isPaused() ?
                    R.drawable.ic_baseline_power_settings_new_24 :
                        R.drawable.ic_baseline_play_circle_outline_24
            );
        });
    }

    private void updateStatus(Manager m, View view) {
        TextView statusText = view.findViewById(R.id.node_status);
        m.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status.equals(Manager.Status.STARTED)) {
                statusText.setText(R.string.node_running);
            } else if (status.equals(Manager.Status.STARTING_UP)) {
                statusText.setText(R.string.node_starting_up);
            } else if (status.equals(Manager.Status.PAUSED)) {
                statusText.setText(R.string.node_paused);
            } else if (status.equals(Manager.Status.STOPPING)) {
                statusText.setText(R.string.node_shutting_down);
            } else if (status.equals(Manager.Status.ERROR)) {
                statusText.setText(R.string.error_starting_up);
            } else {
                statusText.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
            }
        });
    }

    private void updateStatusDetail(Manager m, View view) {
        TextView detailText = view.findViewById(R.id.node_status_detail);
        m.getStatus().observe(getViewLifecycleOwner(), status -> {
            detailText.setOnClickListener(null);
            if (status.equals(Manager.Status.STARTED)) {
                detailText.setText(R.string.tap_to_navigate);
                detailText.setOnClickListener(view12 -> {
                    Uri uri = Uri.parse(
                        view.getContext().getString(R.string.default_url)
                    );

                    startActivity(
                        new Intent(Intent.ACTION_VIEW).setData(uri)
                    );
                });
            } else if (status.equals(Manager.Status.STARTING_UP)) {
                detailText.setText(R.string.may_take_a_while);
            } else if (status.equals(Manager.Status.ERROR)) {
                detailText.setText(R.string.error_detail);
            } else {
                detailText.setText("");
            }
        });
    }

    private void updateSharedPreferences(Manager m, View view) {
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        m.getStatus().observe(getViewLifecycleOwner(), status -> {
            editor.putInt("status", m.getStatus().getValue().ordinal());
            editor.apply();
        });
    }

    private void updateSettings(Manager m, View view) {
        Button settings = view.findViewById(R.id.settings_button);
        settings.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });
    }

    private void updateAbout(Manager m, View view) {
        Button settings = view.findViewById(R.id.about_button);
        settings.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), AboutActivity.class));
        });
    }
}