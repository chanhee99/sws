const API_BASE = import.meta.env.VITE_API_URL || ''

/** 아임웹에 로그인한 운영자 식별자 — 관리자 API(/api/admin)에만 붙입니다. */
function imwebAdminMemberHeaders(): Record<string, string> {
  const id =
    (import.meta.env.VITE_IMWEB_ADMIN_MEMBER_ID as string | undefined) ||
    (typeof localStorage !== 'undefined' ? localStorage.getItem('imweb_admin_member_id') : null)
  if (!id) return {}
  return { 'X-Imweb-Member-Id': id }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const isAdmin = path.startsWith('/api/admin')
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(isAdmin ? imwebAdminMemberHeaders() : {}),
      ...options?.headers,
    },
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error((err as { message?: string }).message || res.statusText)
  }
  return res.json()
}

export const api = {
  getSurveys: () => request<import('./types').SurveyDto[]>('/api/surveys'),
  getSurvey: (id: number) => request<import('./types').SurveyDto>(`/api/surveys/${id}`),
  submitSurvey: (body: import('./types').SubmitSurveyRequest) =>
    request<import('./types').SubmitSurveyResponseDto>('/api/surveys/submit', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  admin: {
    getResponses: (params?: { surveyId?: number; page?: number; size?: number }) => {
      const sp = new URLSearchParams()
      if (params?.surveyId != null) sp.set('surveyId', String(params.surveyId))
      if (params?.page != null) sp.set('page', String(params.page))
      if (params?.size != null) sp.set('size', String(params.size))
      const q = sp.toString()
      return request<import('./types').PageDto<import('./types').ResponseListItemDto>>(
        `/api/admin/responses${q ? `?${q}` : ''}`
      )
    },
    getResponseDetail: (id: number) =>
      request<import('./types').ResponseDetailDto>(`/api/admin/responses/${id}`),
    getStatistics: () => request<import('./types').StatisticsDto[]>('/api/admin/statistics'),
    getStatisticsBySurvey: (surveyId: number) =>
      request<import('./types').StatisticsDto>(`/api/admin/statistics/${surveyId}`),
    listSurveys: () => request<import('./types').SurveyDto[]>('/api/admin/surveys'),
    createSurvey: (body: import('./types').UpsertSurveyRequest) =>
      request<import('./types').SurveyDto>('/api/admin/surveys', {
        method: 'POST',
        body: JSON.stringify(body),
      }),
    updateSurvey: (id: number, body: import('./types').UpsertSurveyRequest) =>
      request<import('./types').SurveyDto>(`/api/admin/surveys/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    setSurveyActive: (id: number, active: boolean) =>
      request<import('./types').SurveyDto>(`/api/admin/surveys/${id}/active?active=${active}`, {
        method: 'PATCH',
      }),
    getResultConfig: (surveyId: number) =>
      request<import('./types').ResultConfigDto>(`/api/admin/surveys/${surveyId}/result-config`),
    upsertResultConfig: (surveyId: number, body: import('./types').ResultConfigUpsertRequest) =>
      request<void>(`/api/admin/surveys/${surveyId}/result-config`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),

    imweb: {
      getMember: (id: string, type: 'uid' | 'member_code' = 'uid') =>
        request<any>(`/api/admin/imweb/members/${encodeURIComponent(id)}?type=${encodeURIComponent(type)}`),
      listProducts: (offset = 1, limit = 25, version = 'latest') =>
        request<any>(`/api/admin/imweb/products?offset=${offset}&limit=${limit}&version=${encodeURIComponent(version)}`),
      listCoupons: () => request<any>(`/api/admin/imweb/coupons`),
      getCoupon: (couponCode: string) =>
        request<any>(`/api/admin/imweb/coupons/${encodeURIComponent(couponCode)}`),
      getIssuedCoupon: (issuedCouponCode: string) =>
        request<any>(`/api/admin/imweb/issue-coupons/${encodeURIComponent(issuedCouponCode)}`),
      issueCoupon: (body: { memberId: string; couponCode: string }) =>
        request<any>(`/api/admin/imweb/coupons/issue`, {
          method: 'POST',
          body: JSON.stringify(body),
        }),
    },
  },
}
