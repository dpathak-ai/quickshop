import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserResponse } from '@/lib/types/api.types'

interface AuthState {
    accessToken: string | null
    refreshToken: string | null
    user: UserResponse | null
    _hasHydrated: boolean
    setTokens: (access: string, refresh: string) => void
    setUser: (user: UserResponse) => void
    clear: () => void
    setHasHydrated: (val: boolean) => void
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            accessToken: null,
            refreshToken: null,
            user: null,
            _hasHydrated: false,
            setTokens: (access, refresh) =>
                set({ accessToken: access, refreshToken: refresh }),
            setUser: (user) => set({ user }),
            clear: () => set({ accessToken: null, refreshToken: null, user: null }),
            setHasHydrated: (val) => set({ _hasHydrated: val }),
        }),
        {
            name: 'auth-store',
            onRehydrateStorage: () => (state) => {
                state?.setHasHydrated(true)
            },
        }
    )
)