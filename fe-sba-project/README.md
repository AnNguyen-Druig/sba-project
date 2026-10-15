# fe-sba-project — Giao diện quản lý phòng trọ (SBA301)

React 19 + Vite + TypeScript + Tailwind CSS v4 + shadcn/ui (Radix) + lucide-react + React Router 7.

## Chạy

```bash
npm install
npm run dev        # http://localhost:5173
npm run build      # kiểm tra type + build
npm run lint
```

## Design system

Toàn bộ token nằm ở `src/index.css` (theo "Frontend Brief" mục 3):

- Một font **Be Vietnam Pro**, hai độ đậm (`font-normal`, `font-bold`).
- Cỡ chữ: `text-xs` 12, `text-sm` 14, `text-base` 16, `text-xl` 24. Riêng tiêu đề homepage dùng `text-display`.
- Màu: `primary`, `primary-soft`, `background`, `surface`, `foreground`, `muted-foreground`, `border`, `success`, `warning`, `danger`, `info`.
  Bảng màu mặc định của Tailwind đã bị tắt, nên class như `bg-red-500` sẽ không có tác dụng.
- Trạng thái luôn hiển thị bằng `<StatusBadge tone="success|warning|danger|info|neutral">`.

## Cấu trúc

```
src/
  components/ui/       # component shadcn/ui (button, select, input, label, skeleton)
  components/common/   # dùng chung: StatusBadge, EmptyState, ErrorState, Logo, Container
  layouts/             # PublicLayout (header + footer cho khách)
  features/home/       # homepage
  features/rooms/      # types, dữ liệu giả, service, hook, RoomCard
  pages/               # trang tạm (ComingSoon), 404
  lib/                 # cn(), formatVnd(), formatDate(), routes, site
  router.tsx
```

## Nối backend

Dữ liệu đang là dữ liệu giả trong `src/features/rooms/data/rooms.mock.ts`.
Khi có API, chỉ cần sửa `src/features/rooms/api/rooms.service.ts`; component không phải đổi.

Xem thử trạng thái của danh sách phòng: thêm `?demo=loading`, `?demo=empty` hoặc `?demo=error` vào URL.

## Video hero (trang chủ)

- File nằm ở `src/assets/hero/`: `living-room-1080.mp4` (máy tính), `living-room-720.mp4` (điện thoại), `living-room-poster.webp` (ảnh chờ).
- Trang cuộn bình thường; nhịp cuộn đầu tiên làm video phát và lặp lại liên tục, tự dừng khi hero khuất màn hình. Logic ở `src/features/home/components/hero-video.tsx`.
- Đổi video: thay 3 file trên (giữ tên). Nên nén lại cho nhẹ:

```bash
ffmpeg -i input.mp4 -an -c:v libx264 -preset slow -crf 24 -pix_fmt yuv420p -movflags +faststart living-room-1080.mp4
ffmpeg -i input.mp4 -an -vf "scale=-2:720,fps=30" -c:v libx264 -preset slow -crf 25 -pix_fmt yuv420p -movflags +faststart living-room-720.mp4
ffmpeg -i input.mp4 -frames:v 1 -vf scale=1280:-2 -c:v libwebp -quality 72 living-room-poster.webp
```

- Người dùng bật "giảm chuyển động" trong hệ điều hành sẽ thấy ảnh tĩnh thay cho video.

## Thêm component shadcn

`components.json` đã cấu hình sẵn: `npx shadcn@latest add dialog`.
