import { useState } from 'react'
import { ResponseList } from './admin/ResponseList'
import { ResponseDetail } from './admin/ResponseDetail'
import { StatisticsView } from './admin/StatisticsView'
import { SurveyManager } from './admin/SurveyManager'
import { ImwebManager } from './admin/ImwebManager'
import './admin/Admin.css'

type Tab = 'list' | 'detail' | 'statistics' | 'surveys' | 'imweb'

export function AdminPage() {
  const [tab, setTab] = useState<Tab>('list')
  const [selectedResponseId, setSelectedResponseId] = useState<number | null>(null)

  const showDetail = tab === 'detail' && selectedResponseId != null

  return (
    <div className="app">
      <header className="header">
        <h1>관리자</h1>
        <p className="subtitle">제출 목록, 상세, 통계를 확인할 수 있습니다.</p>
      </header>
      {!showDetail && (
        <div className="admin-tabs">
          <button
            type="button"
            className={tab === 'list' ? 'active' : ''}
            onClick={() => setTab('list')}
          >
            제출 목록
          </button>
          <button
            type="button"
            className={tab === 'statistics' ? 'active' : ''}
            onClick={() => setTab('statistics')}
          >
            통계
          </button>
          <button
            type="button"
            className={tab === 'surveys' ? 'active' : ''}
            onClick={() => setTab('surveys')}
          >
            설문 관리
          </button>
          <button
            type="button"
            className={tab === 'imweb' ? 'active' : ''}
            onClick={() => setTab('imweb')}
          >
            아임웹 연동
          </button>
        </div>
      )}
      {showDetail && (
        <ResponseDetail
          responseId={selectedResponseId}
          onBack={() => {
            setTab('list')
            setSelectedResponseId(null)
          }}
        />
      )}
      {tab === 'list' && !showDetail && (
        <ResponseList onSelectResponse={(id) => {
          setSelectedResponseId(id)
          setTab('detail')
        }}
        />
      )}
      {tab === 'statistics' && !showDetail && <StatisticsView />}
      {tab === 'surveys' && !showDetail && <SurveyManager />}
      {tab === 'imweb' && !showDetail && <ImwebManager />}
    </div>
  )
}
