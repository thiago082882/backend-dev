package br.pucpr.authserver.orders.responses

import br.pucpr.authserver.orders.Order
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val status: String,
    val createdAt: LocalDateTime,
    val items: List<OrderItemResponse>, 
    val total: Double                   
) {
    // Construtor que converte a entidade Order em DTO de resposta
    constructor(order: Order) : this(
        id = order.id!!,
        userId = order.user.id!!,
        userName = order.user.name,
        status = order.status,
        createdAt = order.createdAt,
        items = order.items.map { OrderItemResponse(it) },       
        total = order.items.sumOf { it.product.price * it.quantity } 
    )
}
