# TroPro Guest + Auth + User API contract

Implementation branch: `feature/fe-rebuild`. Backend source is unchanged. Current Java controllers/DTOs are authoritative; proposed endpoints below are frontend contracts, not a claim that BE implements them.

## Configuration and transport

- `VITE_API_MODE=mock` (default): all domains use MSW under `/__mock/api/...`; complete local demo.
- `VITE_API_MODE=hybrid`: auth and notifications use BE; catalog, leasing, profile and recovery use MSW.
- `VITE_API_MODE=real`: all features send real requests, including proposed endpoints; unsupported endpoints show errors.
- Feature overrides: `VITE_AUTH_PROVIDER`, `VITE_CATALOG_PROVIDER`, `VITE_LEASING_PROVIDER`, `VITE_PROFILE_PROVIDER`, `VITE_NOTIFICATIONS_PROVIDER`, `VITE_RECOVERY_PROVIDER`, each `real|mock`.
- `VITE_API_BASE_URL` is empty by default. Vite proxies `/api` to `VITE_BACKEND_PROXY_TARGET` (default `http://localhost:8080`). Production needs a same-origin reverse proxy, or an explicitly configured API URL plus credentialed CORS and appropriate cookie attributes.
- For live verification email links, set BE environment `FRONTEND_BASE_URL=http://localhost:5173` (current BE defaults to port 3000). This is an environment setup requirement; no BE source was modified.
- BE defaults to secure refresh cookies. For local **HTTP** development only, set `REFRESH_COOKIE_SECURE=false`; keep it true with HTTPS in production. With a cross-origin API URL, configure Spring `app.cors.allowed-origins` (environment `APP_CORS_ALLOWED_ORIGINS`) to the exact FE origin and keep credentials enabled.
- Provider selection happens before sending a request. Real errors (including 401/403/404/409/500) never trigger mock fallback. A protected 401 can trigger one shared refresh request and one retry.
- All requests use `credentials: include`; access token is held only in the Zustand memory store. Refresh comes from BE's HttpOnly `refresh_token` cookie, path `/api/auth`. No real token or password is stored in browser persistence.
- Envelope: `{status, code, message, data}`; success `code=0`. Error responses use HTTP status plus application code. Zod validates response boundaries; malformed data produces a visible contract error.
- UI pages are **1-based**; request `page` is **0-based**; current `PagedResponse.number` is **1-based**. Page body: `{content, number, size, totalElements, totalPages}`. This conversion belongs in services.
- Shared DTO source: `fe-sba-project/src/interfaces/domain.ts`; requests/adapters: `src/services/guest-user.ts`.

## Existing backend endpoints

| Method / endpoint | Auth | Request | Response / behavior |
|---|---|---|---|
| POST `/api/auth/register` | Public | `{fullName,email,password}` | 202, `data:null`. Creates USER pending email verification. Never grants OWNER. Password 8–72 characters and at most 72 UTF-8 bytes. |
| POST `/api/auth/login` | Public | `{email,password}` | `{accessToken,tokenType,expiresIn,accountId,email,fullName,role}` plus HttpOnly refresh cookie. |
| POST `/api/auth/refresh` | Cookie | No body | Same auth DTO, rotated cookie. FE deduplicates concurrent refresh. |
| POST `/api/auth/logout` | Bearer | No body | `null`, invalidates access session and clears refresh cookie. |
| POST `/api/auth/verify-email` | Public | `{token}` | `null`. Token from email link, not a six-digit OTP. |
| GET `/api/v1/public/rooms` | Intended public; currently secured | `address,branchId,roomTypeId,minPrice,maxPrice,minCapacity,status,page,size,sort` | Page of `{room,branchId,branchName,address,roomTypeName}`. See room mapping below. |
| GET `/api/v1/public/rooms/{id}` | Intended public; currently secured | UUID path | Same public room wrapper. |
| GET `/api/notifications` | Bearer, current user | `read?:boolean,page=0,size=10` | Page of notification DTO. |
| GET `/api/notifications/unread-count` | Bearer | None | `{unreadCount:number}`, adapted to UI `{count}`. |
| PUT `/api/notifications/mark-read` | Bearer | `{notificationIds:UUID[]}` nonempty | Updated notification DTOs. Ownership checked by BE. |
| PUT `/api/notifications/mark-all-read` | Bearer | None | `null`. |

Notification DTO: `{id,recipientUserId,eventKey,type,title,message,referenceType,referenceId,read,readAt,createdAt,channel,deliveryStatus}`. Supported UI references: `APPOINTMENT` and `BOOKING`; other types render safely without inventing a route.

### Existing room adapter and gaps

`room` contains `id,branchId,roomTypeId,code,rentalMode,capacity,areaSqm,floorNumber,referencePrice,description,amenities,imageRefs,operationalStatus,isPublic,createdAt,updatedAt,version`.

- `rentalMode`: exactly `ROOM|SLOT`.
- Real title is composed from `roomTypeName` and `code`; no invented rating/popularity/verification labels.
- `areaSqm→sizeM2`, `floorNumber→floor`, `referencePrice→price`. Reference price is not an agreed contractual price.
- String HTTP(S) image references are displayed; unsupported image metadata is not interpreted as a verified image. Empty images use a clearly illustrative fallback.
- Operational ACTIVE does **not** prove rentable availability. Real adapter returns `status:UNKNOWN, availableSlots:null`; it does not invent occupancy/slots/services.
- UI real sorts map to `createdAt,desc`, `referencePrice,asc|desc`, `areaSqm,desc`.
- Current BE lacks mode filtering, full availability and structured area filtering. Real service rejects unsupported requested filters; hybrid uses the mock catalog for these capabilities.
- `SecurityConfig` currently has `.anyRequest().authenticated()` without public room exceptions. BE must explicitly authorize the intended public GET paths and review public fields.

## Proposed public contracts (mock today)

| Method / endpoint | Request | Response | Rules |
|---|---|---|---|
| GET `/api/v1/public/room-types` | None | `[{id:UUID,name}]` | Public types only. Do not use manager-only room type APIs. |
| GET `/api/v1/public/branches/{id}` | UUID | `{id,name,address,description,images,publicPhone,publicEmail,latitude,longitude,rules}` | Only public fields. No banking credentials, tenant identities or tenant-only Zalo links. Null contacts/coordinates are valid. |
| GET `/api/v1/public/branches/{id}/policy` | UUID; future BE may add `effectiveAt` | `{id,branchId,version,effectiveFrom,bookingHoldAmount,paymentWindowMinutes,temporaryHoldMinutes,confirmedHoldMinutes,cancellationTerms,appointmentPolicy}` | Return policy effective at booking request time. BE chooses and snapshots authoritative policy. |
| GET `/api/v1/public/rooms/{id}/availability` | `startDate,endDate` ISO local dates | `{roomId,startDate,endDate,available,reason,slots:[{id,label,available}]}` | Date-sensitive `[startDate,endDate)`. Validate actual allocations, holds, occupancy and operational state. No private occupants. |

`appointmentPolicy`: `{openingHour,closingHour,maxDurationMinutes,cancellationHours}` in Asia/Ho_Chi_Minh. These are explicit demo policy fields for BE agreement; current entity stores appointment policy as JSONB.

Catalog mock list accepts existing filters plus `rentalMode=ALL|ROOM|SLOT`, `area`, `availableOnly`, `sort=recommended|price-asc|price-desc|area-desc`. `availableOnly` describes availability **today**, not an assurance for an arbitrary future rental. Detail/create screens recheck the actual requested date range. All list endpoints support paging.

## Proposed authenticated appointment contracts

| Method / endpoint | Request | Response |
|---|---|---|
| POST `/api/appointments` | `{roomId,startAt,endAt,note}` | Appointment DTO, 201, initially PENDING. |
| GET `/api/appointments` | `status?,page,size` | Page scoped to authenticated requester. |
| GET `/api/appointments/{id}` | UUID | Owned appointment. |
| POST `/api/appointments/{id}/cancel` | No body | Updated appointment. |

Appointment DTO: `{id,requesterUserId,roomId,roomTitle,branchName,startAt,endAt,note,status,assignedManager,policySnapshot,createdAt,timeline}`. `timeline:[{status,at,note}]`; manager display is optional public assignment information.

Statuses: `PENDING,CONFIRMED,COMPLETED,REJECTED,CANCELLED`. Validate future timestamps, strict start/end ordering, same-day viewing within effective branch hours, duration and scheduling conflict. Mock also prevents the requester from overlapping appointments in different rooms. Cancellation permits only PENDING/CONFIRMED before the policy cutoff. 403 for other users, 404 missing, 409 invalid state/conflict. An appointment never reserves a rental resource.

Existing BE has appointment entity/service fragments but no Guest/User creation controller matching these contracts. Do not route normal users through manager status APIs.

## Proposed authenticated booking and payment contracts

| Method / endpoint | Request | Response |
|---|---|---|
| POST `/api/bookings` | `{roomId,rentalMode,roomSlotId?,startDate,endDate,policyId,policyVersion,acknowledged}` | Booking DTO, 201. Mock records REQUESTED→AWAITING_PAYMENT. |
| GET `/api/bookings` | `status?,page,size` | Owned booking page. |
| GET `/api/bookings/{id}` | UUID | Owned booking; current expiry evaluated by server. |
| POST `/api/bookings/{id}/cancel` | No body | Updated booking, only REQUESTED/AWAITING_PAYMENT for self-service. |
| GET `/api/bookings/{id}/payments` | UUID | Payment DTO array, newest first; instructions/QR only from trusted BE. |
| POST `/api/bookings/{id}/payments` | Real provider contract: initiation details to be finalized. Mock accepts `{scenario}` only. | Payment DTO. Never accepts a USER-provided verified state. |

Booking DTO: `{id,requesterUserId,roomId,roomTitle,branchId,branchName,roomSlotId,slotLabel,rentalMode,startDate,endDate,holdAmount,paymentDueAt,temporaryHoldUntil,confirmedHoldUntil,status,policySnapshot,paymentEnvironment,createdAt,timeline,contractId}`.

Statuses: `REQUESTED,AWAITING_PAYMENT,CONFIRMED,CONVERTED,EXPIRED,CANCELLED,REJECTED`.

- ROOM must omit slot; SLOT must select a slot belonging to the room and available for the requested dates. UUIDs and ownership are server validated.
- Any valid start day is allowed; dates must be valid, start not past, end strictly later. Range is end-exclusive.
- Appointment is optional and independent.
- Hold amount comes from BranchPolicy and is snapshotted. FE submits policy ID/version and acknowledgment, not a trusted monetary amount. Stale policy produces 409.
- Booking Hold differs from PREPAID_RENT and SECURITY_DEPOSIT. Confirmation does not create TENANT rights or a contract.
- Lock/check resource and create hold atomically; recheck conflicts on write. Cancellation/expiry releases reservation. Whole room allocations conflict with any slot allocation in that room.
- Payment/temporary-hold deadlines are authoritative server times. CONFIRMED hold validity follows policy. Do not reactivate expired/cancelled requests on late payment.

Payment DTO: `{id,bookingId,amount,reference,status,environment,createdAt,verifiedAt,qrUrl,instructions}`. Actual enum: `PENDING,VERIFIED,FAILED,REVIEW`; expiry belongs to Booking rather than inventing a Payment EXPIRED enum. Environment `SANDBOX|PRODUCTION`.

### Demo behavior (never production endpoints)

- MSW storage is local to the browser session. It is an inspectable simulation, not a security boundary or an actual payment server.
- Scenarios: `success` verifies after ~6 seconds; `failure` fails after ~6 seconds; `pending` remains pending; `late` expires booking after ~3 seconds and produces REVIEW after ~10 seconds.
- Only the mock engine settles payments. Initiation and status polling do not accept `VERIFIED` from the user. Duplicate initiation reuses a pending payment.
- Demo has **no receiving bank credentials and no scannable financial QR**. Payment modal explains this explicitly. Real QR must be provided by a future trusted API; no third-party QR generator receives private banking data.
- `POST /__mock/api/appointments/{id}/demo-response` with `{status:CONFIRMED|REJECTED}` is a clearly marked demo tool, only for PENDING owned appointments. It never calls a production manager endpoint.
- `GET /__mock/api/demo/mailbox?email=...` exposes a local synthetic verification/reset link for testing. No real email is sent.

## Proposed self-service profile and recovery

| Method / endpoint | Auth | Request | Response |
|---|---|---|---|
| GET `/api/me` | Bearer | None | User DTO. |
| PATCH `/api/me` | Bearer | `{fullName,phone,dateOfBirth,gender}` | Updated User DTO. No role/status/email edits. |
| POST `/api/auth/forgot-password` | Public | `{email}` | Generic success whether account exists. |
| POST `/api/auth/reset-password` | Public token | `{token,password}` | `null`, consumes challenge and invalidates sessions. |

User DTO: `{id,email,fullName,roles:Role[],phone,dateOfBirth,gender,status}`; nullable optional personal fields. Roles `USER,TENANT,MANAGER,OWNER,ADMIN`; GUEST is never persisted. Current auth response returns only one selected role, mapped to a one-element array without inventing additional roles. A future self-service endpoint must return the complete authorized role set.

Email is read-only in the current profile. Birth date must be valid and in the past; gender optional. FE never calls ADMIN `/api/users` endpoints for profile editing. Recovery tokens expire, are single use, and are separate-purpose challenges.

## Remaining backend work / integration conditions

1. Public room authorization and enhanced list/detail/availability/types/branch/policy APIs.
2. Authenticated self-service appointment and booking controllers with transactions, ownership, conflicts and expiry jobs.
3. Trusted payment initiation/instructions, verification callbacks/admin authorization, late-payment review and idempotency. The mock `{scenario}` input must never become a production verification API.
4. `/api/me`, complete roles and account verification metadata, password recovery/reset; align nullable field/edit rules.
5. Emit actual notification events for booking/appointment/payment changes. In hybrid, mock events are local and are not written into BE's real notification store. Use fully mock mode to exercise simulated notification updates together.
6. Agree public image reference format, timezone semantics, policy effective-time rules and authentication cookie deployment settings.

The implementation follows the installed API/interface skill. Library setup references: [MSW browser integration](https://mswjs.io/guides/integrations/browser), [TanStack Query](https://tanstack.com/query/latest/docs/framework/react/reference/index), [Zustand create](https://zustand.docs.pmnd.rs/reference/apis/create). Installed MSW 3 uses `onUnhandledFrame`; types in the installed package were checked.
