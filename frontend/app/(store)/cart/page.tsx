'use client'

import Link from 'next/link'
import { useCartStore } from '@/lib/store/cart.store'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'

export default function CartPage() {
    const { items, removeItem, updateQuantity, total, clear } = useCartStore()

    if (items.length === 0) return (
        <div className="text-center py-20 space-y-4">
            <p className="text-muted-foreground">Your cart is empty.</p>
            <Link href="/"><Button variant="outline">Browse Products</Button></Link>
        </div>
    )

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold">Your Cart</h1>

            <div className="space-y-3">
                {items.map((item) => (
                    <Card key={item.productId}>
                        <CardContent className="flex items-center justify-between gap-4 py-4">
                            <div className="flex-1">
                                <p className="font-medium">{item.productName}</p>
                                <p className="text-sm text-muted-foreground">
                                    ${item.price.toFixed(2)} each
                                </p>
                            </div>

                            <div className="flex items-center border rounded-md">
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => updateQuantity(item.productId, Math.max(1, item.quantity - 1))}
                                >−</Button>
                                <span className="px-3 text-sm">{item.quantity}</span>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                                >+</Button>
                            </div>

                            <p className="font-semibold w-20 text-right">
                                ${(item.price * item.quantity).toFixed(2)}
                            </p>

                            <Button
                                variant="ghost"
                                size="sm"
                                className="text-destructive"
                                onClick={() => removeItem(item.productId)}
                            >Remove</Button>
                        </CardContent>
                    </Card>
                ))}
            </div>

            <div className="flex items-center justify-between border-t pt-4">
                <div>
                    <p className="text-lg font-bold">Total: ${total().toFixed(2)}</p>
                    <button
                        className="text-sm text-muted-foreground hover:underline"
                        onClick={clear}
                    >Clear cart</button>
                </div>
                <Link href="/checkout">
                    <Button size="lg">Proceed to Checkout</Button>
                </Link>
            </div>
        </div>
    )
}