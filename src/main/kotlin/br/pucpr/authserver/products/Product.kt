package br.pucpr.authserver.products


import jakarta.persistence.*

@Entity
@Table(name = "ProductTable") 
class Product(

    @Id @GeneratedValue 
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,             

    @Column(nullable = false)
    var description: String = "", 

    @Column(nullable = false)
    var price: Double,            

    @Column(nullable = false)
    var category: String          
)
