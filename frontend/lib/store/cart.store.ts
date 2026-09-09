import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface CartItem {
    productId: number
    productName: string
    price: number
    quantity: number
}

interface CartState {
    items: CartItem[]
    addItem: (item: CartItem) => void
    removeItem: (productId: number) => void
    updateQuantity: (productId: number, quantity: number) => void
    clear: () => void
    total: () => number
}

export const useCartStore = create<CartState>()(
    persist(
        (set, get) => ({
            items: [],
            addItem: (item) => {
                const existing = get().items.find((i) => i.productId === item.productId)
                if (existing) {
                    set({
                        items: get().items.map((i) =>
                            i.productId === item.productId
                                ? { ...i, quantity: i.quantity + item.quantity }
                                : i
                        ),
                    })
                } else {
                    set({ items: [...get().items, item] })
                }
            },
            removeItem: (productId) =>
                set({ items: get().items.filter((i) => i.productId !== productId) }),
            updateQuantity: (productId, quantity) =>
                set({
                    items: get().items.map((i) =>
                        i.productId === productId ? { ...i, quantity } : i
                    ),
                }),
            clear: () => set({ items: [] }),
            total: () =>
                get().items.reduce((sum, i) => sum + i.price * i.quantity, 0),
        }),
        { name: 'cart-store' }
    )
)