'use client'

import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getProducts } from '@/lib/api/products'
import { getInventory, updateStock } from '@/lib/api/inventory'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'

function InventoryRow({ productId, productName }: { productId: number; productName: string }) {
    const queryClient = useQueryClient()
    const [editing, setEditing] = useState(false)
    const [qty, setQty] = useState('')
    const [saving, setSaving] = useState(false)

    const { data: inv } = useQuery({
        queryKey: ['inventory', productId],
        queryFn: () => getInventory(productId).then((r) => r.data),
        staleTime: 30_000,
    })

    const handleSave = async () => {
        setSaving(true)
        await updateStock(productId, { quantity: Number(qty), reason: 'Admin update' })
        queryClient.invalidateQueries({ queryKey: ['inventory', productId] })
        setEditing(false)
        setSaving(false)
        setQty('')
    }

    return (
        <Card>
            <CardContent className="py-4 flex items-center justify-between gap-4">
                <div className="flex-1">
                    <p className="font-medium text-sm">{productName}</p>
                    {inv ? (
                        <p className="text-xs text-muted-foreground">
                            Total: {inv.quantity} | Reserved: {inv.reservedQuantity} | Available: {inv.availableQuantity}
                        </p>
                    ) : (
                        <p className="text-xs text-muted-foreground">Loading...</p>
                    )}
                </div>

                {editing ? (
                    <div className="flex items-center gap-2">
                        <Input
                            type="number"
                            value={qty}
                            onChange={(e) => setQty(e.target.value)}
                            className="w-24 h-8 text-sm"
                            placeholder="New qty"
                        />
                        <Button size="sm" disabled={saving || !qty} onClick={handleSave}>
                            {saving ? 'Saving...' : 'Save'}
                        </Button>
                        <Button size="sm" variant="ghost" onClick={() => setEditing(false)}>
                            Cancel
                        </Button>
                    </div>
                ) : (
                    <Button size="sm" variant="outline" onClick={() => setEditing(true)}>
                        Update Stock
                    </Button>
                )}
            </CardContent>
        </Card>
    )
}

export default function AdminInventoryPage() {
    const { data, isLoading } = useQuery({
        queryKey: ['admin-products', 0],
        queryFn: () => getProducts(0, 50).then((r) => r.data),
    })

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold">Inventory</h1>

            {isLoading ? (
                <p className="text-muted-foreground">Loading products...</p>
            ) : (
                <div className="space-y-3">
                    {data?.content.map((p) => (
                        <InventoryRow key={p.id} productId={p.id} productName={p.name} />
                    ))}
                </div>
            )}
        </div>
    )
}