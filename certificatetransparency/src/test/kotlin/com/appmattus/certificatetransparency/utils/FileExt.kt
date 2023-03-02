/*
 * Copyright 2021-2023 Appmattus Limited
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

package com.appmattus.certificatetransparency.utils

import com.appmattus.certificatetransparency.internal.utils.PublicKeyFactory
import java.io.File
import java.security.PublicKey

/**
 * Load EC or RSA [PublicKey] from a PEM file.
 *
 * @receiver [File] containing the key.
 * @return [PublicKey] represented by this [File].
 */
internal fun File.readPemFile(): PublicKey = PublicKeyFactory.fromPemString(readText())
