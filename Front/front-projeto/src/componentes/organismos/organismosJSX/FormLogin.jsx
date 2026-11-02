// organismos/organismosJSX/FormLogin.jsx
//
// USO: tela de acesso (RF13).
// NECESSIDADE: valida e-mail e senha antes de chamar a API.
//
// PROPS:
//   onSubmit      (function) — recebe { email, senha }
//   onRecuperar   (function)
//   erroExterno   (string)
//   enviando      (boolean)
//
// EXEMPLOS DE USO:
//   <FormLogin onSubmit={entrar} onRecuperar={irRecuperar} erroExterno={erro} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import FormFieldSenha from '../../moleculas/moleculasJSX/FormFieldSenha'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import Titulo from '../../atomos/atomosJSX/Titulo'
import Texto from '../../atomos/atomosJSX/Texto'
import '../organismosCSS/FormLogin.css'

function FormLogin({ onSubmit, onRecuperar, erroExterno = '', enviando = false }) {
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erros, setErros] = useState({})

  function validar() {
    const proximos = {}
    if (!email.trim() || !email.includes('@')) proximos.email = 'Informe um e-mail válido.'
    if (!senha) proximos.senha = 'Informe a senha.'
    setErros(proximos)
    return Object.keys(proximos).length === 0
  }

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!validar()) return
    onSubmit({ email: email.trim().toLowerCase(), senha })
  }

  return (
    <form className="form-login" onSubmit={aoEnviar}>
      <Titulo texto="Entrar" nivel={2} />
      <Texto>A sessão fica no cookie do servidor. O front só guarda perfil e estabelecimento para a navegação.</Texto>
      <FormField id="email" rotulo="E-mail" tipo="email" valor={email} onChange={setEmail} erro={erros.email} obrigatorio />
      <FormFieldSenha id="senha" rotulo="Senha" valor={senha} onChange={setSenha} erro={erros.senha} obrigatorio />
      <MensagemErro texto={erroExterno} />
      <div className="formulario__acoes">
        <Botao texto={enviando ? 'Entrando...' : 'Entrar'} tipo="submit" desabilitado={enviando} />
        <Botao texto="Recuperar senha" variante="fantasma" onClick={onRecuperar} />
      </div>
    </form>
  )
}

export default FormLogin
