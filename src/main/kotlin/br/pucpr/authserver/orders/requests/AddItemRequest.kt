package br.pucpr.authserver.orders.requests

// DTO para adicionar um produto a um pedido via POST /orders/{orderId}/items.
// Informa qual produto adicionar e em que quantidade.
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddItemRequest(

    @NotNull(message = "O ID do produto é obrigatório.")
    val productId: Long?,    

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1.")
    val quantity: Int = 1    
)
