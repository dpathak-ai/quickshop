'use client'

import { useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { login, getMe } from '@/lib/api/auth'
import { useAuthStore } from '@/lib/store/auth.store'

const schema = z.object({
    email: z.string().email('Invalid email'),
    password: z.string().min(8, 'Password must be at least 8 characters'),
})

type FormData = z.infer<typeof schema>

export default function LoginPage() {
    const router = useRouter()
    const searchParams = useSearchParams()
    const { setTokens, setUser } = useAuthStore()
    const [error, setError] = useState('')

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
        resolver: zodResolver(schema),
    })

    const onSubmit = async (data: FormData) => {
        setError('')
        try {
            const { data: auth } = await login(data)
            setTokens(auth.accessToken, auth.refreshToken)

            // Set cookies for middleware route protection
            document.cookie = `access-token=${auth.accessToken}; path=/; max-age=900`
            document.cookie = `user-role=${auth.role}; path=/; max-age=86400`

            const { data: user } = await getMe()
            setUser(user)

            const redirect = searchParams.get('redirect') || '/'
            router.push(redirect)
        } catch {
            setError('Invalid email or password')
        }
    }

    return (
        <div className="flex min-h-[80vh] items-center justify-center">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <CardTitle>Welcome back</CardTitle>
                    <CardDescription>Sign in to your QuickShop account</CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="space-y-1">
                            <Label htmlFor="email">Email</Label>
                            <Input id="email" type="email" {...register('email')} />
                            {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
                        </div>

                        <div className="space-y-1">
                            <Label htmlFor="password">Password</Label>
                            <Input id="password" type="password" {...register('password')} />
                            {errors.password && <p className="text-sm text-destructive">{errors.password.message}</p>}
                        </div>

                        {error && <p className="text-sm text-destructive">{error}</p>}

                        <Button type="submit" className="w-full" disabled={isSubmitting}>
                            {isSubmitting ? 'Signing in...' : 'Sign in'}
                        </Button>

                        <p className="text-center text-sm text-muted-foreground">
                            No account?{' '}
                            <Link href="/auth/register" className="text-primary hover:underline">
                                Register
                            </Link>
                        </p>
                    </form>
                </CardContent>
            </Card>
        </div>
    )
}