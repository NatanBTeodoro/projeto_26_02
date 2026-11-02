// organismos/organismosJSX/FormSimulacao.jsx
//
// USO: simulação de preço descartável (RF14, RN20).
// NECESSIDADE: deixa visível que a popularidade permanece constante.
//
// PROPS:
//   produtoId, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormSimulacao produtoId="4" onSubmit={simular} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import { paraInstantLocal, periodoMesAtual } from '../../../utils/formatacao'
import '../organismosCSS/FormSimulacao.css'

function FormSimulacao({ produtoId = '', onSubmit, enviando = false }) {
  const periodo = periodoMesAtual()
  const [id, setId] = useState(String(produtoId || ''))
  const [precoNovo, setPrecoNovo] = useState('')
  const [inicio, setInicio] = useState(periodo.inicio)
  const [fim, setFim] = useState(periodo.fim)
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!id || precoNovo === '' || Number(precoNovo) < 0 || !inicio || !fim) {
      setErro('Produto, preço novo e período são obrigatórios.')
      return
    }
    setErro('')
    onSubmit({
      produtoId: Number(id),
      precoNovo,
      periodoIni: paraInstantLocal(inicio),
      periodoFim: paraInstantLocal(fim),
    })
  }

  return (
    <form className="form-simulacao formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="POST" caminho={endpoints.simular} />
      <p className="aviso-claro">
        A simulação não altera o cardápio. A popularidade permanece a do período; a margem média do cardápio é recalculada com o preço novo.
      </p>
      <div className="formulario__linha">
        <FormField id="sim-produto" rotulo="Produto" tipo="number" minimo="1" valor={id} onChange={setId} obrigatorio />
        <FormField id="sim-preco" rotulo="Preço novo" tipo="number" passo="0.01" minimo="0" valor={precoNovo} onChange={setPrecoNovo} erro={erro} obrigatorio />
      </div>
      <div className="formulario__linha">
        <FormField id="sim-inicio" rotulo="Início" tipo="datetime-local" valor={inicio} onChange={setInicio} obrigatorio />
        <FormField id="sim-fim" rotulo="Fim" tipo="datetime-local" valor={fim} onChange={setFim} obrigatorio />
      </div>
      <Botao texto={enviando ? 'Simulando...' : 'Simular'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormSimulacao
