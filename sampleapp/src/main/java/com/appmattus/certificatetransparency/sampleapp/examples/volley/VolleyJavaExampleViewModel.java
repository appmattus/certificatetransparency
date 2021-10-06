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

package com.appmattus.certificatetransparency.sampleapp.examples.volley;

import android.app.Application;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appmattus.certificatetransparency.CTHostnameVerifierBuilder;
import com.appmattus.certificatetransparency.CTLogger;
import com.appmattus.certificatetransparency.cache.AndroidDiskCache;
import com.appmattus.certificatetransparency.sampleapp.R;
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class VolleyJavaExampleViewModel extends BaseExampleViewModel {

    public VolleyJavaExampleViewModel(@NotNull Application application) {
        super(application);
    }

    @NotNull
    @Override
    public String getSampleCodeTemplate() {
        return "volley-java.txt";
    }

    @NonNull
    @Override
    public String getTitle() {
        return getApplication().getString(R.string.volley_java_example);
    }

    private void enableCertificateTransparencyChecks(
        HttpURLConnection connection,
        Set<String> includeHosts,
        Set<String> excludeHosts,
        boolean isFailOnError,
        CTLogger defaultLogger
    ) {
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

            // Create a hostname verifier wrapping the original
            CTHostnameVerifierBuilder builder = new CTHostnameVerifierBuilder(httpsConnection.getHostnameVerifier())
                .setFailOnError(isFailOnError)
                .setLogger(defaultLogger)
                .setDiskCache(new AndroidDiskCache(getApplication()));

            for (String host : excludeHosts) {
                builder.excludeHost(host);
            }
            for (String host : includeHosts) {
                builder.includeHost(host);
            }

            httpsConnection.setHostnameVerifier(builder.build());
        }
    }

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private RequestQueue createRequestQueue(Set<String> includeHosts, Set<String> excludeHosts, boolean isFailOnError, CTLogger defaultLogger) {
        return Volley.newRequestQueue(getApplication(),
            new HurlStack() {
                @Override
                protected HttpURLConnection createConnection(URL url) throws IOException {
                    HttpURLConnection connection = super.createConnection(url);

                    enableCertificateTransparencyChecks(connection, includeHosts, excludeHosts, isFailOnError, defaultLogger);

                    return connection;
                }
            }
        );
    }

    @Override
    public void openConnection(
        @NotNull String connectionHost,
        @NotNull Set<String> includeHosts,
        @NotNull Set<String> excludeHosts,
        boolean isFailOnError,
        @NotNull CTLogger defaultLogger
    ) {
        RequestQueue queue = createRequestQueue(includeHosts, excludeHosts, isFailOnError, defaultLogger);

        // Failure. Send message to the UI as logger won't catch generic network exceptions
        Request<String> request = new StringRequest(Request.Method.GET, "https://" + connectionHost,
            response -> {
                // Success. Reason will have been sent to the logger
            },
            this::sendException);

        // Explicitly disable cache so we always call the interceptor and thus see the certificate transparency results
        request.setShouldCache(false);

        // Add the request to the RequestQueue.
        queue.add(request);

    }
}
