import axios from 'axios'

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
    if (typeof window !== 'undefined') {
        const raw = localStorage.getItem('auth-store')
        if (raw) {
            const state = JSON.parse(raw)
            const token = state?.state?.accessToken
            if (token) config.headers.Authorization = `Bearer ${token}`
        }
    }
    return config
})

api.interceptors.response.use(
    (res) => res,
    async (error) => {
        const original = error.config
        if (error.response?.status === 401 && !original._retry) {
            original._retry = true
            try {
                const raw = localStorage.getItem('auth-store')
                const state = JSON.parse(raw ?? '{}')
                const refreshToken = state?.state?.refreshToken
                const { data } = await axios.post(
                    `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/auth/refresh`,
                    { refreshToken }
                )
                // Update stored tokens
                state.state.accessToken = data.accessToken
                state.state.refreshToken = data.refreshToken
                localStorage.setItem('auth-store', JSON.stringify(state))
                original.headers.Authorization = `Bearer ${data.accessToken}`
                return api(original)
            } catch {
                localStorage.removeItem('auth-store')
                window.location.href = '/auth/login'
            }
        }
        return Promise.reject(error)
    }
)

export default api