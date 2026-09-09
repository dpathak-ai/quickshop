import api from './axios'
import type { OrderResponse, CreateOrderRequest } from '@/lib/types/api.types'

export const createOrder = (data: CreateOrderRequest) =>
    api.post<OrderResponse>('/api/v1/orders', data)

export const getOrder = (id: number) =>
    api.get<OrderResponse>(`/api/v1/orders/${id}`)

export const getUserOrders = (userId: number) =>
    api.get<OrderResponse[]>(`/api/v1/orders/user/${userId}`)