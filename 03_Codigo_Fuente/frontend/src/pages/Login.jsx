import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import Mensaje from '../components/Mensaje.jsx'

export default function Login() {
  const { login } = useAuth()
  const nav = useNavigate()
  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  const enviar = async (e) => {
    e.preventDefault(); setError(''); setCargando(true)
    try { await login(usuario, clave); nav('/') }
    catch (err) { setError(err.message) }
    finally { setCargando(false) }
  }

  return (
    <div className="login-box card shadow-sm">
      <div className="card-body p-4">
        <h4 className="text-center mb-1">Ferretería Los Olivos</h4>
        <p className="text-center text-muted mb-4">Sistema de inventario y ventas</p>
        <form onSubmit={enviar}>
          <div className="mb-3">
            <label className="form-label">Usuario</label>
            <input className="form-control" value={usuario} onChange={e => setUsuario(e.target.value)} autoFocus required />
          </div>
          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <input type="password" className="form-control" value={clave} onChange={e => setClave(e.target.value)} required />
          </div>
          <Mensaje error={error} />
          <button className="btn btn-primary w-100" disabled={cargando}>{cargando ? 'Ingresando…' : 'Ingresar'}</button>
        </form>
      </div>
    </div>
  )
}
