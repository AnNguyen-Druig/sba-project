# Prompt: Dựng homepage "Trọ Ổn" (website quản lý phòng trọ SBA301)

> Cách dùng: dán toàn bộ nội dung dưới đây vào Claude / Cursor / v0 / Lovable. Gửi kèm 1 video phòng khách ngang 16:9, dài khoảng 4 giây, không có âm thanh.

---

## 0. Vai trò

Bạn là product designer kiêm front-end engineer cấp senior. Hãy dựng **homepage** cho website tìm và quản lý phòng trọ tên **"Trọ Ổn"**. Giao diện phải sạch, nhất quán, đạt chất lượng production. Chưa nối backend: toàn bộ dữ liệu là dữ liệu giả (hard code), đặt sau một lớp service để sau này chỉ cần thay bằng API thật.

Ngôn ngữ hiển thị: **tiếng Việt**. Tiền hiển thị dạng `1.500.000 ₫`, ngày dạng `dd/MM/yyyy`.

---

## 1. Tech stack (dùng đúng các thư viện này)

| Nhóm | Công nghệ |
| --- | --- |
| Build | Vite 8 + React 19 + **TypeScript strict** (không dùng `any`) |
| Router | `react-router` v7: `createBrowserRouter`, `RouterProvider` import từ `react-router/dom` |
| CSS | **Tailwind CSS v4** qua plugin `@tailwindcss/vite`, cấu hình bằng `@theme` trong CSS, không có `tailwind.config.js` |
| Component | Viết theo **shadcn/ui** (style new-york), primitive lấy từ package `radix-ui` (`import { Select as SelectPrimitive } from 'radix-ui'`) |
| Tiện ích | `class-variance-authority`, `clsx`, `tailwind-merge` (hàm `cn()`) |
| Icon | Chỉ dùng `lucide-react` |
| Font | `@fontsource/be-vietnam-pro`, import file `400.css` và `700.css` |
| Lint | ESLint flat config + `typescript-eslint` |

Alias `@/*` trỏ tới `src/*`, khai báo trong cả `vite.config.ts` và `tsconfig.app.json`. Có `components.json` của shadcn để sau này chạy được `npx shadcn add`.

Không dùng thư viện animation, thư viện state hay axios.

---

## 2. Design tokens (`src/index.css`): làm đúng y như sau

Chỉ một font, hai độ đậm, bốn cỡ chữ cộng một cỡ `display` dành riêng cho tiêu đề hero. **Tắt bảng màu mặc định của Tailwind** để không ai dùng được màu ngoài bảng này.

```css
@import 'tailwindcss';
@import '@fontsource/be-vietnam-pro/400.css';
@import '@fontsource/be-vietnam-pro/700.css';

:root {
  --primary: #0f766e;            /* nút chính, link, mục đang chọn */
  --primary-foreground: #ffffff;
  --primary-soft: #e6f2f0;       /* nền mục chọn, nền nhạt */
  --background: #f8fafc;         /* nền trang */
  --surface: #ffffff;            /* nền thẻ, form */
  --foreground: #0f172a;         /* chữ chính */
  --muted-foreground: #64748b;   /* chữ phụ */
  --border: #e2e8f0;
  --success: #16a34a;
  --warning: #d97706;
  --danger: #dc2626;
  --info: #2563eb;
  --radius: 0.75rem;
}

@theme inline {
  --color-*: initial;
  --color-transparent: transparent;
  --color-current: currentColor;
  --color-white: #ffffff;
  --color-primary: var(--primary);
  --color-primary-foreground: var(--primary-foreground);
  --color-primary-soft: var(--primary-soft);
  --color-background: var(--background);
  --color-surface: var(--surface);
  --color-foreground: var(--foreground);
  --color-muted-foreground: var(--muted-foreground);
  --color-border: var(--border);
  --color-input: var(--border);
  --color-ring: var(--primary);
  --color-success: var(--success);
  --color-warning: var(--warning);
  --color-danger: var(--danger);
  --color-info: var(--info);

  --font-sans: 'Be Vietnam Pro', ui-sans-serif, system-ui, sans-serif;

  --text-*: initial;
  --text-xs: 0.75rem;   --text-xs--line-height: 1.125rem;
  --text-sm: 0.875rem;  --text-sm--line-height: 1.375rem;
  --text-base: 1rem;    --text-base--line-height: 1.5rem;
  --text-xl: 1.5rem;    --text-xl--line-height: 2rem;
  --text-display: clamp(2.25rem, 1.25rem + 3.2vw, 4rem);
  --text-display--line-height: 1.1;
  --text-display--letter-spacing: -0.02em;

  --font-weight-*: initial;
  --font-weight-normal: 400;
  --font-weight-bold: 700;

  --radius-sm: calc(var(--radius) - 0.25rem);
  --radius-md: calc(var(--radius) - 0.125rem);
  --radius-lg: var(--radius);
  --radius-xl: calc(var(--radius) + 0.25rem);
  --radius-2xl: calc(var(--radius) + 0.75rem);

  --shadow-*: initial;
  --shadow-sm: 0 1px 2px 0 rgb(15 23 42 / 0.06);
  --shadow-float: 0 12px 32px -8px rgb(15 23 42 / 0.18);

  --animate-rise: rise 600ms cubic-bezier(0.22, 1, 0.36, 1) both;
  @keyframes rise {
    from { opacity: 0; transform: translateY(16px); }
    to   { opacity: 1; transform: translateY(0); }
  }
}

@layer base {
  * { @apply border-border; }
  body { @apply bg-background font-sans text-base text-foreground antialiased; }
  @media (prefers-reduced-motion: reduce) {
    *, *::before, *::after {
      animation-duration: 0.01ms !important;
      animation-iteration-count: 1 !important;
      transition-duration: 0.01ms !important;
    }
  }
}
```

Quy tắc dùng:

- Không viết cứng mã hex hay pixel tuỳ tiện trong component; khoảng cách dùng thang 4px/8px của Tailwind.
- Chữ nội dung phải đạt tương phản WCAG AA. Vì vậy không đặt `text-muted-foreground` trên nền `primary-soft` (không đạt AA); trên nền đó dùng `text-foreground`.
- Mọi phần tử bấm được cao tối thiểu 44px (`h-11` / `min-h-11`) và có `focus-visible:ring-2` rõ ràng.
- Không dùng emoji, không viết chữ IN HOA cho nhãn, không gắn "→" vào nút.

---

## 3. Cấu trúc thư mục

```
src/
  main.tsx                     # RouterProvider
  router.tsx
  index.css                    # tokens ở mục 2
  assets/hero/
    living-room-1080.mp4       # video bản máy tính
    living-room-720.mp4        # video bản điện thoại
    living-room-poster.webp    # khung đầu của video
  components/ui/               # shadcn: button, input, label, select, skeleton
  components/common/           # status-badge, empty-state, error-state, logo, container
  hooks/use-media-query.ts     # useMediaQuery + usePrefersReducedMotion (useSyncExternalStore)
  layouts/
    public-layout.tsx          # header + footer + <Outlet/> + ScrollRestoration + skip link
    use-header-overlay.ts
  lib/
    utils.ts                   # cn()
    format.ts                  # formatVnd(), formatDate()
    routes.ts                  # hằng số đường dẫn
    site.ts                    # tên thương hiệu, hotline, email
  features/home/
    home-page.tsx
    components/                # hero-video, hero-search-form, featured-rooms, rental-steps, role-paths
    data/hero-media.ts         # import 3 file video/poster
    lib/build-search-url.ts
  features/rooms/
    types.ts
    data/filter-options.ts
    data/rooms.mock.ts
    api/rooms.service.ts
    hooks/use-featured-rooms.ts
    lib/room-display.ts
    components/room-card.tsx   # RoomCard + RoomCardSkeleton
  pages/
    coming-soon-page.tsx       # trang tạm cho các route chưa làm
    not-found-page.tsx
```

`lib/site.ts`: `{ name: 'Trọ Ổn', hotline: '0909 123 456', hotlineHref: 'tel:0909123456', email: 'hotro@troon.vn' }`

`lib/routes.ts`: `home '/'`, `rooms '/rooms'`, `roomDetail(id) '/rooms/:id'`, `login '/login'`, `register '/register'`, `myRoom '/tenant/my-room'`.

Router: layout `PublicLayout` bọc các route sau. `/` là HomePage. Các route `rooms`, `rooms/:roomId`, `login`, `register`, `tenant/my-room` dùng `ComingSoonPage` với tiêu đề tương ứng ("Tìm phòng", "Chi tiết phòng", "Đăng nhập", "Đăng ký", "Phòng của tôi"). Route `*` là NotFoundPage. Mục đích là để link trên homepage không dẫn tới 404.

---

## 4. Component dùng chung

- **Button** (cva): variant `default` (nền primary), `outline` (viền, nền surface, hover nền primary-soft), `soft`, `inverse` (nền trắng chữ primary, dùng trên nền primary), `overlay` (`border-white/50 bg-white/10 text-white backdrop-blur`, dùng trên video), `ghost`, `link`. Size `default h-11 px-5`, `lg h-12 px-6 text-base`, `icon size-11`. Có prop `asChild` (Radix Slot) và `loading` (icon `LoaderCircle` xoay, đặt `aria-busy`). Chữ trong nút luôn `font-bold text-sm`.
- **Select** (shadcn, Radix): trigger cao `h-11`; item cao `min-h-11`; item đang focus có nền `primary-soft` và chữ `primary`.
- **Label**: `text-xs font-bold text-muted-foreground`.
- **Skeleton**: `animate-pulse rounded-lg bg-border/70`.
- **StatusBadge** `tone: success | warning | danger | info | neutral`: hình viên thuốc, có chấm tròn 6px màu trạng thái và nền là màu đó ở độ đậm `/10`. **Chữ luôn màu `foreground`** để đạt AA, vì các màu trạng thái trên nền trắng không đủ tương phản cho chữ nhỏ.
- **EmptyState**: icon trong vòng tròn nền `primary-soft`, **một câu** và **một nút**; khung `border-dashed`.
- **ErrorState**: `role="alert"`, icon `CircleAlert` màu danger, câu báo lỗi nói rõ cách khắc phục, nút "Thử lại" (variant outline, icon `RefreshCw`).
- **Logo**: ô vuông bo góc màu primary chứa hình ngôi nhà trắng (SVG `M9 25V14l7-5 7 5v11h-5v-6h-4v6z`, viewBox 32) cùng chữ "Trọ Ổn" đậm. Có prop `inverted`: ô trắng, nhà màu primary, chữ trắng.
- **Container**: `mx-auto w-full max-w-6xl px-4 sm:px-6 lg:px-8`.
- Favicon: cùng SVG logo, nền `#0F766E`.

---

## 5. Layout công khai (header và footer)

**Header** cao 64px; bên trái là Logo, bên phải là NavLink "Tìm phòng" và nút outline "Đăng nhập".

- Ở các trang khác: `sticky top-0`, nền `bg-surface/95 backdrop-blur`, có viền dưới; mục đang mở có nền `primary-soft`.
- **Ở trang chủ**: header `fixed` nằm đè lên video, **trong suốt**, chữ trắng, Logo `inverted`, nút "Đăng nhập" dùng variant `overlay`. Khi hero đã cuộn qua, header chuyển sang nền trắng, có `transition-colors duration-300`.
- Cách nhận biết: hook `useHeaderOverlay(isHome)` nghe sự kiện scroll (passive, throttle bằng `requestAnimationFrame`) và kiểm tra phần tử `#hero-end` (một div cao 1px ở đáy hero). Overlay bằng `true` khi `getBoundingClientRect().top > 64`.

Có **skip link** "Bỏ qua, tới nội dung chính" trỏ tới `#main`; link này ẩn và chỉ hiện khi focus bằng bàn phím.

**Footer** nền surface, viền trên, lưới 3 cột trên `sm`:

1. Logo kèm câu "Tìm phòng trọ cho người thuê, công cụ quản lý cho chủ trọ."
2. "Khám phá": Tìm phòng, Đăng ký chủ trọ.
3. "Hỗ trợ": hotline (icon `Phone`) và email (icon `Mail`).

Dưới cùng là dòng `© {năm} Trọ Ổn` cỡ `text-xs`.

---

## 6. Homepage: 4 section theo thứ tự

### 6.1 Hero video toàn màn hình (`hero-video.tsx`)

- `<section>` cao `h-svh min-h-[600px] overflow-hidden bg-foreground`, **không ghim (không sticky)**, trang cuộn bình thường ngay từ nhịp đầu.
- Video phủ kín khung (`object-cover`), có các thuộc tính `muted loop playsInline preload="auto" disablePictureInPicture aria-hidden tabIndex={-1}` và `poster` là file webp.
- Chọn file **một lần** lúc mount: `matchMedia('(max-width: 767px)')` đúng thì dùng bản 720p, sai thì dùng bản 1080p.
- **Cách phát:** lúc mở trang, video đứng yên ở khung đầu. **Ở nhịp cuộn đầu tiên** (`scroll`, `{ once: true, passive: true }`), video bắt đầu phát và **lặp lại liên tục**. Nếu lúc mở trang đã ở giữa trang (`scrollY > 0`), video phát ngay.
- Dùng **IntersectionObserver**: khi hero khuất màn hình thì `pause()`, khi hiện lại thì `play()`. `play()` luôn có `.catch()`; nếu lỗi thì giữ ảnh poster.
- **Parallax:** khi cuộn, lớp media dịch `translate3d(0, scrollY * 0.25, 0)` (tối đa bằng chiều cao hero), cập nhật bằng rAF và chỉ chạy khi hero còn trên màn hình.
- **Giảm chuyển động** (`prefers-reduced-motion`): không render video và không parallax, chỉ hiện ảnh poster.
- Hai lớp phủ giúp chữ trắng đọc được trên video sáng:
  - lớp trên: `h-48 bg-gradient-to-b from-foreground/60 to-foreground/0`
  - lớp toàn khung: `bg-gradient-to-t from-foreground/90 via-foreground/40 to-foreground/0`
- Nội dung nằm trong Container, căn đáy (`flex flex-col justify-end pt-20 pb-6 sm:pt-24 sm:pb-10`):
  - `h1` `text-display font-bold text-balance text-white`, `max-w-3xl`: **"Tìm phòng trọ vừa túi tiền"**
  - Đoạn `text-base text-white`, `max-w-lg`: **"Ảnh thật, giá rõ, đặt lịch xem phòng trong vài bước."**
  - Bên dưới là form tìm phòng (mục 6.2), cách tiêu đề `mt-6 sm:mt-8`.
- Ở đáy section đặt `<div id="hero-end" className="absolute inset-x-0 bottom-0 h-px" />`.

### 6.2 Form tìm phòng (`hero-search-form.tsx`)

- Thẻ `<form role="search" aria-label="Tìm phòng trọ">`, nền surface, `rounded-xl`, `shadow-float`, `p-4 sm:p-5`, có class `animate-rise` (hiệu ứng duy nhất khi tải trang).
- Lưới: điện thoại `grid-cols-2` (Khu vực chiếm 2 cột; Khoảng giá và Hình thức mỗi ô 1 cột; nút 2 cột) → `sm:grid-cols-3` (nút chiếm 3 cột) → `lg:grid-cols-[1.4fr_1fr_1fr_auto]` (tất cả nằm một hàng). Mỗi ô có `min-w-0`.
- 3 ô Select, mỗi ô có `<Label htmlFor>` hiển thị rõ:
  - **Khu vực** (placeholder "Chọn khu vực"): `khu-cong-nghe-cao` "Gần ĐH FPT, Khu Công nghệ cao", `lang-dai-hoc` "Làng Đại học Thủ Đức", `thu-duc` "Thủ Đức", `binh-thanh` "Bình Thạnh", `go-vap` "Gò Vấp"
  - **Khoảng giá** (placeholder "Mọi mức giá"): `0-2000000` "Dưới 2 triệu", `2000000-3000000` "2 đến 3 triệu", `3000000-5000000` "3 đến 5 triệu", `5000000-` "Trên 5 triệu"
  - **Hình thức** (placeholder "Tất cả"): `WHOLE_ROOM` "Nguyên phòng", `SLOT` "Ở ghép"
- Nút submit size `lg`, icon `Search`, chữ "Tìm phòng". Khi submit, điều hướng tới `/rooms?area=..&price=..&mode=..` và bỏ các giá trị rỗng (hàm `buildSearchUrl`).
- Từ `sm` trở lên, dưới các ô có một hàng `<nav aria-label="Khu vực tìm nhiều">`, ngăn cách bằng `border-t`. Hàng này gồm chữ "Tìm nhiều:" và 3 nút hình viên thuốc (link tới `/rooms?area=...`) cho các khu `khu-cong-nghe-cao`, `lang-dai-hoc`, `binh-thanh`.

### 6.3 "Phòng mới đăng" (`featured-rooms.tsx`)

- Section `py-16 sm:py-20`. Hàng tiêu đề gồm `h2 text-xl font-bold` "Phòng mới đăng" và nút link "Xem tất cả" (tới `/rooms`, class `px-0`).
- Lưới `gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3`, render dạng `<ul>`, mỗi phòng là một `<li>`.
- Có đủ 4 trạng thái (vùng bọc có `aria-live="polite"` và `aria-busy`):
  - **Đang tải:** 6 `RoomCardSkeleton`, kèm dòng `sr-only` "Đang tải danh sách phòng".
  - **Lỗi:** ErrorState "Không tải được danh sách phòng. Kiểm tra kết nối mạng rồi thử lại." với nút Thử lại gọi `retry()`.
  - **Trống:** EmptyState icon `SearchX`, câu "Chưa có phòng mới đăng. Thử tìm theo khu vực khác.", nút outline "Xem tất cả phòng".
  - **Có dữ liệu:** danh sách RoomCard.
- Hook `useFeaturedRooms()` trả `{ status: 'loading' | 'success' | 'error', rooms, retry }`, gọi `getFeaturedRooms()` trong `useEffect` và có cờ `cancelled` để bỏ kết quả cũ.
- `rooms.service.ts`: chờ giả 600ms rồi trả mock. Hỗ trợ query string `?demo=loading` (chờ 60 giây), `?demo=empty` (trả mảng rỗng), `?demo=error` (ném lỗi) để xem từng trạng thái.

**RoomCard** (không viền, dùng khoảng trắng):

- Ảnh tỉ lệ `aspect-[4/3] rounded-xl object-cover`, `loading="lazy"`. Nếu ảnh lỗi thì hiện khung `primary-soft` với icon `ImageOff` và chữ "Chưa tải được ảnh".
- Góc trên trái ảnh là StatusBadge trên nền `bg-surface shadow-sm`.
- `h3` một dòng (`line-clamp-1`) chứa **một** `<Link>` tới `/rooms/:id`. Link có `after:absolute after:inset-0` để cả thẻ bấm được; focus ring vẽ trên `::after`; hover thì gạch chân tiêu đề.
- Dòng địa chỉ có icon `MapPin`. Dòng thông tin `text-xs` gồm `Ruler` + "22 m²", `Users` + "2 người", và chữ "Nguyên phòng" hoặc "Ở ghép".
- Giá: số tiền đậm `formatVnd(price)` cộng hậu tố `text-sm text-muted-foreground`: "/tháng", hoặc "/chỗ/tháng" nếu ở ghép.
- Quy tắc chọn huy hiệu (`getRoomBadge`):
  - `HELD`: warning "Đang được giữ"
  - `OCCUPIED`: neutral "Đã cho thuê"
  - `MAINTENANCE`: danger "Đang sửa chữa"
  - `availableSlots === 0`: neutral "Hết chỗ"
  - Ở ghép còn đúng 1 chỗ: warning "Còn 1 chỗ"
  - Ở ghép còn nhiều chỗ: success "Còn N chỗ"
  - Các trường hợp còn lại: success "Còn trống"

**Kiểu dữ liệu (`types.ts`):**

```ts
export type RentalMode = 'WHOLE_ROOM' | 'SLOT'
export type RoomStatus = 'AVAILABLE' | 'HELD' | 'OCCUPIED' | 'MAINTENANCE'
export interface RoomListing {
  id: string; code: string; title: string; branchName: string; address: string; areaKey: string
  price: number; sizeM2: number; capacity: number; availableSlots: number
  rentalMode: RentalMode; roomTypeName: string; status: RoomStatus
  imageUrl: string; imageAlt: string
}
```

**Dữ liệu giả (6 phòng).** Ảnh lấy từ Unsplash với `https://images.unsplash.com/photo-{ID}?auto=format&fit=crop&w=800&h=600&q=75`:

| id | Tiêu đề | Chi nhánh | Địa chỉ | Giá | m² | Người | Chỗ trống | Hình thức | Trạng thái | Unsplash ID |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| r-101 | Phòng có cửa sổ lớn, gần ĐH FPT | Chi nhánh Lê Văn Việt | 214 Lê Văn Việt, TP.HCM | 3200000 | 22 | 2 | 2 | WHOLE_ROOM | AVAILABLE | 1781249144056-ec397e444dfa |
| r-102 | Studio tầng cao nhìn ra thành phố | Chi nhánh Xa lộ Hà Nội | 88 Xa lộ Hà Nội, TP.HCM | 4800000 | 30 | 2 | 2 | WHOLE_ROOM | AVAILABLE | 1620332372374-f108c53d2e03 |
| r-103 | Phòng ở ghép 4 người, có bàn học | Chi nhánh Man Thiện | 57 Man Thiện, TP.HCM | 1500000 | 28 | 4 | 1 | SLOT | AVAILABLE | 1723640582986-1c969ef540b3 |
| r-104 | Phòng đơn yên tĩnh cho sinh viên | Chi nhánh Làng Đại học | 12 đường Tô Vĩnh Diện, TP.HCM | 2700000 | 18 | 1 | 1 | WHOLE_ROOM | AVAILABLE | 1786886393825-a2aff98f5f0a |
| r-105 | Phòng mới sửa, có ban công | Chi nhánh Bình Thạnh | 31 Nguyễn Gia Trí, TP.HCM | 3900000 | 25 | 2 | 0 | WHOLE_ROOM | HELD | 1781249144083-6264548df901 |
| r-106 | Studio có bếp riêng, cho nuôi mèo | Chi nhánh Nguyễn Xiển | 402 Nguyễn Xiển, TP.HCM | 5200000 | 35 | 3 | 3 | WHOLE_ROOM | AVAILABLE | 1663811396760-b6c84fa45ee9 |

Mỗi phòng có `imageAlt` mô tả ngắn bằng tiếng Việt, ví dụ "Phòng ngủ nhỏ có cửa sổ và giường đôi".

### 6.4 "Thuê phòng trong 4 bước" (`rental-steps.tsx`)

- Section nền surface, có viền trên và dưới, `py-16 sm:py-20`. Tiêu đề `h2` "Thuê phòng trong 4 bước".
- Danh sách `<ol>`, lưới `sm:grid-cols-2 lg:grid-cols-4`. Mỗi bước có số thứ tự trong vòng tròn `size-10 bg-primary-soft text-primary font-bold`, tiêu đề `h3` và một dòng mô tả `text-sm text-muted-foreground`. Trên desktop có một đường kẻ ngang 1px nối giữa các vòng tròn.
- Nội dung 4 bước:
  1. **Tìm phòng**: "Lọc theo khu vực, giá và hình thức thuê."
  2. **Đặt lịch xem**: "Chọn giờ, chủ trọ xác nhận qua thông báo."
  3. **Giữ phòng**: "Đặt cọc để phòng không bị người khác thuê."
  4. **Ký hợp đồng**: "Nhận phòng, theo dõi hoá đơn ngay trên app."

### 6.5 "Đã thuê hoặc đang cho thuê?" (`role-paths.tsx`)

Lưới 2 cột (`md:grid-cols-2`), mỗi cột một khung `rounded-2xl p-6 sm:p-8`:

- **Chủ trọ**: nền `primary`, chữ trắng. Câu phụ "Quản lý cả dãy trọ ở một nơi." Danh sách: `DoorOpen` "Phòng, chỗ ở và người thuê", `Zap` "Chốt điện nước mỗi tháng", `Wrench` "Nhận và giao việc sửa chữa". Mỗi icon nằm trong ô `size-8 rounded-lg bg-white/15`. Nút variant `inverse` "Đăng nhập quản lý" tới `/login`.
- **Người thuê**: nền `primary-soft`, chữ `foreground`. Câu phụ "Mọi thứ về phòng của bạn." Danh sách: `Receipt` "Xem tiền phòng, điện nước", `Wrench` "Báo hỏng và theo dõi tiến độ", `Bell` "Nhận thông báo từ chủ trọ". Mỗi icon nằm trong ô `bg-surface text-primary`. Nút default "Vào phòng của tôi" tới `/tenant/my-room`.

---

## 7. Xử lý video trước khi đưa vào dự án

Bỏ âm thanh, nén nhẹ, bật `faststart` để trình duyệt phát được khi file chưa tải xong:

```bash
ffmpeg -i input.mp4 -an -c:v libx264 -preset slow -crf 24 -pix_fmt yuv420p -movflags +faststart living-room-1080.mp4
ffmpeg -i input.mp4 -an -vf "scale=-2:720,fps=30" -c:v libx264 -preset slow -crf 25 -pix_fmt yuv420p -movflags +faststart living-room-720.mp4
ffmpeg -i input.mp4 -frames:v 1 -vf scale=1280:-2 -c:v libwebp -quality 72 living-room-poster.webp
```

`features/home/data/hero-media.ts` import 3 file trên và export `{ poster, sources: { desktop, mobile } }`.

---

## 8. Tiêu chí hoàn thành (tự kiểm tra trước khi trả kết quả)

- [ ] `npm run build` (`tsc -b && vite build`) và `npm run lint` không có lỗi; console trình duyệt không có lỗi.
- [ ] Không còn màu viết cứng; mọi màu đi qua token. Chỉ có một font với hai độ đậm.
- [ ] Không có thanh cuộn ngang ở 375px, 768px và 1280px.
- [ ] Hero: trang **không bị ghim** khi cuộn. Video phát từ nhịp cuộn đầu tiên, lặp lại, dừng khi khuất và phát tiếp khi hiện lại. Bật giảm chuyển động thì chỉ thấy ảnh tĩnh.
- [ ] Header trong suốt trên hero và chuyển sang nền trắng sau khi cuộn qua hero.
- [ ] Danh sách phòng xem được đủ 4 trạng thái qua `?demo=loading|empty|error`.
- [ ] Mọi input có label; mọi ảnh có alt (ảnh trang trí dùng `alt=""`); thao tác được bằng bàn phím và có focus ring rõ; vùng chạm từ 44px trở lên.
- [ ] Nội dung bám đúng phần chữ ở trên, không dùng lorem ipsum.
