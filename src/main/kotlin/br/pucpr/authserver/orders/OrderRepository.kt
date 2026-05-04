package br.pucpr.authserver.orders


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

  
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    fun findByUserId(userId: Long): List<Order>

   
    fun findByStatus(status: String): List<Order>


    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    fun findByUserIdAndStatus(userId: Long, status: String): List<Order>
}
