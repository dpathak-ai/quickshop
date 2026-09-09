import api from './axios'
import type { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '@/lib/types/api.types'

export const register = (data: RegisterRequest) =>
    api.post<AuthResponse>('/api/v1/auth/register', data)

export const login = (data: LoginRequest) =>
    api.post<AuthResponse>('/api/v1/auth/login', data)

export const getMe = () =>
    api.get<UserResponse>('/api/v1/users/me')