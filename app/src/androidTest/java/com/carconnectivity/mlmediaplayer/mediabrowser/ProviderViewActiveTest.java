package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.media.MediaBrowserService;
import android.test.AndroidTestCase;

import java.util.List;

/**
 * Created by mszafranek on 04.08.16.
 */
public class ProviderViewActiveTest extends AndroidTestCase{

    private List<ResolveInfo> getMediaBrowserPackages() {
        PackageManager mManager = getContext().getPackageManager();

        final Intent intent = new Intent(MediaBrowserService.SERVICE_INTERFACE);
        List<ResolveInfo> resolveInfo = mManager.queryIntentServices(intent, 0);
        if (resolveInfo.size() == 0) {
            throw new IllegalStateException("Media browser packages can not be empty. " +
                    "You must install at least one provider to run testing");
        }
        return resolveInfo;
    }

    public void testCreation(){
        ProvidersManager providersManager = new ProvidersManager(getContext(), getContext().getPackageManager());
        List<ResolveInfo> resolveInfo = getMediaBrowserPackages();
        ProviderViewActive test =new ProviderViewActive(new Provider(providersManager, resolveInfo.get(0), false), null, null, null, 0, 0, null);
        assertNotNull(test);
    }

    public void testCreationThrowException() {
        try {
            ProviderViewActive test = new ProviderViewActive(null, null, null, null, 0, 0, null);
            fail("should throw exception");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

}
