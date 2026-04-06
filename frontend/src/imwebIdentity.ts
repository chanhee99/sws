export type ImwebIdentity = {
  respondentId?: string
  adminMemberId?: string
  isLoggedIn: boolean
  isAdmin: boolean
}

function safeGetLocalStorage(key: string): string | undefined {
  try {
    if (typeof localStorage === 'undefined') return undefined
    const v = localStorage.getItem(key)
    return v == null || v.trim() === '' ? undefined : v.trim()
  } catch {
    return undefined
  }
}

function safeSetLocalStorage(key: string, value: string | undefined) {
  try {
    if (typeof localStorage === 'undefined') return
    if (!value) localStorage.removeItem(key)
    else localStorage.setItem(key, value)
  } catch {
    // ignore
  }
}

function fromSearch(search: string): { respondentId?: string; adminMemberId?: string } {
  try {
    const sp = new URLSearchParams(search)
    const respondentId = (sp.get('respondentId') || sp.get('memberId') || undefined)?.trim()
    const adminMemberId = (sp.get('adminMemberId') || undefined)?.trim()
    return {
      respondentId: respondentId || undefined,
      adminMemberId: adminMemberId || undefined,
    }
  } catch {
    return {}
  }
}

export function readImwebIdentity(search: string): ImwebIdentity {
  const fromQ = fromSearch(search)

  const respondentId =
    fromQ.respondentId ||
    safeGetLocalStorage('imweb_respondent_id') ||
    safeGetLocalStorage('imweb_member_id') ||
    undefined

  // 관리자 식별자는 보안상 “명시적으로” 저장된 경우에만 사용합니다.
  // (백엔드에서도 allowlist로 한 번 더 검증하므로, 프론트의 isAdmin은 UX 차원의 숨김/차단 역할)
  const adminMemberId =
    fromQ.adminMemberId ||
    (import.meta.env.VITE_IMWEB_ADMIN_MEMBER_ID as string | undefined) ||
    safeGetLocalStorage('imweb_admin_member_id') ||
    undefined

  return {
    respondentId,
    adminMemberId,
    isLoggedIn: Boolean(respondentId),
    isAdmin: Boolean(adminMemberId),
  }
}

export function setImwebAdminMemberId(id: string | undefined) {
  safeSetLocalStorage('imweb_admin_member_id', id?.trim() ? id.trim() : undefined)
}

