# TroPro Guest + User implementation status

Branch: `feature/fe-rebuild`. Work remains uncommitted as requested. Backend source, schema and API permissions were not changed.

## Delivered screens and behavior

| Scope | Routes | Implemented |
|---|---|---|
| Guest | `/` | Existing video/poster hero preserved; service-backed rooms, search, accurate rental process, Owner/User entry points, compact FAQ. |
| Discovery | `/rooms`, `/rooms/:roomId`, `/branches/:branchId` | URL filters/sort/pagination, debounced text query, browser history, desktop filters/mobile drawer, gallery, services, date-sensitive availability/slots, branch public details, sticky rental actions. |
| Auth | `/login`, `/register`, `/verify-email`, `/forgot-password`, `/reset-password` | Validated forms, password visibility, safe intended-destination restore, token verification, mock mailbox/recovery, memory access token, credentialed refresh-cookie recovery and shared refresh. |
| User | `/user/dashboard` | Upcoming appointments, active holds/deadlines, recent notifications, useful empty state; no charts. |
| Appointments | `/rooms/:roomId/appointments/new`, `/user/appointments`, `/user/appointments/:appointmentId` | Create PENDING, list/status filter/pagination, details/timeline, valid cancellation and explicit simulated branch response. |
| Bookings | `/rooms/:roomId/bookings/new`, `/user/bookings`, `/user/bookings/:bookingId` | ROOM/SLOT, arbitrary start day, required end date, policy snapshot/acknowledgment, availability/conflict checks, details/status filtering/timeline/cancellation/expiry. |
| Payment | `/user/bookings/:bookingId/payment` | Amount/reference copy, deadline, polling, history, trusted mock success/pending/failure/late scenarios, accurate distinction between pending and verified, no real payment credentials. |
| Shared | `/profile`, `/notifications` | Supported personal fields/save/cancel/logout, notifications/filter/read/read-all/linked records and unread bell. |
| Owner entry | `/owner-onboarding` | Explains onboarding requirement and offers ordinary USER registration; does not grant OWNER. |

## Architecture and API coverage

- Existing React/TS/Vite/Tailwind/Radix/Lucide stack retained. Be Vietnam Pro and teal `#0F766E` remain; global tokens/styles stay in `src/index.css`.
- Primary visual skill: `redesign-existing-projects`; complementary contract skill: `api-and-interface-design`. Browser skill was loaded; its integrated browser was unavailable, so verification uses local Playwright/Edge.
- Zustand stores only session/user/active-role state. TanStack Query owns server data, retries, polling and invalidation. Shared forms use React Hook Form/Zod; booking/date/policy inputs are schema validated.
- `src/interfaces/domain.ts`: validated shared contracts; feature room types alias this source.
- `src/services/{config,http,guest-user,query-client}.ts`: explicit providers, HTTP envelope/error handling, adapters and services.
- `src/mocks/{fixtures,database,handlers,browser}.ts`: coherent UUID fixtures, session persistence, ownership/state machines, MSW transport; components never import fixture arrays.
- Route chunks load lazily. Public/Auth/User layouts share branding, accessible navigation, focus trapping and responsive spacing.
- Real adapters cover existing auth, public rooms and notifications. Public rooms remain incomplete/secured in current BE; hybrid intentionally uses mock catalog. Missing APIs use explicitly configured mocks rather than error fallback.
- Mock session data is kept in sessionStorage only for the local simulation. Real access/refresh tokens and passwords are not persisted by the client.

## Run and test

```powershell
cd fe-sba-project
npm.cmd ci
npm.cmd run dev
```

Default: `http://localhost:5173`, fully mock. Demo login: `an@tropro.demo` / `TroPro@123`. New users can register, verify via the explicit demo mailbox and recover their password. Changing/reloading a tab preserves that tab's mock session; a fresh browser session starts new fixtures.

Copy `.env.example` to `.env.local` and set `VITE_API_MODE=hybrid` to use the running BE for auth/notifications. Restart Vite after changing env. `real` mode does not conceal missing endpoints. Production hosting must rewrite frontend routes to `index.html` and provide the correct API proxy/cookie/CORS setup.

Commands: `npm run typecheck`, `npm run lint`, `npm run build`, `npm test`, `npm run test:e2e -- --workers=2`. Browser tests use installed Edge; set another Playwright channel/install Chromium if Edge is unavailable on another machine.

Final verification: TypeScript, ESLint and production build passed; 26 unit/API boundary tests and 26 desktop/mobile browser tests passed after the final changes. Tests cover resource conflicts, dates, ownership, state transitions, late payments, adapters, paging, credentialed auth, refresh deduplication, explicit no-fallback behavior, registration/verification/recovery, responsive navigation and loading/error/empty states. Desktop/mobile screenshots of home, search, room detail, login, dashboard and booking were inspected; bundled images were checked after local image polish. Screenshots are available locally under `fe-sba-project/test-results/visual/` (ignored by Git).

Mock state scenarios: `?demo=loading`, `?demo=empty`, `?demo=error`. Payment scenarios and simulated appointment responses are explicit controls in mock mode only. UI never offers a USER action to verify real receipt of money.

## Known boundaries and backend handoff

- This delivers Guest/Auth/User, not Manager/Owner/Admin/Tenant workspaces or contract/invoice workflows. A returned contract reference is displayed; the Tenant contract screen is outside this task.
- Real API adapters are verified with contract tests. Live BE auth, SMTP, database transactions and real payments have not been exercised by this FE task.
- Public room security, availability, policies and missing endpoints still require BE implementation. See [guest-user-api-contract.md](guest-user-api-contract.md).
- Hybrid simulated leasing events do not create real BE notifications; fully mock mode demonstrates the complete linked notification flow. Hybrid mock profile/recovery edits affect the local simulation, not the real account.
- MSW is a local browser simulation, not a multi-client server or durable datastore. Its identity checks model ownership within the demo and cannot replace real BE authorization.
- Demo room images are optimized, bundled local WebP illustrations from Unsplash; source links are in `fe-sba-project/public/demo/README.md`. No claim of verified photography, actual inventory or ratings. Demo map/contact fields are fixture data rather than production branch information.
- No real QR/bank account is fabricated. Real payment instructions await a trusted BE contract; financial verification and expiry remain backend responsibilities.
- The existing untracked `docs/frontend/prompt-homepage-tro-on.md` was preserved.
