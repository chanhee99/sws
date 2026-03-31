import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom'
import { SurveyParticipantPage } from './pages/SurveyParticipantPage'
import { AdminPage } from './pages/AdminPage'
import { ResultPage } from './pages/ResultPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <nav className="top-nav">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            설문 참여
          </NavLink>
          <NavLink to="/admin" className={({ isActive }) => (isActive ? 'active' : '')}>
            관리자
          </NavLink>
        </nav>
        <main>
          <Routes>
            <Route path="/" element={<SurveyParticipantPage />} />
            <Route path="/admin" element={<AdminPage />} />
            <Route path="/result" element={<ResultPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

export default App
