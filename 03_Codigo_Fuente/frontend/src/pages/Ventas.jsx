import { useEffect, useState } from 'react'
import { api, soles } from '../api.js'
import Mensaje from '../components/Mensaje.jsx'

export default function Ventas() {
  const [q, setQ] = useState('')
  const [resultados, setResultados] = useState([])
  const [carrito, setCarrito] = useState([])     // { producto, cantidad }
  const [error, setError] = useState('')
  const [ultima, setUltima] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const buscar = async (texto = q) => {
    try { setResultados(await api.get('/api/productos?q=' + encodeURIComponent(texto))) }
    catch (e) { setError(e.message) }
  }
  useEffect(() => { buscar('') }, [])

  const agregar = (p) => {
    setError(''); setUltima(null)
    setCarrito(c => {
      const ex = c.find(i => i.producto.id === p.id)
      if (ex) return c.map(i => i.producto.id === p.id ? { ...i, cantidad: i.cantidad + 1 } : i)
      return [...c, { producto: p, cantidad: 1 }]
    })
  }
  const cambiar = (id, cant) => setCarrito(c => c.map(i => i.producto.id === id ? { ...i, cantidad: Math.max(1, Number(cant) || 1) } : i))
  const quitar = (id) => setCarrito(c => c.filter(i => i.producto.id !== id))
  const total = carrito.reduce((s, i) => s + i.cantidad * i.producto.precio, 0)

  const registrar = async () => {
    setError(''); setEnviando(true)
    try {
      const v = await api.post('/api/ventas', { items: carrito.map(i => ({ idProducto: i.producto.id, cantidad: i.cantidad })) })
      setUltima(v); setCarrito([]); buscar()
    } catch (e) { setError(e.message) }
    finally { setEnviando(false) }
  }

  return (
    <div className="row g-4">
      <div className="col-lg-6">
        <h4 className="mb-3">Registro de ventas</h4>
        <form className="input-group mb-2" onSubmit={e => { e.preventDefault(); buscar() }}>
          <input className="form-control" placeholder="Buscar producto" value={q} onChange={e => setQ(e.target.value)} />
          <button className="btn btn-primary">Buscar</button>
        </form>
        <div className="card"><div className="table-responsive" style={{ maxHeight: 460 }}>
          <table className="table table-sm table-hover mb-0">
            <thead className="table-light"><tr><th>Producto</th><th className="text-end">Precio</th><th className="text-end">Stock</th><th></th></tr></thead>
            <tbody>
              {resultados.map(p => (
                <tr key={p.id}>
                  <td>{p.codigo} – {p.descripcion}</td><td className="text-end">{soles(p.precio)}</td><td className="text-end">{p.stockActual}</td>
                  <td className="text-end"><button className="btn btn-sm btn-outline-success" disabled={p.stockActual === 0} onClick={() => agregar(p)}>Agregar</button></td>
                </tr>))}
            </tbody>
          </table>
        </div></div>
      </div>

      <div className="col-lg-6">
        <h4 className="mb-3">Detalle de la venta</h4>
        <Mensaje error={error} />
        {ultima && (
          <div className="alert alert-success">
            Venta N° {ultima.id} registrada por {soles(ultima.total)}. El stock fue descontado.
            <button className="btn btn-sm btn-success ms-2" onClick={() => api.descargar(`/api/ventas/${ultima.id}/comprobante`, `comprobante-${ultima.id}.pdf`)}>Descargar comprobante</button>
          </div>)}
        <div className="card"><div className="table-responsive">
          <table className="table table-sm mb-0">
            <thead className="table-light"><tr><th>Producto</th><th style={{ width: 90 }}>Cant.</th><th className="text-end">Subtotal</th><th></th></tr></thead>
            <tbody>
              {carrito.map(i => (
                <tr key={i.producto.id}>
                  <td>{i.producto.descripcion}<div className="text-muted small">{soles(i.producto.precio)} c/u · stock {i.producto.stockActual}</div></td>
                  <td><input type="number" min="1" max={i.producto.stockActual} className="form-control form-control-sm" value={i.cantidad} onChange={e => cambiar(i.producto.id, e.target.value)} /></td>
                  <td className="text-end">{soles(i.cantidad * i.producto.precio)}</td>
                  <td className="text-end"><button className="btn btn-sm btn-outline-danger" onClick={() => quitar(i.producto.id)}>✕</button></td>
                </tr>))}
              {carrito.length === 0 && <tr><td colSpan="4" className="text-center text-muted py-4">Agregue productos a la venta</td></tr>}
            </tbody>
          </table>
        </div></div>
        <div className="d-flex justify-content-between align-items-center mt-3">
          <span className="fs-4 fw-bold">TOTAL: {soles(total)}</span>
          <button className="btn btn-success btn-lg" disabled={carrito.length === 0 || enviando} onClick={registrar}>
            {enviando ? 'Registrando…' : 'Registrar venta'}
          </button>
        </div>
      </div>
    </div>
  )
}
