package br.pucpr.authserver.orders.responses


import br.pucpr.authserver.orders.OrderItem

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val subtotal: Double         
) {
    
    constructor(item: OrderItem) : this(
        id = item.id!!,
        productId = item.product.id!!,
        productName = item.product.name,
        productPrice = item.product.price,
        quantity = item.quantity,
        subtotal = item.product.price * item.quantity 
    )
}
