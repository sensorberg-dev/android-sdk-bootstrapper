package com.sensorberg.sdk.demo;

import com.sensorberg.sdk.cluster.BeaconCluster;
import com.sensorberg.sdk.exception.SdkException;
import com.sensorberg.sdk.presenter.Presentation;
import com.sensorberg.sdk.presenter.PresentationConfiguration;
import com.sensorberg.sdk.presenter.Presenter;
import com.sensorberg.sdk.presenter.PresenterConfiguration;
import com.sensorberg.sdk.resolver.BeaconEvent;
import com.sensorberg.sdk.resolver.Resolution;
import com.sensorberg.sdk.resolver.ResolutionConfiguration;
import com.sensorberg.sdk.resolver.Resolver;
import com.sensorberg.sdk.resolver.ResolverConfiguration;
import com.sensorberg.sdk.resolver.ResolverListener;
import com.sensorberg.sdk.scanner.Scan;
import com.sensorberg.sdk.scanner.ScanConfiguration;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;
import com.sensorberg.sdk.scanner.Scanner;
import com.sensorberg.sdk.scanner.ScannerConfiguration;
import com.sensorberg.sdk.scanner.ScannerListener;

import java.util.ArrayList;
import java.util.List;

public class SensorbergApplicationBootstrapper implements ScannerListener, ResolverListener {

    private Resolver resolver;
    private Presenter presenter;
    private Scanner scanner;
    private int notificationIconResourceId;

    public SensorbergApplicationBootstrapper bootstrapApplication( ScannerConfiguration scannerConfiguration, ResolverConfiguration resolverConfiguration, PresenterConfiguration presenterConfiguration){
        scanner = Scanner.setupInstance(scannerConfiguration);
        resolver = Resolver.setupInstance(resolverConfiguration);
        presenter = Presenter.setupInstance(presenterConfiguration);

        scanner.addScannerListener(this);

        resolver.addResolverListener(this);

        return this;
    }

    public SensorbergApplicationBootstrapper bootstrapBackgroundScanning() {
        ScanConfiguration scanConfiguration = new ScanConfiguration();
        scanConfiguration.setBeaconCluster(BeaconCluster.SENSORBERG_CLUSTER);
        scanConfiguration.setEventMask(ScanEventType.ENTRY.getMask());
        scanConfiguration.setScanDuration(Long.MAX_VALUE);

        bootstrapBackgroundScanning(scanConfiguration);

        return this;
    }

    public SensorbergApplicationBootstrapper bootstrapBackgroundScanning(ScanConfiguration scanConfiguration) {
        ArrayList<ScanConfiguration> list = new ArrayList<ScanConfiguration>();
        list.add(scanConfiguration);
        bootstrapBackgroundScanning(list);
        return this;
    }

    public SensorbergApplicationBootstrapper bootstrapBackgroundScanning(List<ScanConfiguration> scanConfigurations) {
        for (ScanConfiguration scanConfiguration : scanConfigurations){
            scanner.backgroundConfiguration.addScanConfiguration(scanConfiguration);
        }
        scanner.backgroundConfiguration.registerForBackgroundScanning();
        return this;
    }

    @Override
    public void onScanEventDetected(Scan scan, ScanEvent scanEvent) {
        ResolutionConfiguration resolutionConfiguration = new ResolutionConfiguration();
        resolutionConfiguration.setScanEvent(scanEvent);
        Resolution resolution = resolver.createResolution(resolutionConfiguration);
        resolution.start();
    }

    @Override
    public void onScanFailed(Scan scan, Throwable cause) {

    }

    @Override
    public void onScanFinished(Scan scan) {

    }

    @Override
    public void onScanStarted(Scan scan) {

    }

    @Override
    public void onResolutionFinished(Resolution resolution, final BeaconEvent beaconEvent) {
        presentBeaconEvent(beaconEvent);
    }

    protected void presentBeaconEvent(BeaconEvent beaconEvent) {
        if(beaconEvent.getAction() != null) {
            PresentationConfiguration presentationConfiguration = new PresentationConfiguration();
            presentationConfiguration.setBeaconEvent(beaconEvent);
            presentationConfiguration.setNotificationIcon(this.notificationIconResourceId);
            presentationConfiguration.setNotificationId(beaconEvent.hashCode());
            Presentation presentation = presenter.createPresentation(presentationConfiguration);
            try {
                presentation.start();
            } catch (SdkException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResolutionFailed(Resolution resolution, Throwable cause) {
    }

    public void setNotificationIconResourceId(int notificationIconResourceId) {
        this.notificationIconResourceId = notificationIconResourceId;
    }
}
