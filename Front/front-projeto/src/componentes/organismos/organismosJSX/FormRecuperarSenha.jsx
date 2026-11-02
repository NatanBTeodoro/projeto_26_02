// organismos/organismosJSX/FormRecuperarSenha.jsx
//
// USO: recuperação de senha por e-mail (RF32).
// NECESSIDADE: o pedido é só o e-mail, separado do login.
//
// PROPS:
//   onSubmit     (function)
//   onVoltar     (function)
//   erroExterno  (string)
//   enviando     (boolean)
//
// EXEMPLOS DE USO:
//   <FormRecuperarSenha onSubmit={recuperar} onVoltar={voltar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import Titulo from '../../atomos/atomosJSX/Titulo'
import Texto from '../../atomos/atomosJSX/Texto'
import '../organismosCSS/FormRecuperarSenha.css'

function FormRecuperarSenha({ onSubmit, onVoltar, erroExterno = '', enviando = false }) {
  const [email, setEmail] = useState('')
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!email.trim() || !email.includes('@')) {
      setErro('Informe um e-mail válido.')
      return
    }
    setErro('')
    onSubmit({ email: email.trim().toLowerCase() })
  }

  return (
    <form className="form-recuperar-senha" onSubmit={aoEnviar}>
      <Titulo texto="Recuperar senha" nivel={2} />
      <Texto>O back envia o fluxo por e-mail. Se o SMTP não estiver configurado, a resposta de erro aparece abaixo.</Texto>
      <FormField id="email-recuperar" rotulo="E-mail" tipo="email" valor={email} onChange={setEmail} erro={erro} obrigatorio />
      <MensagemErro texto={erroExterno} />
      <div className="formulario__acoes">
        <Botao texto={enviando ? 'Enviando...' : 'Enviar'} tipo="submit" desabilitado={enviando} />
        <Botao texto="Voltar ao login" variante="fantasma" onClick={onVoltar} />
      </div>
    </form>
  )
}

export default FormRecuperarSenha
