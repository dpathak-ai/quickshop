'use client'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState } from 'react'
import './globals.css'
import Navbar from "@/components/shared/Navbar";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: { staleTime: 60 * 1000, retry: 1 },
    },
  }))

  return (
      <html lang="en">
      <body>
      <Navbar />
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
      </body>
      </html>
  )
}