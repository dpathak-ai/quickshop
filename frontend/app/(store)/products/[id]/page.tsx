'use client'

import { use, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { getProduct } from '@/lib/api/products'
import { getInventory } from '@/lib/api/inventory'
import { useCartStore } from '@/lib/store/cart.store'
import { useAuthStore } from '@/lib/store/auth.store'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'

export default function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params)
    const productId = Number(id)
    const router = useRouter()
    const { user } = useAuthStore()
    const addItem = useCartStore((s) => s.addItem)
    const [quantity, setQuantity] = useState(1)

    const { data: product, isLoading: loadingProduct } = useQuery({
        queryKey: ['product', productId],
        queryFn: () => getProduct(productId).then((r) => r.data),
    })

    const { data: inventory } = useQuery({
        queryKey: ['inventory', productId],
        queryFn: () => getInventory(productId).then((r) => r.data),
        staleTime: 30_000,
    })

    const handleAddToCart = () => {
        if (!product) return
        addItem({ productId: product.id, productName: product.name, price: product.price, quantity })
        router.push('/cart')
    }

    if (loadingProduct) return (
        <div className="max-w-2xl mx-auto space-y-4">
            <Skeleton className="h-8 w-3/4" />
            <Skeleton className="h-4 w-1/2" />
            <Skeleton className="h-32 w-full" />
        </div>
    )

    if (!product) return <div className="text-center py-20 text-destructive">Product not found.</div>

    const available = inventory?.availableQuantity ?? 0

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <div className="space-y-2">
                <div className="flex items-start justify-between gap-4">
                    <h1 className="text-3xl font-bold">{product.name}</h1>
                    <Badge variant={product.inStock ? 'default' : 'secondary'}>
                        {product.inStock ? 'In Stock' : 'Out of Stock'}
                    </Badge>
                </div>
                <p className="text-muted-foreground">{product.category}</p>
                <p className="text-3xl font-bold">{product.formattedPrice}</p>
            </div>

            {product.description && (
                <p className="text-muted-foreground leading-relaxed">{product.description}</p>
            )}

            {inventory && (
                <p className="text-sm text-muted-foreground">
                    {available} units available
                </p>
            )}

            <div className="flex items-center gap-4">
                <div className="flex items-center border rounded-md">
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                    >−</Button>
                    <span className="px-4 text-sm font-medium">{quantity}</span>
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setQuantity((q) => Math.min(available || 99, q + 1))}
                    >+</Button>
                </div>

                <Button
                    disabled={!product.inStock || available === 0}
                    onClick={handleAddToCart}
                    className="flex-1"
                >
                    {user ? 'Add to Cart' : 'Login to Buy'}
                </Button>
            </div>
        </div>
    )
}