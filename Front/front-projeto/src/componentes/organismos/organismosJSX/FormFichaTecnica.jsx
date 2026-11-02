// organismos/organismosJSX/FormFichaTecnica.jsx
//
// USO: incluir insumo na ficha técnica do produto (RF18).
// NECESSIDADE: corpo de FichaTecnicaItemDTO.Create.
//
// PROPS:
//   produtoId, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormFichaTecnica produtoId="4" onSubmit={adicionar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormFichaTecnica.css'

function FormFichaTecnica({ produtoId = '', onSubmit, enviando = false }) {
  const [insumoId, setInsumoId] = useState('')
  const [quantidade, setQuantidade] = useState('')
  const [erro, setErro] = useState('')

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!produtoId || !insumoId || !quantidade || Number(quantidade) <= 0) {
      setErro('Insumo e quantidade maior que zero são obrigatórios.')
      return
    }
    setErro('')
    onSubmit({
      produtoId: Number(produtoId),
      insumoId: Number(insumoId),
      quantidade,
    })
  }

  return (
    <form className="form-ficha-tecnica formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="POST" caminho={endpoints.fichaTecnica(produtoId || ':id')} />
      <div className="formulario__linha">
        <FormField id="ficha-insumo" rotulo="Insumo" tipo="number" minimo="1" valor={insumoId} onChange={setInsumoId} erro={erro} obrigatorio />
        <FormField id="ficha-quantidade" rotulo="Quantidade" tipo="number" passo="0.0001" minimo="0.0001" valor={quantidade} onChange={setQuantidade} obrigatorio />
      </div>
      <Botao texto={enviando ? 'Incluindo...' : 'Incluir na ficha'} tipo="submit" desabilitado={enviando} />
    </form>
  )
}

export default FormFichaTecnica
