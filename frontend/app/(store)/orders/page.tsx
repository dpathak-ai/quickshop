'use client'

import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '@/lib/store/auth.store'
import { getUserOrders } from '@/lib/api/orders'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

const statusVariant = (status: string) => {
    if (status === 'CONFIRMED') return 'default'
    if (status === 'FAILED') return 'destructive'
    return 'secondary'
}

export default function OrdersPage() {
    const { user } = useAuthStore()

    const { data: orders, isLoading } = useQuery({
        queryKey: ['orders', user?.id],
        queryFn: () => getUserOrders(user!.id).then((r) => r.data),
        enabled: !!user,
    })

    if (isLoading) return (
        <div className="space-y-3 max-w-2xl mx-auto">
            {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-20 rounded-xl" />
            ))}
        </div>
    )

    if (!orders?.length) return (
        <div className="text-center py-20 text-muted-foreground">
            No orders yet.
        </div>
    )

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold">My Orders</h1>
            <div className="space-y-3">
                {orders.map((order) => (
                    <Card key={order.id}>
                        <CardContent className="py-4 space-y-1">
                            <div className="flex items-center justify-between">
                                <p className="font-medium">{order.productName}</p>
                                <Badge variant={statusVariant(order.status)}>{order.status}</Badge>
                            </div>
                            <div className="flex justify-between text-sm text-muted-foreground">
                                <span>Qty: {order.quantity}</span>
                                <span>${order.totalPrice.toFixed(2)}</span>
                            </div>
                            {order.failureReason && (
                                <p className="text-sm text-destructive">{order.failureReason}</p>
                            )}
                            <p className="text-xs text-muted-foreground">
                                {new Date(order.createdAt).toLocaleString()}
                            </p>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    )
}