const CHAVES = ['usuarioId', 'usuarioEmail', 'perfil', 'estabelecimentoId']

export function gravarSessao(usuario) {
  if (!usuario) return
  localStorage.setItem('usuarioId', usuario.id != null ? String(usuario.id) : '')
  localStorage.setItem('usuarioEmail', usuario.email || '')
  localStorage.setItem('perfil', usuario.perfil || '')
  localStorage.setItem(
    'estabelecimentoId',
    usuario.estabelecimentoId != null ? String(usuario.estabelecimentoId) : '',
  )
}

export function lerSessao() {
  return {
    usuarioId: localStorage.getItem('usuarioId') || '',
    usuarioEmail: localStorage.getItem('usuarioEmail') || '',
    perfil: localStorage.getItem('perfil') || '',
    estabelecimentoId: localStorage.getItem('estabelecimentoId') || '',
  }
}

export function autenticado() {
  return Boolean(localStorage.getItem('perfil'))
}

export function limparSessao() {
  CHAVES.forEach((chave) => localStorage.removeItem(chave))
}
