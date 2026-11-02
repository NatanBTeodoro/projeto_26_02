// organismos/organismosJSX/FormConfigAnalise.jsx
//
// USO: parâmetros de análise do estabelecimento (RF22).
// NECESSIDADE: o PUT segue ConfigAnaliseDTO.Update.
//
// PROPS:
//   valoresIniciais, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormConfigAnalise valoresIniciais={config} onSubmit={salvar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import FormFieldSelect from '../../moleculas/moleculasJSX/FormFieldSelect'
import Checkbox from '../../atomos/atomosJSX/Checkbox'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormConfigAnalise.css'

const CRITERIOS = [
  { valor: 'PONDERADA', rotulo: 'Margem média ponderada' },
  { valor: 'SIMPLES', rotulo: 'Margem média simples' },
]

function FormConfigAnalise({ valoresIniciais = null, onSubmit, enviando = false }) {
  const [suporteMin, setSuporteMin] = useState(String(valoresIniciais?.suporteMin ?? '0.02'))
  const [confiancaMin, setConfiancaMin] = useState(String(valoresIniciais?.confiancaMin ?? '0.30'))
  const [liftMin, setLiftMin] = useState(String(valoresIniciais?.liftMin ?? '1'))
  const [periodoMinimoDias, setPeriodoMinimoDias] = useState(String(valoresIniciais?.periodoMinimoDias ?? '30'))
  const [nAlertaCao, setNAlertaCao] = useState(String(valoresIniciais?.nAlertaCao ?? '3'))
  const [criterio, setCriterio] = useState(valoresIniciais?.criterioPopularidade || 'PONDERADA')
  const [escopo, setEscopo] = useState(valoresIniciais?.escopoPorCategoria !== false)
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if ([suporteMin, confiancaMin, liftMin, periodoMinimoDias, nAlertaCao].some((valor) => valor === '' || Number(valor) < 0)) {
      setErro('Nenhum parâmetro pode ficar vazio ou negativo.')
      return
    }
    if (Number(periodoMinimoDias) < 1 || Number(nAlertaCao) < 1) {
      setErro('Período mínimo e N do alerta precisam ser pelo menos 1.')
      return
    }
    setErro('')
    onSubmit({
      suporteMin,
      confiancaMin,
      liftMin,
      periodoMinimoDias: Number(periodoMinimoDias),
      nAlertaCao: Number(nAlertaCao),
      criterioPopularidade: criterio,
      escopoPorCategoria: escopo,
    })
  }

  return (
    <form className="form-config-analise formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="PUT" caminho={endpoints.configAnalise} />
      <div className="formulario__linha">
        <FormField id="cfg-suporte" rotulo="Suporte mínimo" tipo="number" passo="0.01" minimo="0" valor={suporteMin} onChange={setSuporteMin} obrigatorio />
        <FormField id="cfg-confianca" rotulo="Confiança mínima" tipo="number" passo="0.01" minimo="0" valor={confiancaMin} onChange={setConfiancaMin} obrigatorio />
      </div>
      <div className="formulario__linha">
        <FormField id="cfg-lift" rotulo="Lift mínimo" tipo="number" passo="0.01" minimo="0" valor={liftMin} onChange={setLiftMin} obrigatorio />
        <FormField id="cfg-dias" rotulo="Período mínimo (dias)" tipo="number" minimo="1" passo="1" valor={periodoMinimoDias} onChange={setPeriodoMinimoDias} obrigatorio />
      </div>
      <FormField id="cfg-n" rotulo="Períodos consecutivos para alerta de Cão" tipo="number" minimo="1" passo="1" valor={nAlertaCao} onChange={setNAlertaCao} erro={erro} obrigatorio />
      <FormFieldSelect id="cfg-criterio" rotulo="Critério de margem" valor={criterio} onChange={setCriterio} opcoes={CRITERIOS} />
      <Checkbox id="cfg-escopo" texto="Classificar por categoria" marcado={escopo} onChange={setEscopo} />
      <Botao texto={enviando ? 'Salvando...' : 'Salvar parâmetros'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormConfigAnalise
