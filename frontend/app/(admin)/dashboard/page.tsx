'use client'

import { useAuthStore } from '@/lib/store/auth.store'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import Link from 'next/link'
import { Button } from '@/components/ui/button'

export default function DashboardPage() {
    const { user } = useAuthStore()

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold">Dashboard</h1>
                <p className="text-muted-foreground">Welcome back, {user?.name}</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Card>
                    <CardHeader><CardTitle className="text-base">Products</CardTitle></CardHeader>
                    <CardContent className="space-y-2">
                        <p className="text-sm text-muted-foreground">Manage your product catalog</p>
                        <Link href="/admin-products">
                            <Button size="sm" className="w-full">Go to Products</Button>
                        </Link>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader><CardTitle className="text-base">Inventory</CardTitle></CardHeader>
                    <CardContent className="space-y-2">
                        <p className="text-sm text-muted-foreground">Update stock levels</p>
                        <Link href="/inventory">
                            <Button size="sm" className="w-full">Go to Inventory</Button>
                        </Link>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader><CardTitle className="text-base">Storefront</CardTitle></CardHeader>
                    <CardContent className="space-y-2">
                        <p className="text-sm text-muted-foreground">View the customer store</p>
                        <Link href="/">
                            <Button size="sm" variant="outline" className="w-full">View Store</Button>
                        </Link>
                    </CardContent>
                </Card>
            </div>
        </div>
    )
}