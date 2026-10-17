# TroPro Tenant API contract — 2026-10-16

Tenant extends the existing Guest/Auth/User application. `src/interfaces/tenant.ts` contains the executable Zod DTO contracts; `src/services/tenant.ts` uses the existing credentialed HTTP client and explicit feature provider. These Tenant endpoints are **proposed and implemented in MSW**, not claims that the current BE implements them.

## Actual local Backend audit

| Existing capability | Current decision |
| --- | --- |
| `/api/auth/*` | Reuse existing real auth adapter, refresh coordination and in-memory access token. No second Tenant login. |
| `/api/notifications`, unread count, mark-read, mark-all-read | Reuse existing real notification adapter and recipient authorization. Current controller accepts read/page/size, not Tenant category/context filters. Category filter is enabled in mock only. |
| `POST /api/contract-services/register`, `PUT /api/contract-services/{id}/cancel`, `GET /api/contracts/{id}/services` | Not connected to Tenant. `UtilityServiceManagerImpl` does not establish the caller's ContractTenant ownership; registration also accepts a client price. Existence and date validation are insufficient for Tenant self-service. |
| Cost allocation propose/read/default-allocation endpoints | Not connected to Tenant. Caller membership and authorized participant projections are missing in the inspected service/controller paths. Approval endpoints remain management-only. |
| Room service charge list/detail | Not connected to Tenant. Inspected reads fetch by room/charge ID without a Tenant membership context. |
| `/api/furnitures/*` | Not connected to Tenant. `PendingSecurityMaintenanceAccessPolicy` is a placeholder that fails closed. Also uses different pagination conventions. |
| Tenant contract/tenancy, invoice, invoice payment, maintenance self-service, private branch Zalo | Compatible complete self-service APIs were not found. Mock provider supplies these contracts. |

No Backend source, database schema or migration was changed. Real endpoints were inspected locally; live BE integration was not claimed or tested in this task.

## Provider and transport

`VITE_API_MODE=mock` supplies the complete local experience. `hybrid` keeps real auth/notifications and mocks missing domains, including Tenant. `VITE_TENANT_PROVIDER=mock|real` overrides that group. Real requests never silently fall back to mock after an error. Selecting real Tenant currently produces visible missing/unsupported API errors until BE implements this contract.

Mock URLs prepend `/__mock`. Responses follow `{ status, code, message, data }`. UUIDs are required. Dates are ISO `YYYY-MM-DD`; timestamps include an offset. Amounts are integer VND; timestamps are displayed in Vietnam time. Error statuses: 401 unauthenticated, 404 unavailable/unauthorized resource, 409 conflicting transition or inactive tenancy, 422 invalid input. Unknown management actions return 404.

All endpoints below require authentication. Let `S = /api/tenant/tenancies/{contractTenantId}`.

| Feature | Method and endpoint | Request | Response |
| --- | --- | --- | --- |
| Authorized relationships | `GET /api/tenant/tenancies` | Session identity only | `Tenancy[]`, only the caller's ContractTenants, including readable history |
| Room / private branch resources | `GET S/room` | Authorized context | `TenantRoom`: tenancy, images, area/floor/type, amenities/rules, authorized manager contact, assets, nullable Zalo URL |
| Main contract + own terms | `GET S/contracts/{contractId}` | Exact context/contract match | `Tenancy` projection with shared terms and only the caller's personal terms; nullable actual document metadata |
| Invoices | `GET S/invoices` | Authorized context | `Invoice[]` with financial summaries |
| Invoice detail / instructions | `GET S/invoices/{invoiceId}` | Exact invoice beneficiary/context | `Invoice`, item snapshots, prepaid source, totals, available-to-pay and provider instructions |
| Initiate partial payment | `POST S/invoices/{invoiceId}/payments` | `{amount, payerName, idempotencyKey, outcome?}` | `TenantPayment`, initially PENDING; `outcome` is mock-only, never a proposed real payment authority |
| Payment history / status | `GET S/payments`, `GET S/payments/{paymentId}` | Beneficiary relationship | `TenantPayment[]` / `TenantPayment`; authorized payer name, receipt/allocation/prepaid buckets, verification date, evidence |
| Service catalog + requests | `GET S/services` | Room and beneficiary scope | `TenantService[]`, mandatory flag, provider price/unit, nullable subscription and status/history |
| Optional service request | `POST S/services` | `{serviceId, startDate, endDate, note}` | Service PENDING; price comes from provider, never caller input |
| Optional service cancellation | `POST S/services/{serviceId}/cancel` | APPROVED optional service | CANCEL_REQUESTED; requires management approval to finish |
| Utility charge breakdown | `GET S/utilities` | Authorized room/shared-charge membership | `UtilityCharge[]`, multiple segments, readings where applicable, quantity/price, total, personal share, evidence, approved snapshot, invoice link, anonymized participants |
| Allocation history | `GET S/allocations` | Context | Own `AllocationProposal[]` |
| Allocation proposal | `POST S/allocations` | `{chargeId, method, reason, shares:[{id,amount}]}` | PENDING proposal, never an approval |
| Maintenance list/detail | `GET S/maintenance`, `GET S/maintenance/{reportId}` | Requester's ContractTenant | `MaintenanceReport[]` / detail with multi-record timeline and approved/pending cost responsibilities |
| Maintenance creation | `POST S/maintenance` | `{assetId, category, title, description, priority, attachments}` | OPEN report, visible in subsequent scoped reads |
| Maintenance cancellation | `POST S/maintenance/{reportId}/cancel` | Own OPEN request and active tenancy | CANCELLED report with timeline record |
| Existing inbox | `GET /api/notifications?read=&page=&size=` | Recipient from session | Existing page DTO; mock additionally supports exact `type` filter |
| Development reset | `POST /api/tenant/demo/reset` | Explicit local demo account, development build only | `null`; restores linked Tenant fixtures and clears their Tenant notifications; Guest/User activities remain |

Current Tenant list DTOs are bounded demo collections; the UI filters and paginates invoices/payments/maintenance in memory (6 rows/page). Existing notifications use server pagination (request zero-based, adapter/UI one-based, size 10). Production Tenant endpoints need server-side filters and pagination before large datasets; do not silently reinterpret these arrays as BE page DTOs. Proposed pagination should follow the existing zero-based request / normalized one-based UI convention.

## DTO and ownership invariants

`Tenancy.id` is a ContractTenant ID, not a room ID or role. It carries user/contract/branch/room/slot relationships, separate contract and occupancy statuses, individual agreed rent, first/last prepaid obligations and approved terms. Contract documents are nullable; the UI never fabricates a PDF or signature.

Every resource lookup checks both authenticated user → ContractTenant and resource → selected ContractTenant. An account's other context is also rejected until explicitly selected. A roommate's common contract or charge does not grant access to their personal obligations, invoices, payer details or reports. Shared charge participants expose only authorized relationship IDs, anonymized labels and allocation inputs, not private contracts or debt.

Client route guards improve navigation only. BE must perform these ownership checks for every read and mutation, derive ownership from the authenticated principal, validate UUID/foreign keys and never trust role strings or client-supplied `userId`, totals, prices, financial status or approval status. Confirmed booking does not create tenancy permission.

The shared Zustand selector stores only selected relationship IDs per account in sessionStorage. Each render validates selection against the authorized API list. Explicit invalid `tenancyId` URLs fail closed. Tenant Query keys include account ID, ContractTenant ID, feature and route/scenario parameters; switching context remounts feature forms and uses separate cache entries. Direct notification links carry the authorized `contractTenantId`. Existing `referenceId` remains a UUID; mock notification DTO adds optional `contractTenantId`. Real notifications require that field or an authorized reference-resolution API before safe Tenant deep links can be enabled.

The hybrid identity bridge is accepted only with real auth configured and never authorizes the seeded demo identities. Mock auth requires a valid mock session. Real accounts receive no seeded Tenant relationships automatically. Furniture IDs distinguish rooms and assigned personal slots; shared utility charges expose the same source charge ID in each authorized personal projection.

## Financial rules

Invoice document status is DRAFT / ISSUED / CANCELLED. Payment state is derived separately for issued invoices: UNPAID / PARTIAL / PAID / OVERDUE. Non-issued invoices have zero payable debt and never receive a payment action. Their internal financial label is ignored in display/filtering.

For an eligible issued invoice:

`outstanding = approved total - approved prepaid application - VERIFIED allocated amounts`

`availableToPay = outstanding - pending/review reservations`

The engine uses integer VND, rejects zero/fractional/negative/excess amounts, validates idempotency keys, rejects reused keys with a changed payload and serializes settlement. PENDING/REVIEW/FAILED cash does not lower debt. Received cash and allocated cash remain separate fields. Different authorized payer names are allowed; the invoice beneficiary remains the same ContractTenant.

PREPAID_RENT receipts have first/last buckets and applied amounts. Applying a bucket to an invoice never creates a new receipt. Booking Hold remains in the existing booking payment domain. There is no invented security deposit, `payment_allocations` table, or new database table.

Current payment initiation is sandbox-only: no bank account, real credentials, invented QR or real money transfer. A QR appears only if the provider supplies a URL. Real payment integration needs secure instructions, server-side idempotency/reservations, provider callback verification, receipt reconciliation, authorized evidence and atomic beneficiary allocation. Tenants never call verify/approve endpoints.

Historical relationships are readable; this demo conservatively disallows creating new payments/services/proposals/reports after the tenancy ends. BE/product must explicitly decide historical-debt collection permissions.

## Services, allocations and maintenance

Optional service dates must be within an active rental period, use `[startDate,endDate)`, not start in the past, and not duplicate an active/pending subscription. Electricity/water cannot be canceled. Registration stays PENDING; cancellation stays CANCEL_REQUESTED until the trusted demo processor decides it.

Resident-day allocation intersects each residence interval with the charge interval. Integer VND uses largest remainder, ties by relationship UUID, so shares sum exactly to the source. Zero total residence is rejected. Custom proposals require exactly one valid entry per authorized participant, nonnegative integer amounts and exact total. Neither submission nor approval rewrites an issued invoice. Production approval must coordinate supersession and explicit billing adjustments; it must be management-only.

Maintenance category is FURNITURE or SERVICE; the affected item must belong to the context's room/active services. Title 5–120 characters, description 15–2000, priority LOW/NORMAL/HIGH/URGENT. Images are PNG/JPEG/WebP, at most 3, at most 1 MB each; client and mock validate MIME/size/signature. Mock stores local data URLs in sessionStorage and surfaces quota errors. Real upload API/object storage, malware scanning and authorized evidence URLs are still required.

Only OPEN own reports can be canceled. Multiple ReportRecords retain before/after evidence, handler, status and cost approval independently. Unapproved tenant liability is not debt; approved liability becomes debt only through an issued invoice item. No Tenant approval UI or API exists.

Zalo is read from the private room projection, requires a currently active authorized relationship and validated HTTPS `zalo.me/g/…` URL, opens in a new tab with noopener/noreferrer. Missing/invalid/historical links stay disabled. The seeded link is fictional demo data, not an actual group guarantee.

## Policy boundaries

`docs/backend/contract-policies.md` Q1–Q5 remain OPEN. Demo rent, FIRST/LAST application, deadlines and repair invoice lines are explicit approved **fixture snapshots**, not a newly finalized policy. The engine does not calculate partial-month rent, calendar-month FIRST/LAST distribution, post-activation rent changes or early-move-out refunds. Those policies and their authoritative snapshots must be supplied by BE/product. T15 move-out is deferred.
