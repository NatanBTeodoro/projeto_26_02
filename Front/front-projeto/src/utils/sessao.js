const CHAVES = ['usuarioId', 'usuarioEmail', 'perfil']

export function gravarSessao(usuario) {
  if (!usuario) return
  localStorage.setItem('usuarioId', usuario.id != null ? String(usuario.id) : '')
  localStorage.setItem('usuarioEmail', usuario.email || '')
  localStorage.setItem('perfil', usuario.perfil || '')
}

export function lerSessao() {
  return {
    usuarioId: localStorage.getItem('usuarioId') || '',
    usuarioEmail: localStorage.getItem('usuarioEmail') || '',
    perfil: localStorage.getItem('perfil') || '',
  }
}

export function autenticado() {
  return Boolean(localStorage.getItem('perfil'))
}

export function limparSessao() {
  CHAVES.forEach((chave) => localStorage.removeItem(chave))
}
