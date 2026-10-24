import { z } from 'zod'
import { request, write } from './http'
import { provider } from './config'
import * as maintenance from './tenant-maintenance'
import { utilitiesService } from './utilities'
import type {
  ContractServiceRegistrationInput,
  CreateCostAllocationProposalInput,
} from '@/interfaces/utilities'
import { pageSchema } from '@/interfaces/domain'
import {
  tenancySchema,
  tenantRoomSchema,
  invoiceSchema,
  tenantPaymentSchema,
  serviceSchema,
  chargeSchema,
  proposalSchema,
  reportSchema,
  type ReportInput,
  type Tenancy,
} from '@/interfaces/tenant'

// BE TenantResponse (GET /api/tenants, /api/tenants/me): display name only, no contact/profile data.
export const tenantResponseSchema = z.object({
  contractTenantId: z.uuid(),
  contractId: z.uuid(),
  branchId: z.uuid(),
  roomId: z.uuid(),
  roomSlotId: z.uuid().nullable(),
  tenantUserId: z.uuid(),
  fullName: z.string(),
  role: tenancySchema.shape.role,
  status: tenancySchema.shape.status,
  startDate: z.iso.date(),
  endDate: z.iso.date(),
  actualLeaveDate: z.iso.date().nullable(),
  branchName: z.string(),
  roomCode: z.string(),
  slotName: z.string().nullable(),
  contractCode: z.string(),
  contractStatus: tenancySchema.shape.contractStatus,
  rentalMode: tenancySchema.shape.rentalMode,
  monthlyRent: z.number(),
  firstMonthRent: z.number(),
  lastMonthRent: z.number(),
  prepaidFirstRequired: z.number(),
  prepaidLastRequired: z.number(),
  terms: z.string(),
  contractTerms: z.string(),
})
export type TenantResponse = z.infer<typeof tenantResponseSchema>
export const tenantPageSchema = pageSchema(tenantResponseSchema)
export const toTenancy = (t: TenantResponse): Tenancy => ({
  id: t.contractTenantId,
  userId: t.tenantUserId,
  contractId: t.contractId,
  branchId: t.branchId,
  roomId: t.roomId,
  roomSlotId: t.roomSlotId,
  branchName: t.branchName,
  roomName: t.roomCode,
  slotName: t.slotName,
  rentalMode: t.rentalMode,
  status: t.status,
  contractStatus: t.contractStatus,
  code: t.contractCode,
  role: t.role,
  startDate: t.startDate,
  endDate: t.endDate,
  actualLeaveDate: t.actualLeaveDate,
  monthlyRent: t.monthlyRent,
  firstMonthRent: t.firstMonthRent,
  lastMonthRent: t.lastMonthRent,
  prepaidFirstRequired: t.prepaidFirstRequired,
  prepaidLastRequired: t.prepaidLastRequired,
  terms: t.terms,
  sharedTerms: t.contractTerms,
  // BE has no contract document URL yet.
  document: null,
})

const path = (id: string, resource: string) =>
  `/api/tenant/tenancies/${encodeURIComponent(id)}/${resource}`
export const tenantApi = {
  manualPay: (id: string, invoiceId: string, value: unknown) =>
    request(
      'tenant',
      path(id, `invoices/${encodeURIComponent(invoiceId)}/manual-payments`),
      tenantPaymentSchema,
      write('POST', value),
    ),
  prepay: (id: string, value: unknown) =>
    request('tenant', path(id, 'prepaid-payments'), tenantPaymentSchema, write('POST', value)),
  tenancies: async () => {
    if (provider('tenancies') === 'mock')
      return request('tenancies', '/api/tenant/tenancies', z.array(tenancySchema))
    const page = await request('tenancies', '/api/tenants/me?page=1&size=100', tenantPageSchema)
    return page.content.map(toTenancy)
  },
  room: (id: string) => request('tenant', path(id, 'room'), tenantRoomSchema),
  contract: (id: string, contractId: string) =>
    request('tenant', path(id, `contracts/${encodeURIComponent(contractId)}`), tenancySchema),
  invoices: (id: string) => request('tenant', path(id, 'invoices'), z.array(invoiceSchema)),
  invoice: (id: string, invoiceId: string) =>
    request('tenant', path(id, `invoices/${encodeURIComponent(invoiceId)}`), invoiceSchema),
  payments: (id: string) => request('tenant', path(id, 'payments'), z.array(tenantPaymentSchema)),
  payment: (id: string, paymentId: string) =>
    request('tenant', path(id, `payments/${encodeURIComponent(paymentId)}`), tenantPaymentSchema),
  pay: (id: string, invoiceId: string, value: unknown) =>
    request(
      'tenant',
      path(id, `invoices/${encodeURIComponent(invoiceId)}/payments`),
      tenantPaymentSchema,
      write('POST', value),
    ),
  services: (id: string) => request('tenant', path(id, 'services'), z.array(serviceSchema)),
  registerService: async (id: string, value: unknown) => {
    if (provider('utilities') === 'real') {
      const item = await utilitiesService.contractServices.register(
        value as ContractServiceRegistrationInput,
      )
      return {
        id: item.id,
        name: item.serviceName,
        mandatory: false,
        unit: '',
        unitPrice: item.agreedUnitPrice,
        subscriptionId: item.id,
        status: item.status,
        startDate: item.startDate,
        endDate: item.endDate,
        note: item.reason || '',
        history: [],
      }
    }
    return request('tenant', path(id, 'services'), serviceSchema, write('POST', value))
  },
  cancelService: async (id: string, serviceId: string) => {
    if (provider('utilities') === 'real') {
      const item = await utilitiesService.contractServices.cancel(serviceId)
      return {
        id: item.id,
        name: item.serviceName,
        mandatory: false,
        unit: '',
        unitPrice: item.agreedUnitPrice,
        subscriptionId: item.id,
        status: item.status,
        startDate: item.startDate,
        endDate: item.endDate,
        note: item.reason || '',
        history: [],
      }
    }
    return request(
      'tenant',
      path(id, `services/${encodeURIComponent(serviceId)}/cancel`),
      serviceSchema,
      write('POST'),
    )
  },
  utilities: (id: string) => request('tenant', path(id, 'utilities'), z.array(chargeSchema)),
  allocations: (id: string) => request('tenant', path(id, 'allocations'), z.array(proposalSchema)),
  propose: async (id: string, value: unknown) => {
    if (provider('utilities') === 'real') {
      const p = await utilitiesService.costAllocation.propose(
        value as CreateCostAllocationProposalInput,
      )
      return {
        id: p.id,
        contractTenantId: p.proposedBy,
        chargeId: p.roomServiceChargeId,
        method: p.method === 'RESIDENT_DAYS' ? 'RESIDENT_DAYS' : ('APPROVED_CUSTOM' as const),
        reason: p.reason || '',
        status: (p.status === 'APPROVED'
          ? 'APPROVED'
          : p.status === 'REJECTED'
            ? 'REJECTED'
            : 'PENDING') as 'PENDING' | 'APPROVED' | 'REJECTED',
        createdAt: p.createdAt || new Date().toISOString(),
        decidedAt: p.processedAt || null,
        decision: p.status === 'PENDING' ? null : p.status,
        shares: [],
      }
    }
    return request('tenant', path(id, 'allocations'), proposalSchema, write('POST', value))
  },
  // Real maintenance APIs are room-scoped; the mock ignores roomId.
  real: {
    maintenance: () => provider('tenantMaintenance') === 'real',
  },
  reportOptions: (roomId: string) => maintenance.reportOptions(roomId),
  rateReport: (roomId: string, reportId: string, rating: number, comment: string) =>
    maintenance.rateReport(roomId, reportId, rating, comment),
  reports: (id: string, roomId: string) =>
    provider('tenantMaintenance') === 'real'
      ? maintenance.myReports(roomId)
      : request('tenant', path(id, 'maintenance'), z.array(reportSchema)),
  report: (id: string, reportId: string, roomId: string) =>
    provider('tenantMaintenance') === 'real'
      ? maintenance.myReport(roomId, reportId)
      : request('tenant', path(id, `maintenance/${encodeURIComponent(reportId)}`), reportSchema),
  createReport: (id: string, value: ReportInput, roomId: string) =>
    provider('tenantMaintenance') === 'real'
      ? maintenance.createReport(roomId, value)
      : request('tenant', path(id, 'maintenance'), reportSchema, write('POST', value)),
  cancelReport: (id: string, reportId: string) =>
    request(
      'tenant',
      path(id, `maintenance/${encodeURIComponent(reportId)}/cancel`),
      reportSchema,
      write('POST'),
    ),
  reset: () => request('tenant', '/api/tenant/demo/reset', z.null(), write('POST')),
}
