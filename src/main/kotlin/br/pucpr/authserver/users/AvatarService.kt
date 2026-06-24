package br.pucpr.authserver.users

import br.pucpr.authserver.exceptions.UnsupportedMediaTypeException
import br.pucpr.authserver.files.FileStorage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarService(
    @Qualifier("fileStorage") val storage: FileStorage,
    val providerClient: AvatarProviderClient
) {
    fun save(user: User, avatar: MultipartFile): String =
        try {
            val extension = when (avatar.contentType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                else -> throw UnsupportedMediaTypeException("jpeg", "png")
            }
            val path = "${user.id}/a_${user.id}.$extension"
            storage.save(user, "$ROOT/$path", avatar)
            "${user.id}/xl_a_${user.id}.png"
        } catch (exception: Error) {
            log.error("Unable to store avatar of user ${user.id}! Using default.", exception)
            DEFAULT_AVATAR
        }

    /**
     * Busca um avatar automaticamente: Gravatar primeiro, ui-avatars.com como fallback.
     * Baixa os bytes e sobe para o storage configurado (S3 ou FileSystem).
     */
    fun resolveAndSave(user: User): String {
        val bytes = providerClient.fetchGravatar(user.email)
            ?: providerClient.fetchUiAvatar(user.name)

        val path = "${user.id}/auto_a_${user.id}.png"

        return try {
            storage.saveBytes(user, "$ROOT/$path", bytes, "image/png")
            path
        } catch (exception: Exception) {
            log.error("Unable to store resolved avatar of user ${user.id}! Using default.", exception)
            DEFAULT_AVATAR
        }
    }

    fun urlFor(path: String) = storage.urlFor("$ROOT/$path")

    companion object {
        const val ROOT = "avatar"
        const val DEFAULT_AVATAR = "default_avatar.jpg"
        private val log = LoggerFactory.getLogger(AvatarService::class.java)
    }
}