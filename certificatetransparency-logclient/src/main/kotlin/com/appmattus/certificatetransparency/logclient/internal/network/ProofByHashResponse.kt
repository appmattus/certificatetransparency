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

package com.appmattus.certificatetransparency.logclient.internal.network

import com.google.gson.annotations.SerializedName

/**
 * @property leafIndex The 0-based index of the end entity corresponding to the "hash" parameter.
 * @property auditPath An array of base64-encoded Merkle Tree nodes proving the inclusion of the chosen certificate.
 */
internal data class ProofByHashResponse(
    @SerializedName("leaf_index") val leafIndex: Long,
    @SerializedName("audit_path") val auditPath: List<String>
)
