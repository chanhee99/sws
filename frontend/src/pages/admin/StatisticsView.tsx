import { useState, useEffect } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Legend } from 'recharts'
import { api } from '../../api'
import type { StatisticsDto, SurveyDto } from '../../types'
import './Admin.css'

const CHART_COLORS = ['#6366f1', '#818cf8', '#a5b4fc', '#c7d2fe', '#e0e7ff']

export function StatisticsView() {
  const [surveys, setSurveys] = useState<SurveyDto[]>([])
  const [surveyId, setSurveyId] = useState<number | ''>('')
  const [stats, setStats] = useState<StatisticsDto | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.getSurveys().then(setSurveys).catch(() => setSurveys([]))
  }, [])

  useEffect(() => {
    if (surveyId === '') {
      setStats(null)
      return
    }
    setLoading(true)
    api.admin
      .getStatisticsBySurvey(surveyId)
      .then(setStats)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [surveyId])

  if (error) return <div className="admin-error">오류: {error}</div>

  return (
    <section className="admin-section">
      <h2>통계</h2>
      <div className="admin-filters">
        <label>
          설문
          <select
            value={surveyId === '' ? '' : surveyId}
            onChange={(e) => setSurveyId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">선택</option>
            {surveys.map((s) => (
              <option key={s.id} value={s.id}>
                {s.title}
              </option>
            ))}
          </select>
        </label>
      </div>
      {loading && <div className="admin-loading">통계를 불러오는 중...</div>}
      {!loading && stats && (
        <div className="admin-stats">
          <p className="admin-total-responses">총 응답 수: {stats.totalResponses}건</p>
          {stats.questionStats.map((qs) => (
            <div key={qs.questionId} className="admin-question-stat">
              <h4>{qs.questionText}</h4>
              {(qs.type === 'SINGLE_CHOICE' || qs.type === 'MULTIPLE_CHOICE') && qs.optionCounts && (
                <div className="admin-chart-wrap">
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart
                      data={Object.entries(qs.optionCounts).map(([name, count]) => ({ name, count }))}
                      margin={{ top: 8, right: 8, left: 8, bottom: 8 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                      <XAxis dataKey="name" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} />
                      <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 12 }} />
                      <Tooltip
                        contentStyle={{ background: 'var(--surface)', border: '1px solid var(--border)' }}
                        labelStyle={{ color: 'var(--text)' }}
                      />
                      <Bar dataKey="count" fill={CHART_COLORS[0]} radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie
                        data={Object.entries(qs.optionCounts).map(([name, count], i) => ({
                          name,
                          value: count,
                          fill: CHART_COLORS[i % CHART_COLORS.length],
                        }))}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        outerRadius={70}
                        label={({ name, value }) => `${name}: ${value}`}
                      />
                      <Tooltip
                        contentStyle={{ background: 'var(--surface)', border: '1px solid var(--border)' }}
                      />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              )}
              {qs.textAnswers && qs.textAnswers.length > 0 && (
                <ul className="admin-text-answers">
                  {qs.textAnswers.map((text, i) => (
                    <li key={i}>{text}</li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </div>
      )}
      {!loading && surveyId !== '' && !stats?.questionStats?.length && stats && (
        <p className="admin-muted">문항이 없거나 응답이 없습니다.</p>
      )}
    </section>
  )
}
