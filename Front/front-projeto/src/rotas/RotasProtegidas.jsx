import { Navigate } from 'react-router-dom'
import { autenticado, lerSessao } from '../utils/sessao'

export function RotaAutenticada({ children }) {
  if (!autenticado()) return <Navigate to="/login" replace />
  return children
}

export function RotaPerfil({ perfis, children }) {
  if (!autenticado()) return <Navigate to="/login" replace />
  const { perfil } = lerSessao()
  if (perfis?.length && !perfis.includes(perfil)) return <Navigate to="/" replace />
  return children
}
