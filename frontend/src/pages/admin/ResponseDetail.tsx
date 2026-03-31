import { useEffect, useState } from 'react'
import { api } from '../../api'
import type { ResponseDetailDto } from '../../types'
import './Admin.css'

type Props = {
  responseId: number
  onBack: () => void
}

export function ResponseDetail({ responseId, onBack }: Props) {
  const [detail, setDetail] = useState<ResponseDetailDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    api.admin
      .getResponseDetail(responseId)
      .then(setDetail)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [responseId])

  if (loading) return <div className="admin-loading">상세를 불러오는 중...</div>
  if (error) return <div className="admin-error">오류: {error}</div>
  if (!detail) return null

  const formatDate = (s: string) => {
    try {
      return new Date(s).toLocaleString('ko-KR')
    } catch {
      return s
    }
  }

  return (
    <section className="admin-section">
      <div className="admin-detail-header">
        <button type="button" className="admin-btn-secondary" onClick={onBack}>
          ← 목록으로
        </button>
      </div>
      <h2>제출 상세</h2>
      <dl className="admin-detail-meta">
        <dt>설문</dt>
        <dd>{detail.surveyTitle}</dd>
        <dt>제출일시</dt>
        <dd>{formatDate(detail.submittedAt)}</dd>
        <dt>응답자 ID</dt>
        <dd>{detail.respondentId ?? '-'}</dd>
      </dl>
      <div className="admin-answers">
        <h3>답변</h3>
        {detail.answers.length === 0 ? (
          <p className="admin-muted">답변이 없습니다.</p>
        ) : (
          <ul>
            {detail.answers.map((a) => (
              <li key={a.questionId} className="admin-answer-item">
                <div className="admin-answer-question">{a.questionText}</div>
                <div className="admin-answer-value">{a.value || '-'}</div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}
