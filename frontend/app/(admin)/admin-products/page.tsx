'use client'

import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getProducts, createProduct } from '@/lib/api/products'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import type { CreateProductRequest } from '@/lib/types/api.types'

const schema = z.object({
    name: z.string().min(1, 'Required'),
    description: z.string().optional(),
    price: z.coerce.number().min(0.01, 'Must be greater than 0'),
    category: z.string().min(1, 'Required'),
    stockHint: z.coerce.number().min(0).optional(),
})

type FormData = z.infer<typeof schema>

export default function AdminProductsPage() {
    const [page, setPage] = useState(0)
    const [open, setOpen] = useState(false)
    const queryClient = useQueryClient()

    const { data, isLoading } = useQuery({
        queryKey: ['admin-products', page],
        queryFn: () => getProducts(page, 10).then((r) => r.data),
    })

    const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
        resolver: zodResolver(schema) as any,
        defaultValues: {
            name: '',
            description: '',
            price: '',
            category: '',
            stockHint: '',
        }
    })


    const onSubmit = async (data: any) => {
        await createProduct({
            name: data.name,
            description: data.description || undefined,
            price: parseFloat(data.price),
            category: data.category,
            stockHint: data.stockHint ? parseInt(data.stockHint) : undefined,
        })
        queryClient.invalidateQueries({ queryKey: ['admin-products'] })
        reset()
        setOpen(false)
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold">Products</h1>

                    <Button onClick={() => setOpen(true)}>Add Product</Button>
                    <Dialog open={open} onOpenChange={setOpen}>
                    <DialogContent className="max-w-md">
                        <DialogHeader>
                            <DialogTitle>New Product</DialogTitle>
                        </DialogHeader>
                        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                            <div className="space-y-1">
                                <Label>Name</Label>
                                <Input {...register('name')} />
                                {errors.name && <p className="text-sm text-destructive">{errors.name.message}</p>}
                            </div>
                            <div className="space-y-1">
                                <Label>Description</Label>
                                <Textarea {...register('description')} rows={3} />
                            </div>
                            <div className="grid grid-cols-2 gap-3">
                                <div className="space-y-1">
                                    <Label>Price ($)</Label>
                                    <Input type="number" step="0.01" {...register('price')} />
                                    {errors.price && <p className="text-sm text-destructive">{errors.price.message}</p>}
                                </div>
                                <div className="space-y-1">
                                    <Label>Stock Hint</Label>
                                    <Input type="number" {...register('stockHint')} />
                                </div>
                            </div>
                            <div className="space-y-1">
                                <Label>Category</Label>
                                <Input {...register('category')} />
                                {errors.category && <p className="text-sm text-destructive">{errors.category.message}</p>}
                            </div>
                            <Button type="submit" className="w-full" disabled={isSubmitting}>
                                {isSubmitting ? 'Creating...' : 'Create Product'}
                            </Button>
                        </form>
                    </DialogContent>
                </Dialog>
            </div>

            {isLoading ? (
                <p className="text-muted-foreground">Loading...</p>
            ) : (
                <div className="border rounded-lg overflow-hidden">
                    <table className="w-full text-sm">
                        <thead className="bg-muted/50 text-muted-foreground">
                        <tr>
                            <th className="text-left p-3">Name</th>
                            <th className="text-left p-3">Category</th>
                            <th className="text-left p-3">Price</th>
                            <th className="text-left p-3">Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data?.content.map((p) => (
                            <tr key={p.id} className="border-t hover:bg-muted/20">
                                <td className="p-3 font-medium">{p.name}</td>
                                <td className="p-3 text-muted-foreground">{p.category}</td>
                                <td className="p-3">{p.formattedPrice}</td>
                                <td className="p-3">
                                    <Badge variant={p.inStock ? 'default' : 'secondary'}>
                                        {p.inStock ? 'In Stock' : 'Out of Stock'}
                                    </Badge>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}

            {data && data.totalPages > 1 && (
                <div className="flex items-center gap-4">
                    <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
                        Previous
                    </Button>
                    <span className="text-sm text-muted-foreground">Page {page + 1} of {data.totalPages}</span>
                    <Button variant="outline" size="sm" disabled={data.last} onClick={() => setPage(p => p + 1)}>
                        Next
                    </Button>
                </div>
            )}
        </div>
    )
}
