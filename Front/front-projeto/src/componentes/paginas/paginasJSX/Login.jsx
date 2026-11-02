// paginas/paginasJSX/Login.jsx

import { useState } from 'react'
import TemplatePublico from '../../templates/templatesJSX/TemplatePublico'
import FormLogin from '../../organismos/organismosJSX/FormLogin'
import { apiFetch, apiJson, garantirCsrf } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { gravarSessao } from '../../../utils/sessao'
import '../paginasCSS/Login.css'

function Login() {
  const [erroApi, setErroApi] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function handleLogin(dados) {
    setErroApi('')
    setEnviando(true)
    try {
      await garantirCsrf()
      const response = await apiFetch(endpoints.login, {
        method: 'POST',
        body: JSON.stringify(dados),
      })
      if (!response.ok) {
        const body = await response.json().catch(() => ({}))
        const detalhes = Array.isArray(body.detalhes) ? body.detalhes.join(' ') : ''
        if (body.mensagem || detalhes) {
          setErroApi([body.mensagem, detalhes].filter(Boolean).join(' '))
        } else if (response.status === 401 || response.status === 400 || response.status === 429) {
          setErroApi('Usuário ou senha inválidos.')
        } else {
          setErroApi('Erro de conexão. Tente novamente.')
        }
        return
      }
      const texto = await response.text()
      let usuario = texto ? JSON.parse(texto) : null
      if (!usuario?.perfil) usuario = await apiJson(endpoints.sessao)
      gravarSessao(usuario)
      window.location.href = '/'
    } catch {
      setErroApi('Erro de conexão. Tente novamente.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <TemplatePublico>
      <div className="login">
        <FormLogin
          onSubmit={handleLogin}
          onRecuperar={() => { window.location.href = '/recuperar-senha' }}
          erroExterno={erroApi}
          enviando={enviando}
        />
      </div>
    </TemplatePublico>
  )
}

export default Login
