package com.sensorberg.sdk.bootstrapper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.background.ScannerBroadcastReceiver;
import com.sensorberg.sdk.internal.AndroidPlattform;
import com.sensorberg.sdk.internal.Plattform;
import com.sensorberg.sdk.presenter.PresenterConfiguration;
import com.sensorberg.sdk.resolver.BeaconEvent;

public class SensorbergApplicationBootstrapper implements Plattform.ForegroundStateListener {

    private static final String TAG = "ServiceBootStrapper";
    protected final Context context;

    protected boolean presentationDelegationEnabled;
    protected final Messenger messenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SensorbergService.MSG_PRESENT_ACTION:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(BeaconEvent.class.getClassLoader());
                    BeaconEvent beaconEvent = bundle.getParcelable(SensorbergService.MSG_PRESENT_ACTION_BEACONEVENT);
                    presentBeaconEvent(beaconEvent);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public SensorbergApplicationBootstrapper(Context context) {
        this(context, false);
    }

    public SensorbergApplicationBootstrapper(Context context, boolean enablePresentationDelegation) {
        this.context = context;
        this.presentationDelegationEnabled = enablePresentationDelegation;
    }

    
    public void connectToService(String apiKey, PresenterConfiguration presenterConfiguration) {
        if (new AndroidPlattform(context).isBluetoothLowEnergySupported()) {
            Intent service = new Intent(context, SensorbergService.class);
            service.putExtra(SensorbergService.EXTRA_START_SERVICE, 1);
            service.putExtra(SensorbergService.EXTRA_PRESENTER_CONFIGURATION, (Parcelable) presenterConfiguration);
            service.putExtra(SensorbergService.EXTRA_API_KEY, apiKey);
            context.startService(service);
        }

    }

    
    public void presentBeaconEvent(BeaconEvent beaconEvent) {
        Intent intent = ActionActivity.intentFor(context, beaconEvent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    
    public void setPresentationDelegationEnabled(boolean value) {
        presentationDelegationEnabled = value;
        if (value) {
            registerForPresentationDelegation();
        } else {
            unRegisterFromPresentationDelegation();
        }
    }


    
    public void disableServiceCompletely(Context context) {
        sendEmptyMessage(SensorbergService.MSG_SHUTDOWN);
    }

    
    public void enableService(Context context, String apiKey, PresenterConfiguration presenterConfiguration) {
        ScannerBroadcastReceiver.setManifestReceiverEnabled(true, context);
        connectToService(apiKey, presenterConfiguration);
        hostApplicationInForeground();
    }

    
    public void hostApplicationInBackground() {
        Logger.log.applicationStateChanged("hostApplicationInBackground");
        sendEmptyMessage(SensorbergService.MSG_APPLICATION_IN_BACKGROUND);
        unRegisterFromPresentationDelegation();
    }

    
    public void hostApplicationInForeground() {
        sendEmptyMessage(SensorbergService.MSG_APPLICATION_IN_FOREGROUND);
        if (presentationDelegationEnabled) {
            registerForPresentationDelegation();
        }
    }

    protected void sendEmptyMessage(int messageType) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, messageType);
        context.startService(service);
    }

    protected void unRegisterFromPresentationDelegation() {
        sendReplyToMessage(SensorbergService.MSG_UNREGISTER_PRESENTATION_DELEGATE);
    }

    protected void sendReplyToMessage(int messageType) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, messageType);
        service.putExtra(SensorbergService.EXTRA_MESSENGER, messenger);
        context.startService(service);
    }

    protected void registerForPresentationDelegation() {
        sendReplyToMessage(SensorbergService.MSG_REGISTER_PRESENTATION_DELEGATE);
    }

    
    public void changeAPIToken(String newApiToken) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SET_API_TOKEN);
        service.putExtra(SensorbergService.MSG_SET_API_TOKEN_TOKEN, newApiToken);
        context.startService(service);
    }

    
    public void updatePresenterConfiguration(PresenterConfiguration presenterConfiguration) {
        Intent service = new Intent(context, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_GENERIC_TYPE, SensorbergService.MSG_SET_PRESENTER_CONFIGURATION);
        service.putExtra(SensorbergService.MSG_SET_PRESENTER_CONFIGURATION_PRESENTER_CONFIGURATION, (Parcelable) presenterConfiguration);
        context.startService(service);
    }
}
