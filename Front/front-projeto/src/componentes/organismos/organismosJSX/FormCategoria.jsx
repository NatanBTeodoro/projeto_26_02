// organismos/organismosJSX/FormCategoria.jsx
//
// USO: criar e editar categoria (RF16).
// NECESSIDADE: corpo de CategoriaDTO.Create e Update.
//
// PROPS:
//   estabelecimentoId, onSubmit, enviando, modo, valoresIniciais
//
// EXEMPLOS DE USO:
//   <FormCategoria estabelecimentoId="1" onSubmit={criar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Checkbox from '../../atomos/atomosJSX/Checkbox'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormCategoria.css'

function FormCategoria({
  estabelecimentoId = '',
  onSubmit,
  enviando = false,
  modo = 'criar',
  valoresIniciais = null,
}) {
  const [estabelecimento, setEstabelecimento] = useState(String(valoresIniciais?.estabelecimentoId ?? estabelecimentoId ?? ''))
  const [nome, setNome] = useState(valoresIniciais?.nome || '')
  const [ativa, setAtiva] = useState(valoresIniciais?.ativa !== false)
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!nome.trim()) {
      setErro('Informe o nome.')
      return
    }
    if (modo === 'criar' && !estabelecimento) {
      setErro('Informe o estabelecimento.')
      return
    }
    setErro('')
    if (modo === 'criar') {
      onSubmit({ estabelecimentoId: Number(estabelecimento), nome: nome.trim() })
      return
    }
    onSubmit({ nome: nome.trim(), ativa })
  }

  return (
    <form className="form-categoria formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo={modo === 'criar' ? 'POST' : 'PUT'} caminho={modo === 'criar' ? endpoints.categorias : endpoints.categoria(':id')} />
      {modo === 'criar' ? (
        <FormField id="categoria-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} obrigatorio />
      ) : null}
      <FormField id="categoria-nome" rotulo="Nome" valor={nome} onChange={setNome} erro={erro} obrigatorio />
      {modo === 'editar' ? <Checkbox id="categoria-ativa" texto="Categoria ativa" marcado={ativa} onChange={setAtiva} /> : null}
      <Botao texto={enviando ? 'Salvando...' : modo === 'criar' ? 'Cadastrar categoria' : 'Salvar categoria'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormCategoria
