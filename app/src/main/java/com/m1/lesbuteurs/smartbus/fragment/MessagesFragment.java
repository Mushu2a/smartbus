package com.m1.lesbuteurs.smartbus.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.m1.lesbuteurs.smartbus.R;
import com.m1.lesbuteurs.smartbus.helper.Helper;
import com.m1.lesbuteurs.smartbus.helper.SQLiteHandler;
import com.m1.lesbuteurs.smartbus.helper.SessionManager;
import com.m1.lesbuteurs.smartbus.sendbird.SendBirdOpenChannelListActivity;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.HashMap;

public class MessagesFragment extends Fragment {

    /* SENDERBIRD CONFIG */
    public static String VERSION = "3.0.8.0";

    private enum State {DISCONNECTED, CONNECTING, CONNECTED};
    private static final String appSenderBirdId = "413E6E38-0D52-4523-AB7F-22A62053B908";
    public static String senderID = "241818782395";
    private static String username;

    private SharedPreferences sharedPreferences;
    private static String PREF_NAME = "prefs";

    private SQLiteHandler db;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // senderID = getPreferences(Context.MODE_PRIVATE).getString("user_id", "");
        // username = getPreferences(Context.MODE_PRIVATE).getString("nickname", "");
        senderID = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString("user_id", "");
        username = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString("nickname", "");

        db = new SQLiteHandler(getContext());

        SendBird.init(appSenderBirdId, getActivity());

        // Retour détails de l'utilisateur gràce à sqlite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");

        Toast.makeText(getActivity(), "En attente de connexion au channel", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        connect(rootView);
        Helper.hideKeyboard(getActivity());

        rootView.findViewById(R.id.btn_open_channel_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(getActivity(), SendBirdOpenChannelListActivity.class);
            startActivity(intent);
            }
        });

        setState(State.DISCONNECTED, rootView);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.disconnect(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * If the minimum SDK version you support is under Android 4.0,
         * you MUST uncomment the below code to receive push notifications.
         */
        SendBird.notifyActivityResumedForOldAndroids();
    }

    @Override
    public void onPause() {
        super.onPause();
        /**
         * If the minimum SDK version you support is under Android 4.0,
         * you MUST uncomment the below code to receive push notifications.
         */
        SendBird.notifyActivityPausedForOldAndroids();
    }

    private void setState(State state, View rootView) {
        switch (state) {
            case DISCONNECTED:
                ((TextView) rootView.findViewById(R.id.btn_open_channel_list)).setText("Connexion");
                rootView.findViewById(R.id.btn_open_channel_list).setEnabled(false);
                break;

            case CONNECTING:
                ((TextView) rootView.findViewById(R.id.btn_open_channel_list)).setText("Se connecte...");
                rootView.findViewById(R.id.btn_open_channel_list).setEnabled(false);
                break;

            case CONNECTED:
                ((TextView) rootView.findViewById(R.id.btn_open_channel_list)).setText("Ouvrir Channel");
                rootView.findViewById(R.id.btn_open_channel_list).setEnabled(true);
                rootView.findViewById(R.id.btn_open_channel_list).setBackgroundColor(Color.parseColor("#1393d3"));
                break;
        }
    }

    private void connect(final View rootView) {
        SendBird.connect(senderID, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
            if (e != null) {
                Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                setState(State.DISCONNECTED, null);
                return;
            }

            SendBird.updateCurrentUserInfo(username, null, new SendBird.UserInfoUpdateHandler() {
                @Override
                public void onUpdated(SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setState(State.DISCONNECTED, null);
                        return;
                    }

                    SharedPreferences.Editor editor = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
                    // SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putString("user_id", senderID);
                    editor.putString("nickname", username);
                    editor.commit();

                    setState(State.CONNECTED, rootView);
                }
            });

            if (FirebaseInstanceId.getInstance().getToken() == null) return;

            SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), new SendBird.RegisterPushTokenWithStatusHandler() {
                @Override
                public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            });
            }
        });

        setState(State.CONNECTING, rootView);
    }

    private void disconnect(final View rootView) {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                setState(State.DISCONNECTED, rootView);
            }
        });
    }
}
