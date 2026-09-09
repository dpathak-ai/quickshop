import Navbar from '@/components/shared/Navbar'

export default function StoreLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="min-h-screen bg-background">
            {/*<Navbar />*/}
            <main className="container mx-auto px-4 py-8">
                {children}
            </main>
        </div>
    )
}