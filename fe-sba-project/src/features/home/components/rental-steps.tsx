import { Container } from '@/components/common/container'

const steps = [
  { title: 'Tìm phòng', text: 'Lọc theo khu vực, giá và hình thức thuê.' },
  { title: 'Đặt lịch xem', text: 'Chọn giờ, chủ trọ xác nhận qua thông báo.' },
  { title: 'Giữ phòng', text: 'Đặt cọc để phòng không bị người khác thuê.' },
  { title: 'Ký hợp đồng', text: 'Nhận phòng, theo dõi hoá đơn ngay trên app.' },
]

export function RentalSteps() {
  return (
    <section aria-labelledby="steps-title" className="border-y border-border bg-surface py-16 sm:py-20">
      <Container>
        <h2 id="steps-title" className="text-xl font-bold">
          Thuê phòng trong 4 bước
        </h2>
        <ol className="mt-10 grid gap-8 sm:grid-cols-2 lg:grid-cols-4 lg:gap-6">
          {steps.map((step, i) => (
            <li key={step.title} className="relative flex gap-4 lg:flex-col lg:gap-3">
              <span
                aria-hidden="true"
                className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary-soft text-base font-bold text-primary"
              >
                {i + 1}
              </span>
              {/* Đường nối giữa các bước, chỉ hiện trên desktop */}
              {i < steps.length - 1 && (
                <span
                  aria-hidden="true"
                  className="absolute top-5 left-14 hidden h-px w-[calc(100%-2rem)] bg-border lg:block"
                />
              )}
              <div>
                <h3 className="text-base font-bold">{step.title}</h3>
                <p className="mt-1 text-sm text-muted-foreground">{step.text}</p>
              </div>
            </li>
          ))}
        </ol>
      </Container>
    </section>
  )
}
