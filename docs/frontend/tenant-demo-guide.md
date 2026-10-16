# Tenant demo

Run from `fe-sba-project`:

```powershell
npm.cmd install
npm.cmd run dev
```

Open `http://localhost:5173/login`. Default `VITE_API_MODE=mock` supports the complete demo. The login page has **Điền tài khoản Tenant demo**; use fictional local credentials `tenant@tropro.demo` / `TroPro@123`. `roommate@tropro.demo` uses the same fictional password and has separate SLOT obligations. Existing `an@tropro.demo` remains a USER without a tenancy, even with confirmed booking activity. These credentials are not real BE credentials.

After login choose **Không gian thuê** in the header/menu or open `/tenant/dashboard`. The account has an active ROOM, active SLOT at another branch, and historical LEFT relationship. The shared selector updates all scoped screens. Guest/User flows remain available under **Về tài khoản** or the header menu.

Suggested flow:

1. Open My Room, gallery, contract and My Terms. Compare approved rent and the first/last prepaid receipt/application. Contract PDF is absent intentionally. Zalo URL is fictional demo data; SLOT and history demonstrate missing/disabled access.
2. Open Invoices and the issued ROOM invoice: rent, utilities and an approved repair, one verified partial payment, one prepaid application. Draft/canceled invoices cannot be paid.
3. Pay 100,000 VND in sandbox. It starts pending; refresh/polling shows verification after about 5 seconds and lowers only that invoice. Repeat with pending/review/failure scenarios to see debt preserved. No real transfer occurs and no QR is invented.
4. Register an optional service: pending, then approved by the simulated management process after about 12 seconds. Request cancellation to see CANCEL_REQUESTED before cancellation. Electricity/water cannot be canceled.
5. Switch to SLOT, inspect utility segments and anonymous residency-day shares, then propose an exact-total allocation. It stays pending for about 15 seconds before a trusted result; issued invoice history does not change.
6. Create a maintenance request with a valid asset, description and up to 3 images (1 MB each). New requests appear immediately and progress OPEN → ACCEPTED → IN_PROGRESS → COMPLETED at roughly 12-second intervals while queries run. Cancel only before acceptance. Existing completed examples show owner-approved, tenant-approved and unapproved tenant costs.
7. Open the shared Notifications inbox, filter categories, mark read and follow a Tenant reference. Its URL selects the correct authorized context. Historical contexts show records but block active actions.

Development-only **Công cụ demo → Đặt lại dữ liệu Tenant** restores coherent linked fixtures and Tenant notifications without resetting Guest/User booking activities. It is not shown in production builds. For a complete local sign-out/reset of all demos, clear this site's sessionStorage and reload; no real access token is stored there.

Advanced development scenarios: append `demo=loading|empty|error` to a Tenant URL. Auth refresh is excluded from forced business errors. For service/proposal rejection, keep `workflow=reject` in that page URL while the simulated workflow processes. This flag is forwarded only to the development mock provider; it is not a management permission or a real API parameter.

Mock mutations persist within the tab across reloads. Dates are generated relative to the month when demo data is first seeded/reset. Reset after long periods. Timed workflows advance on API reads; this is not a background service.

Hybrid: `VITE_API_MODE=hybrid` uses real auth/notifications and mock Tenant. Existing real accounts do not automatically gain seeded tenancy. For the full linked fictional Tenant demo use mock auth and mock notifications (or set their feature overrides). `VITE_TENANT_PROVIDER=real` requires the proposed APIs; missing APIs display errors without fallback.

Checks: `npm.cmd run typecheck`, `npm.cmd run lint`, `npm.cmd run build`, `npm.cmd test`, `npm.cmd run test:e2e`. Playwright uses installed Microsoft Edge and tests desktop/mobile; visual captures are in ignored `test-results/visual/`.
