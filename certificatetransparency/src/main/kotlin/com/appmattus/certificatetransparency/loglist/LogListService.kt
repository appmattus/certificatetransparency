/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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

package com.appmattus.certificatetransparency.loglist

/**
 * Service to load log_list.json and log_list.sig file from the network.
 * Typically returned from https://www.gstatic.com/ct/log_list/v3/log_list.json
 */
public interface LogListService {
    /**
     * Contents of https://www.gstatic.com/ct/log_list/v3/log_list.json
     * The list of CT Logs that are currently compliant with Chrome's CT policy (or have been and were disqualified), and are included in Chrome
     */
    public suspend fun getLogList(): ByteArray

    /**
     * Contents of https://www.gstatic.com/ct/log_list/v3/log_list.sig
     * log_list.json is signed by Google. The public key to verify log_list.sig can be found at
     * https://www.gstatic.com/ct/log_list/v3/log_list_pubkey.pem
     */
    public suspend fun getLogListSignature(): ByteArray

    /**
     * Contents of https://www.gstatic.com/ct/log_list/v3/log_list.zip
     * log_list.json and the corresponding log_list.sig can also be obtained by downloading the zip file containing both of them
     */
    public suspend fun getLogListZip(): ByteArray
}
