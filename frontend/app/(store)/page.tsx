'use client'

import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getProducts } from '@/lib/api/products'
import ProductCard from '@/components/product/ProductCard'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'

export default function HomePage() {
    const [page, setPage] = useState(0)

    const { data, isLoading, isError } = useQuery({
        queryKey: ['products', page],
        queryFn: () => getProducts(page, 12).then((r) => r.data),
    })

    if (isError) return (
        <div className="text-center py-20 text-destructive">
            Failed to load products. Make sure the backend is running.
        </div>
    )

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold">Products</h1>

            {isLoading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    {Array.from({ length: 8 }).map((_, i) => (
                        <Skeleton key={i} className="h-64 rounded-xl" />
                    ))}
                </div>
            ) : (
                <>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {data?.content.map((product) => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>

                    {data && data.totalPages > 1 && (
                        <div className="flex items-center justify-center gap-4 pt-4">
                            <Button
                                variant="outline"
                                disabled={page === 0}
                                onClick={() => setPage((p) => p - 1)}
                            >
                                Previous
                            </Button>
                            <span className="text-sm text-muted-foreground">
                Page {page + 1} of {data.totalPages}
              </span>
                            <Button
                                variant="outline"
                                disabled={data.last}
                                onClick={() => setPage((p) => p + 1)}
                            >
                                Next
                            </Button>
                        </div>
                    )}
                </>
            )}
        </div>
    )
}