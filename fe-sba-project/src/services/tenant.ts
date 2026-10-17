import { z } from 'zod'
import { request, write } from './http'
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
} from '@/interfaces/tenant'

const path = (id: string, resource: string) =>
  `/api/tenant/tenancies/${encodeURIComponent(id)}/${resource}`
export const tenantApi = {
  manualPay: (id: string, invoiceId: string, value: unknown) => request('tenant', path(id, `invoices/${encodeURIComponent(invoiceId)}/manual-payments`), tenantPaymentSchema, write('POST', value)),
  prepay: (id: string, value: unknown) =>
    request('tenant', path(id, 'prepaid-payments'), tenantPaymentSchema, write('POST', value)),
  tenancies: () => request('tenant', '/api/tenant/tenancies', z.array(tenancySchema)),
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
  registerService: (id: string, value: unknown) =>
    request('tenant', path(id, 'services'), serviceSchema, write('POST', value)),
  cancelService: (id: string, serviceId: string) =>
    request(
      'tenant',
      path(id, `services/${encodeURIComponent(serviceId)}/cancel`),
      serviceSchema,
      write('POST'),
    ),
  utilities: (id: string) => request('tenant', path(id, 'utilities'), z.array(chargeSchema)),
  allocations: (id: string) => request('tenant', path(id, 'allocations'), z.array(proposalSchema)),
  propose: (id: string, value: unknown) =>
    request('tenant', path(id, 'allocations'), proposalSchema, write('POST', value)),
  reports: (id: string) => request('tenant', path(id, 'maintenance'), z.array(reportSchema)),
  report: (id: string, reportId: string) =>
    request('tenant', path(id, `maintenance/${encodeURIComponent(reportId)}`), reportSchema),
  createReport: (id: string, value: ReportInput) =>
    request('tenant', path(id, 'maintenance'), reportSchema, write('POST', value)),
  cancelReport: (id: string, reportId: string) =>
    request(
      'tenant',
      path(id, `maintenance/${encodeURIComponent(reportId)}/cancel`),
      reportSchema,
      write('POST'),
    ),
  reset: () => request('tenant', '/api/tenant/demo/reset', z.null(), write('POST')),
}
