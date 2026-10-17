# Tenant implementation status — 2026-10-16

T01–T14 implemented as an extension of the current local Guest/Auth/User frontend. T15 move-out remains Phase 2. No branch change, commit, push, Backend source or database change is part of this implementation.

| Scope | Routes / behavior |
| --- | --- |
| T01 | `/tenant/dashboard`: at most 3 summary cards, personal debt/deadline/contract, recent invoices, pending work and shared notifications |
| T02, T14 | `/tenant/my-room`: ROOM/SLOT context, gallery, amenities/assets, active services, rules/contact, authorized nullable branch Zalo |
| T03 | `/tenant/contracts`, `/tenant/contracts/:contractId`: authorized searchable/filterable contracts, shared terms, nullable document metadata |
| T04 | `/tenant/contracts/:contractId/my-terms`: own agreed rent, individual dates/status, approved first/last obligations and verified/applied prepaid, linked invoices |
| T05, T06 | `/tenant/invoices`, `/tenant/invoices/:invoiceId`: month/document/payment-state filters, pagination, approved line items, server summaries, partial sandbox payment, reservations, idempotency and status polling |
| T07 | `/tenant/payments`: filters/pagination, details drawer, authorized payer/evidence/verification, invoice links, separate cash/allocation/prepaid |
| T08 | `/tenant/services`: mandatory defaults, optional registration/cancellation awaiting trusted approval, price/date validation and history |
| T09, T10 | `/tenant/utilities`, `/tenant/utilities/allocations`: charge segments/evidence/snapshots, anonymous resident-day shares, exact-total proposal validation, history/results; no Tenant approval |
| T11, T12 | `/tenant/maintenance`, `/tenant/maintenance/new`, `/tenant/maintenance/:reportId`: valid affected assets/services, images, cancellation, multi-record progress/evidence and cost approval display |
| T13 | Existing `/notifications`: shared inbox/count/read/all-read, mock Tenant category filters and scoped deep links |

Architecture: existing teal/Be Vietnam Pro/Tailwind/Radix/Lucide components, session store, credentialed HTTP client, MSW and QueryClient are reused. Primary visual skill: `minimalist-ui`, with project brand requirements taking precedence over generic palette/font/icon preferences. Tenant server data uses account + ContractTenant query keys. Zustand stores only active relationship selection. Explicit invalid contexts fail closed; eligible tenancy is determined by the API relationship list, not TENANT role or booking confirmation. Desktop sidebar and compact mobile selector share routes; feature pages load lazily.

Real coverage: existing real auth/notifications adapters remain available. Tenant domain uses explicit mock contracts because current local self-service APIs are absent or lack required ownership checks. Real Tenant provider does not silently substitute mocks. See [API contract](tenant-api-contract.md) for audited controllers, DTOs, authorization, pagination and BE dependencies.

Demo coverage S01–S12: coherent ROOM/SLOT/history and roommate fixtures; partial/prepaid payments; pending/review/verified/failed settlement; mandatory/optional service transitions and rejection; shared charges and proposal approval/rejection; multi-record repairs with approved/pending liability; scoped notifications; missing/empty/error/conflict paths; explicit development reset. No real payment success is simulated as live payment. Fixtures are tab-local, and timed trusted workflows advance on API reads.

Verification on 2026-10-16:

| Check | Actual result |
| --- | --- |
| `npm.cmd run typecheck` | Passed |
| `npm.cmd run lint` | Passed, no errors or warnings |
| `npm.cmd run build` | Passed; Tenant page modules load lazily |
| `npm.cmd test` | 89 passed across 4 files: 63 Tenant domain/API cases and 26 existing cases |
| `npm.cmd exec playwright -- test --workers=3` | 52 passed: 26 Tenant desktop/mobile cases and 26 existing Guest/User/visual cases |
| Focused browser rerun after dashboard polling refinement | 4 passed: context switching and every major Tenant page, on desktop/mobile |
| Final fixed-viewport overflow rerun after grid correction | 2 passed: every major Tenant page on desktop/mobile; mobile maintenance document width equals the 390px device viewport |
| Final form/upload regression after intrinsic-width correction | 4 passed: every major Tenant page and maintenance upload/create/cancel/notifications on desktop/mobile; all form contents fit their panels |
| `git diff --check` | Passed |
| Visual inspection | Actual desktop/mobile dashboard, room, invoices and maintenance screenshots inspected; browser tests check all major Tenant pages for overflow and runtime errors |

Targeted suites cover ownership at the MSW boundary, forged demo identities, integer finance, full/partial settlement, idempotency, reservations, prepaid cash distinction, resident-day rounding, service transitions, furniture ownership per room/slot, maintenance validation/timeline/liability and linked reset. Browser tests cover desktop/mobile workflows, direct navigation/reload, selectors, foreign resources, uploads, notifications and Guest/User regression. The integrated browser was unavailable, so installed Edge via Playwright was used.

Final visual review found a mobile maintenance grid expanding the layout viewport by 18px. Tenant content/form grids now use explicit `minmax(0,1fr)` columns, and the upload row permits its native file control to shrink. Overflow assertions compare document width against the configured device viewport, not `window.innerWidth`, which mobile browsers can expand; they also check that each form's content fits its own width.

Remaining BE dependencies: authorized self-service read/write endpoints, real document/evidence/upload delivery, private Zalo resource, secure payment provider/callback/reconciliation, atomic reservations and allocation, Tenant-scoped utility/service reads, authoritative approval workflows, notification context/category support and server-side pagination for production volumes. Contract-policy Q1–Q5 and historical debt payment permissions require product decisions. Mock Zalo/contact/evidence and sandbox instructions are fictional fixtures. T15 and management workspaces are excluded.

Guest/Auth/User pages are preserved. Small compatibility changes add Tenant navigation/demo login, optional notification context/category support and prevent forced business-error demo parameters from breaking auth refresh.
