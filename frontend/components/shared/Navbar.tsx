'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { ShoppingCart, LogOut, LayoutDashboard } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/lib/store/auth.store'
import { useCartStore } from '@/lib/store/cart.store'

export default function Navbar() {
    const router = useRouter()
    const { user, clear } = useAuthStore()
    const itemCount = useCartStore((s) => s.items.reduce((n, i) => n + i.quantity, 0))

    const handleLogout = () => {
        clear()
        document.cookie = 'access-token=; max-age=0; path=/'
        document.cookie = 'user-role=; max-age=0; path=/'
        router.push('/auth/login')
    }

    return (
        <nav className="border-b bg-background sticky top-0 z-50">
            <div className="container mx-auto px-4 h-16 flex items-center justify-between">
                <Link href="/" className="text-xl font-bold tracking-tight">
                    QuickShop
                </Link>

                <div className="flex items-center gap-4">
                    <Link href="/cart">
                        <Button variant="ghost" size="sm" className="relative">
                            <ShoppingCart className="h-5 w-5" />
                            {itemCount > 0 && (
                                <span className="absolute -top-1 -right-1 bg-primary text-primary-foreground text-xs rounded-full h-5 w-5 flex items-center justify-center">
                  {itemCount}
                </span>
                            )}
                        </Button>
                    </Link>

                    {user ? (
                        <>
              <span className="text-sm text-muted-foreground hidden sm:block">
                {user.name}
              </span>
                            {user.role === 'ADMIN' && (
                                <Link href="/dashboard">
                                    <Button variant="ghost" size="sm">
                                        <LayoutDashboard className="h-4 w-4 mr-1" />
                                        Admin
                                    </Button>
                                </Link>
                            )}
                            <Button variant="ghost" size="sm" onClick={handleLogout}>
                                <LogOut className="h-4 w-4" />
                            </Button>
                        </>
                    ) : (
                        <Link href="/auth/login">
                            <Button size="sm">Login</Button>
                        </Link>
                    )}
                </div>
            </div>
        </nav>
    )
}