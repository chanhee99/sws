import { useEffect, useMemo, useState } from 'react'
import { api } from '../../api'
import type {
  QuestionDto,
  SurveyDto,
  UpsertSurveyRequest,
  ResultConfigDto,
  ResultConditionDto,
  ResultConfigUpsertRequest,
  ResultProfileDto,
} from '../../types'
import './Admin.css'

const QUESTION_TYPES: Array<{ value: QuestionDto['type']; label: string }> = [
  { value: 'SINGLE_CHOICE', label: '단일 선택' },
  { value: 'MULTIPLE_CHOICE', label: '복수 선택' },
  { value: 'SHORT_TEXT', label: '단답' },
  { value: 'LONG_TEXT', label: '장문' },
]

function emptySurvey(): UpsertSurveyRequest {
  return { title: '', description: '', active: true, questions: [] }
}

export function SurveyManager() {
  const [surveys, setSurveys] = useState<SurveyDto[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [form, setForm] = useState<UpsertSurveyRequest>(emptySurvey())
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const [resultProfiles, setResultProfiles] = useState<ResultProfileDto[]>([])
  const [resultConditions, setResultConditions] = useState<ResultConditionDto[]>([])
  const [resultSaving, setResultSaving] = useState(false)
  const [resultError, setResultError] = useState<string | null>(null)
  const [resultMessage, setResultMessage] = useState<string | null>(null)

  const selected = useMemo(
    () => surveys.find((s) => s.id === selectedId) ?? null,
    [surveys, selectedId]
  )

  const refresh = async () => {
    setLoading(true)
    setError(null)
    try {
      const list = await api.admin.listSurveys()
      setSurveys(list)
    } catch (e) {
      setError(e instanceof Error ? e.message : '설문 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refresh()
  }, [])

  useEffect(() => {
    if (!selected) {
      setForm(emptySurvey())
      return
    }
    setForm({
      title: selected.title,
      description: selected.description ?? '',
      active: selected.active,
      questions: selected.questions
        .slice()
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map((q) => ({
          id: q.id,
          text: q.text,
          type: q.type,
          options: q.options ?? [],
          required: q.required,
          sortOrder: q.sortOrder,
        })),
    })
  }, [selected])

  const loadResultConfig = async (surveyId: number) => {
    try {
      const cfg: ResultConfigDto = await api.admin.getResultConfig(surveyId)
      setResultProfiles(cfg.profiles ?? [])
      setResultConditions(cfg.conditions ?? [])
      setResultMessage(null)
      setResultError(null)
    } catch (e) {
      setResultProfiles([])
      setResultConditions([])
      setResultError(e instanceof Error ? e.message : '결과 설정을 불러오지 못했습니다.')
    }
  }

  useEffect(() => {
    if (selectedId == null) {
      setResultProfiles([])
      setResultConditions([])
      return
    }
    loadResultConfig(selectedId)
  }, [selectedId])

  const addQuestion = () => {
    setForm((prev) => ({
      ...prev,
      questions: [
        ...prev.questions,
        {
          text: '',
          type: 'SINGLE_CHOICE',
          options: ['옵션 1', '옵션 2'],
          required: true,
          sortOrder: prev.questions.length,
        },
      ],
    }))
  }

  const removeQuestion = (idx: number) => {
    setForm((prev) => ({
      ...prev,
      questions: prev.questions.filter((_, i) => i !== idx).map((q, i) => ({ ...q, sortOrder: i })),
    }))
  }

  const updateQuestion = (idx: number, patch: Partial<UpsertSurveyRequest['questions'][number]>) => {
    setForm((prev) => ({
      ...prev,
      questions: prev.questions.map((q, i) => (i === idx ? { ...q, ...patch } : q)),
    }))
  }

  const moveQuestion = (idx: number, dir: -1 | 1) => {
    setForm((prev) => {
      const next = prev.questions.slice()
      const swapIdx = idx + dir
      if (swapIdx < 0 || swapIdx >= next.length) return prev
      const tmp = next[idx]
      next[idx] = next[swapIdx]
      next[swapIdx] = tmp
      return {
        ...prev,
        questions: next.map((q, i) => ({ ...q, sortOrder: i })),
      }
    })
  }

  const toggleActive = async (surveyId: number, active: boolean) => {
    setSaving(true)
    setMessage(null)
    try {
      await api.admin.setSurveyActive(surveyId, active)
      await refresh()
      setMessage(active ? '설문을 활성화했습니다.' : '설문을 비활성화했습니다.')
    } catch (e) {
      setError(e instanceof Error ? e.message : '변경에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const save = async () => {
    setSaving(true)
    setError(null)
    setMessage(null)
    try {
      if (!form.title.trim()) throw new Error('설문 제목을 입력하세요.')
      const body: UpsertSurveyRequest = {
        ...form,
        questions: form.questions.map((q, i) => ({
          ...q,
          sortOrder: i,
          options:
            q.type === 'SINGLE_CHOICE' || q.type === 'MULTIPLE_CHOICE'
              ? (q.options ?? []).filter((x) => x.trim().length > 0)
              : [],
        })),
      }
      if (selectedId == null) {
        const created = await api.admin.createSurvey(body)
        await refresh()
        setSelectedId(created.id)
        await loadResultConfig(created.id)
        setMessage('설문을 생성했습니다.')
      } else {
        await api.admin.updateSurvey(selectedId, body)
        await refresh()
        await loadResultConfig(selectedId)
        setMessage('설문을 저장했습니다.')
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const addResultProfile = () => {
    const nextIdx = resultProfiles.length + 1
    const key = `type_${Date.now()}_${nextIdx}`
    const next: ResultProfileDto = {
      key,
      skinTypeLabel: '',
      statusText: '',
      productSetTitle: '추천 제품 세트',
      products: [
        { name: '', desc: '' },
        { name: '', desc: '' },
        { name: '', desc: '' },
      ],
      couponButtonText: '맞춤 케어 시작하기 (5,000원 혜택 적용)',
      couponUrl: '',
      couponCode: '',
      priority: 100,
      defaultProfile: resultProfiles.length === 0,
    }
    setResultProfiles((prev) => [...prev, next])
  }

  const upsertCondition = (questionId: number, matchValue: string, profileKey: string) => {
    setResultConditions((prev) => {
      const filtered = prev.filter(
        (c) => !(c.questionId === questionId && c.matchValue === matchValue),
      )
      if (!profileKey) return filtered
      return [...filtered, { questionId, matchValue, profileKey }]
    })
  }

  const removeResultProfile = (profileKey: string) => {
    setResultProfiles((prev) => {
      const next = prev.filter((p) => p.key !== profileKey)
      if (next.length === 0) return next
      const hasDefault = next.some((p) => p.defaultProfile)
      if (hasDefault) return next.map((p) => ({ ...p, defaultProfile: p.key === profileKey ? false : p.defaultProfile }))
      return next.map((p, i) => ({ ...p, defaultProfile: i === 0 }))
    })
    setResultConditions((prev) => prev.filter((c) => c.profileKey !== profileKey))
  }

  const saveResultConfig = async () => {
    if (selectedId == null) return
    if (resultProfiles.length === 0) {
      setResultError('결과 프로필을 추가해 주세요.')
      return
    }
    setResultSaving(true)
    setResultError(null)
    setResultMessage(null)
    try {
      const body: ResultConfigUpsertRequest = {
        profiles: resultProfiles,
        conditions: resultConditions,
      }
      await api.admin.upsertResultConfig(selectedId, body)
      setResultMessage('결과 설정을 저장했습니다.')
      await loadResultConfig(selectedId)
    } catch (e) {
      setResultError(e instanceof Error ? e.message : '결과 설정 저장에 실패했습니다.')
    } finally {
      setResultSaving(false)
    }
  }

  if (loading) return <div className="admin-loading">설문을 불러오는 중...</div>
  if (error) return <div className="admin-error">오류: {error}</div>

  return (
    <section className="admin-section">
      <h2>설문 관리</h2>
      {message && <div className="admin-muted">{message}</div>}
      <div className="admin-survey-grid">
        <div className="admin-survey-list">
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem' }}>
            <button
              type="button"
              className="admin-btn-sm"
              onClick={() => {
                setSelectedId(null)
                setForm(emptySurvey())
              }}
            >
              + 새 설문
            </button>
          </div>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>설문</th>
                  <th>상태</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {surveys.map((s) => (
                  <tr key={s.id} style={{ opacity: s.active ? 1 : 0.7 }}>
                    <td>
                      <button
                        type="button"
                        className="admin-link-btn"
                        onClick={() => setSelectedId(s.id)}
                      >
                        {s.title}
                      </button>
                    </td>
                    <td>{s.active ? '활성' : '비활성'}</td>
                    <td>
                      <button
                        type="button"
                        className="admin-btn-sm"
                        disabled={saving}
                        onClick={() => toggleActive(s.id, !s.active)}
                      >
                        {s.active ? '비활성' : '활성'}
                      </button>
                    </td>
                  </tr>
                ))}
                {surveys.length === 0 && (
                  <tr>
                    <td colSpan={3}>등록된 설문이 없습니다.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="admin-survey-editor">
          <h3>{selectedId == null ? '새 설문 만들기' : '설문 편집'}</h3>
          <div className="admin-form">
            <label className="admin-form-row">
              <span>제목</span>
              <input
                className="admin-input"
                value={form.title}
                onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                placeholder="설문 제목"
              />
            </label>
            <label className="admin-form-row">
              <span>설명</span>
              <textarea
                className="admin-input"
                value={form.description ?? ''}
                onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                placeholder="설문 설명"
                rows={3}
              />
            </label>
            <label className="admin-form-row" style={{ alignItems: 'center' }}>
              <span>활성</span>
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm((p) => ({ ...p, active: e.target.checked }))}
              />
            </label>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 style={{ margin: '1rem 0 0.5rem' }}>문항</h3>
              <button type="button" className="admin-btn-sm" onClick={addQuestion}>
                + 문항 추가
              </button>
            </div>

            {form.questions.length === 0 && <p className="admin-muted">문항을 추가해 주세요.</p>}

            {form.questions.map((q, idx) => (
              <div key={idx} className="admin-question-editor">
                <div className="admin-question-toolbar">
                  <strong>Q{idx + 1}</strong>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button type="button" className="admin-btn-sm" onClick={() => moveQuestion(idx, -1)} disabled={idx === 0}>
                      ↑
                    </button>
                    <button type="button" className="admin-btn-sm" onClick={() => moveQuestion(idx, 1)} disabled={idx === form.questions.length - 1}>
                      ↓
                    </button>
                    <button type="button" className="admin-btn-sm" onClick={() => removeQuestion(idx)}>
                      삭제
                    </button>
                  </div>
                </div>

                <label className="admin-form-row">
                  <span>질문</span>
                  <input
                    className="admin-input"
                    value={q.text}
                    onChange={(e) => updateQuestion(idx, { text: e.target.value })}
                    placeholder="질문 텍스트"
                  />
                </label>

                <div className="admin-form-row">
                  <span>유형</span>
                  <select
                    value={q.type}
                    onChange={(e) => updateQuestion(idx, { type: e.target.value as QuestionDto['type'] })}
                  >
                    {QUESTION_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>
                        {t.label}
                      </option>
                    ))}
                  </select>
                </div>

                <label className="admin-form-row" style={{ alignItems: 'center' }}>
                  <span>필수</span>
                  <input
                    type="checkbox"
                    checked={q.required}
                    onChange={(e) => updateQuestion(idx, { required: e.target.checked })}
                  />
                </label>

                {(q.type === 'SINGLE_CHOICE' || q.type === 'MULTIPLE_CHOICE') && (
                  <div className="admin-options">
                    <div className="admin-muted" style={{ marginBottom: '0.25rem' }}>옵션</div>
                    {(q.options ?? []).map((opt, oi) => (
                      <div key={oi} className="admin-option-row">
                        <input
                          className="admin-input"
                          value={opt}
                          onChange={(e) => {
                            const next = (q.options ?? []).slice()
                            next[oi] = e.target.value
                            updateQuestion(idx, { options: next })
                          }}
                        />
                        <button
                          type="button"
                          className="admin-btn-sm"
                          onClick={() => {
                            const next = (q.options ?? []).filter((_, j) => j !== oi)
                            updateQuestion(idx, { options: next })
                          }}
                        >
                          -
                        </button>
                      </div>
                    ))}
                    <button
                      type="button"
                      className="admin-btn-sm"
                      onClick={() => updateQuestion(idx, { options: [...(q.options ?? []), `옵션 ${(q.options?.length ?? 0) + 1}`] })}
                    >
                      + 옵션 추가
                    </button>
                  </div>
                )}
              </div>
            ))}

            {error && <div className="admin-error">{error}</div>}
            <div style={{ marginTop: '1rem', display: 'flex', justifyContent: 'flex-end' }}>
              <button type="button" className="admin-btn-sm" disabled={saving} onClick={save}>
                {saving ? '저장 중...' : '저장'}
              </button>
            </div>

            {selectedId != null && (
              <div style={{ marginTop: '1.75rem' }}>
                <h3 style={{ margin: '0 0 0.75rem' }}>결과 설정</h3>
                {resultMessage && <div className="admin-muted">{resultMessage}</div>}
                {resultError && <div className="admin-error">{resultError}</div>}

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '0.75rem' }}>
                  <button type="button" className="admin-btn-sm" onClick={addResultProfile}>
                    + 프로필 추가
                  </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {resultProfiles.map((p, idx) => (
                    <div key={p.key} className="admin-question-editor">
                      <div className="admin-question-toolbar">
                        <strong>유형 {idx + 1}</strong>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <label style={{ display: 'inline-flex', gap: '0.4rem', alignItems: 'center' }}>
                            <input
                              type="checkbox"
                              checked={p.defaultProfile}
                              onChange={(e) => {
                                const checked = e.target.checked
                                setResultProfiles((prev) =>
                                  prev.map((x) => ({
                                    ...x,
                                    defaultProfile: checked ? x.key === p.key : x.key === p.key ? false : x.defaultProfile,
                                  })),
                                )
                              }}
                            />
                            기본
                          </label>
                          <button type="button" className="admin-btn-sm" onClick={() => removeResultProfile(p.key)}>
                            삭제
                          </button>
                        </div>
                      </div>

                      <div className="admin-form" style={{ gap: '0.65rem' }}>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>피부 유형</span>
                          <input
                            className="admin-input"
                            value={p.skinTypeLabel}
                            onChange={(e) =>
                              setResultProfiles((prev) => prev.map((x) => (x.key === p.key ? { ...x, skinTypeLabel: e.target.value } : x)))
                            }
                            placeholder="예) 건강형"
                          />
                        </label>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>현재 상태</span>
                          <textarea
                            className="admin-input"
                            value={p.statusText}
                            onChange={(e) =>
                              setResultProfiles((prev) => prev.map((x) => (x.key === p.key ? { ...x, statusText: e.target.value } : x)))
                            }
                            rows={3}
                          />
                        </label>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>제품 세트</span>
                          <input
                            className="admin-input"
                            value={p.productSetTitle}
                            onChange={(e) =>
                              setResultProfiles((prev) => prev.map((x) => (x.key === p.key ? { ...x, productSetTitle: e.target.value } : x)))
                            }
                            placeholder="추천 제품 세트"
                          />
                        </label>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                          <div className="admin-muted">제품 3개</div>
                          {p.products.map((pr, pi) => (
                            <div key={pi} className="admin-option-row" style={{ gridTemplateColumns: '1fr 1fr auto' }}>
                              <input
                                className="admin-input"
                                placeholder="상품명"
                                value={pr.name}
                                onChange={(e) =>
                                  setResultProfiles((prev) =>
                                    prev.map((x) =>
                                      x.key === p.key
                                        ? { ...x, products: x.products.map((y, j) => (j === pi ? { ...y, name: e.target.value } : y)) }
                                        : x,
                                    ),
                                  )
                                }
                              />
                              <input
                                className="admin-input"
                                placeholder="설명"
                                value={pr.desc}
                                onChange={(e) =>
                                  setResultProfiles((prev) =>
                                    prev.map((x) =>
                                      x.key === p.key
                                        ? { ...x, products: x.products.map((y, j) => (j === pi ? { ...y, desc: e.target.value } : y)) }
                                        : x,
                                    ),
                                  )
                                }
                              />
                              <div style={{ width: 0 }} />
                            </div>
                          ))}
                        </div>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>쿠폰 문구</span>
                          <input
                            className="admin-input"
                            value={p.couponButtonText}
                            onChange={(e) =>
                              setResultProfiles((prev) =>
                                prev.map((x) => (x.key === p.key ? { ...x, couponButtonText: e.target.value } : x)),
                              )
                            }
                            placeholder="맞춤 케어 시작하기 (5,000원 혜택 적용)"
                          />
                        </label>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>쿠폰 URL</span>
                          <input
                            className="admin-input"
                            value={p.couponUrl ?? ''}
                            onChange={(e) =>
                              setResultProfiles((prev) =>
                                prev.map((x) => (x.key === p.key ? { ...x, couponUrl: e.target.value } : x)),
                              )
                            }
                            placeholder="https://swanevekr.com/... (선택)"
                          />
                        </label>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>쿠폰 코드</span>
                          <input
                            className="admin-input"
                            value={p.couponCode ?? ''}
                            onChange={(e) =>
                              setResultProfiles((prev) =>
                                prev.map((x) => (x.key === p.key ? { ...x, couponCode: e.target.value } : x)),
                              )
                            }
                            placeholder="Imweb coupon_code (선택)"
                          />
                        </label>
                        <label className="admin-form-row" style={{ gridTemplateColumns: '120px 1fr' }}>
                          <span>우선순위</span>
                          <input
                            className="admin-input"
                            type="number"
                            value={p.priority}
                            onChange={(e) =>
                              setResultProfiles((prev) =>
                                prev.map((x) =>
                                  x.key === p.key ? { ...x, priority: Number(e.target.value) } : x,
                                ),
                              )
                            }
                          />
                        </label>
                      </div>
                    </div>
                  ))}
                </div>

                <div style={{ marginTop: '1.5rem' }}>
                  <h3 style={{ margin: '0 0 0.75rem' }}>선택지 매핑(객관식)</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {form.questions
                      .filter((q) => q.id != null && (q.type === 'SINGLE_CHOICE' || q.type === 'MULTIPLE_CHOICE'))
                      .map((q) => (
                        <div key={q.id} className="admin-question-editor">
                          <div style={{ fontWeight: 700, marginBottom: '0.75rem' }}>{q.text}</div>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            {(q.options ?? []).filter((x) => x.trim().length > 0).map((opt) => {
                              const assigned =
                                resultConditions.find(
                                  (c) => c.questionId === q.id && c.matchValue === opt,
                                )?.profileKey ?? ''
                              return (
                                <label key={opt} className="admin-option-row" style={{ gridTemplateColumns: '1fr 1fr' }}>
                                  <span style={{ color: 'var(--text-muted)' }}>{opt}</span>
                                  <select
                                    value={assigned}
                                    onChange={(e) => upsertCondition(q.id!, opt, e.target.value)}
                                  >
                                    <option value="">미매핑</option>
                                    {resultProfiles.map((rp) => (
                                      <option key={rp.key} value={rp.key}>
                                        {rp.skinTypeLabel || rp.key}
                                      </option>
                                    ))}
                                  </select>
                                </label>
                              )
                            })}
                          </div>
                        </div>
                      ))}
                    {form.questions.filter((q) => q.type === 'SINGLE_CHOICE' || q.type === 'MULTIPLE_CHOICE').length === 0 && (
                      <div className="admin-muted">객관식 문항이 없습니다.</div>
                    )}
                  </div>
                </div>

                <div style={{ marginTop: '1.25rem', display: 'flex', justifyContent: 'flex-end' }}>
                  <button type="button" className="admin-btn-sm" disabled={resultSaving} onClick={saveResultConfig}>
                    {resultSaving ? '결과 저장 중...' : '결과 설정 저장'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}

