// organismos/organismosJSX/FormUsuario.jsx
//
// USO: cadastro e edição de usuário pelo administrador (RF17).
// NECESSIDADE: aplica a política de senha antes do POST e não reenvia senha vazia na edição.
//
// PROPS:
//   estabelecimentoId, onSubmit, enviando, modo, valoresIniciais
//
// EXEMPLOS DE USO:
//   <FormUsuario estabelecimentoId="1" onSubmit={criar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import FormFieldSenha from '../../moleculas/moleculasJSX/FormFieldSenha'
import FormFieldSelect from '../../moleculas/moleculasJSX/FormFieldSelect'
import Checkbox from '../../atomos/atomosJSX/Checkbox'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import { validarSenha } from '../../../utils/formatacao'
import '../organismosCSS/FormUsuario.css'

const PERFIS = [
  { valor: 'ADMINISTRADOR', rotulo: 'Administrador' },
  { valor: 'GESTOR', rotulo: 'Gestor' },
  { valor: 'OPERADOR', rotulo: 'Operador' },
  { valor: 'ANALISTA', rotulo: 'Analista' },
]

function FormUsuario({
  estabelecimentoId = '',
  onSubmit,
  enviando = false,
  modo = 'criar',
  valoresIniciais = null,
}) {
  const [estabelecimento, setEstabelecimento] = useState(String(valoresIniciais?.estabelecimentoId ?? estabelecimentoId ?? ''))
  const [email, setEmail] = useState(valoresIniciais?.email || '')
  const [senha, setSenha] = useState('')
  const [perfil, setPerfil] = useState(valoresIniciais?.perfil || 'OPERADOR')
  const [ativo, setAtivo] = useState(valoresIniciais?.ativo !== false)
  const [erros, setErros] = useState({})

  function aoEnviar(evento) {
    evento.preventDefault()
    const proximos = {}
    if (!email.trim() || !email.includes('@')) proximos.email = 'Informe um e-mail válido.'
    if (modo === 'criar' || senha) {
      const falhaSenha = validarSenha(senha)
      if (falhaSenha) proximos.senha = falhaSenha
    }
    if (modo === 'criar' && !estabelecimento) proximos.estabelecimento = 'Informe o estabelecimento.'
    setErros(proximos)
    if (Object.keys(proximos).length) return

    if (modo === 'criar') {
      onSubmit({
        estabelecimentoId: Number(estabelecimento),
        email: email.trim().toLowerCase(),
        senha,
        perfil,
      })
      return
    }
    const corpo = { email: email.trim().toLowerCase(), perfil, ativo }
    if (senha) corpo.senha = senha
    onSubmit(corpo)
  }

  return (
    <form className="form-usuario formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo={modo === 'criar' ? 'POST' : 'PUT'} caminho={modo === 'criar' ? endpoints.usuarios : endpoints.usuario(':id')} />
      {modo === 'criar' ? (
        <FormField id="usuario-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} erro={erros.estabelecimento} obrigatorio />
      ) : null}
      <FormField id="usuario-email" rotulo="E-mail" tipo="email" valor={email} onChange={setEmail} erro={erros.email} obrigatorio />
      <FormFieldSenha
        id="usuario-senha"
        rotulo={modo === 'criar' ? 'Senha' : 'Nova senha'}
        valor={senha}
        onChange={setSenha}
        erro={erros.senha}
        obrigatorio={modo === 'criar'}
        dica="Entre 8 e 72 caracteres, com letra e número."
      />
      <FormFieldSelect id="usuario-perfil" rotulo="Perfil" valor={perfil} onChange={setPerfil} opcoes={PERFIS} obrigatorio />
      {modo === 'editar' ? <Checkbox id="usuario-ativo" texto="Usuário ativo" marcado={ativo} onChange={setAtivo} /> : null}
      <Botao texto={enviando ? 'Salvando...' : modo === 'criar' ? 'Cadastrar usuário' : 'Salvar usuário'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormUsuario
