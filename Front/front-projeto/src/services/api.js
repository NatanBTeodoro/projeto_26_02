import { limparSessao } from '../utils/sessao'

const definida = import.meta.env.VITE_API_URL
export const API_BASE = definida == null || definida === ''
  ? ''
  : String(definida).replace(/\/$/, '')

let tokenCsrf = ''

function lerCookie(nome) {
  const alvo = `${nome}=`
  const parte = document.cookie.split('; ').find((item) => item.startsWith(alvo))
  return parte ? decodeURIComponent(parte.slice(alvo.length)) : ''
}

export async function garantirCsrf() {
  const cookie = lerCookie('XSRF-TOKEN')
  if (cookie) {
    tokenCsrf = cookie
    return tokenCsrf
  }
  if (tokenCsrf) return tokenCsrf

  const response = await fetch(`${API_BASE}/api/auth/csrf`, { credentials: 'include' })
  const renovado = lerCookie('XSRF-TOKEN')
  if (renovado) {
    tokenCsrf = renovado
    return tokenCsrf
  }
  try {
    const corpo = await response.json()
    tokenCsrf = corpo.token || corpo.headerValue || ''
  } catch {
    tokenCsrf = ''
  }
  return tokenCsrf
}

export function esquecerCsrf() {
  tokenCsrf = ''
}

export async function apiFetch(caminho, opcoes = {}) {
  const metodo = String(opcoes.method || 'GET').toUpperCase()
  const headers = new Headers(opcoes.headers || {})
  const corpoFormulario = typeof FormData !== 'undefined' && opcoes.body instanceof FormData

  if (!corpoFormulario && opcoes.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  headers.set('Accept', headers.get('Accept') || 'application/json')

  if (metodo !== 'GET' && metodo !== 'HEAD') {
    const token = await garantirCsrf()
    if (token) headers.set('X-XSRF-TOKEN', token)
  }

  return fetch(`${API_BASE}${caminho}`, {
    ...opcoes,
    method: metodo,
    headers,
    credentials: 'include',
  })
}

function mensagemDoCorpo(corpo, status) {
  if (!corpo || typeof corpo !== 'object') return `Erro ${status}`
  const detalhes = Array.isArray(corpo.detalhes) ? corpo.detalhes.filter(Boolean) : []
  const base = corpo.mensagem || corpo.erro || `Erro ${status}`
  return detalhes.length ? `${base} ${detalhes.join(' ')}` : base
}

export async function apiJson(caminho, opcoes = {}) {
  const response = await apiFetch(caminho, opcoes)
  if (response.status === 204) return null

  const texto = await response.text()
  let corpo = null
  if (texto) {
    try {
      corpo = JSON.parse(texto)
    } catch {
      corpo = { mensagem: texto }
    }
  }

  if (!response.ok) {
    if (response.status === 401 && !caminho.includes('/api/auth/login')) {
      limparSessao()
      esquecerCsrf()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    const erro = new Error(mensagemDoCorpo(corpo, response.status))
    erro.status = response.status
    erro.corpo = corpo
    throw erro
  }

  return corpo
}

export async function baixarArquivo(caminho, nomeFallback) {
  const response = await apiFetch(caminho, { headers: { Accept: '*/*' } })
  if (!response.ok) {
    const texto = await response.text()
    let corpo = null
    try {
      corpo = texto ? JSON.parse(texto) : null
    } catch {
      corpo = { mensagem: texto }
    }
    const erro = new Error(mensagemDoCorpo(corpo, response.status))
    erro.status = response.status
    erro.corpo = corpo
    throw erro
  }

  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = nomeFallback
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
