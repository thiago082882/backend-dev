package br.pucpr.authserver.users

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.HttpClientErrorException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class AvatarProviderClient {
    private val client = RestClient.create()

    /** Tenta buscar o avatar no Gravatar. Retorna null se o usuário não tiver um. */
    fun fetchGravatar(email: String): ByteArray? {
        val hash = md5Hex(email.trim().lowercase())
        val url = "https://gravatar.com/avatar/$hash?d=404&size=256"

        return try {
            client.get()
                .uri(url)
                .retrieve()
                .body(ByteArray::class.java)
        } catch (e: HttpClientErrorException.NotFound) {
            log.info("Gravatar não encontrado para o e-mail informado")
            null
        } catch (e: Exception) {
            log.warn("Falha ao consultar o Gravatar: ${e.message}")
            null
        }
    }

    /** Gera um avatar de letras a partir do nome via ui-avatars.com. */
    fun fetchUiAvatar(name: String): ByteArray {
        val encodedName = URLEncoder.encode(name.ifBlank { "U" }, StandardCharsets.UTF_8)
        val url = "https://ui-avatars.com/api/?name=$encodedName&format=png&size=256&background=random"

        return client.get()
            .uri(url)
            .retrieve()
            .body(ByteArray::class.java)
            ?: throw IllegalStateException("ui-avatars.com não retornou conteúdo")
    }

    private fun md5Hex(input: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AvatarProviderClient::class.java)
    }
}