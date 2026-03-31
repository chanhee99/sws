import { useState, useEffect } from 'react'
import { api } from '../../api'
import type { ResponseListItemDto, SurveyDto } from '../../types'
import './Admin.css'

type Props = {
  onSelectResponse: (id: number) => void
}

export function ResponseList({ onSelectResponse }: Props) {
  const [page, setPage] = useState(0)
  const [surveyId, setSurveyId] = useState<number | ''>('')
  const [surveys, setSurveys] = useState<SurveyDto[]>([])
  const [data, setData] = useState<{ content: ResponseListItemDto[]; totalPages: number; totalElements: number } | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const size = 20

  useEffect(() => {
    api.getSurveys().then(setSurveys).catch(() => setSurveys([]))
  }, [])

  useEffect(() => {
    setLoading(true)
    api.admin
      .getResponses({
        surveyId: surveyId === '' ? undefined : surveyId,
        page,
        size,
      })
      .then((res) => {
        setData({
          content: res.content,
          totalPages: res.totalPages,
          totalElements: res.totalElements,
        })
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [surveyId, page])

  if (loading && !data) return <div className="admin-loading">제출 목록을 불러오는 중...</div>
  if (error) return <div className="admin-error">오류: {error}</div>
  if (!data) return null

  const formatDate = (s: string) => {
    try {
      const d = new Date(s)
      return d.toLocaleString('ko-KR')
    } catch {
      return s
    }
  }

  return (
    <section className="admin-section">
      <h2>제출 목록</h2>
      <div className="admin-filters">
        <label>
          설문
          <select
            value={surveyId === '' ? '' : surveyId}
            onChange={(e) => {
              setSurveyId(e.target.value === '' ? '' : Number(e.target.value))
              setPage(0)
            }}
          >
            <option value="">전체</option>
            {surveys.map((s) => (
              <option key={s.id} value={s.id}>
                {s.title}
              </option>
            ))}
          </select>
        </label>
      </div>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>제출일시</th>
              <th>설문</th>
              <th>응답자 ID</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.content.length === 0 ? (
              <tr>
                <td colSpan={4}>제출된 설문이 없습니다.</td>
              </tr>
            ) : (
              data.content.map((row) => (
                <tr key={row.id}>
                  <td>{formatDate(row.submittedAt)}</td>
                  <td>{row.surveyTitle}</td>
                  <td>{row.respondentId ?? '-'}</td>
                  <td>
                    <button
                      type="button"
                      className="admin-btn-sm"
                      onClick={() => onSelectResponse(row.id)}
                    >
                      상세
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {data.totalPages > 1 && (
        <div className="admin-pagination">
          <button
            type="button"
            disabled={page <= 0}
            onClick={() => setPage((p) => p - 1)}
          >
            이전
          </button>
          <span>
            {page + 1} / {data.totalPages} (총 {data.totalElements}건)
          </span>
          <button
            type="button"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            다음
          </button>
        </div>
      )}
    </section>
  )
}
