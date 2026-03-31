import { useState } from 'react'
import { api } from '../../api'

export function ImwebManager() {
  const [memberId, setMemberId] = useState('')
  const [memberType, setMemberType] = useState<'uid' | 'member_code'>('uid')
  const [memberLoading, setMemberLoading] = useState(false)
  const [memberError, setMemberError] = useState<string | null>(null)
  const [memberResult, setMemberResult] = useState<any>(null)

  const [productsLoading, setProductsLoading] = useState(false)
  const [productsError, setProductsError] = useState<string | null>(null)
  const [products, setProducts] = useState<any[]>([])

  const [couponCode, setCouponCode] = useState('')
  const [couponLoading, setCouponLoading] = useState(false)
  const [couponError, setCouponError] = useState<string | null>(null)
  const [couponResult, setCouponResult] = useState<any>(null)

  const [issuedCouponCode, setIssuedCouponCode] = useState('')
  const [issuedLoading, setIssuedLoading] = useState(false)
  const [issuedError, setIssuedError] = useState<string | null>(null)
  const [issuedResult, setIssuedResult] = useState<any>(null)

  const [issueLoading, setIssueLoading] = useState(false)
  const [issueError, setIssueError] = useState<string | null>(null)
  const [issueResult, setIssueResult] = useState<any>(null)

  const loadMember = async () => {
    if (!memberId.trim()) return
    setMemberLoading(true)
    setMemberError(null)
    setMemberResult(null)
    try {
      const res = await api.admin.imweb.getMember(memberId.trim(), memberType)
      setMemberResult(res)
    } catch (e) {
      setMemberError(e instanceof Error ? e.message : '회원 조회 실패')
    } finally {
      setMemberLoading(false)
    }
  }

  const loadProducts = async () => {
    setProductsLoading(true)
    setProductsError(null)
    try {
      const res = await api.admin.imweb.listProducts(1, 25, 'latest')
      const list = res?.data?.list ?? res?.data ?? res?.list ?? []
      setProducts(Array.isArray(list) ? list : [])
    } catch (e) {
      setProductsError(e instanceof Error ? e.message : '상품 조회 실패')
    } finally {
      setProductsLoading(false)
    }
  }

  const loadCoupon = async () => {
    if (!couponCode.trim()) return
    setCouponLoading(true)
    setCouponError(null)
    setCouponResult(null)
    try {
      const res = await api.admin.imweb.getCoupon(couponCode.trim())
      setCouponResult(res)
    } catch (e) {
      setCouponError(e instanceof Error ? e.message : '쿠폰 조회 실패')
    } finally {
      setCouponLoading(false)
    }
  }

  const loadIssuedCoupon = async () => {
    if (!issuedCouponCode.trim()) return
    setIssuedLoading(true)
    setIssuedError(null)
    setIssuedResult(null)
    try {
      const res = await api.admin.imweb.getIssuedCoupon(issuedCouponCode.trim())
      setIssuedResult(res)
    } catch (e) {
      setIssuedError(e instanceof Error ? e.message : '발행쿠폰 조회 실패')
    } finally {
      setIssuedLoading(false)
    }
  }

  const issueCoupon = async () => {
    setIssueLoading(true)
    setIssueError(null)
    setIssueResult(null)
    try {
      const res = await api.admin.imweb.issueCoupon({ memberId: memberId.trim(), couponCode: couponCode.trim() })
      setIssueResult(res)
    } catch (e) {
      setIssueError(e instanceof Error ? e.message : '쿠폰 발급 요청 실패')
    } finally {
      setIssueLoading(false)
    }
  }

  return (
    <section className="admin-section">
      <h2>아임웹 연동</h2>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
        <div className="admin-form" style={{ gap: '0.75rem' }}>
          <h3 style={{ margin: 0 }}>1) 회원 정보 조회</h3>
          <div className="admin-form-row" style={{ gridTemplateColumns: '140px 1fr', gap: '0.75rem' }}>
            <span>회원 식별자</span>
            <input
              className="admin-input"
              value={memberId}
              onChange={(e) => setMemberId(e.target.value)}
              placeholder="respondentId (uid/member_code)"
            />
          </div>
          <div className="admin-form-row" style={{ gridTemplateColumns: '140px 1fr', gap: '0.75rem' }}>
            <span>식별 타입</span>
            <select className="admin-input" value={memberType} onChange={(e) => setMemberType(e.target.value as any)}>
              <option value="uid">uid</option>
              <option value="member_code">member_code</option>
            </select>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
            <button type="button" className="admin-btn-sm" disabled={memberLoading} onClick={loadMember}>
              {memberLoading ? '조회중...' : '회원 조회'}
            </button>
          </div>
          {memberError && <div className="admin-error">{memberError}</div>}
          {memberResult && (
            <pre style={{ whiteSpace: 'pre-wrap', background: 'var(--surface)', border: '1px solid var(--border)', padding: '12px', borderRadius: '12px' }}>
              {JSON.stringify(memberResult, null, 2)}
            </pre>
          )}
        </div>

        <div className="admin-form" style={{ gap: '0.75rem' }}>
          <h3 style={{ margin: 0 }}>2) 상품 정보 조회</h3>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button type="button" className="admin-btn-sm" disabled={productsLoading} onClick={loadProducts}>
              {productsLoading ? '불러오는 중...' : '상품 목록 불러오기'}
            </button>
          </div>
          {productsError && <div className="admin-error">{productsError}</div>}
          {products.length > 0 && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0,1fr))', gap: '0.75rem' }}>
              {products.slice(0, 10).map((p, idx) => (
                <div key={idx} style={{ border: '1px solid var(--border)', borderRadius: '12px', padding: '12px' }}>
                  <div style={{ fontWeight: 700, marginBottom: 6 }}>{p.name ?? '-'}</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>custom_prod_code: {p.custom_prod_code ?? '-'}</div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="admin-form" style={{ gap: '0.75rem' }}>
          <h3 style={{ margin: 0 }}>3) 쿠폰 조회 / 발행 쿠폰 조회</h3>
          <div className="admin-form-row" style={{ gridTemplateColumns: '140px 1fr', gap: '0.75rem' }}>
            <span>쿠폰 코드</span>
            <input className="admin-input" value={couponCode} onChange={(e) => setCouponCode(e.target.value)} placeholder="coupon_code" />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button type="button" className="admin-btn-sm" disabled={couponLoading} onClick={loadCoupon}>
              {couponLoading ? '쿠폰 조회중...' : '쿠폰 조회'}
            </button>
          </div>
          {couponError && <div className="admin-error">{couponError}</div>}
          {couponResult && (
            <pre style={{ whiteSpace: 'pre-wrap', background: 'var(--surface)', border: '1px solid var(--border)', padding: '12px', borderRadius: '12px', maxHeight: 280, overflow: 'auto' }}>
              {JSON.stringify(couponResult, null, 2)}
            </pre>
          )}

          <div className="admin-form-row" style={{ gridTemplateColumns: '140px 1fr', gap: '0.75rem', marginTop: '0.5rem' }}>
            <span>발행쿠폰 코드</span>
            <input
              className="admin-input"
              value={issuedCouponCode}
              onChange={(e) => setIssuedCouponCode(e.target.value)}
              placeholder="issue-coupons의 발행코드"
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button type="button" className="admin-btn-sm" disabled={issuedLoading} onClick={loadIssuedCoupon}>
              {issuedLoading ? '발행쿠폰 조회중...' : '발행쿠폰 조회'}
            </button>
          </div>
          {issuedError && <div className="admin-error">{issuedError}</div>}
          {issuedResult && (
            <pre style={{ whiteSpace: 'pre-wrap', background: 'var(--surface)', border: '1px solid var(--border)', padding: '12px', borderRadius: '12px', maxHeight: 280, overflow: 'auto' }}>
              {JSON.stringify(issuedResult, null, 2)}
            </pre>
          )}
        </div>

        <div className="admin-form" style={{ gap: '0.75rem' }}>
          <h3 style={{ margin: 0 }}>4) 쿠폰 “발급”(issued) 요청</h3>
          <div className="admin-muted">
            서버에서 env의 `IMWEB_COUPON_ISSUE_PATH`/필드명으로 POST 요청을 보냅니다. 실제 엔드포인트가 다르면 env 값을 수정하세요.
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              type="button"
              className="admin-btn-sm"
              disabled={issueLoading || !memberId.trim() || !couponCode.trim()}
              onClick={issueCoupon}
            >
              {issueLoading ? '요청중...' : '쿠폰 발급 요청 보내기'}
            </button>
          </div>
          {issueError && <div className="admin-error">{issueError}</div>}
          {issueResult && (
            <pre style={{ whiteSpace: 'pre-wrap', background: 'var(--surface)', border: '1px solid var(--border)', padding: '12px', borderRadius: '12px', maxHeight: 280, overflow: 'auto' }}>
              {JSON.stringify(issueResult, null, 2)}
            </pre>
          )}
        </div>
      </div>
    </section>
  )
}

