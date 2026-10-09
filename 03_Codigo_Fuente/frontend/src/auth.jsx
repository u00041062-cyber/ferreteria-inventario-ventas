import { createContext, useContext, useState } from 'react'
import { api, getSesion, guardarSesion, borrarSesion } from './api.js'

const Ctx = createContext(null)
export const useAuth = () => useContext(Ctx)

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(getSesion())

  const login = async (usuario, contrasena) => {
    const s = await api.post('/api/auth/login', { usuario, contrasena })
    guardarSesion(s); setSesion(s)
  }
  const logout = () => { borrarSesion(); setSesion(null) }
  const esAdmin = sesion?.rol === 'Administrador'

  return <Ctx.Provider value={{ sesion, login, logout, esAdmin }}>{children}</Ctx.Provider>
}
