package br.pucpr.authserver.products.requests

import br.pucpr.authserver.products.Product
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateProductRequest(

    @NotBlank(message = "O nome do produto é obrigatório.")
    val name: String?,

    val description: String? = "",

    @Positive(message = "O preço deve ser maior que zero.")
    val price: Double?,

    @NotBlank(message = "A categoria é obrigatória.")
    val category: String?
) {

    fun toProduct() = Product(
        name = name!!,
        description = description ?: "",
        price = price!!,
        category = category!!.uppercase() // Normaliza a categoria para maiúsculas
    )
}
