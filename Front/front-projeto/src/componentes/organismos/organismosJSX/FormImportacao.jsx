// organismos/organismosJSX/FormImportacao.jsx
//
// USO: pré-visualizar e confirmar CSV de vendas (RF03, RF20).
// NECESSIDADE: envia multipart sem definir Content-Type, para o boundary do navegador.
//
// PROPS:
//   estabelecimentoId, onPrevisualizar, onConfirmar, enviando
//
// EXEMPLOS DE USO:
//   <FormImportacao estabelecimentoId="1" onPrevisualizar={ver} onConfirmar={gravar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormImportacao.css'

function montarFormulario(arquivo, estabelecimentoId) {
  const dados = new FormData()
  dados.append('arquivo', arquivo)
  dados.append('estabelecimentoId', String(estabelecimentoId))
  return dados
}

function FormImportacao({ estabelecimentoId = '', onPrevisualizar, onConfirmar, enviando = false }) {
  const [estabelecimento, setEstabelecimento] = useState(String(estabelecimentoId || ''))
  const [arquivo, setArquivo] = useState(null)
  const [erro, setErro] = useState('')

  function validar() {
    if (!estabelecimento || !arquivo) {
      setErro('Escolha o arquivo CSV e o estabelecimento.')
      return false
    }
    setErro('')
    return true
  }

  return (
    <form className="form-importacao formulario" onSubmit={(evento) => evento.preventDefault()}>
      <LegendaMetodo metodo="POST" caminho={`${endpoints.previsualizarCsv} e ${endpoints.importacoes}`} />
      <FormField id="csv-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} obrigatorio />
      <FormField id="csv-arquivo" rotulo="Arquivo CSV" tipo="file" onChange={setArquivo} erro={erro} dica="UTF-8, separador vírgula ou ponto e vírgula." />
      <div className="formulario__acoes">
        <Botao
          texto="Pré-visualizar"
          variante="secundario"
          desabilitado={enviando}
          onClick={() => validar() && onPrevisualizar(montarFormulario(arquivo, estabelecimento))}
        />
        <Botao
          texto={enviando ? 'Enviando...' : 'Confirmar importação'}
          desabilitado={enviando}
          onClick={() => validar() && onConfirmar(montarFormulario(arquivo, estabelecimento))}
        />
      </div>
    </form>
  )
}

export default FormImportacao
