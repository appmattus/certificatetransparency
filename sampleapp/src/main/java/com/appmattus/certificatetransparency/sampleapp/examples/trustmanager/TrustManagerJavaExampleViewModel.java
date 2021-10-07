/*
 * Copyright 2021 Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appmattus.certificatetransparency.sampleapp.examples.trustmanager;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appmattus.certificatetransparency.CTLogger;
import com.appmattus.certificatetransparency.CTTrustManagerBuilder;
import com.appmattus.certificatetransparency.cache.AndroidDiskCache;
import com.appmattus.certificatetransparency.sampleapp.R;
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrustManagerJavaExampleViewModel extends BaseExampleViewModel {

    public TrustManagerJavaExampleViewModel(@NotNull Application application) {
        super(application);
    }

    @NotNull
    @Override
    public String getSampleCodeTemplate() {
        return "trustmanager-java.txt";
    }

    @NonNull
    @Override
    public String getTitle() {
        return getApplication().getString(R.string.trust_manager_java_example);
    }

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private OkHttpClient createOkHttpClient(Set<String> includeHosts, Set<String> excludeHosts, boolean isFailOnError, CTLogger defaultLogger) {

        // Create a trust manager
        CTTrustManagerBuilder builder = new CTTrustManagerBuilder(getTrustManager())
            .setFailOnError(isFailOnError)
            .setLogger(defaultLogger)
            .setDiskCache(new AndroidDiskCache(getApplication()));

        for (String host : excludeHosts) {
            builder.excludeCommonName(host);
        }
        for (String host : includeHosts) {
            builder.includeCommonName(host);
        }

        X509TrustManager wrappedTrustManager = builder.build();

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{wrappedTrustManager}, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException expected) {
            throw new IllegalStateException("Unable to create an SSLContext");
        }

        // Set the interceptor when creating the OkHttp client
        return new OkHttpClient.Builder()
            .sslSocketFactory(sslContext.getSocketFactory(), wrappedTrustManager)
            .build();
    }

    public X509TrustManager getTrustManager() throws IllegalStateException {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    return (X509TrustManager) trustManager;
                }
            }
        } catch (KeyStoreException | NoSuchAlgorithmException exception) {
            // ignored
        }
        throw new IllegalStateException("Unable to create a TrustManager");
    }

    @Override
    public void openConnection(
        @NotNull String connectionHost,
        @NotNull Set<String> includeHosts,
        @NotNull Set<String> excludeHosts,
        boolean isFailOnError,
        @NotNull CTLogger defaultLogger
    ) {
        OkHttpClient client = createOkHttpClient(includeHosts, excludeHosts, isFailOnError, defaultLogger);

        Request request = new Request.Builder().url("https://" + connectionHost).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Failure. Send message to the UI as logger won't catch generic network exceptions
                sendException(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                // Success. Reason will have been sent to the logger
            }
        });
    }
}
