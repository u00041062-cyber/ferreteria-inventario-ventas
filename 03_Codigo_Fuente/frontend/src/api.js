const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const getSesion = () => {
  try { return JSON.parse(localStorage.getItem('sesion')) } catch { return null }
}
export const guardarSesion = (s) => localStorage.setItem('sesion', JSON.stringify(s))
export const borrarSesion = () => localStorage.removeItem('sesion')

async function pedir(path, { method = 'GET', body } = {}) {
  const s = getSesion()
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (s?.token) headers['Authorization'] = `Bearer ${s.token}`
  let res
  try {
    res = await fetch(BASE + path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined })
  } catch {
    throw new Error('No se pudo conectar con el servidor. Verifique que la API esté en ejecución.')
  }
  if (res.status === 401 && path !== '/api/auth/login') {
    borrarSesion()
    window.location.href = '/login'
    throw new Error('Sesión expirada')
  }
  if (!res.ok) {
    let msg = 'Ocurrió un error'
    try { msg = (await res.json()).mensaje || msg } catch { /* sin cuerpo */ }
    if (res.status === 403) msg = 'No tiene permiso para esta acción'
    throw new Error(msg)
  }
  return res
}

export const api = {
  get: async (p) => (await pedir(p)).json(),
  post: async (p, b) => (await pedir(p, { method: 'POST', body: b })).json(),
  put: async (p, b) => (await pedir(p, { method: 'PUT', body: b })).json(),
  patch: async (p, b) => (await pedir(p, { method: 'PATCH', body: b })).json(),
  del: async (p) => { await pedir(p, { method: 'DELETE' }) },
  // Descarga de archivos (PDF / Excel) con el token de sesión
  descargar: async (p, nombre) => {
    const blob = await (await pedir(p)).blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = nombre; a.click()
    URL.revokeObjectURL(url)
  },
}

export const soles = (n) => 'S/ ' + Number(n ?? 0).toFixed(2)
export const fechaHora = (iso) => iso ? new Date(iso).toLocaleString('es-PE') : ''
