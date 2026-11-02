// organismos/organismosJSX/FormEstorno.jsx
//
// USO: estorno de venda (RF19). O pedido original permanece.
// NECESSIDADE: envia só o motivo, como PedidoDTO.Estorno.
//
// PROPS:
//   pedidoId, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormEstorno pedidoId="10" onSubmit={estornar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import FormFieldTextarea from '../../moleculas/moleculasJSX/FormFieldTextarea'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormEstorno.css'

function FormEstorno({ pedidoId = '', onSubmit, enviando = false }) {
  const [id, setId] = useState(String(pedidoId || ''))
  const [motivo, setMotivo] = useState('')
  const [erros, setErros] = useState({})

  function aoEnviar(evento) {
    evento.preventDefault()
    const proximos = {}
    if (!id) proximos.id = 'Informe o pedido.'
    if (!motivo.trim()) proximos.motivo = 'Informe o motivo do estorno.'
    setErros(proximos)
    if (Object.keys(proximos).length) return
    onSubmit(Number(id), { motivo: motivo.trim() })
  }

  return (
    <form className="form-estorno formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="POST" caminho={endpoints.estorno(id || ':id')} />
      <FormField id="estorno-pedido" rotulo="Pedido" tipo="number" minimo="1" valor={id} onChange={setId} erro={erros.id} obrigatorio />
      <FormFieldTextarea id="estorno-motivo" rotulo="Motivo" valor={motivo} onChange={setMotivo} erro={erros.motivo} obrigatorio />
      <Botao texto={enviando ? 'Estornando...' : 'Estornar venda'} tipo="submit" variante="perigo" desabilitado={enviando} />
    </form>
  )
}

export default FormEstorno
