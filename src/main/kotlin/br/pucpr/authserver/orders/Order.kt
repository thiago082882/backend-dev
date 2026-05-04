package br.pucpr.authserver.orders


import br.pucpr.authserver.users.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "OrderTable") 
class Order(

    @Id @GeneratedValue
    var id: Long? = null,

    
    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser", nullable = false)
    var user: User,

   
    @Column(nullable = false)
    var status: String = "OPEN",

  
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),


    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var items: MutableList<OrderItem> = mutableListOf()
)
