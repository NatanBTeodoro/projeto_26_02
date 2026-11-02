// organismos/organismosJSX/FiltroPeriodo.jsx
//
// USO: recorte de dia, mês, trimestre ou ano nas telas de relatório.
// NECESSIDADE: o mesmo par de datas alimenta dashboard, ABC e comparativo.
//
// PROPS:
//   onAplicar (function) — recebe { inicio, fim } já em ISO
//   rotuloBotao (string)
//
// EXEMPLOS DE USO:
//   <FiltroPeriodo onAplicar={buscar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import { paraInstantLocal, periodoMesAtual } from '../../../utils/formatacao'
import '../organismosCSS/FiltroPeriodo.css'

function FiltroPeriodo({ onAplicar, rotuloBotao = 'Aplicar período', idPrefixo = 'periodo' }) {
  const mes = periodoMesAtual()
  const [inicio, setInicio] = useState(mes.inicio)
  const [fim, setFim] = useState(mes.fim)
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!inicio || !fim || new Date(fim) < new Date(inicio)) {
      setErro('Informe um intervalo válido.')
      return
    }
    setErro('')
    onAplicar({
      inicio: paraInstantLocal(inicio),
      fim: paraInstantLocal(fim),
    })
  }

  return (
    <form className="filtro-periodo formulario" onSubmit={aoEnviar}>
      <div className="formulario__linha">
        <FormField id={`${idPrefixo}-inicio`} rotulo="Início" tipo="datetime-local" valor={inicio} onChange={setInicio} obrigatorio />
        <FormField id={`${idPrefixo}-fim`} rotulo="Fim" tipo="datetime-local" valor={fim} onChange={setFim} erro={erro} obrigatorio />
      </div>
      <Botao texto={rotuloBotao} tipo="submit" />
    </form>
  )
}

export default FiltroPeriodo
