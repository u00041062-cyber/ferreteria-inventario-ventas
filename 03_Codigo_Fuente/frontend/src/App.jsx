import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import Navbar from './components/Navbar.jsx'
import Login from './pages/Login.jsx'
import Stock from './pages/Stock.jsx'
import Productos from './pages/Productos.jsx'
import Movimientos from './pages/Movimientos.jsx'
import Ventas from './pages/Ventas.jsx'
import Reportes from './pages/Reportes.jsx'
import Usuarios from './pages/Usuarios.jsx'

function Privada({ children, soloAdmin }) {
  const { sesion, esAdmin } = useAuth()
  if (!sesion) return <Navigate to="/login" replace />
  if (soloAdmin && !esAdmin) return <Navigate to="/" replace />
  return children
}

export default function App() {
  const { sesion } = useAuth()
  return (
    <>
      {sesion && <Navbar />}
      <div className="container py-4">
        <Routes>
          <Route path="/login" element={sesion ? <Navigate to="/" replace /> : <Login />} />
          <Route path="/" element={<Privada><Stock /></Privada>} />
          <Route path="/ventas" element={<Privada><Ventas /></Privada>} />
          <Route path="/productos" element={<Privada soloAdmin><Productos /></Privada>} />
          <Route path="/movimientos" element={<Privada soloAdmin><Movimientos /></Privada>} />
          <Route path="/reportes" element={<Privada soloAdmin><Reportes /></Privada>} />
          <Route path="/usuarios" element={<Privada soloAdmin><Usuarios /></Privada>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </>
  )
}
