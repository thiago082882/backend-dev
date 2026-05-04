package br.pucpr.authserver.products.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class UpdateProductRequest(

    @NotBlank(message = "O nome do produto é obrigatório.")
    val name: String?,

    val description: String? = "",

    @Positive(message = "O preço deve ser maior que zero.")
    val price: Double?,

    @NotBlank(message = "A categoria é obrigatória.")
    val category: String?
)
