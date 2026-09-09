import api from './axios'
import type { InventoryResponse, UpdateStockRequest } from '@/lib/types/api.types'

export const getInventory = (productId: number) =>
    api.get<InventoryResponse>(`/api/v1/inventory/${productId}`)

export const updateStock = (productId: number, data: UpdateStockRequest) =>
    api.put<InventoryResponse>(`/api/v1/inventory/${productId}`, data)