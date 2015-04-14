package com.sensorberg.sdk.bootstrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sensorberg.sdk.action.Action;
import com.sensorberg.sdk.action.UriMessageAction;
import com.sensorberg.sdk.resolver.BeaconEvent;

public class ActionActivity extends Activity{

    private static final String TAG = "ActionActivity";
    private static final String EXTRA_BEACON_EVENT = "com.sensorberg.android.sdk.actionActivity.extra.beaconEvent";

    private TextView titleTextView;
    private TextView contentTextView;
    private Button openURLButton;
    private UriMessageAction messageAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_action);

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        contentTextView = (TextView) findViewById(R.id.contentTextView);
        openURLButton = (Button) findViewById(R.id.openURLButton);

        openURLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Intent(Intent.ACTION_VIEW, Uri.parse((messageAction).getUri()));
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null){
            BeaconEvent beaconEvent = intent.getParcelableExtra(EXTRA_BEACON_EVENT);
            Action action = beaconEvent.getAction();
            messageAction = (UriMessageAction) action;

            contentTextView.setText(messageAction.getContent());
            titleTextView.setText(messageAction.getTitle());

            Log.d(TAG, messageAction.getTitle() + messageAction.getContent() + messageAction.getUri());
        }
    }

    public static Intent intentFor(Context context, BeaconEvent beaconEvent) {
        Intent intent = new Intent(context, ActionActivity.class);
        intent.putExtra(EXTRA_BEACON_EVENT, beaconEvent);
        return intent;
    }
}
