// organismos/organismosJSX/FormUnidade.jsx
//
// USO: cadastro opcional de unidade (RF23).
// NECESSIDADE: a tela existe para testar o método quando o back expuser a rota.
//
// PROPS:
//   estabelecimentoId, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormUnidade estabelecimentoId="1" onSubmit={criar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormUnidade.css'

function FormUnidade({ estabelecimentoId = '', onSubmit, enviando = false }) {
  const [estabelecimento, setEstabelecimento] = useState(String(estabelecimentoId || ''))
  const [nome, setNome] = useState('')
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!nome.trim() || !estabelecimento) {
      setErro('Nome e estabelecimento são obrigatórios.')
      return
    }
    setErro('')
    onSubmit({ estabelecimentoId: Number(estabelecimento), nome: nome.trim() })
  }

  return (
    <form className="form-unidade formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="POST" caminho={endpoints.unidades} />
      <FormField id="unidade-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} obrigatorio />
      <FormField id="unidade-nome" rotulo="Nome da unidade" valor={nome} onChange={setNome} erro={erro} obrigatorio />
      <Botao texto={enviando ? 'Salvando...' : 'Cadastrar unidade'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormUnidade
