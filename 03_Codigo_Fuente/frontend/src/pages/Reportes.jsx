import { useEffect, useState } from 'react'
import { api, soles, fechaHora } from '../api.js'
import Mensaje from '../components/Mensaje.jsx'

const hoy = () => new Date().toISOString().slice(0, 10)
const haceUnMes = () => { const d = new Date(); d.setMonth(d.getMonth() - 1); return d.toISOString().slice(0, 10) }

export default function Reportes() {
  const [tipo, setTipo] = useState('ventas')
  const [desde, setDesde] = useState(haceUnMes())
  const [hasta, setHasta] = useState(hoy())
  const [idProducto, setIdProducto] = useState('')
  const [idVendedor, setIdVendedor] = useState('')
  const [productos, setProductos] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [filas, setFilas] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/api/productos?soloActivos=false').then(setProductos).catch(() => {})
    api.get('/api/usuarios').then(setUsuarios).catch(() => {})
  }, [])

  const params = () => {
    let p = `desde=${desde}&hasta=${hasta}`
    if (idProducto) p += `&idProducto=${idProducto}`
    if (idVendedor) p += `&idVendedor=${idVendedor}`
    return p
  }
  const generar = async (e) => {
    e?.preventDefault(); setError('')
    try { setFilas(await api.get(tipo === 'ventas' ? `/api/reportes/ventas?${params()}` : '/api/reportes/inventario')) }
    catch (err) { setError(err.message); setFilas([]) }
  }
  useEffect(() => { setFilas([]) }, [tipo])

  const exportar = async (formato) => {
    setError('')
    try {
      const url = tipo === 'ventas' ? `/api/reportes/ventas/exportar?${params()}&formato=${formato}` : `/api/reportes/inventario/exportar?formato=${formato}`
      await api.descargar(url, `reporte-${tipo}.${formato}`)
    } catch (err) { setError(err.message) }
  }
  const totalVentas = filas.reduce((s, f) => s + Number(f.subtotal ?? 0), 0)

  return (
    <>
      <h4 className="mb-3">Reportes</h4>
      <div className="card mb-3"><div className="card-body">
        <form className="row g-2 align-items-end" onSubmit={generar}>
          <div className="col-md-2">
            <label className="form-label small mb-0">Tipo</label>
            <select className="form-select" value={tipo} onChange={e => setTipo(e.target.value)}>
              <option value="ventas">Ventas</option><option value="inventario">Inventario</option>
            </select>
          </div>
          {tipo === 'ventas' && <>
            <div className="col-md-2"><label className="form-label small mb-0">Desde</label><input type="date" className="form-control" value={desde} onChange={e => setDesde(e.target.value)} required /></div>
            <div className="col-md-2"><label className="form-label small mb-0">Hasta</label><input type="date" className="form-control" value={hasta} onChange={e => setHasta(e.target.value)} required /></div>
            <div className="col-md-2">
              <label className="form-label small mb-0">Producto</label>
              <select className="form-select" value={idProducto} onChange={e => setIdProducto(e.target.value)}>
                <option value="">Todos</option>{productos.map(p => <option key={p.id} value={p.id}>{p.codigo}</option>)}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label small mb-0">Vendedor</label>
              <select className="form-select" value={idVendedor} onChange={e => setIdVendedor(e.target.value)}>
                <option value="">Todos</option>{usuarios.map(u => <option key={u.id} value={u.id}>{u.usuario}</option>)}
              </select>
            </div>
          </>}
          <div className="col-md-2"><button className="btn btn-primary w-100">Generar</button></div>
        </form>
      </div></div>

      <Mensaje error={error} />
      <div className="mb-2">
        <button className="btn btn-outline-danger btn-sm me-2" disabled={!filas.length} onClick={() => exportar('pdf')}>Exportar PDF</button>
        <button className="btn btn-outline-success btn-sm" disabled={!filas.length} onClick={() => exportar('xlsx')}>Exportar Excel</button>
      </div>

      <div className="card"><div className="table-responsive">
        {tipo === 'ventas' ? (
          <table className="table table-sm mb-0">
            <thead className="table-light"><tr><th>N°</th><th>Fecha</th><th>Vendedor</th><th>Producto</th><th className="text-end">Cant.</th><th className="text-end">P. unit.</th><th className="text-end">Subtotal</th></tr></thead>
            <tbody>
              {filas.map((f, i) => <tr key={i}><td>{f.idVenta}</td><td>{fechaHora(f.fecha)}</td><td>{f.vendedor}</td><td>{f.codigo} – {f.producto}</td><td className="text-end">{f.cantidad}</td><td className="text-end">{soles(f.precioUnitario)}</td><td className="text-end">{soles(f.subtotal)}</td></tr>)}
              {filas.length > 0 && <tr className="table-light fw-bold"><td colSpan="6" className="text-end">TOTAL</td><td className="text-end">{soles(totalVentas)}</td></tr>}
              {filas.length === 0 && <tr><td colSpan="7" className="text-center text-muted py-3">Genere el reporte para ver los resultados</td></tr>}
            </tbody>
          </table>
        ) : (
          <table className="table table-sm mb-0">
            <thead className="table-light"><tr><th>Código</th><th>Descripción</th><th>Categoría</th><th className="text-end">Stock</th><th className="text-end">Mín.</th><th>Ubicación</th><th className="text-end">Unid. vendidas</th><th>Estado</th></tr></thead>
            <tbody>
              {filas.map((f, i) => <tr key={i} className={f.requiereReposicion ? 'fila-alerta' : ''}><td>{f.codigo}</td><td>{f.descripcion}</td><td>{f.categoria}</td><td className="text-end">{f.stockActual}</td><td className="text-end">{f.stockMinimo}</td><td>{f.ubicacion}</td><td className="text-end">{f.unidadesVendidas}</td><td>{f.requiereReposicion ? 'Reponer' : 'OK'}</td></tr>)}
              {filas.length === 0 && <tr><td colSpan="8" className="text-center text-muted py-3">Genere el reporte para ver los resultados</td></tr>}
            </tbody>
          </table>
        )}
      </div></div>
    </>
  )
}
