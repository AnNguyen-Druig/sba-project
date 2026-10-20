# Manager implementation — completed after resume

Resumed by the user with **TIẾP TỤC TIẾN TRÌNH**. M01–M29 implementation and handoff completed on 2026-10-16. Current authoritative status: [manager-implementation-status.md](manager-implementation-status.md). API and demo guide are alongside that document.

Final validation: typecheck/lint/build PASS; 171 unit/API tests PASS; complete 76-test desktop/mobile E2E suite PASS, followed by 4 focused E2E tests PASS after the final changes. Existing Guest/Auth/User/Tenant preserved; no BE/database edits, branch change, commit or push.

**The text below is the historical pause checkpoint, not outstanding work. Do not restart that list.**

## Historical checkpoint

Paused explicitly on 2026-10-16. Resume only when the user asks, with the agreed phrase **TIẾP TỤC TIẾN TRÌNH**.

Authoritative request: `C:/Users/Admin/.codex/attachments/c65d56f1-0f0b-4330-8def-f9b88a373531/Pasted text.txt`. Continue the complete M01–M29 implementation and all requirements in that prompt. This checkpoint is not a completion report.

Workspace: `D:/SEM7/SBA301/LamNN15/project_ass/sba-project`; branch `feature/fe-rebuild`. Preserve all existing uncommitted Guest/Auth/User/Tenant code and the Manager changes. No Backend/database changes, branch switch, commit or push is authorized. Do not spawn agents unless separately authorized. Primary skill already read/applied: `minimalist-ui`; complementary API skill already read. Preserve TroPro teal, Be Vietnam Pro, Tailwind and existing UI primitives.

## Implemented so far, not fully verified

- New typed Manager contracts in `src/interfaces/manager.ts`, explicit manager provider and `src/services/manager.ts`.
- Shared persisted inventory (`src/mocks/inventory.ts`) used by existing MockDatabase. Rooms/types/slots are shared with public reads instead of a separate Manager universe.
- Manager fixtures: `manager@tropro.demo`, `owner@tropro.demo`, `manager-other@tropro.demo`, fictional password `TroPro@123`; primary Manager assigned branches fixture IDs 10 and 12 under owner 7001; branch 11 belongs to another owner/manager. `fixtureId(n)` is the existing UUID helper.
- `manager-engine.ts`: scope guards, authorized branch list, Room/RoomType/Slot mutations, shared read projections, occupancy, notification scanning, revoke helper, reset foundation.
- `manager-leasing.ts`: appointment transitions/conflicts; verified-hold booking processing; exactly-once booking conversion to DRAFT contract/ContractTenant; contract and individual terms; representative/prepaid activation checks.
- `manager-finance.ts`: shared invoice sources, integer totals, draft/issue guards, prepaid application without new cash, manual bank receipt reconciliation, reporting aggregates separating sandbox and Booking Hold.
- `manager-operations.ts`: catalog/configuration, service approvals, utility segments/finalization, exact allocation review, asset/warranty and maintenance records/cost decisions, private branch Zalo.
- `manager-handler.ts` integrated into existing MSW handler. Shared notification DTO adds optional branchId and existing notify method accepts it. Incoming events use the existing inbox.
- Existing Tenant engine accepts shared inventory/asset/Zalo callbacks and optional `workflowMode=managed`. Standalone Tenant demo remains automatic; Manager session sets managed workflows while sandbox payment processing remains trusted.
- Manager UI foundation: context/hooks, generic typed list/form/dialog/image components, ManagerLayout, dashboard/reports, property/slots/tenancy screens and catalog/furniture/warranty screens.

## Important unfinished work

1. Finish Manager UI: appointments/bookings/detail/conversion; contracts/editor/individual tenants; service requests; utility segment editor/details/finalization; allocations; invoice editor/issuance; payments/detail/reconciliation; debts; maintenance queue/detail/records/cost decisions; Zalo settings. M29 must reuse shared notifications/profile.
2. Wire Manager routes into `src/router.tsx` with lazy page modules. ManagerLayout exists but is not routed yet. Add actual-assignment-based workspace entry in SiteHeader/menu and Manager demo login filler. Existing `referenceLink` still needs Manager branch-scoped references, Tenant utility references and notification consumers must pass optional branchId.
3. Complete cross-role prepaid-before-activation flow. Leasing engine already requires VERIFIED first/last prepaid amounts; Tenant needs a scoped DRAFT-eligible prepaid sandbox initiation endpoint/UI in My Terms. Payment settlement must set verifiedAt for prepaid receipts. Do not bypass the activation guard or convert Booking Hold into rent.
4. Finish permission revocation UX/cache invalidation. Layout polls assignments and developer-only revocation tool exists; check stale caches, denied selection and reset behavior carefully. Owner compatibility must stay operational only, without an Owner workspace.
5. Fix Manager reset: current method recreates MockDatabase state and loses sessions/users. Preserve authenticated sessions, refresh identity and canonical users; restore coherent shared fixtures and demo bank receipt. Constructor currently seeds manual receipt 7900 only when paymentMeta lacks its ID; reset needs the same initialization. Tenant reset interactions also require review.
6. Add transaction rollback for Manager mutations across inventory/tenant/manager/activity state if validation or persistence fails; conversion already rolls back contract creation if tenancy validation fails, but general mutation atomicity still needs hardening.
7. Harden image validation and storage quota behavior for room/utility/maintenance uploads; reuse existing validated attachment helper and enforce at mock boundaries.
8. Ensure new rooms receive mandatory electricity/water room configurations. New ContractTenant service choices must reflect valid room configurations. RoomService effective-price updates preserve history; do not overwrite issued fees.
9. Extend filters in ManagerEngine.list: currently handles common statuses/room/type/price/capacity/date/month, but roomSlotId and other provided UI filters must actually work. Eligible-user lookup should include authorized appointment requesters where appropriate, without exposing unrelated global users. Add public room-detail rejection for nonpublic rooms.
10. Review financial invariants: overdue/debt periods, invoice source duplication, only approved completed Tenant repair costs, prepaid reservations and issuance revalidation, source snapshots, Provider transaction uniqueness and no self-verification. Owner-only issued-invoice changes must remain denied.
11. Add focused Manager domain/API tests covering the prompt's minimum 41 scenarios and cross-role Playwright tests desktop/mobile. Run all existing Guest/User/Tenant regression tests, typecheck, lint and build; inspect actual screenshots. Test overflow against configured device width AND each form's scrollWidth/clientWidth, not only window.innerWidth.
12. Write final `manager-api-contract.md`, `manager-implementation-status.md`, `manager-demo-guide.md` only after implementation, with actual results and gaps. Remove this checkpoint or mark it superseded when complete.

## Current validation state

The first focused typecheck found unused imports and a generic Zod cast issue; those specific findings were fixed. **No fresh complete typecheck/lint/build/test run has been performed since adding the latest Manager UI files.** Expect compile/lint issues to resolve; do not report Manager as finished or passing.

Likely immediate cleanup: `property-pages.tsx` has unused `z` and `ManagerRoom` imports; check other new files. `components.tsx` generic forms use React Hook Form plus Zod safeParse; required numeric blanks should become NaN rather than accidentally coercing to 0. Readonly disabled form selections must preserve intended defaults. `ActionDialog` currently validates reason >=5 but financial notes require >=10.

The latest existing application regression baseline before Manager work was 89 unit/API tests and 52 E2E tests passing, plus focused mobile form fixes. These are historical Tenant results, not Manager validation.

Shared inventory/occupancy now includes ACTIVE Tenant relationships in public availability. Existing Guest/User browser tests that create bookings in room fixture 100 may conflict with the genuinely occupied Tenant fixture. Preserve behavior and test intent while making data coherent; use a genuinely available room for positive booking scenarios (e.g. room fixture 109, checking seed holds/period first). Do not weaken conflict checks to make old fixtures pass. Isolated MockDatabase unit instances have an empty occupancyProvider until a ManagerEngine binds one.

## BE audit already performed

Current property CRUD controllers have no demonstrated assigned-Branch enforcement in inspected paths. Utilities have OWNER/MANAGER role annotations but inspected service implementations lack sufficient caller Branch ownership validation. ServiceCatalog still includes STAFF, inconsistent with approved roles. Furniture access uses `PendingSecurityMaintenanceAccessPolicy` and fails closed. Manager branch-assignment, contracts, billing/payments, maintenance self-service/reporting and Zalo APIs are missing/incomplete. Use explicit mock Manager provider; reuse real auth/notifications adapters, never fallback after real errors, never claim live BE writes succeeded.

Reviewed current contract-policies Q1–Q5, relevant all-feature/be_tasks/database docs, entities/controllers/enums, and previous frontend contracts. Do not invent partial-month rent, FIRST/LAST distribution, early refund or post-ACTIVE price changes. Manager invoice snapshots require approved amounts/schedules.

Browser skill/runtime was used in earlier Tenant work; integrated iab was unavailable and Edge Playwright fallback worked. On resume, follow browser skill and retry integrated availability before fallback. Dev server port 5173 belongs to the existing environment; preserve it.
