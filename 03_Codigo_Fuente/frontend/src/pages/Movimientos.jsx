import { useEffect, useState } from 'react'
import { api, fechaHora } from '../api.js'
import Mensaje from '../components/Mensaje.jsx'

export default function Movimientos() {
  const [productos, setProductos] = useState([])
  const [lista, setLista] = useState([])
  const [form, setForm] = useState({ tipo: 'ENTRADA', idProducto: '', cantidad: '', motivo: '' })
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const cargar = async () => {
    try { setProductos(await api.get('/api/productos')); setLista(await api.get('/api/movimientos')) }
    catch (e) { setError(e.message) }
  }
  useEffect(() => { cargar() }, [])
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const registrar = async (e) => {
    e.preventDefault(); setError(''); setOk('')
    try {
      await api.post('/api/movimientos', { ...form, idProducto: Number(form.idProducto), cantidad: Number(form.cantidad) })
      setOk('Movimiento registrado; el stock fue actualizado')
      setForm({ ...form, cantidad: '', motivo: '' }); cargar()
    } catch (err) { setError(err.message) }
  }

  return (
    <>
      <h4 className="mb-3">Entradas y salidas de almacén</h4>
      <Mensaje error={error} ok={ok} />
      <div className="card mb-4"><div className="card-body">
        <form className="row g-2" onSubmit={registrar}>
          <div className="col-md-2">
            <select className="form-select" value={form.tipo} onChange={set('tipo')}>
              <option value="ENTRADA">Entrada</option><option value="SALIDA">Salida</option>
            </select>
          </div>
          <div className="col-md-4">
            <select className="form-select" value={form.idProducto} onChange={set('idProducto')} required>
              <option value="">Producto…</option>
              {productos.map(p => <option key={p.id} value={p.id}>{p.codigo} – {p.descripcion} (stock {p.stockActual})</option>)}
            </select>
          </div>
          <div className="col-md-2"><input type="number" min="1" className="form-control" placeholder="Cantidad" value={form.cantidad} onChange={set('cantidad')} required /></div>
          <div className="col-md-3"><input className="form-control" placeholder="Motivo (compra a proveedor, ajuste…)" value={form.motivo} onChange={set('motivo')} required /></div>
          <div className="col-md-1"><button className="btn btn-primary w-100">Registrar</button></div>
        </form>
      </div></div>

      <h5>Historial reciente</h5>
      <div className="card"><div className="table-responsive">
        <table className="table table-sm mb-0">
          <thead className="table-light"><tr><th>Fecha</th><th>Tipo</th><th>Producto</th><th className="text-end">Cantidad</th><th>Motivo</th><th>Usuario</th></tr></thead>
          <tbody>
            {lista.map(m => (
              <tr key={m.id}>
                <td>{fechaHora(m.fecha)}</td>
                <td><span className={'badge ' + (m.tipo === 'ENTRADA' ? 'text-bg-success' : 'text-bg-warning')}>{m.tipo}</span></td>
                <td>{m.producto}</td><td className="text-end">{m.cantidad}</td><td>{m.motivo}</td><td>{m.usuario}</td>
              </tr>))}
          </tbody>
        </table>
      </div></div>
    </>
  )
}
