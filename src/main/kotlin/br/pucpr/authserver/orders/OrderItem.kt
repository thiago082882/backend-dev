package br.pucpr.authserver.orders


import br.pucpr.authserver.products.Product
import jakarta.persistence.*

@Entity
@Table(name = "OrderItemTable") 
class OrderItem(

    @Id @GeneratedValue 
    var id: Long? = null,

    
    @ManyToOne(optional = false)
    @JoinColumn(name = "idOrder", nullable = false)
    var order: Order,

  
    @ManyToOne(optional = false)
    @JoinColumn(name = "idProduct", nullable = false)
    var product: Product,

    
    @Column(nullable = false)
    var quantity: Int = 1
)
