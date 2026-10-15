import { Link } from 'react-router'
import { Hammer } from 'lucide-react'

import { Container } from '@/components/common/container'
import { EmptyState } from '@/components/common/empty-state'
import { Button } from '@/components/ui/button'
import { routes } from '@/lib/routes'

type ComingSoonPageProps = { title: string }

/** Tạm thời cho các trang chưa làm, để link trên homepage không dẫn tới 404. */
export function ComingSoonPage({ title }: ComingSoonPageProps) {
  return (
    <Container className="py-16">
      <h1 className="text-xl font-bold">{title}</h1>
      <EmptyState
        className="mt-8"
        icon={Hammer}
        title="Trang này đang được hoàn thiện."
        action={
          <Button asChild variant="outline">
            <Link to={routes.home}>Về trang chủ</Link>
          </Button>
        }
      />
    </Container>
  )
}
