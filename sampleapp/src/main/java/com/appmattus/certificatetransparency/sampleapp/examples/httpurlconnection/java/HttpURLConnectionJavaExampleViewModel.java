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

package com.appmattus.certificatetransparency.sampleapp.examples.httpurlconnection.java;

import android.app.Application;

import com.appmattus.certificatetransparency.CTHostnameVerifierBuilder;
import com.appmattus.certificatetransparency.CTLogger;
import com.appmattus.certificatetransparency.cache.AndroidDiskCache;
import com.appmattus.certificatetransparency.sampleapp.examples.BaseExampleViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class HttpURLConnectionJavaExampleViewModel extends BaseExampleViewModel {

    public HttpURLConnectionJavaExampleViewModel(@NotNull Application application) {
        super(application);
    }

    @NotNull
    @Override
    public String getSampleCodeTemplate() {
        return "httpurlconnection-java.txt";
    }

    private void enableCertificateTransparencyChecks(
        HttpURLConnection connection,
        Set<String> hosts,
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

            for (String host : hosts) {
                builder.includeHost(host);
            }

            httpsConnection.setHostnameVerifier(builder.build());
        }
    }

    @Override
    public void openConnection(@NotNull String connectionHost, @NotNull Set<String> hosts, boolean isFailOnError, @NotNull CTLogger defaultLogger) {
        // Quick and dirty way to push the network call onto a background thread, don't do this is a real app
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://" + connectionHost).openConnection();

                enableCertificateTransparencyChecks(connection, hosts, isFailOnError, defaultLogger);

                connection.connect();
            } catch (IOException e) {
                sendException(e);
            }
        }).start();
    }
}
