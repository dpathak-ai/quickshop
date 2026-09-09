import Link from 'next/link'
import { Card, CardContent, CardFooter } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { ProductResponseV2 } from '@/lib/types/api.types'
import { useCartStore } from '@/lib/store/cart.store'

interface Props {
    product: ProductResponseV2
}

export default function ProductCard({ product }: Props) {
    const addItem = useCartStore((s) => s.addItem)

    const handleAddToCart = () => {
        addItem({
            productId: product.id,
            productName: product.name,
            price: product.price,
            quantity: 1,
        })
    }

    return (
        <Card className="flex flex-col h-full">
            <CardContent className="flex-1 pt-6 space-y-2">
                <div className="flex items-start justify-between gap-2">
                    <h3 className="font-semibold text-sm leading-tight line-clamp-2">
                        {product.name}
                    </h3>
                    <Badge variant={product.inStock ? 'default' : 'secondary'} className="shrink-0">
                        {product.inStock ? 'In Stock' : 'Out of Stock'}
                    </Badge>
                </div>
                <p className="text-xs text-muted-foreground line-clamp-2">{product.description}</p>
                <p className="text-xs text-muted-foreground">{product.category}</p>
                <p className="text-lg font-bold">{product.formattedPrice}</p>
            </CardContent>
            <CardFooter className="gap-2">
                <Link href={`/products/${product.id}`} className="flex-1">
                    <Button variant="outline" size="sm" className="w-full">View</Button>
                </Link>
                <Button size="sm" disabled={!product.inStock} onClick={handleAddToCart} className="flex-1">
                    Add to Cart
                </Button>
            </CardFooter>
        </Card>
    )
}