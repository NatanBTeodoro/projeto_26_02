// organismos/organismosJSX/FormPedido.jsx
//
// USO: registro manual de venda (RF02).
// NECESSIDADE: monta PedidoDTO.Create com ao menos um item. O custo não vai no corpo.
//
// PROPS:
//   estabelecimentoId, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormPedido estabelecimentoId="1" onSubmit={registrar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import { paraInstantLocal } from '../../../utils/formatacao'
import '../organismosCSS/FormPedido.css'

function itemVazio() {
  return { chave: crypto.randomUUID(), produtoId: '', quantidade: '1' }
}

function FormPedido({ estabelecimentoId = '', onSubmit, enviando = false }) {
  const [estabelecimento, setEstabelecimento] = useState(String(estabelecimentoId || ''))
  const [externalId, setExternalId] = useState('')
  const [dataHora, setDataHora] = useState('')
  const [itens, setItens] = useState([itemVazio()])
  const [erro, setErro] = useState('')

  function alterarItem(chave, campo, valor) {
    setItens((atual) => atual.map((item) => (item.chave === chave ? { ...item, [campo]: valor } : item)))
  }

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!estabelecimento) {
      setErro('Informe o estabelecimento.')
      return
    }
    const montados = itens
      .filter((item) => item.produtoId)
      .map((item) => ({ produtoId: Number(item.produtoId), quantidade: Number(item.quantidade) }))
    if (!montados.length) {
      setErro('Inclua ao menos um item com produto.')
      return
    }
    if (montados.some((item) => !Number.isInteger(item.quantidade) || item.quantidade < 1)) {
      setErro('A quantidade de cada item precisa ser um inteiro maior que zero.')
      return
    }
    if (dataHora) {
      const escolhida = new Date(dataHora)
      if (escolhida.getTime() > Date.now()) {
        setErro('A data da venda não pode ser futura.')
        return
      }
    }
    setErro('')
    const corpo = {
      estabelecimentoId: Number(estabelecimento),
      externalId: externalId.trim() || null,
      itens: montados,
    }
    if (dataHora) corpo.dataHora = paraInstantLocal(dataHora)
    onSubmit(corpo)
  }

  return (
    <form className="form-pedido formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo="POST" caminho={endpoints.pedidos} />
      <div className="formulario__linha">
        <FormField id="pedido-estabelecimento" rotulo="Estabelecimento" tipo="number" minimo="1" valor={estabelecimento} onChange={setEstabelecimento} obrigatorio />
        <FormField id="pedido-external" rotulo="Identificador externo" valor={externalId} onChange={setExternalId} dica="Opcional. Duplicado no mesmo estabelecimento é ignorado na importação." />
      </div>
      <FormField id="pedido-data" rotulo="Data e hora" tipo="datetime-local" valor={dataHora} onChange={setDataHora} dica="Vazio: o servidor define o instante." />
      <div className="form-pedido__itens">
        {itens.map((item, indice) => (
          <div className="formulario__linha" key={item.chave}>
            <FormField id={`pedido-produto-${item.chave}`} rotulo={`Produto ${indice + 1}`} tipo="number" minimo="1" valor={item.produtoId} onChange={(valor) => alterarItem(item.chave, 'produtoId', valor)} />
            <FormField id={`pedido-qtd-${item.chave}`} rotulo="Quantidade" tipo="number" minimo="1" passo="1" valor={item.quantidade} onChange={(valor) => alterarItem(item.chave, 'quantidade', valor)} />
          </div>
        ))}
      </div>
      <MensagemErro texto={erro} />
      <div className="formulario__acoes">
        <Botao texto="Adicionar item" variante="secundario" onClick={() => setItens((atual) => [...atual, itemVazio()])} />
        <Botao texto="Remover último" variante="fantasma" desabilitado={itens.length <= 1} onClick={() => setItens((atual) => atual.slice(0, -1))} />
        <Botao texto={enviando ? 'Registrando...' : 'Registrar venda'} tipo="submit" desabilitado={enviando} />
      </div>
    </form>
  )
}

export default FormPedido
