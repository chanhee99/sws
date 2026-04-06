import { BrowserRouter, Routes, Route, NavLink, Navigate } from 'react-router-dom'
import { SurveyParticipantPage } from './pages/SurveyParticipantPage'
import { AdminPage } from './pages/AdminPage'
import { ResultPage } from './pages/ResultPage'
import { readImwebIdentity } from './imwebIdentity'
import './App.css'

function AdminRouteGuard() {
  const { isAdmin } = readImwebIdentity(window.location.search)
  if (!isAdmin) return <Navigate to="/" replace />
  return <AdminPage />
}

function App() {
  const isEmbed = (() => {
    try {
      const sp = new URLSearchParams(window.location.search)
      return sp.get('embed') === '1'
    } catch {
      return false
    }
  })()

  return (
    <BrowserRouter>
      <div className={`app-shell ${isEmbed ? 'app-shell--embed' : ''}`}>
        {!isEmbed && <nav className="top-nav">
          <div className="top-nav-inner">
            <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
              설문 참여
            </NavLink>
            <NavLinkGuardedAdmin />
          </div>
        </nav>}
        <main>
          <Routes>
            <Route path="/" element={<SurveyParticipantPage />} />
            <Route path="/admin" element={<AdminRouteGuard />} />
            <Route path="/result" element={<ResultPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

function NavLinkGuardedAdmin() {
  const { isAdmin } = readImwebIdentity(window.location.search)
  if (!isAdmin) return null
  return (
    <NavLink to="/admin" className={({ isActive }) => (isActive ? 'active' : '')}>
      관리자
    </NavLink>
  )
}

export default App
