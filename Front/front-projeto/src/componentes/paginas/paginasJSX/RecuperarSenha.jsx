// paginas/paginasJSX/RecuperarSenha.jsx

import { useState } from 'react'
import TemplatePublico from '../../templates/templatesJSX/TemplatePublico'
import FormRecuperarSenha from '../../organismos/organismosJSX/FormRecuperarSenha'
import MensagemSucesso from '../../atomos/atomosJSX/MensagemSucesso'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import '../paginasCSS/RecuperarSenha.css'

function RecuperarSenha() {
  const [erroApi, setErroApi] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function recuperar(dados) {
    setErroApi('')
    setSucesso('')
    setEnviando(true)
    try {
      await apiJson(endpoints.recuperarSenha, {
        method: 'POST',
        body: JSON.stringify(dados),
      })
      setSucesso('Se o e-mail existir, o back segue o fluxo de recuperação.')
    } catch (erro) {
      setErroApi(erro.message || 'Erro de conexão. Tente novamente.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <TemplatePublico>
      <div className="recuperar-senha">
        <FormRecuperarSenha
          onSubmit={recuperar}
          onVoltar={() => { window.location.href = '/login' }}
          erroExterno={erroApi}
          enviando={enviando}
        />
        <MensagemSucesso texto={sucesso} />
      </div>
    </TemplatePublico>
  )
}

export default RecuperarSenha
