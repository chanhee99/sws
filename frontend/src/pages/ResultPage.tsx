import { useLocation, useNavigate } from 'react-router-dom'
import type { ResultDto } from '../types'
import './ResultPage.css'

type ResultState = {
  result?: ResultDto
}

export function ResultPage() {
  const nav = useNavigate()
  const location = useLocation()
  const state = (location.state ?? {}) as ResultState
  const result = state.result

  if (!result) {
    return (
      <div className="result-wrap">
        <header className="result-header">
          <h1>결과</h1>
          <p className="result-sub">결과 데이터를 불러오지 못했습니다.</p>
        </header>
        <section className="result-card">
          <div className="result-content" style={{ gridColumn: '2 / span 1' }}>
            <button type="button" className="btn-primary" onClick={() => nav('/')}>
              설문으로 돌아가기
            </button>
          </div>
        </section>
      </div>
    )
  }

  return (
    <div className="result-wrap">
      <header className="result-header">
        <h1>결과</h1>
        <p className="result-sub">설문 응답을 바탕으로 맞춤 결과를 안내합니다.</p>
      </header>

      <section className="result-card">
        <div className="result-number">①</div>
        <div className="result-content">
          <h2 className="result-title">
            당신의 피부 유형은 <span className="accent">{result.skinType}</span>입니다
          </h2>
        </div>
      </section>

      <section className="result-card">
        <div className="result-number">②</div>
        <div className="result-content">
          <h3 className="result-section-title">현재 상태 설명</h3>
          <p className="result-text">{result.statusText}</p>
        </div>
      </section>

      <section className="result-card">
        <div className="result-number">③</div>
        <div className="result-content">
          <h3 className="result-section-title">{result.productSetTitle}</h3>
          <div className="product-grid">
            {result.products.map((p) => (
              <div key={p.name} className="product-card">
                <div className="product-name">{p.name}</div>
                <div className="product-desc">{p.desc}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="result-card">
        <div className="result-number">④</div>
        <div className="result-content">
          <h3 className="result-section-title">쿠폰</h3>
          <button
            type="button"
            className="coupon-btn"
            onClick={() => {
              if (result.couponUrl) window.location.href = result.couponUrl
              else nav('/')
            }}
          >
            {result.couponButtonText}
          </button>
          <div className="result-hint">버튼 클릭 시 다음 단계(상품 상세/구매/아임웹 페이지)로 연결할 수 있어요.</div>
        </div>
      </section>
    </div>
  )
}

