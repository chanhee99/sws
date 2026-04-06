import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

try {
  const sp = new URLSearchParams(window.location.search)
  if (sp.get('embed') === '1') {
    document.body.dataset.embed = '1'
  }
} catch {
  // ignore
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
