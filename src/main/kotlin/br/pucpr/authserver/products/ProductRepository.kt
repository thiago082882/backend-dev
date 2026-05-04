package br.pucpr.authserver.products

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun findByCategoryIgnoreCase(category: String): List<Product>

  
    fun findByPriceBetween(minPrice: Double, maxPrice: Double): List<Product>

    fun findByCategoryIgnoreCaseAndPriceBetween(
        category: String,
        minPrice: Double,
        maxPrice: Double
    ): List<Product>

    fun findByNameIgnoreCase(name: String): Product?

    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    fun findAllOrderByPriceAsc(): List<Product>

    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    fun findAllOrderByPriceDesc(): List<Product>
}
