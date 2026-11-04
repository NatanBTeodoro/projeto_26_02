import { useEffect, useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Select from '../../atomos/atomosJSX/Select'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarDataHora, formatarMoeda } from '../../../utils/formatacao'

function agoraLocal() {
  const data = new Date()
  data.setMinutes(data.getMinutes() - data.getTimezoneOffset())
  return data.toISOString().slice(0, 16)
}

function Pedidos() {
  const [produtos, setProdutos] = useState([])
  const [pedidos, setPedidos] = useState([])
  const [produtoId, setProdutoId] = useState('')
  const [quantidade, setQuantidade] = useState('1')
  const [itens, setItens] = useState([])
  const [dataHora, setDataHora] = useState(agoraLocal)
  const [motivos, setMotivos] = useState({})
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  async function carregar() {
    const [listaProdutos, listaPedidos] = await Promise.all([
      apiJson(endpoints.produtos),
      apiJson(endpoints.pedidos),
    ])
    setProdutos(listaProdutos.filter((produto) => produto.ativo))
    setPedidos(listaPedidos)
  }

  useEffect(() => {
    carregar().catch((falha) => setErro(falha.message))
  }, [])

  function incluirItem() {
    const produto = produtos.find((item) => String(item.id) === produtoId)
    if (!produto || Number(quantidade) < 1) {
      setErro('Escolha um produto e uma quantidade maior que zero.')
      return
    }
    setErro('')
    setItens((atual) => [...atual, { produtoId: produto.id, nome: produto.nome, quantidade: Number(quantidade) }])
    setQuantidade('1')
  }

  async function salvar(evento) {
    evento.preventDefault()
    setErro('')
    try {
      await apiJson(endpoints.pedidos, {
        method: 'POST',
        body: JSON.stringify({
          dataHora: new Date(dataHora).toISOString(),
          itens: itens.map((item) => ({ produtoId: item.produtoId, quantidade: item.quantidade })),
        }),
      })
      setItens([])
      setAviso('Pedido registrado. Preço e custo foram copiados do cardápio.')
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  async function estornar(pedido) {
    const motivo = (motivos[pedido.id] || '').trim()
    if (!motivo) {
      setErro('Informe o motivo do estorno.')
      return
    }
    setErro('')
    try {
      await apiJson(endpoints.estorno(pedido.id), {
        method: 'POST',
        body: JSON.stringify({ motivo }),
      })
      setAviso('Pedido estornado.')
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  const opcoes = [
    { valor: '', rotulo: 'Selecione' },
    ...produtos.map((produto) => ({ valor: String(produto.id), rotulo: produto.nome })),
  ]

  return (
    <TemplateAutenticado rotaAtual="/pedidos">
      <div className="pagina">
        <CabecalhoPagina
          titulo="Pedidos"
          descricao="O estorno guarda o motivo e tira a venda da matriz."
        />
        <MensagemErro texto={erro} />
        {aviso ? <p>{aviso}</p> : null}
        <section className="pagina__painel">
          <form className="formulario" onSubmit={salvar}>
            <FormField id="data" rotulo="Data e hora" tipo="datetime-local" valor={dataHora} onChange={setDataHora} obrigatorio />
            <div className="formulario__linha">
              <div>
                <label htmlFor="produto">Produto</label>
                <Select id="produto" valor={produtoId} onChange={setProdutoId} opcoes={opcoes} />
              </div>
              <FormField id="quantidade" rotulo="Quantidade" tipo="number" valor={quantidade} onChange={setQuantidade} />
            </div>
            <Botao texto="Adicionar item" variante="secundario" onClick={incluirItem} />
            <ul>
              {itens.map((item, indice) => (
                <li key={`${item.produtoId}-${indice}`}>{item.quantidade} × {item.nome}</li>
              ))}
            </ul>
            <Botao texto="Registrar pedido" tipo="submit" desabilitado={itens.length === 0} />
          </form>
        </section>
        <section className="pagina__painel">
          <table className="tabela">
            <thead>
              <tr>
                <th>Quando</th>
                <th>Itens</th>
                <th>Status</th>
                <th>Estorno</th>
              </tr>
            </thead>
            <tbody>
              {pedidos.map((pedido) => (
                <tr key={pedido.id}>
                  <td>{formatarDataHora(pedido.dataHora)}</td>
                  <td>
                    {pedido.itens.map((item) => (
                      <div key={item.id}>
                        {item.quantidade} × {item.produtoNome} ({formatarMoeda(item.precoUnitario)})
                      </div>
                    ))}
                  </td>
                  <td>{pedido.status === 'ESTORNADO' ? `Estornado: ${pedido.motivoEstorno}` : 'Ativo'}</td>
                  <td>
                    {pedido.status === 'ATIVO' ? (
                      <div className="formulario">
                        <FormField
                          id={`motivo-${pedido.id}`}
                          rotulo="Motivo"
                          valor={motivos[pedido.id] || ''}
                          onChange={(valor) => setMotivos((atual) => ({ ...atual, [pedido.id]: valor }))}
                        />
                        <Botao texto="Estornar" variante="perigo" onClick={() => estornar(pedido)} />
                      </div>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Pedidos
