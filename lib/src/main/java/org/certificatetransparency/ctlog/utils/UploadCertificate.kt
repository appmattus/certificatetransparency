package org.certificatetransparency.ctlog.utils

import com.google.common.io.Files
import org.certificatetransparency.ctlog.comm.HttpLogClient
import org.certificatetransparency.ctlog.serialization.CryptoDataLoader
import java.io.File
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException

/** Utility class for uploading a certificate.  */
object UploadCertificate {
    @Throws(IOException::class, CertificateException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, SignatureException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("Usage: ${UploadCertificate::class.java.simpleName} <certificates chain> [output file]")
            return
        }

        val pemFile = args[0]

        val certs = CryptoDataLoader.certificatesFromFile(File(pemFile))
        println("Total number of certificates in chain: ${certs.size}")

        val client = HttpLogClient("http://ct.googleapis.com/pilot/ct/v1/")

        val resp = client.addCertificate(certs)

        println(resp)
        if (args.size >= 2) {
            val outputFile = args[1]
            //TODO(eranm): Binary encoding compatible with the C++ code.
            Files.write(resp.toByteArray(), File(outputFile))
        }
    }
}
