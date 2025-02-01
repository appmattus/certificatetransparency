/*
 * Copyright 2023 Appmattus Limited
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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.readBytes

abstract class UpdateLogListTask : DefaultTask() {

    private fun String.decodeKey(): PublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(this)))
    }

    private val googleLogListPublicKey =
        ("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsu0BHGnQ++W2CTdyZyxvHHRALOZPlnu/VMVgo2m+JZ8MNbAOH2cgXb8mvOj8flsX/qPMuKIaauO+PwROMjiqfUpc" +
            "Fm80Kl7i97ZQyBDYKm3MkEYYpGN+skAR2OebX9G2DfDqFY8+jUpOOWtBNr3LrmVcwx+FcFdMjGDlrZ5JRmoJ/SeGKiORkbbu9eY1Wd0uVhz/xI5bQb0OgII7hEj+i/IP" +
            "bJqOHgB8xQ5zWAJJ0DmG+FM6o7gk403v6W3S8qRYiR84c50KppGwe4YqSMkFbLDleGQWLoaDSpEWtESisb4JiLaY4H+Kk0EyAhPSb+49JfUozYl+lf7iFN3qRq/SIXXT" +
            "h6z0S7Qa8EYDhKGCrpI03/+qprwy+my6fpWHi6aUIk4holUCmWvFxZDfixoxK0RlqbFDl2JXMBquwlQpm8u5wrsic1ksIv9z8x9zh4PJqNpCah0ciemI3YGRQqSe/mRR" +
            "XBiSn9YQBUPcaeqCYan+snGADFwHuXCd9xIAdFBolw9R9HTedHGUfVXPJDiF4VusfX6BRR/qaadB+bqEArF/TzuDUr6FvOR4o8lUUxgLuZ/7HO+bHnaPFKYHHSm++z1l" +
            "VDhhYuSZ8ax3T0C3FZpb7HMjZtpEorSV5ElKJEJwrhrBCMOD8L01EoSPrGlS1w22i9uGHMn/uGQKo28u7AsCAwEAAQ==").decodeKey()

    @TaskAction
    fun greet() {
        val url = "https://www.gstatic.com/ct/log_list/v3/log_list.zip"
        val outputFolder = project.projectDir.toPath().resolve("src/main/resources").apply {
            createDirectories()
        }

        val connection = URI(url).toURL().openConnection().apply {
            connect()
        }

        ZipInputStream(connection.getInputStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val canonicalOutputFolder = outputFolder.normalize()
                val outputFile = outputFolder.resolve(entry.name)
                val canonicalOutputFile = outputFile.normalize()

                if (!canonicalOutputFile.startsWith(canonicalOutputFolder)) {
                    throw IllegalStateException("Entry is outside of the target dir: ${entry.name}")
                }

                if (!entry.isDirectory) {
                    canonicalOutputFile.toFile().outputStream().use { output ->
                        zipInputStream.copyTo(output)
                    }
                }
                entry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
        }

        if (!Signature.getInstance("SHA256withRSA").apply {
                initVerify(googleLogListPublicKey)
                update(outputFolder.resolve("log_list.json").readBytes())
            }.verify(outputFolder.resolve("log_list.sig").readBytes())) {
            throw IllegalStateException("Signature validation failed")
        }
    }
}
