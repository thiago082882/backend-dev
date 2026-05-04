package br.pucpr.authserver.orders

import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.orders.requests.AddItemRequest
import br.pucpr.authserver.orders.responses.OrderResponse
import br.pucpr.authserver.security.UserToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders") // Todas as rotas começam com /orders
@PreAuthorize("isAuthenticated()") // Todos os endpoints de orders exigem autenticação
@SecurityRequirement(name = "jwt-auth")  // Exibe cadeado no Swagger para todos os endpoints
class OrderController(val service: OrderService) {

   
    @PostMapping
    @Operation(summary = "Cria novo pedido para o usuário autenticado")
    fun create(auth: Authentication): ResponseEntity<OrderResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        return service.create(token.id)
            .let { OrderResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Somente admin pode ver todos os pedidos
    @Operation(summary = "Lista todos os pedidos (ADMIN)")
    fun listAll(
        @RequestParam status: String? = null
    ): ResponseEntity<List<OrderResponse>> =
        service.findAll(status)
            .map { OrderResponse(it) }
            .let { ResponseEntity.ok(it) }

    
    @Operation(summary = "Lista os pedidos do usuário autenticado")
    fun listMine(
        @RequestParam status: String? = null,
        auth: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        return service.findByUser(token.id, status)
            .map { OrderResponse(it) }
            .let { ResponseEntity.ok(it) }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por ID (dono ou ADMIN)")
    fun getById(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        val order = service.findById(id)


        if (!token.isAdmin && order.user.id != token.id) {
            throw ForbiddenException("Você não tem permissão para visualizar este pedido.")
        }

        return ResponseEntity.ok(OrderResponse(order))
    }


    @PostMapping("/{id}/close")
    @Operation(summary = "Fecha o pedido (dono ou ADMIN)")
    fun close(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        return service.closeOrder(id, token.id, token.isAdmin)
            .let { OrderResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Somente admin pode deletar pedidos
    @Operation(summary = "Remove pedido (ADMIN)")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Adiciona produto ao pedido (dono ou ADMIN)")
    fun addItem(
        @PathVariable id: Long,
        @Valid @RequestBody request: AddItemRequest,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        return service.addItem(
            orderId = id,
            productId = request.productId!!,
            quantity = request.quantity,
            requestingUserId = token.id,
            isAdmin = token.isAdmin
        )
            .let { OrderResponse(it) }
            .let { ResponseEntity.ok(it) }
    }


    @DeleteMapping("/{id}/items/{productId}")
    @Operation(summary = "Remove produto do pedido (dono ou ADMIN)")
    fun removeItem(
        @PathVariable id: Long,
        @PathVariable productId: Long,
        auth: Authentication
    ): ResponseEntity<OrderResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        return service.removeItem(
            orderId = id,
            productId = productId,
            requestingUserId = token.id,
            isAdmin = token.isAdmin
        )
            .let { OrderResponse(it) }
            .let { ResponseEntity.ok(it) }
    }
}
