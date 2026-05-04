package br.pucpr.authserver.products


import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductService(
    val repository: ProductRepository
) {

    fun insert(product: Product): Product {
        if (repository.findByNameIgnoreCase(product.name) != null) {
            throw BadRequestException("Produto '${product.name}' já existe.")
        }
        if (product.price < 0) {
            throw BadRequestException("O preço do produto não pode ser negativo.")
        }
        val saved = repository.save(product)
        log.info("Produto criado: id=${saved.id}, nome='${saved.name}', categoria='${saved.category}'")
        return saved
    }

    fun findAll(
        category: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,     // "name" ou "price"
        sortDir: SortDir = SortDir.ASC
    ): List<Product> {
    
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw BadRequestException("minPrice ($minPrice) não pode ser maior que maxPrice ($maxPrice).")
        }

        val filtered: List<Product> = when {
            category != null && minPrice != null && maxPrice != null ->
                repository.findByCategoryIgnoreCaseAndPriceBetween(category, minPrice, maxPrice)

            category != null ->
                repository.findByCategoryIgnoreCase(category)

            minPrice != null && maxPrice != null ->
                repository.findByPriceBetween(minPrice, maxPrice)

            sortBy?.lowercase() == "price" && sortDir == SortDir.ASC ->
                repository.findAllOrderByPriceAsc()

            sortBy?.lowercase() == "price" && sortDir == SortDir.DESC ->
                repository.findAllOrderByPriceDesc()

            else -> {
                val sort = if (sortDir == SortDir.ASC)
                    Sort.by("name").ascending()
                else
                    Sort.by("name").descending()
                repository.findAll(sort)
            }
        }

        return if (sortBy?.lowercase() == "name" && (category != null || minPrice != null)) {
            if (sortDir == SortDir.ASC) filtered.sortedBy { it.name.lowercase() }
            else filtered.sortedByDescending { it.name.lowercase() }
        } else if (sortBy?.lowercase() == "price" && (category != null || minPrice != null)) {
            if (sortDir == SortDir.ASC) filtered.sortedBy { it.price }
            else filtered.sortedByDescending { it.price }
        } else {
            filtered
        }
    }

    fun findById(id: Long): Product =
        repository.findByIdOrNull(id) ?: throw NotFoundException("Produto não encontrado. id=$id")

    fun update(id: Long, name: String, description: String, price: Double, category: String): Product? {
        if (price < 0) {
            throw BadRequestException("O preço do produto não pode ser negativo.")
        }
        val product = findById(id)

        val hasChanges = product.name != name ||
                product.description != description ||
                product.price != price ||
                product.category != category

        if (!hasChanges) return null

        product.name = name
        product.description = description
        product.price = price
        product.category = category

        val updated = repository.save(product)
        log.info("Produto atualizado: id=${updated.id}, nome='${updated.name}'")
        return updated
    }

    fun delete(id: Long) {
        val product = findById(id) // Lança NotFoundException se não existir
        repository.delete(product)
        log.info("Produto removido: id=$id, nome='${product.name}'")
    }

    companion object {
        // Logger SLF4J para registrar operações importantes do service
        val log = LoggerFactory.getLogger(ProductService::class.java)
    }
}
