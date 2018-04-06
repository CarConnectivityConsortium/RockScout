/*
 * Copyright Car Connectivity Consortium
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
 *
 * You may decide to give the Car Connectivity Consortium input, suggestions
 * or feedback of a technical nature which may be implemented on the
 * Car Connectivity Consortium products (“Feedback”).
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee’s copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.carconnectivity.mlmediaplayer.mediabrowser.cache.ProvidersToDownloadCache;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderToDownloadDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.retrofit.ProvidersToDownloadService;
import com.carconnectivity.mlmediaplayer.mediabrowser.retrofit.model.ProviderToDownloadModel;
import com.carconnectivity.mlmediaplayer.mediabrowser.retrofit.model.ProvidersToDownloadModel;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by sebastian.sokolowski on 17.02.16.
 */
public class ProvidersToDownloadManager {
    private static final String TAG = ProvidersToDownloadManager.class.getSimpleName();

    private static final String URL = "https://www.mirrorlink.com/";

    private final ProvidersToDownloadCache providersFromServerCache;
    private final Set<ComponentName> mediaBrowserPackages;

    public ProvidersToDownloadManager(Context context, Set<ComponentName> mediaBrowserPackages) {
        this.mediaBrowserPackages = mediaBrowserPackages;
        providersFromServerCache = new ProvidersToDownloadCache(context);
    }

    public void getCachedProviders() {
        List<ProviderToDownloadModel> providerFromServers = providersFromServerCache.getCachedProviders();
        if (providerFromServers != null && providerFromServers.size() > 0) {
            notifyAboutProviders(providerFromServers);
        }
    }

    public void refreshProviders() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .client(new OkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ProvidersToDownloadService apiService = retrofit.create(ProvidersToDownloadService.class);

        Call<ProvidersToDownloadModel> response = apiService.getProviders();
        response.enqueue(new Callback<ProvidersToDownloadModel>() {
            @Override
            public void onResponse(Call<ProvidersToDownloadModel> call, Response<ProvidersToDownloadModel> response) {
                if (validateProviderResponse(response)) {
                    List<ProviderToDownloadModel> providerToDownloadModels = response.body().getProviderToDownloadModelList();

                    providersFromServerCache.deleteAllRows();
                    providersFromServerCache.insert(providerToDownloadModels);

                    notifyAboutProviders(providerToDownloadModels);
                }
            }

            @Override
            public void onFailure(Call<ProvidersToDownloadModel> call, Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    private void notifyAboutProviders(List<ProviderToDownloadModel> providerToDownloadModels) {
        for (ProviderToDownloadModel providerToDownloadModel : providerToDownloadModels
                ) {
            boolean newProvider = isNewProvider(providerToDownloadModel);

            if (newProvider) {
                try {
                    URL url = new URL(providerToDownloadModel.getIconUrl());

                    ProviderViewToDownload providerViewToDownload =
                            new ProviderViewToDownload(providerToDownloadModel.getLabel(), providerToDownloadModel.getId(), url);
                    RsEventBus.post(new ProviderToDownloadDiscoveredEvent(providerViewToDownload));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean validateProviderResponse(Response<ProvidersToDownloadModel> response) {
        if (response == null) {
            return false;
        }
        if (response.body() == null) {
            return false;
        }
        List<ProviderToDownloadModel> providerToDownloadModels = response.body().getProviderToDownloadModelList();
        if (providerToDownloadModels == null || providerToDownloadModels.isEmpty()) {
            return false;
        }

        return true;
    }


    private boolean isNewProvider(ProviderToDownloadModel providerToDownloadModel) {
        String label = providerToDownloadModel.getLabel();
        String iconUrl = providerToDownloadModel.getIconUrl();
        String id = providerToDownloadModel.getId();

        if (label == null || label.isEmpty()) return false;
        if (iconUrl == null || iconUrl.isEmpty()) return false;
        if (id == null || id.isEmpty()) return false;

        boolean newProvider = true;
        for (ComponentName componentName : mediaBrowserPackages
                ) {
            if (componentName.getPackageName().equals(id)) {
                newProvider = false;
            }
        }

        return newProvider;
    }

}
