import { useEffect, useState } from 'react'
import { api, soles } from '../api.js'
import Mensaje from '../components/Mensaje.jsx'

export default function Stock() {
  const [q, setQ] = useState('')
  const [lista, setLista] = useState([])
  const [alertas, setAlertas] = useState([])
  const [error, setError] = useState('')

  const buscar = async (texto = q) => {
    try { setError(''); setLista(await api.get('/api/productos?q=' + encodeURIComponent(texto))) }
    catch (e) { setError(e.message) }
  }
  useEffect(() => { buscar(''); api.get('/api/productos/alertas').then(setAlertas).catch(() => {}) }, [])

  return (
    <>
      <h4 className="mb-3">Consulta de stock</h4>
      <form className="input-group mb-3" onSubmit={e => { e.preventDefault(); buscar() }}>
        <input className="form-control" placeholder="Buscar por código o descripción" value={q} onChange={e => setQ(e.target.value)} />
        <button className="btn btn-primary">Buscar</button>
      </form>
      <Mensaje error={error} />
      <div className="card mb-4"><div className="table-responsive">
        <table className="table table-sm table-hover mb-0">
          <thead className="table-light"><tr><th>Código</th><th>Descripción</th><th>Categoría</th><th className="text-end">Precio</th><th className="text-end">Stock</th><th>Ubicación</th></tr></thead>
          <tbody>
            {lista.map(p => (
              <tr key={p.id} className={p.requiereReposicion ? 'fila-alerta' : ''}>
                <td>{p.codigo}</td><td>{p.descripcion}</td><td>{p.categoria}</td>
                <td className="text-end">{soles(p.precio)}</td><td className="text-end fw-semibold">{p.stockActual}</td><td>{p.ubicacion}</td>
              </tr>))}
            {lista.length === 0 && <tr><td colSpan="6" className="text-center text-muted py-3">Sin resultados</td></tr>}
          </tbody>
        </table>
      </div></div>

      <h5 className="text-danger">⚠ Productos bajo el stock mínimo</h5>
      <div className="card"><div className="table-responsive">
        <table className="table table-sm mb-0">
          <thead className="table-light"><tr><th>Código</th><th>Descripción</th><th className="text-end">Stock</th><th className="text-end">Mínimo</th></tr></thead>
          <tbody>
            {alertas.map(p => <tr key={p.id}><td>{p.codigo}</td><td>{p.descripcion}</td><td className="text-end">{p.stockActual}</td><td className="text-end">{p.stockMinimo}</td></tr>)}
            {alertas.length === 0 && <tr><td colSpan="4" className="text-center text-muted py-3">No hay productos por reponer</td></tr>}
          </tbody>
        </table>
      </div></div>
    </>
  )
}
