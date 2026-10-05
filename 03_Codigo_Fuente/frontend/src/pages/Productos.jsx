import { useEffect, useState } from 'react'
import { api, soles } from '../api.js'
import Mensaje from '../components/Mensaje.jsx'

const vacio = { codigo: '', descripcion: '', precio: '', stockActual: 0, stockMinimo: 0, ubicacion: '', idCategoria: '' }

export default function Productos() {
  const [productos, setProductos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [filtroCat, setFiltroCat] = useState('')
  const [q, setQ] = useState('')
  const [form, setForm] = useState(vacio)
  const [editando, setEditando] = useState(null)
  const [catForm, setCatForm] = useState({ nombre: '', descripcion: '' })
  const [catEdit, setCatEdit] = useState(null)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const aviso = (m, esError = false) => { setError(esError ? m : ''); setOk(esError ? '' : m) }
  const cargar = async () => {
    try {
      const [p, c] = await Promise.all([
        api.get('/api/productos?soloActivos=true&q=' + encodeURIComponent(q) + (filtroCat ? '&idCategoria=' + filtroCat : '')),
        api.get('/api/categorias')])
      setProductos(p); setCategorias(c)
    } catch (e) { aviso(e.message, true) }
  }
  useEffect(() => { cargar() }, [filtroCat])

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const guardar = async (e) => {
    e.preventDefault()
    const body = { ...form, precio: Number(form.precio), stockActual: Number(form.stockActual),
                   stockMinimo: Number(form.stockMinimo), idCategoria: Number(form.idCategoria) }
    try {
      if (editando) await api.put('/api/productos/' + editando, body)
      else await api.post('/api/productos', body)
      aviso(editando ? 'Producto actualizado' : 'Producto registrado')
      setForm(vacio); setEditando(null); cargar()
    } catch (err) { aviso(err.message, true) }
  }
  const editar = (p) => {
    setEditando(p.id)
    setForm({ codigo: p.codigo, descripcion: p.descripcion, precio: p.precio, stockActual: p.stockActual,
              stockMinimo: p.stockMinimo, ubicacion: p.ubicacion ?? '', idCategoria: p.idCategoria })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
  const baja = async (p) => {
    if (!confirm(`¿Dar de baja "${p.descripcion}"? Se conservará su historial.`)) return
    try { await api.del('/api/productos/' + p.id); aviso('Producto dado de baja'); cargar() } catch (e) { aviso(e.message, true) }
  }

  const guardarCat = async (e) => {
    e.preventDefault()
    try {
      if (catEdit) await api.put('/api/categorias/' + catEdit, catForm)
      else await api.post('/api/categorias', catForm)
      aviso('Categoría guardada'); setCatForm({ nombre: '', descripcion: '' }); setCatEdit(null); cargar()
    } catch (err) { aviso(err.message, true) }
  }
  const borrarCat = async (c) => {
    if (!confirm(`¿Eliminar la categoría "${c.nombre}"?`)) return
    try { await api.del('/api/categorias/' + c.id); aviso('Categoría eliminada'); cargar() } catch (e) { aviso(e.message, true) }
  }

  return (
    <>
      <h4 className="mb-3">Productos y categorías</h4>
      <Mensaje error={error} ok={ok} />

      <div className="card mb-4"><div className="card-body">
        <h6 className="card-title-bar">{editando ? 'Editar producto' : 'Nuevo producto'}</h6>
        <form className="row g-2" onSubmit={guardar}>
          <div className="col-md-2"><input className="form-control" placeholder="Código" value={form.codigo} onChange={set('codigo')} required /></div>
          <div className="col-md-4"><input className="form-control" placeholder="Descripción" value={form.descripcion} onChange={set('descripcion')} required /></div>
          <div className="col-md-2"><input type="number" step="0.01" min="0.01" className="form-control" placeholder="Precio" value={form.precio} onChange={set('precio')} required /></div>
          <div className="col-md-2"><input type="number" min="0" className="form-control" placeholder="Stock mínimo" value={form.stockMinimo} onChange={set('stockMinimo')} required /></div>
          <div className="col-md-2"><input className="form-control" placeholder="Ubicación" value={form.ubicacion} onChange={set('ubicacion')} /></div>
          <div className="col-md-3">
            <select className="form-select" value={form.idCategoria} onChange={set('idCategoria')} required>
              <option value="">Categoría…</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </div>
          {!editando && <div className="col-md-2"><input type="number" min="0" className="form-control" placeholder="Stock inicial" value={form.stockActual} onChange={set('stockActual')} /></div>}
          <div className="col-md-4">
            <button className="btn btn-primary me-2">Guardar</button>
            {editando && <button type="button" className="btn btn-outline-secondary" onClick={() => { setEditando(null); setForm(vacio) }}>Cancelar</button>}
          </div>
        </form>
        {editando && <div className="form-text">El stock solo cambia desde Almacén (entradas/salidas) o con las ventas.</div>}
      </div></div>

      <div className="row g-2 mb-2">
        <div className="col-md-3">
          <select className="form-select" value={filtroCat} onChange={e => setFiltroCat(e.target.value)}>
            <option value="">Todas las categorías</option>
            {categorias.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </select>
        </div>
        <form className="col-md-5 input-group" onSubmit={e => { e.preventDefault(); cargar() }}>
          <input className="form-control" placeholder="Buscar…" value={q} onChange={e => setQ(e.target.value)} />
          <button className="btn btn-outline-primary">Buscar</button>
        </form>
      </div>
      <div className="card mb-4"><div className="table-responsive">
        <table className="table table-sm table-hover mb-0">
          <thead className="table-light"><tr><th>Código</th><th>Descripción</th><th>Categoría</th><th className="text-end">Precio</th><th className="text-end">Stock</th><th className="text-end">Mín.</th><th>Ubicación</th><th></th></tr></thead>
          <tbody>
            {productos.map(p => (
              <tr key={p.id}>
                <td>{p.codigo}</td><td>{p.descripcion}</td><td>{p.categoria}</td><td className="text-end">{soles(p.precio)}</td>
                <td className="text-end">{p.stockActual}</td><td className="text-end">{p.stockMinimo}</td><td>{p.ubicacion}</td>
                <td className="text-end text-nowrap">
                  <button className="btn btn-sm btn-outline-primary me-1" onClick={() => editar(p)}>Editar</button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => baja(p)}>Baja</button>
                </td>
              </tr>))}
          </tbody>
        </table>
      </div></div>

      <h5>Categorías</h5>
      <div className="card"><div className="card-body">
        <form className="row g-2 mb-3" onSubmit={guardarCat}>
          <div className="col-md-3"><input className="form-control" placeholder="Nombre" value={catForm.nombre} onChange={e => setCatForm({ ...catForm, nombre: e.target.value })} required /></div>
          <div className="col-md-5"><input className="form-control" placeholder="Descripción" value={catForm.descripcion} onChange={e => setCatForm({ ...catForm, descripcion: e.target.value })} /></div>
          <div className="col-md-4">
            <button className="btn btn-success me-2">{catEdit ? 'Actualizar' : 'Agregar'}</button>
            {catEdit && <button type="button" className="btn btn-outline-secondary" onClick={() => { setCatEdit(null); setCatForm({ nombre: '', descripcion: '' }) }}>Cancelar</button>}
          </div>
        </form>
        <ul className="list-group">
          {categorias.map(c => (
            <li key={c.id} className="list-group-item d-flex justify-content-between align-items-center">
              <span><strong>{c.nombre}</strong> <span className="text-muted">{c.descripcion}</span></span>
              <span>
                <button className="btn btn-sm btn-outline-primary me-1" onClick={() => { setCatEdit(c.id); setCatForm({ nombre: c.nombre, descripcion: c.descripcion ?? '' }) }}>Editar</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => borrarCat(c)}>Eliminar</button>
              </span>
            </li>))}
        </ul>
      </div></div>
    </>
  )
}
