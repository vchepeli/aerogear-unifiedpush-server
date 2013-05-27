/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aerogear.connectivity.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aerogear.connectivity.message.sender.APNsPushNotificationSender;
import org.aerogear.connectivity.message.sender.GCMPushNotificationSender;
import org.aerogear.connectivity.message.sender.UnifiedPushMessage;
import org.aerogear.connectivity.model.AndroidApplication;
import org.aerogear.connectivity.model.MobileApplicationInstance;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.iOSApplication;

public class SenderServiceImpl implements SenderService {
    
    
    
    @Override
    public void sendToClientIdentifiers(PushApplication pushApplication, final List<String> identifiers, Map<String, ? extends Object> payload) {
        
        final UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage(payload);

        // TODO: Make better...
        final Set<iOSApplication> iOSapps = pushApplication.getIOSApps();
        for (iOSApplication iOSApp : iOSapps) {
            
            final List<String> iOSTokenPerVariant = new ArrayList<String>();
            final APNsPushNotificationSender apnsSender = new APNsPushNotificationSender(iOSApp.getCertificate(), iOSApp.getPassphrase());
            // get all instances
            final Set<MobileApplicationInstance> instancesPerVariant = iOSApp.getInstances();
            for (MobileApplicationInstance instance : instancesPerVariant) {
                
                // see if the identifer does match for the instance
                if (identifiers.contains(instance.getClientIdentifier())) {
                    // add it
                    iOSTokenPerVariant.add(instance.getDeviceToken());
                }
            }
            // deliver to network
            apnsSender.sendPushMessage(iOSTokenPerVariant, unifiedPushMessage);
        }

        // TODO: make better :)
        Set<AndroidApplication> androidApps = pushApplication.getAndroidApps();
        for (AndroidApplication androidApplication : androidApps) {
            
            final List<String> androidTokenPerVariant = new ArrayList<String>();
            final GCMPushNotificationSender gcmSender = new GCMPushNotificationSender(androidApplication.getGoogleKey());

            // get all instances
            Set<MobileApplicationInstance> instancesPerVariant = androidApplication.getInstances();
            for (MobileApplicationInstance instance : instancesPerVariant) {
                
                // see if the identifer does match for the instance
                if (identifiers.contains(instance.getClientIdentifier())) {
                    // add it
                    androidTokenPerVariant.add(instance.getDeviceToken());
                }
            }
            gcmSender.sendPushMessage(androidTokenPerVariant, unifiedPushMessage);
        }
    }

    
    @Override
    public void broadcast(PushApplication pushApplication,
            Map<String, ? extends Object> jsonMap) {
        
        
        final UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage(jsonMap);
        
        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSApplication> iOSapps = pushApplication.getIOSApps();
        for (iOSApplication iOSApp : iOSapps) {
            
            final APNsPushNotificationSender apnsSender = new APNsPushNotificationSender(iOSApp.getCertificate(), iOSApp.getPassphrase());

            // get all the tokens:
            final Set<String> iOStokenz = new HashSet<String>();
            Set<MobileApplicationInstance> iOSinstallations = iOSApp
                    .getInstances();
            for (MobileApplicationInstance mobileApplicationInstance : iOSinstallations) {
                iOStokenz.add(mobileApplicationInstance.getDeviceToken());
            }

            apnsSender.sendPushMessage(iOStokenz, unifiedPushMessage);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidApplication> androidApps = pushApplication.getAndroidApps();
        for (AndroidApplication androidApplication : androidApps) {

            final List<String> androidtokenz = new ArrayList<String>();
            final GCMPushNotificationSender gcmSender = new GCMPushNotificationSender(androidApplication.getGoogleKey());

            Set<MobileApplicationInstance> androidApplications = androidApplication
                    .getInstances();
            for (MobileApplicationInstance mobileApplicationInstance : androidApplications) {
                androidtokenz.add(mobileApplicationInstance.getDeviceToken());
            }
            gcmSender.sendPushMessage(androidtokenz, unifiedPushMessage);
        }
    }
}

