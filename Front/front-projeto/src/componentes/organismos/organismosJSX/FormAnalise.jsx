// organismos/organismosJSX/FormAnalise.jsx
//
// USO: dispara uma execução de análise (RF09, RF11).
// NECESSIDADE: o corpo segue AnaliseDTO.Create; o cálculo não acontece aqui.
//
// PROPS:
//   estabelecimentoId, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormAnalise estabelecimentoId="1" onSubmit={iniciar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import FormFieldSelect from '../../moleculas/moleculasJSX/FormFieldSelect'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import { paraInstantLocal, periodoMesAtual } from '../../../utils/formatacao'
import '../organismosCSS/FormAnalise.css'

const GRANULARIDADES = [
  { valor: 'DIA', rotulo: 'Dia' },
  { valor: 'MES', rotulo: 'Mês' },
  { valor: 'TRIMESTRE', rotulo: 'Trimestre' },
  { valor: 'ANO', rotulo: 'Ano' },
]

function FormAnalise({ estabelecimentoId = '', onSubmit, enviando = false }) {
  const periodo = periodoMesAtual()
  const [estabelecimento, setEstabelecimento] = useState(String(estabelecimentoId || ''))
  const [inicio, setInicio] = useState(periodo.inicio)
  const [fim, setFim] = useState(periodo.fim)
  const [granularidade, setGranularidade] = useState('MES')
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!estabelecimento || !inicio || !fim) {
      setErro('Estabelecimento e período são obrigatórios.')
      return
    }
    if (new Date(fim) < new Date(inicio)) {
      setErro('O fim do período precisa ser depois do início.')
      return
    }
    setErro('')
    onSubmit({
      estabelecimentoId: Number(estabelecimento),
      periodoIni: paraInstantLocal(inicio),
      periodoFim: paraInstantLocal(fim),
      granularidade,
    })
  }

  return (
    <form className="form-analise formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="POST" caminho={endpoints.analises} />
      <FormField id="analise-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} obrigatorio />
      <div className="formulario__linha">
        <FormField id="analise-inicio" rotulo="Início" tipo="datetime-local" valor={inicio} onChange={setInicio} obrigatorio />
        <FormField id="analise-fim" rotulo="Fim" tipo="datetime-local" valor={fim} onChange={setFim} erro={erro} obrigatorio />
      </div>
      <FormFieldSelect id="analise-granularidade" rotulo="Granularidade" valor={granularidade} onChange={setGranularidade} opcoes={GRANULARIDADES} />
      <Botao texto={enviando ? 'Enfileirando...' : 'Gerar análise'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormAnalise
