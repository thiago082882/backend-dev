package br.pucpr.authserver.products.responses


import br.pucpr.authserver.products.Product

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val category: String
) {
    constructor(product: Product) : this(
        id = product.id!!,
        name = product.name,
        description = product.description,
        price = product.price,
        category = product.category
    )
}
