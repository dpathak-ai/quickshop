import { NextRequest, NextResponse } from 'next/server'

const PROTECTED_ROUTES = ['/orders', '/checkout', '/cart']
const ADMIN_ROUTES = ['/dashboard', '/inventory']

export function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl

    const isProtected = PROTECTED_ROUTES.some((r) => pathname.startsWith(r))
    const isAdmin = ADMIN_ROUTES.some((r) => pathname.startsWith(r))

    if (isProtected || isAdmin) {
        // Read token from cookie (we'll set this on login)
        const token = request.cookies.get('access-token')?.value
        if (!token) {
            const loginUrl = new URL('/auth/login', request.url)
            loginUrl.searchParams.set('redirect', pathname)
            return NextResponse.redirect(loginUrl)
        }

        if (isAdmin) {
            const role = request.cookies.get('user-role')?.value
            if (role !== 'ADMIN') {
                return NextResponse.redirect(new URL('/', request.url))
            }
        }
    }

    return NextResponse.next()
}

export const config = {
    matcher: ['/orders/:path*', '/checkout/:path*', '/cart/:path*', '/admin/:path*'],
}