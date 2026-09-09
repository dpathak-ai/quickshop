'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useCartStore } from '@/lib/store/cart.store'
import { useAuthStore } from '@/lib/store/auth.store'
import { createOrder, getOrder } from '@/lib/api/orders'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import type { OrderResponse } from '@/lib/types/api.types'

type OrderResult = {
    productName: string
    status: 'PENDING' | 'CONFIRMED' | 'FAILED'
    failureReason: string | null
    totalPrice: number
}

export default function CheckoutPage() {
    const router = useRouter()
    const { items, clear } = useCartStore()
    const { user } = useAuthStore()
    const [placing, setPlacing] = useState(false)
    const [results, setResults] = useState<OrderResult[]>([])
    const [done, setDone] = useState(false)

    if (!user) {
        router.push('/auth/login?redirect=/checkout')
        return null
    }

    if (items.length === 0 && !done) {
        router.push('/cart')
        return null
    }

    const pollOrder = (orderId: number): Promise<OrderResponse> =>
        new Promise((resolve) => {
            const interval = setInterval(async () => {
                const { data } = await getOrder(orderId)
                if (data.status !== 'PENDING') {
                    clearInterval(interval)
                    resolve(data)
                }
            }, 2000)
        })

    const handlePlaceOrders = async () => {
        setPlacing(true)
        const orderResults: OrderResult[] = []

        for (const item of items) {
            try {
                const { data: pending } = await createOrder({
                    productId: item.productId,
                    quantity: item.quantity,
                })

                const final = await pollOrder(pending.id)
                orderResults.push({
                    productName: item.productName,
                    status: final.status,
                    failureReason: final.failureReason,
                    totalPrice: final.totalPrice,
                })
            } catch {
                orderResults.push({
                    productName: item.productName,
                    status: 'FAILED',
                    failureReason: 'Could not place order. Please try again.',
                    totalPrice: 0,
                })
            }
        }

        setResults(orderResults)
        setDone(true)
        setPlacing(false)
        clear()
    }

    const statusVariant = (status: string) => {
        if (status === 'CONFIRMED') return 'default'
        if (status === 'FAILED') return 'destructive'
        return 'secondary'
    }

    if (done) return (
        <div className="max-w-xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold">Order Summary</h1>
            <div className="space-y-3">
                {results.map((r, i) => (
                    <Card key={i}>
                        <CardContent className="py-4 space-y-1">
                            <div className="flex items-center justify-between">
                                <p className="font-medium">{r.productName}</p>
                                <Badge variant={statusVariant(r.status)}>{r.status}</Badge>
                            </div>
                            {r.status === 'CONFIRMED' && (
                                <p className="text-sm text-muted-foreground">
                                    Total: ${r.totalPrice.toFixed(2)}
                                </p>
                            )}
                            {r.status === 'FAILED' && r.failureReason && (
                                <p className="text-sm text-destructive">{r.failureReason}</p>
                            )}
                        </CardContent>
                    </Card>
                ))}
            </div>
            <Button onClick={() => router.push('/orders')} className="w-full">
                View My Orders
            </Button>
        </div>
    )

    return (
        <div className="max-w-xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold">Checkout</h1>

            <Card>
                <CardHeader><CardTitle>Order Review</CardTitle></CardHeader>
                <CardContent className="space-y-2">
                    {items.map((item) => (
                        <div key={item.productId} className="flex justify-between text-sm">
                            <span>{item.productName} × {item.quantity}</span>
                            <span>${(item.price * item.quantity).toFixed(2)}</span>
                        </div>
                    ))}
                    <div className="border-t pt-2 flex justify-between font-bold">
                        <span>Total</span>
                        <span>
              ${items.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2)}
            </span>
                    </div>
                </CardContent>
            </Card>

            {placing && (
                <p className="text-center text-sm text-muted-foreground animate-pulse">
                    Placing orders and waiting for confirmation...
                </p>
            )}

            <Button
                className="w-full"
                size="lg"
                disabled={placing}
                onClick={handlePlaceOrders}
            >
                {placing ? 'Processing...' : 'Place Order'}
            </Button>
        </div>
    )
}