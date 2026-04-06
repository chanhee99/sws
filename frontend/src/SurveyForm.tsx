import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from './api'
import type { SurveyDto, AnswerInput } from './types'
import './SurveyForm.css'

type Props = {
  surveyId: number
  respondentId?: string
  onBack: () => void
}

export function SurveyForm({ surveyId, respondentId, onBack }: Props) {
  const nav = useNavigate()
  const [survey, setSurvey] = useState<SurveyDto | null>(null)
  const [answers, setAnswers] = useState<Record<number, string | string[]>>({})
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [done, setDone] = useState(false)

  useEffect(() => {
    api.getSurvey(surveyId)
      .then(setSurvey)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [surveyId])

  const setAnswer = (questionId: number, value: string | string[]) => {
    setAnswers((prev) => ({ ...prev, [questionId]: value }))
  }

  const toggleMulti = (questionId: number, option: string) => {
    setAnswers((prev) => {
      const current = (prev[questionId] as string[] | undefined) || []
      const next = current.includes(option)
        ? current.filter((x) => x !== option)
        : [...current, option]
      return { ...prev, [questionId]: next }
    })
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const form = e.currentTarget
    if (!form.checkValidity()) {
      form.reportValidity()
      setError('필수 문항을 입력해 주세요.')
      return
    }
    if (!survey) return
    const answerList: AnswerInput[] = survey.questions.map((q) => {
      const v = answers[q.id]
      const value = Array.isArray(v) ? JSON.stringify(v) : (v ?? '')
      return { questionId: q.id, value }
    })
    setSubmitting(true)
    setError(null)
    try {
      const res = await api.submitSurvey({
        surveyId: survey.id,
        respondentId: respondentId ?? undefined,
        answers: answerList,
      })
      setDone(true)
      // 결과 페이지로 이동 (요청하신 결과 UI)
      setTimeout(() => {
        nav('/result', { state: { result: res.result } })
      }, 400)
    } catch (err) {
      setError(err instanceof Error ? err.message : '제출에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="loading">설문을 불러오는 중...</div>
  if (error && !survey) return <div className="error">오류: {error}</div>
  if (!survey) return null

  if (done) {
    return (
      <div className="success-box">
        <p className="success-text">설문에 참여해 주셔서 감사합니다.</p>
        <p className="success-sub">잠시 후 결과로 이동합니다.</p>
      </div>
    )
  }

  return (
    <div className="form-wrap">
      <button type="button" className="back-btn" onClick={onBack}>
        ← 목록으로
      </button>
      <header className="form-header">
        <h1>{survey.title}</h1>
        {survey.description && <p className="form-desc">{survey.description}</p>}
      </header>
      <form onSubmit={handleSubmit} className="survey-form">
        {survey.questions
          .sort((a, b) => a.sortOrder - b.sortOrder)
          .map((q) => (
            <fieldset key={q.id} className="question-block">
              <legend>
                {q.text}
                {q.required && <span className="required"> *</span>}
              </legend>
              {q.type === 'SINGLE_CHOICE' && q.options?.length && (
                <div className={`options ${q.options.length >= 6 ? 'options--grid' : ''}`}>
                  {q.options.map((opt) => (
                    <label key={opt} className="option-label">
                      <input
                        type="radio"
                        name={`q-${q.id}`}
                        value={opt}
                        checked={(answers[q.id] as string) === opt}
                        onChange={() => setAnswer(q.id, opt)}
                        required={q.required}
                      />
                      <span>{opt}</span>
                    </label>
                  ))}
                </div>
              )}
              {q.type === 'MULTIPLE_CHOICE' && q.options?.length && (
                <div className={`options ${q.options.length >= 6 ? 'options--grid' : ''}`}>
                  {q.options.map((opt) => (
                    <label key={opt} className="option-label">
                      <input
                        type="checkbox"
                        checked={((answers[q.id] as string[]) || []).includes(opt)}
                        onChange={() => toggleMulti(q.id, opt)}
                      />
                      <span>{opt}</span>
                    </label>
                  ))}
                </div>
              )}
              {q.type === 'SHORT_TEXT' && (
                <input
                  type="text"
                  className="input-short"
                  value={(answers[q.id] as string) ?? ''}
                  onChange={(e) => setAnswer(q.id, e.target.value)}
                  required={q.required}
                  placeholder="답변을 입력하세요"
                />
              )}
              {q.type === 'LONG_TEXT' && (
                <textarea
                  className="input-long"
                  value={(answers[q.id] as string) ?? ''}
                  onChange={(e) => setAnswer(q.id, e.target.value)}
                  required={q.required}
                  placeholder="답변을 입력하세요"
                  rows={4}
                />
              )}
            </fieldset>
          ))}
        {error && <p className="form-error">{error}</p>}
        <div className="form-actions">
          <button type="button" className="btn-secondary" onClick={onBack}>
            취소
          </button>
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? '제출 중...' : '제출하기'}
          </button>
        </div>
      </form>
    </div>
  )
}
