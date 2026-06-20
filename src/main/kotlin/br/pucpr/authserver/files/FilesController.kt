package br.pucpr.authserver.files

import br.pucpr.authserver.exception.NotFoundException
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/files")
class FilesController(
    private val storage: FileStorage
) {

    @SecurityRequirement(name = "AuthServer")
    @GetMapping("/{filename}", produces = ["image/jpeg", "image/png"])
    fun serve(@PathVariable filename: String): ResponseEntity<Resource> {

        val contentType = if (filename.endsWith("png"))
            MediaType.IMAGE_PNG
        else
            MediaType.IMAGE_JPEG

        val resource = storage.load(filename)
            ?: throw NotFoundException(filename)

        return ResponseEntity.ok()
            .contentType(contentType)
            .body(resource)
    }
}