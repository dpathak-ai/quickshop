import api from './axios'
import type { ProductResponseV2, PagedResponse, CreateProductRequest, ProductResponse } from '@/lib/types/api.types'

export const getProducts = (page = 0, size = 12) =>
    api.get<PagedResponse<ProductResponseV2>>('/api/v2/products', { params: { page, size } })

export const getProduct = (id: number) =>
    api.get<ProductResponseV2>(`/api/v2/products/${id}`)

export const createProduct = (data: CreateProductRequest) =>
    api.post<ProductResponse>('/api/v1/products', data)