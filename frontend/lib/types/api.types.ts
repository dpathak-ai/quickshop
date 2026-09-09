// Auth
export interface AuthResponse {
    accessToken: string
    refreshToken: string
    tokenType: string
    userId: number
    email: string
    role: string
}

export interface UserResponse {
    id: number
    name: string
    email: string
    role: string
    createdAt: string
}

// Products
export interface ProductResponse {
    id: number
    name: string
    description: string | null
    price: number
    category: string
    stockHint: number | null
    active: boolean
    createdAt: string
}

export interface ProductResponseV2 extends ProductResponse {
    formattedPrice: string
    inStock: boolean
    version: string
}

export interface PagedResponse<T> {
    content: T[]
    pageNumber: number
    pageSize: number
    totalElements: number
    totalPages: number
    last: boolean
}

// Orders
export interface OrderResponse {
    id: number
    userId: number
    productId: number
    productName: string
    quantity: number
    unitPrice: number
    totalPrice: number
    status: 'PENDING' | 'CONFIRMED' | 'FAILED'
    failureReason: string | null
    traceId: string
    createdAt: string
}

export interface CreateOrderRequest {
    productId: number
    quantity: number
}

// Inventory
export interface InventoryResponse {
    productId: number
    productName: string
    quantity: number
    reservedQuantity: number
    availableQuantity: number
    lastUpdated: string
}

export interface UpdateStockRequest {
    quantity: number
    reason?: string
}

// Forms
export interface LoginRequest {
    email: string
    password: string
}

export interface RegisterRequest {
    name: string
    email: string
    password: string
    role?: 'USER' | 'ADMIN'
}

export interface CreateProductRequest {
    name: string
    description?: string
    price: number
    category: string
    stockHint?: number
}