import { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import Mensaje from '../components/Mensaje.jsx'

const vacio = { nombre: '', usuario: '', contrasena: '', idRol: '' }

export default function Usuarios() {
  const { sesion } = useAuth()
  const [lista, setLista] = useState([])
  const [roles, setRoles] = useState([])
  const [form, setForm] = useState(vacio)
  const [editando, setEditando] = useState(null)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const cargar = async () => {
    try { setLista(await api.get('/api/usuarios')); setRoles(await api.get('/api/roles')) }
    catch (e) { setError(e.message) }
  }
  useEffect(() => { cargar() }, [])
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const guardar = async (e) => {
    e.preventDefault(); setError(''); setOk('')
    const body = { ...form, idRol: Number(form.idRol), contrasena: form.contrasena || null }
    try {
      if (editando) await api.put('/api/usuarios/' + editando, body)
      else await api.post('/api/usuarios', body)
      setOk(editando ? 'Usuario actualizado' : 'Usuario creado')
      setForm(vacio); setEditando(null); cargar()
    } catch (err) { setError(err.message) }
  }
  const editar = (u) => { setEditando(u.id); setForm({ nombre: u.nombre, usuario: u.usuario, contrasena: '', idRol: u.idRol }) }
  const alternar = async (u) => {
    setError(''); setOk('')
    try { await api.patch(`/api/usuarios/${u.id}/estado`, { estado: !u.estado }); cargar() } catch (e) { setError(e.message) }
  }

  return (
    <>
      <h4 className="mb-3">Administración de usuarios y roles</h4>
      <Mensaje error={error} ok={ok} />
      <div className="card mb-4"><div className="card-body">
        <h6 className="card-title-bar">{editando ? 'Editar usuario' : 'Nuevo usuario'}</h6>
        <form className="row g-2" onSubmit={guardar}>
          <div className="col-md-3"><input className="form-control" placeholder="Nombre completo" value={form.nombre} onChange={set('nombre')} required /></div>
          <div className="col-md-2"><input className="form-control" placeholder="Usuario" value={form.usuario} onChange={set('usuario')} required /></div>
          <div className="col-md-3"><input type="password" className="form-control" placeholder={editando ? 'Nueva contraseña (opcional)' : 'Contraseña (mín. 6)'} value={form.contrasena} onChange={set('contrasena')} required={!editando} minLength={6} /></div>
          <div className="col-md-2">
            <select className="form-select" value={form.idRol} onChange={set('idRol')} required>
              <option value="">Rol…</option>{roles.map(r => <option key={r.id} value={r.id}>{r.nombre}</option>)}
            </select>
          </div>
          <div className="col-md-2">
            <button className="btn btn-primary me-1">Guardar</button>
            {editando && <button type="button" className="btn btn-outline-secondary" onClick={() => { setEditando(null); setForm(vacio) }}>✕</button>}
          </div>
        </form>
      </div></div>

      <div className="card"><div className="table-responsive">
        <table className="table table-sm mb-0">
          <thead className="table-light"><tr><th>Nombre</th><th>Usuario</th><th>Rol</th><th>Estado</th><th></th></tr></thead>
          <tbody>
            {lista.map(u => (
              <tr key={u.id}>
                <td>{u.nombre}</td><td>{u.usuario}</td><td>{u.rol}</td>
                <td><span className={'badge ' + (u.estado ? 'text-bg-success' : 'text-bg-secondary')}>{u.estado ? 'Activo' : 'Inactivo'}</span></td>
                <td className="text-end text-nowrap">
                  <button className="btn btn-sm btn-outline-primary me-1" onClick={() => editar(u)}>Editar</button>
                  <button className="btn btn-sm btn-outline-secondary" disabled={u.usuario === sesion.usuario} onClick={() => alternar(u)}>{u.estado ? 'Desactivar' : 'Activar'}</button>
                </td>
              </tr>))}
          </tbody>
        </table>
      </div></div>
    </>
  )
}
