// organismos/organismosJSX/FormEstabelecimento.jsx
//
// USO: criar ou renomear o estabelecimento.
// NECESSIDADE: o primeiro cadastro precisa existir antes dos demais métodos.
//
// PROPS:
//   onSubmit, enviando, modo, valoresIniciais
//
// EXEMPLOS DE USO:
//   <FormEstabelecimento onSubmit={criar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Checkbox from '../../atomos/atomosJSX/Checkbox'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormEstabelecimento.css'

function FormEstabelecimento({ onSubmit, enviando = false, modo = 'criar', valoresIniciais = null }) {
  const [nome, setNome] = useState(valoresIniciais?.nome || '')
  const [ativo, setAtivo] = useState(valoresIniciais?.ativo !== false)
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!nome.trim()) {
      setErro('Informe o nome.')
      return
    }
    setErro('')
    if (modo === 'criar') onSubmit({ nome: nome.trim() })
    else onSubmit({ nome: nome.trim(), ativo })
  }

  return (
    <form className="form-estabelecimento formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo={modo === 'criar' ? 'POST' : 'PUT'} caminho={modo === 'criar' ? endpoints.estabelecimentos : endpoints.estabelecimento(':id')} />
      <FormField id="estabelecimento-nome" rotulo="Nome" valor={nome} onChange={setNome} erro={erro} obrigatorio />
      {modo === 'editar' ? <Checkbox id="estabelecimento-ativo" texto="Ativo" marcado={ativo} onChange={setAtivo} /> : null}
      <Botao texto={enviando ? 'Salvando...' : modo === 'criar' ? 'Cadastrar' : 'Salvar'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormEstabelecimento
