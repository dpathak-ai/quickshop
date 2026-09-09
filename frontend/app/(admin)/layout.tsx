'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth.store'
import { Button } from '@/components/ui/button'
import { useEffect } from 'react'

export default function AdminLayout({ children }: { children: React.ReactNode }) {
    const { user, clear } = useAuthStore()
    const router = useRouter()

    useEffect(() => {
        if (!user) {
            router.push('/auth/login?redirect=/dashboard')
        } else if (user.role !== 'ADMIN') {
            router.push('/')
        }
    }, [user, router])

    const handleLogout = () => {
        clear()
        document.cookie = 'access-token=; max-age=0; path=/'
        document.cookie = 'user-role=; max-age=0; path=/'
        router.push('/auth/login')
    }

    if (!user || user.role !== 'ADMIN') return null

    return (
        <div className="min-h-screen flex">
            {/* Sidebar */}
            <aside className="w-56 border-r bg-muted/40 flex flex-col p-4 gap-2">
                <p className="font-bold text-lg mb-4 px-2">Admin</p>
                <Link href="/dashboard">
                    <Button variant="ghost" className="w-full justify-start">Dashboard</Button>
                </Link>
                <Link href="/admin-products">
                    <Button variant="ghost" className="w-full justify-start">Products</Button>
                </Link>
                <Link href="/inventory">
                    <Button variant="ghost" className="w-full justify-start">Inventory</Button>
                </Link>
                <div className="mt-auto">
                    <Button variant="ghost" className="w-full justify-start text-destructive" onClick={handleLogout}>
                        Logout
                    </Button>
                </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 p-8 overflow-auto">
                {children}
            </main>
        </div>
    )
}