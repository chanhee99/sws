import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { api } from '../api'
import type { SurveyDto } from '../types'
import { SurveyForm } from '../SurveyForm'
import '../App.css'

export function SurveyParticipantPage() {
  const location = useLocation()
  const [surveys, setSurveys] = useState<SurveyDto[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Aimyweb 로그인 연동 시, 로그인된 회원 식별자를 URL/스토리지에서 찾아 넘깁니다.
  // (예: URL ?respondentId=..., 또는 localStorage에 imweb_respondent_id 저장)
  const respondentId = (() => {
    try {
      const sp = new URLSearchParams(location.search)
      return (
        sp.get('respondentId') ||
        sp.get('memberId') ||
        localStorage.getItem('imweb_respondent_id') ||
        localStorage.getItem('imweb_member_id') ||
        undefined
      )
    } catch {
      return undefined
    }
  })()

  useEffect(() => {
    api.getSurveys()
      .then(setSurveys)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="loading">설문 목록을 불러오는 중...</div>
  if (error) return <div className="error">오류: {error}</div>

  if (selectedId) {
    return (
      <div className="app">
        <SurveyForm
          surveyId={selectedId}
          respondentId={respondentId}
          onBack={() => setSelectedId(null)}
        />
      </div>
    )
  }

  return (
    <div className="app">
      <header className="header">
        <h1>설문조사</h1>
        <p className="subtitle">참여할 설문을 선택해 주세요.</p>
      </header>
      <ul className="survey-list">
        {surveys.map((s) => (
          <li key={s.id}>
            <button
              type="button"
              className="survey-card"
              onClick={() => setSelectedId(s.id)}
            >
              <span className="survey-title">{s.title}</span>
              {s.description && <span className="survey-desc">{s.description}</span>}
            </button>
          </li>
        ))}
      </ul>
      {surveys.length === 0 && (
        <p className="empty">등록된 설문이 없습니다.</p>
      )}
    </div>
  )
}
