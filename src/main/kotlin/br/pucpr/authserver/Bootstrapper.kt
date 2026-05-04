package br.pucpr.authserver

import br.pucpr.authserver.products.Product
import br.pucpr.authserver.products.ProductRepository
import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.users.User
import br.pucpr.authserver.users.UserRepository
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class Bootstrapper(
    val rolesRepository: RoleRepository,
    val userRepository: UserRepository,
    val productRepository: ProductRepository  
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {

       
        val adminRole =
            rolesRepository.findByName("ADMIN") ?: rolesRepository
                .save(Role(name = "ADMIN", description = "System Administrator"))
        rolesRepository.findByName("PREMIUM") ?: rolesRepository
            .save(Role(name = "PREMIUM", description = "Premium user"))

    
        if (userRepository.findByRole("ADMIN").isEmpty()) {
            val admin = User(
                email = "admin@authserver.com",
                password = "admin",
                name = "Auth Server Administrator",
            )
            admin.roles.add(adminRole)
            userRepository.save(admin)
        }

       
        if (productRepository.count() == 0L) {
            listOf(
                Product(name = "Notebook Pro 15",    description = "Notebook de alta performance", price = 4999.90, category = "ELETRONICO"),
                Product(name = "Mouse Sem Fio",      description = "Mouse ergonômico bluetooth",   price = 129.90,  category = "ELETRONICO"),
                Product(name = "Teclado Mecânico",   description = "Teclado gamer RGB",            price = 299.90,  category = "ELETRONICO"),
                Product(name = "Cadeira Gamer",      description = "Cadeira com apoio lombar",     price = 899.00,  category = "MOBILIARIO"),
                Product(name = "Mesa de Escritório", description = "Mesa L 1,5m",                  price = 650.00,  category = "MOBILIARIO"),
                Product(name = "Café Premium",       description = "Café arábica 500g",            price = 39.90,   category = "ALIMENTO"),
            ).forEach { productRepository.save(it) }
        }
    }
}