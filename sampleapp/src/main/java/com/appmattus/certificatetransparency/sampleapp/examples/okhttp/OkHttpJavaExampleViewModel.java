/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2019 Babylon Partners Limited
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
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.appmattus.certificatetransparency.sampleapp.examples.okhttp;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appmattus.certificatetransparency.CTInterceptorBuilder;
import com.appmattus.certificatetransparency.CTLogger;
import com.appmattus.certificatetransparency.cache.AndroidDiskCache;
import com.appmattus.certificatetransparency.sampleapp.R;
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpJavaExampleViewModel extends BaseExampleViewModel {

    public OkHttpJavaExampleViewModel(@NotNull Application application) {
        super(application);
    }

    @NotNull
    @Override
    public String getSampleCodeTemplate() {
        return "okhttp-java.txt";
    }

    @NonNull
    @Override
    public String getTitle() {
        return getApplication().getString(R.string.okhttp_java_example);
    }

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private OkHttpClient createOkHttpClient(Set<String> includeHosts, Set<String> excludeHosts, boolean isFailOnError, CTLogger defaultLogger) {
        // Create a network interceptor
        CTInterceptorBuilder builder = new CTInterceptorBuilder()
            .setFailOnError(isFailOnError)
            .setLogger(defaultLogger)
            .setDiskCache(new AndroidDiskCache(getApplication()));

        for (String host : excludeHosts) {
            builder.excludeHost(host);
        }
        for (String host : includeHosts) {
            builder.includeHost(host);
        }

        Interceptor networkInterceptor = builder.build();

        // Set the interceptor when creating the OkHttp client
        return new OkHttpClient.Builder()
            .addNetworkInterceptor(networkInterceptor)
            .build();
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
