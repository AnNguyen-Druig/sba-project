import { z } from 'zod'
import { pageSchema, userSchema } from '@/interfaces/domain'
import {
  adminUserSchema,
  springUsersSchema,
  reviewSchema,
  adminRoleSchema,
  adminBranchSchema,
  auditSchema,
  adminSummarySchema,
  userCreateSchema,
} from '@/interfaces/admin'
import { ApiError, request, write } from './http'
import { provider } from './config'
const schemas = {
  verifications: reviewSchema,
  owners: adminUserSchema,
  branches: adminBranchSchema,
  activity: auditSchema,
}
const realVerificationSchema = z.object({
  userId: z.uuid(),
  fullName: z.string(),
  status: z.enum(['NOT_SUBMITTED', 'PENDING', 'APPROVED', 'REJECTED']),
  documentPublicId: z.string().nullable().optional(),
  documentResourceType: z.string().nullable().optional(),
  reviewedBy: z.uuid().nullable().optional(),
  reviewedAt: z.string().nullable().optional(),
  reviewReason: z.string().nullable().optional(),
})
// BE exposes only the review record; fields it does not return stay empty instead of being invented.
const toReview = (v: z.infer<typeof realVerificationSchema>): z.infer<typeof reviewSchema> => ({
  user: {
    id: v.userId,
    fullName: v.fullName,
    email: null,
    phone: null,
    status: 'ACTIVE',
    createdAt: '',
  },
  verification: {
    userId: v.userId,
    status: v.status,
    fullName: v.fullName,
    phone: null,
    contactVerified: false,
    submittedAt: null,
    reviewedAt: v.reviewedAt ?? null,
    reviewedBy: v.reviewedBy ?? null,
    reason: v.reviewReason ?? null,
    document: v.documentPublicId
      ? {
          name: v.documentPublicId,
          mime: v.documentResourceType || 'unknown',
          size: 0,
          resourceId: v.documentPublicId,
        }
      : null,
    eligible: v.status === 'APPROVED',
  },
  version: '0',
  reviewerName: null,
  history: [],
})
const realVerifications = async (params: Record<string, string>) => {
  const raw = await request(
    'adminVerifications',
    `/api/admin/owner-verifications?${new URLSearchParams({
      ...(params.status ? { status: params.status } : {}),
      page: String(Number(params.page || 0) + 1),
      size: params.size || '8',
    })}`,
    pageSchema(realVerificationSchema),
  )
  return { ...raw, content: raw.content.map(toReview) }
}
const realReview = async (id: string) => {
  for (const status of ['PENDING', 'APPROVED', 'REJECTED']) {
    const page = await realVerifications({ status, page: '0', size: '100' })
    const found = page.content.find((r) => r.user.id === id)
    if (found) return found
  }
  throw new ApiError('Không tìm thấy hồ sơ xác minh.', 404)
}
export type AdminResource = keyof typeof schemas
export const adminApi = {
  entitlements: () =>
    provider('admin') === 'real' || provider('auth') === 'real'
      ? Promise.resolve(null)
      : request(
          'admin',
          '/api/admin/entitlements',
          z.object({ user: userSchema, permissions: z.array(z.string()) }),
        ),
  users: async (params: Record<string, string>) => {
    const supported =
      provider('adminUsers') === 'real'
        ? { page: params.page || '0', size: params.size || '20' }
        : params
    const raw = await request(
      'adminUsers',
      `/api/users?${new URLSearchParams(supported)}`,
      springUsersSchema,
    )
    return { ...raw, number: raw.number + 1 }
  },
  user: (id: string) =>
    request('adminUsers', `/api/users/${encodeURIComponent(id)}`, adminUserSchema),
  create: (value: unknown) => {
    const data = userCreateSchema.parse(value)
    return request(
      'adminUsers',
      '/api/users',
      adminUserSchema,
      write(
        'POST',
        provider('adminUsers') === 'real'
          ? {
              ...data,
              email: data.email || null,
              phone: data.phone || null,
              dateOfBirth: data.dateOfBirth || null,
              gender: data.gender || null,
            }
          : data,
      ),
    )
  },
  status: (id: string, active: boolean, version?: number, reason?: string) =>
    request(
      'adminUsers',
      `/api/users/${encodeURIComponent(id)}/active-status`,
      adminUserSchema,
      write('PATCH', provider('adminUsers') === 'real' ? { active } : { active, version, reason }),
    ),
  summary: () => request('admin', '/api/admin/dashboard', adminSummarySchema),
  list: <K extends AdminResource>(resource: K, params: Record<string, string>) =>
    (resource === 'verifications' && provider('adminVerifications') === 'real'
      ? realVerifications(params)
      : request(
          'admin',
          `/api/admin/${resource === 'verifications' ? 'owner-verifications' : resource}?${new URLSearchParams(params)}`,
          pageSchema(schemas[resource]),
        )) as Promise<import('@/interfaces/domain').Page<z.infer<(typeof schemas)[K]>>>,
  review: (id: string) =>
    provider('adminVerifications') === 'real'
      ? realReview(id)
      : request('admin', `/api/admin/owner-verifications/${encodeURIComponent(id)}`, reviewSchema),
  decide: async (id: string, value: unknown) => {
    if (provider('adminVerifications') === 'real') {
      const { decision, reason } = z
        .object({ decision: z.enum(['APPROVED', 'REJECTED']), reason: z.string() })
        .parse(value)
      return toReview(
        await request(
          'adminVerifications',
          `/api/admin/owner-verifications/${encodeURIComponent(id)}/${decision === 'APPROVED' ? 'approve' : 'reject'}`,
          realVerificationSchema,
          write('POST', { reason }),
        ),
      )
    }
    return request(
      'admin',
      `/api/admin/owner-verifications/${encodeURIComponent(id)}/decision`,
      reviewSchema,
      write('POST', value),
    )
  },
  document: (id: string) =>
    request(
      'admin',
      `/api/admin/owner-verifications/${encodeURIComponent(id)}/document`,
      z.object({ mime: z.literal('image/svg+xml'), content: z.string(), expiresAt: z.string() }),
    ),
  roles: () => request('admin', '/api/admin/roles', z.array(adminRoleSchema)),
  event: (id: string) =>
    request('admin', `/api/admin/activity/${encodeURIComponent(id)}`, auditSchema),
  reset: () => request('admin', '/api/admin/demo/reset', z.null(), write('POST', {})),
}
