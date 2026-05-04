package br.pucpr.authserver.products

import br.pucpr.authserver.products.requests.CreateProductRequest
import br.pucpr.authserver.products.requests.UpdateProductRequest
import br.pucpr.authserver.products.responses.ProductResponse
import br.pucpr.authserver.users.SortDir
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products") // Todas as rotas começam com /products
class ProductController(val service: ProductService) {

    @GetMapping
    @Operation(summary = "Lista produtos com filtros e ordenação")
    fun list(
        @RequestParam category: String? = null,
        @RequestParam minPrice: Double? = null,
        @RequestParam maxPrice: Double? = null,
        @RequestParam sortBy: String? = null,
        @RequestParam sortDir: String? = null
    ): ResponseEntity<List<ProductResponse>> {
        val dir = SortDir.find(sortDir ?: "ASC")
        return service.findAll(category, minPrice, maxPrice, sortBy, dir)
            .map { ProductResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto pelo ID")
    fun getById(@PathVariable id: Long): ResponseEntity<ProductResponse> =
        service.findById(id)
            .let { ProductResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Somente administradores podem criar produtos
    @SecurityRequirement(name = "jwt-auth") // Indica no Swagger que precisa de token
    @Operation(summary = "Cria novo produto (ADMIN)")
    fun insert(
        @Valid @RequestBody request: CreateProductRequest
    ): ResponseEntity<ProductResponse> =
        service.insert(request.toProduct())
            .let { ProductResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") 
    @SecurityRequirement(name = "jwt-auth")
    @Operation(summary = "Atualiza produto (ADMIN)")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest
    ): ResponseEntity<ProductResponse> =
        service.update(
            id,
            name = request.name!!,
            description = request.description ?: "",
            price = request.price!!,
            category = request.category!!.uppercase()
        )
            ?.let { ProductResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()

   
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Somente administradores podem remover produtos
    @SecurityRequirement(name = "jwt-auth")
    @Operation(summary = "Remove produto (ADMIN)")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.ok().build()
    }
}
