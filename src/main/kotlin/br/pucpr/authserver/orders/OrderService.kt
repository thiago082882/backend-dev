package br.pucpr.authserver.orders

// Service responsável pela lógica de negócio dos Pedidos.
// Implementa criação de pedidos, adição/remoção de produtos e consultas.
// Utiliza logs e exceções conforme as boas práticas da aula.
import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.products.ProductRepository
import br.pucpr.authserver.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class OrderService(
    val orderRepository: OrderRepository,
    val userRepository: UserRepository,       
    val productRepository: ProductRepository  
) {

    fun create(userId: Long): Order {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException("Usuário não encontrado. id=$userId")

        val order = Order(user = user)
        val saved = orderRepository.save(order)
        log.info("Pedido criado: id=${saved.id}, usuário=$userId")
        return saved
    }

 
    fun findById(id: Long): Order =
        orderRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Pedido não encontrado. id=$id")


    fun findByUser(userId: Long, status: String? = null): List<Order> {
        return if (status != null) {
            val upperStatus = status.uppercase()
            validateStatus(upperStatus) // Valida se o status é um valor permitido
            orderRepository.findByUserIdAndStatus(userId, upperStatus)
        } else {
            orderRepository.findByUserId(userId)
        }
    }

    fun findAll(status: String? = null): List<Order> {
        return if (status != null) {
            val upperStatus = status.uppercase()
            validateStatus(upperStatus)
            orderRepository.findByStatus(upperStatus)
        } else {
            orderRepository.findAll()
        }
    }


    fun closeOrder(orderId: Long, requestingUserId: Long, isAdmin: Boolean): Order {
        val order = findById(orderId)
        checkOwnership(order, requestingUserId, isAdmin, "fechar")

        if (order.status != "OPEN") {
            throw BadRequestException("Somente pedidos com status OPEN podem ser fechados. Status atual: ${order.status}")
        }
        if (order.items.isEmpty()) {
            throw BadRequestException("Não é possível fechar um pedido sem itens.")
        }

        order.status = "CLOSED"
        val saved = orderRepository.save(order)
        log.info("Pedido fechado: id=$orderId, usuário=$requestingUserId")
        return saved
    }

    fun delete(orderId: Long) {
        val order = findById(orderId) // Lança NotFoundException se não existir
        orderRepository.delete(order)
        log.info("Pedido removido: id=$orderId")
    }


    fun addItem(orderId: Long, productId: Long, quantity: Int, requestingUserId: Long, isAdmin: Boolean): Order {
        if (quantity <= 0) {
            throw BadRequestException("A quantidade deve ser maior que zero.")
        }

        val order = findById(orderId)
        checkOwnership(order, requestingUserId, isAdmin, "adicionar itens ao")

        if (order.status != "OPEN") {
            throw BadRequestException("Só é possível adicionar itens a pedidos com status OPEN.")
        }

     
        val product = productRepository.findByIdOrNull(productId)
            ?: throw NotFoundException("Produto não encontrado. id=$productId")

    
        val existingItem = order.items.find { it.product.id == productId }
        if (existingItem != null) {
            existingItem.quantity += quantity
            log.info("Quantidade do produto $productId incrementada no pedido $orderId. Nova qtd: ${existingItem.quantity}")
        } else {
            val item = OrderItem(order = order, product = product, quantity = quantity)
            order.items.add(item)
            log.info("Produto $productId adicionado ao pedido $orderId, qtd=$quantity")
        }

        return orderRepository.save(order)
    }
    fun removeItem(orderId: Long, productId: Long, requestingUserId: Long, isAdmin: Boolean): Order {
        val order = findById(orderId)
        checkOwnership(order, requestingUserId, isAdmin, "remover itens do")

        if (order.status != "OPEN") {
            throw BadRequestException("Só é possível remover itens de pedidos com status OPEN.")
        }

        val item = order.items.find { it.product.id == productId }
            ?: throw NotFoundException("Produto $productId não encontrado no pedido $orderId.")

        order.items.remove(item)
        val saved = orderRepository.save(order)
        log.info("Produto $productId removido do pedido $orderId")
        return saved
    }

    private fun validateStatus(status: String) {
        val allowed = setOf("OPEN", "CLOSED", "CANCELLED")
        if (status !in allowed) {
            throw BadRequestException("Status inválido: '$status'. Use: ${allowed.joinToString(", ")}.")
        }
    }

    private fun checkOwnership(order: Order, requestingUserId: Long, isAdmin: Boolean, action: String) {
        if (!isAdmin && order.user.id != requestingUserId) {
            throw ForbiddenException("Você não tem permissão para $action pedido ${order.id}.")
        }
    }

    companion object {
        // Logger SLF4J para registrar operações do service de pedidos
        val log = LoggerFactory.getLogger(OrderService::class.java)
    }
}
