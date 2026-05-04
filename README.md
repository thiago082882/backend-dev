# Backend Dev – API com Produtos e Pedidos

## Visão Geral

Este projeto é um backend Spring Boot (Kotlin) com autenticação JWT, banco H2 em memória e os módulos:

- **Users** (existente) – cadastro, login, gestão de papéis
- **Roles** (existente) – administração de papéis
- **Products** (novo) – CRUD completo de produtos com filtragem e ordenação
- **Orders** (novo) – Pedidos de usuários com associação de produtos

---

## Novas Classes e Relacionamento

```
User ──< Order ──< OrderItem >── Product
```

| Relação | Tipo |
|---|---|
| User → Order | one-to-many (um usuário tem vários pedidos) |
| Order → OrderItem | one-to-many (um pedido tem vários itens) |
| OrderItem → Product | many-to-one (muitos itens referenciam um produto) |

Isso cria um **many-to-many entre Order e Product** intermediado por `OrderItem`, que também guarda a quantidade.

---

## Módulo: Products

### Entidade `Product`

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long | Chave primária |
| name | String | Nome do produto |
| description | String | Descrição |
| price | Double | Preço unitário |
| category | String | Categoria (ELETRONICO, MOBILIARIO, etc.) |

### Endpoints

| Método | URL | Auth | Descrição |
|---|---|---|---|
| GET | `/api/products` | Público | Lista produtos com filtros e ordenação |
| GET | `/api/products/{id}` | Público | Busca produto por ID |
| POST | `/api/products` | **ADMIN** | Cria produto |
| PUT | `/api/products/{id}` | **ADMIN** | Atualiza produto |
| DELETE | `/api/products/{id}` | **ADMIN** | Remove produto |

### Filtros e Ordenação – GET /api/products

Todos os parâmetros são opcionais:

```
GET /api/products?category=ELETRONICO&minPrice=100&maxPrice=500&sortBy=price&sortDir=DESC
```

| Parâmetro | Exemplo | Descrição |
|---|---|---|
| category | `ELETRONICO` | Filtra por categoria |
| minPrice | `100.0` | Preço mínimo |
| maxPrice | `500.0` | Preço máximo |
| sortBy | `name` ou `price` | Campo de ordenação |
| sortDir | `ASC` ou `DESC` | Direção (padrão: ASC) |

---

## Módulo: Orders

### Entidades

**`Order`** – Pedido feito por um usuário

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long | Chave primária |
| user | User | Usuário dono do pedido |
| status | String | OPEN / CLOSED / CANCELLED |
| createdAt | LocalDateTime | Data de criação |
| items | List<OrderItem> | Itens do pedido |

**`OrderItem`** – Item de um pedido (associação com produto)

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long | Chave primária |
| order | Order | Pedido pai |
| product | Product | Produto associado |
| quantity | Int | Quantidade |

### Endpoints

| Método | URL | Auth | Descrição |
|---|---|---|---|
| POST | `/api/orders` | Autenticado | Cria pedido para o usuário logado |
| GET | `/api/orders` | **ADMIN** | Lista todos os pedidos |
| GET | `/api/orders/my` | Autenticado | Lista pedidos do usuário logado |
| GET | `/api/orders/{id}` | Dono ou ADMIN | Busca pedido por ID |
| POST | `/api/orders/{id}/close` | Dono ou ADMIN | Fecha o pedido |
| DELETE | `/api/orders/{id}` | **ADMIN** | Remove pedido (destrutivo) |
| POST | `/api/orders/{id}/items` | Dono ou ADMIN | Adiciona produto ao pedido |
| DELETE | `/api/orders/{id}/items/{productId}` | Dono ou ADMIN | Remove produto do pedido |

### Filtro de pedidos por status

```
GET /api/orders?status=OPEN
GET /api/orders/my?status=CLOSED
```

---

## Autenticação

A API usa **JWT Bearer Token**. Para endpoints protegidos:

1. Faça login: `POST /api/users/login` com `{ "email": "admin@authserver.com", "password": "admin" }`
2. Copie o `token` da resposta
3. Envie no header: `Authorization: Bearer <token>`

No Swagger UI, clique no cadeado e cole o token.

### Níveis de acesso

| Role | Pode |
|---|---|
| Público | GET /products, GET /products/{id}, GET /users |
| Autenticado | Criar pedido, ver seus próprios pedidos, adicionar/remover itens |
| ADMIN | Tudo acima + CRUD de produtos, ver todos os pedidos, deletar pedidos |

---

## Boas Práticas Implementadas

### Logs (SLF4J)
- `ProductService`: log de criação, atualização e remoção de produtos
- `OrderService`: log de criação, adição/remoção de itens, fechamento e remoção

### Exceções
- `BadRequestException` – dados inválidos (preço negativo, status inválido, pedido vazio)
- `NotFoundException` – produto ou pedido não encontrado pelo ID
- `ForbiddenException` – usuário sem permissão para acessar/modificar recurso

---

## Como Rodar

```bash
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/api
- H2 Console: http://localhost:8080/api/h2-console  
  JDBC URL: `jdbc:h2:mem:db` | user: `sa` | password: `sa`

---

## Dados Iniciais (Bootstrapper)

Ao iniciar, o sistema cria automaticamente:

- Papéis: `ADMIN` e `PREMIUM`
- Usuário admin: email `admin@authserver.com`, senha `admin`
- 6 produtos de exemplo nas categorias ELETRONICO, MOBILIARIO e ALIMENTO

---

## Estrutura de Pacotes (novos arquivos)

```
br.pucpr.authserver
├── Bootstrapper.kt         
├── products/               
│   ├── Product.kt
│   ├── ProductRepository.kt
│   ├── ProductService.kt
│   ├── ProductController.kt
│   ├── requests/
│   │   ├── CreateProductRequest.kt
│   │   └── UpdateProductRequest.kt
│   └── responses/
│       └── ProductResponse.kt
└── orders/                
    ├── Order.kt
    ├── OrderItem.kt
    ├── OrderRepository.kt
    ├── OrderService.kt
    ├── OrderController.kt
    ├── requests/
    │   ├── CreateOrderRequest.kt
    │   └── AddItemRequest.kt
    └── responses/
        ├── OrderResponse.kt
        └── OrderItemResponse.kt
```
