import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { api } from '../api'
import type { SurveyDto } from '../types'
import { SurveyForm } from '../SurveyForm'
import { readImwebIdentity } from '../imwebIdentity'
import '../App.css'

export function SurveyParticipantPage() {
  const location = useLocation()
  const [surveys, setSurveys] = useState<SurveyDto[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const { respondentId, isLoggedIn } = readImwebIdentity(location.search)

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
        <p className="subtitle">
          {isLoggedIn ? '참여할 설문을 선택해 주세요.' : '아임웹에 로그인한 회원만 참여할 수 있습니다.'}
        </p>
      </header>
      {!isLoggedIn && (
        <div className="notice-card">
          <div className="notice-title">로그인이 필요합니다</div>
          <div className="notice-body">
            스와니브 홈페이지에서 로그인한 뒤 다시 접속해 주세요.
            <br />
            (임베드 시, 로그인 회원의 식별자(memberUid)를 URL의 `respondentId` 또는 localStorage로 전달해야 합니다.)
          </div>
        </div>
      )}
      <ul className="survey-list">
        {surveys.map((s) => (
          <li key={s.id}>
            <button
              type="button"
              className="survey-card"
              onClick={() => setSelectedId(s.id)}
              disabled={!isLoggedIn}
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
