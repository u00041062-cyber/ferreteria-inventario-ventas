import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

export default function Navbar() {
  const { sesion, esAdmin, logout } = useAuth()
  const nav = useNavigate()
  const salir = () => { logout(); nav('/login') }
  const link = ({ isActive }) => 'nav-link' + (isActive ? ' active fw-semibold' : '')

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <span className="navbar-brand">Ferretería Los Olivos <small>Inventario y Ventas</small></span>
        <ul className="navbar-nav me-auto">
          <li className="nav-item"><NavLink to="/" end className={link}>Stock</NavLink></li>
          <li className="nav-item"><NavLink to="/ventas" className={link}>Ventas</NavLink></li>
          {esAdmin && <>
            <li className="nav-item"><NavLink to="/productos" className={link}>Productos</NavLink></li>
            <li className="nav-item"><NavLink to="/movimientos" className={link}>Almacén</NavLink></li>
            <li className="nav-item"><NavLink to="/reportes" className={link}>Reportes</NavLink></li>
            <li className="nav-item"><NavLink to="/usuarios" className={link}>Usuarios</NavLink></li>
          </>}
        </ul>
        <span className="navbar-text me-3">{sesion.nombre} · <em>{sesion.rol}</em></span>
        <button className="btn btn-outline-light btn-sm" onClick={salir}>Salir</button>
      </div>
    </nav>
  )
}
