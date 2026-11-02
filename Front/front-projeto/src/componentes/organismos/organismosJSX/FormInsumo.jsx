// organismos/organismosJSX/FormInsumo.jsx
//
// USO: criar e editar insumo (RF18).
// NECESSIDADE: corpo de InsumoDTO.Create e Update.
//
// PROPS:
//   estabelecimentoId, onSubmit, enviando, modo, valoresIniciais
//
// EXEMPLOS DE USO:
//   <FormInsumo estabelecimentoId="1" onSubmit={criar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Checkbox from '../../atomos/atomosJSX/Checkbox'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormInsumo.css'

function FormInsumo({
  estabelecimentoId = '',
  onSubmit,
  enviando = false,
  modo = 'criar',
  valoresIniciais = null,
}) {
  const [estabelecimento, setEstabelecimento] = useState(String(valoresIniciais?.estabelecimentoId ?? estabelecimentoId ?? ''))
  const [nome, setNome] = useState(valoresIniciais?.nome || '')
  const [unidade, setUnidade] = useState(valoresIniciais?.unidadeMedida || 'un')
  const [custo, setCusto] = useState(valoresIniciais?.custoUnitario != null ? String(valoresIniciais.custoUnitario) : '')
  const [ativo, setAtivo] = useState(valoresIniciais?.ativo !== false)
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!nome.trim() || !unidade.trim()) {
      setErro('Nome e unidade de medida são obrigatórios.')
      return
    }
    if (modo === 'criar' && !estabelecimento) {
      setErro('Informe o estabelecimento.')
      return
    }
    setErro('')
    const corpo = {
      nome: nome.trim(),
      unidadeMedida: unidade.trim(),
      custoUnitario: custo === '' ? null : custo,
    }
    if (modo === 'criar') corpo.estabelecimentoId = Number(estabelecimento)
    else corpo.ativo = ativo
    onSubmit(corpo)
  }

  return (
    <form className="form-insumo formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo={modo === 'criar' ? 'POST' : 'PUT'} caminho={modo === 'criar' ? endpoints.insumos : endpoints.insumo(':id')} />
      {modo === 'criar' ? (
        <FormField id="insumo-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} obrigatorio />
      ) : null}
      <FormField id="insumo-nome" rotulo="Nome" valor={nome} onChange={setNome} erro={erro} obrigatorio />
      <div className="formulario__linha">
        <FormField id="insumo-unidade" rotulo="Unidade" valor={unidade} onChange={setUnidade} placeholder="kg, L, un" obrigatorio />
        <FormField id="insumo-custo" rotulo="Custo unitário" tipo="number" passo="0.01" minimo="0" valor={custo} onChange={setCusto} />
      </div>
      {modo === 'editar' ? <Checkbox id="insumo-ativo" texto="Insumo ativo" marcado={ativo} onChange={setAtivo} /> : null}
      <Botao texto={enviando ? 'Salvando...' : modo === 'criar' ? 'Cadastrar insumo' : 'Salvar insumo'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormInsumo
