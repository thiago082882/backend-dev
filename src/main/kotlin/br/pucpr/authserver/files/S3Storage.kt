package br.pucpr.authserver.files

import br.pucpr.authserver.users.User
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component("fileStorage")
class S3Storage : FileStorage {

    private val s3: AmazonS3 = AmazonS3ClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    override fun save(user: User, path: String, file: MultipartFile) {

        val key = normalizeKey(path)

        val meta = ObjectMetadata().apply {
            contentType = file.contentType ?: "application/octet-stream"
            contentLength = file.size
            userMetadata["userId"] = user.id.toString()
            userMetadata["originalFilename"] = file.originalFilename ?: "unknown"
        }

        val transferManager = TransferManagerBuilder.standard()
            .withS3Client(s3)
            .build()

        transferManager.upload(PUBLIC, key, file.inputStream, meta)
            .waitForUploadResult()
    }

    override fun load(path: String): Resource? {
        val key = normalizeKey(path)

        return try {
            val obj = s3.getObject(PUBLIC, key)
            val bytes = obj.objectContent.readAllBytes()
            ByteArrayResource(bytes)
        } catch (e: AmazonS3Exception) {
            if (e.statusCode == 404) null else throw e
        }
    }

    override fun urlFor(name: String): String {
        val key = normalizeKey(name)
        return "$PREFIX$key"
    }

    private fun normalizeKey(path: String): String =
        path.replace("--", "/")
            .removePrefix("/")

    companion object {
        private const val PUBLIC = "th-authserver-public"
        private const val PREFIX =
            "https://th-authserver-public.s3.us-east-1.amazonaws.com/"
    }
}