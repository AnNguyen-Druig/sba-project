import { Link, NavLink, Outlet, ScrollRestoration, useLocation } from 'react-router'
import { Mail, Phone } from 'lucide-react'

import { Container } from '@/components/common/container'
import { Logo } from '@/components/common/logo'
import { Button } from '@/components/ui/button'
import { routes } from '@/lib/routes'
import { site } from '@/lib/site'
import { cn } from '@/lib/utils'

import { useHeaderOverlay } from './use-header-overlay'

function SiteHeader() {
  const isHome = useLocation().pathname === routes.home
  // Trang chủ: header nằm đè lên video hero và trong suốt cho tới khi cuộn qua hero
  const overlay = useHeaderOverlay(isHome)

  return (
    <header
      data-overlay={overlay || undefined}
      className={cn(
        'top-0 z-40 border-b transition-colors duration-300',
        isHome ? 'fixed inset-x-0' : 'sticky',
        overlay
          ? 'border-transparent bg-transparent'
          : 'border-border bg-surface/95 backdrop-blur supports-[backdrop-filter]:bg-surface/80',
      )}
    >
      <Container className="flex h-16 items-center justify-between gap-4">
        <Logo inverted={overlay} />
        <nav aria-label="Điều hướng chính" className="flex items-center gap-1 sm:gap-2">
          <NavLink
            to={routes.rooms}
            className={({ isActive }) =>
              cn(
                'inline-flex h-11 items-center rounded-lg px-3 text-sm font-bold transition-colors outline-none focus-visible:ring-2',
                overlay
                  ? 'text-white hover:bg-white/15 focus-visible:ring-white'
                  : 'hover:bg-primary-soft hover:text-primary focus-visible:ring-ring',
                isActive && !overlay && 'bg-primary-soft text-primary',
              )
            }
          >
            Tìm phòng
          </NavLink>
          <Button asChild variant={overlay ? 'overlay' : 'outline'}>
            <Link to={routes.login}>Đăng nhập</Link>
          </Button>
        </nav>
      </Container>
    </header>
  )
}

function SiteFooter() {
  const year = new Date().getFullYear()
  return (
    <footer className="border-t border-border bg-surface">
      <Container className="grid gap-8 py-12 sm:grid-cols-[2fr_1fr_1fr]">
        <div>
          <Logo />
          <p className="mt-2 max-w-xs text-sm text-muted-foreground">
            Tìm phòng trọ cho người thuê, công cụ quản lý cho chủ trọ.
          </p>
        </div>
        <nav aria-label="Liên kết cuối trang">
          <h2 className="text-sm font-bold">Khám phá</h2>
          <ul className="mt-2 grid text-sm">
            <li>
              <Link to={routes.rooms} className="inline-flex min-h-11 items-center text-muted-foreground outline-none hover:text-primary focus-visible:underline">
                Tìm phòng
              </Link>
            </li>
            <li>
              <Link to={routes.register} className="inline-flex min-h-11 items-center text-muted-foreground outline-none hover:text-primary focus-visible:underline">
                Đăng ký chủ trọ
              </Link>
            </li>
          </ul>
        </nav>
        <div>
          <h2 className="text-sm font-bold">Hỗ trợ</h2>
          <ul className="mt-2 grid text-sm">
            <li>
              <a href={site.hotlineHref} className="inline-flex min-h-11 items-center gap-2 text-muted-foreground outline-none hover:text-primary focus-visible:underline">
                <Phone className="size-4" aria-hidden="true" />
                {site.hotline}
              </a>
            </li>
            <li>
              <a href={`mailto:${site.email}`} className="inline-flex min-h-11 items-center gap-2 text-muted-foreground outline-none hover:text-primary focus-visible:underline">
                <Mail className="size-4" aria-hidden="true" />
                {site.email}
              </a>
            </li>
          </ul>
        </div>
      </Container>
      <Container className="border-t border-border py-6 text-xs text-muted-foreground">
        © {year} {site.name}
      </Container>
    </footer>
  )
}

export function PublicLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      <a
        href="#main"
        className="sr-only z-50 rounded-lg bg-primary px-4 py-3 text-sm font-bold text-primary-foreground focus:not-sr-only focus:fixed focus:top-3 focus:left-3"
      >
        Bỏ qua, tới nội dung chính
      </a>
      <SiteHeader />
      <main id="main" className="flex-1">
        <Outlet />
      </main>
      <SiteFooter />
      <ScrollRestoration />
    </div>
  )
}
