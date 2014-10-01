package com.sensorberg.sdk.bootstrapper;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.SensorbergService;
import com.sensorberg.sdk.internal.Plattform;
import com.sensorberg.sdk.presenter.PresenterConfiguration;
import com.sensorberg.sdk.resolver.BeaconEvent;

public class SensorbergApplicationBootstrapper implements Plattform.ForegroundStateListener {

    private static final String TAG = "ServiceBootStrapper";
    private final Application application;

    private Messenger serviceMessenger;
    private boolean presentationDelegationEnabled;
    private boolean hostApplicationInForegroundNotDelivered;

    public void setPresentationDelegationEnabled(boolean value) {
        presentationDelegationEnabled = value;
        if (value) {
            registerForPresentationDelegation();
        }
        else{
            unRegisterFromPresentationDelegation();
        }
    }

    class IncomingHandler extends Handler {
        @Override
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

    public void presentBeaconEvent(BeaconEvent beaconEvent) {
        Intent intent = ActionActivity.intentFor(application, beaconEvent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    final Messenger messenger = new Messenger(new IncomingHandler());


    public SensorbergApplicationBootstrapper(Application application, PresenterConfiguration presenterConfiguration, String apiKey) {
        this(application, apiKey, false, presenterConfiguration);
    }

    public SensorbergApplicationBootstrapper(Application application, String apiKey, boolean enablePresentationDelegation, PresenterConfiguration presenterConfiguration) {
        this.application = application;
        this.presentationDelegationEnabled = enablePresentationDelegation;
        Intent service = new Intent(application, SensorbergService.class);
        service.putExtra(SensorbergService.EXTRA_PRESENTER_CONFIGURATION, presenterConfiguration);
        service.putExtra(SensorbergService.EXTRA_API_KEY, apiKey);

        application.bindService(service, beaconServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void hostApplicationInBackground() {
        Logger.log.applicationStateChanged("hostApplicationInBackground");
        if (serviceMessenger != null){
            sendEmptyMessage(SensorbergService.MSG_APPLICATION_IN_BACKGROUND);
            unRegisterFromPresentationDelegation();
        }
    }

    @Override
    public void hostApplicationInForeground() {
        Logger.log.applicationStateChanged("hostApplicationInForeground");
        if (serviceMessenger != null){
            sendEmptyMessage(SensorbergService.MSG_APPLICATION_IN_FOREGROUND);
            if (presentationDelegationEnabled){
                registerForPresentationDelegation();
            }
        } else {
            hostApplicationInForegroundNotDelivered = true;
        }
    }

    private void sendEmptyMessage(int messageType) {
        Message message = Message.obtain(null, messageType);
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, "could not send the hostApplicationInForeground message", e);
        }
    }

    private void unRegisterFromPresentationDelegation() {
        sendReplyToMessage(SensorbergService.MSG_UNREGISTER_PRESENTATION_DELEGATE);
    }

    private void sendReplyToMessage(int messageType) {
        if (serviceMessenger != null) {
            Message msg = Message.obtain(null, messageType);
            msg.replyTo = messenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerForPresentationDelegation() {
        sendReplyToMessage(SensorbergService.MSG_REGISTER_PRESENTATION_DELEGATE);
    }

    public void changeAPIToken(String newApiToken) {
        if (serviceMessenger != null){
            Message message = Message.obtain(null, SensorbergService.MSG_SET_API_TOKEN);

            Bundle bundle = new Bundle();
            bundle.putString(SensorbergService.MSG_SET_API_TOKEN_TOKEN, newApiToken);
            message.obj = bundle;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                Log.e(TAG, "could not send the hostApplicationInForeground message", e);
            }
        }
    }

    public void updatePresenterConfiguration(PresenterConfiguration presenterConfiguration) {
        if (serviceMessenger != null){
            Message message = Message.obtain(null, SensorbergService.MSG_SET_PRESENTER_CONFIGURATION);

            Bundle bundle = new Bundle();
            bundle.putParcelable(SensorbergService.MSG_SET_PRESENTER_CONFIGURATION_PRESENTER_CONFIGURATION, presenterConfiguration);
            message.obj = bundle;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                Log.e(TAG, "could not send the hostApplicationInForeground message", e);
            }
        }
    }

    private ServiceConnection beaconServiceConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG,  "we have a connection to the service now");
            serviceMessenger = new Messenger(service);
            if (presentationDelegationEnabled){
                registerForPresentationDelegation();
            }
            if (hostApplicationInForegroundNotDelivered){
                sendEmptyMessage(SensorbergService.MSG_APPLICATION_IN_FOREGROUND);
                hostApplicationInForegroundNotDelivered = false;
            }
        }

        // Called when the connection with the service disconnects
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected");
            serviceMessenger = null;
        }
    };
}
