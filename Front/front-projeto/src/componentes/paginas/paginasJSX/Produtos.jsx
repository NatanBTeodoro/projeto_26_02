import { useEffect, useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Select from '../../atomos/atomosJSX/Select'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarBooleano, formatarMoeda } from '../../../utils/formatacao'

const produtoVazio = {
  categoriaId: '',
  nome: '',
  precoAtual: '',
  custoAtual: '',
}

function Produtos() {
  const [categorias, setCategorias] = useState([])
  const [produtos, setProdutos] = useState([])
  const [nomeCategoria, setNomeCategoria] = useState('')
  const [form, setForm] = useState(produtoVazio)
  const [editando, setEditando] = useState(null)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  async function carregar() {
    const [listaCategorias, listaProdutos] = await Promise.all([
      apiJson(endpoints.categorias),
      apiJson(endpoints.produtos),
    ])
    setCategorias(listaCategorias)
    setProdutos(listaProdutos)
  }

  useEffect(() => {
    carregar().catch((falha) => setErro(falha.message))
  }, [])

  function alterar(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }))
  }

  async function criarCategoria(evento) {
    evento.preventDefault()
    setErro('')
    try {
      await apiJson(endpoints.categorias, {
        method: 'POST',
        body: JSON.stringify({ nome: nomeCategoria.trim() }),
      })
      setNomeCategoria('')
      setAviso('Categoria salva.')
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  async function alternarCategoria(categoria) {
    setErro('')
    try {
      await apiJson(endpoints.categoria(categoria.id), {
        method: 'PUT',
        body: JSON.stringify({ nome: categoria.nome, ativa: !categoria.ativa }),
      })
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  async function salvarProduto(evento) {
    evento.preventDefault()
    setErro('')
    const corpo = {
      categoriaId: Number(form.categoriaId),
      nome: form.nome.trim(),
      precoAtual: form.precoAtual,
      custoAtual: form.custoAtual === '' ? null : form.custoAtual,
    }
    try {
      if (editando) {
        corpo.ativo = editando.ativo
        await apiJson(endpoints.produto(editando.id), { method: 'PUT', body: JSON.stringify(corpo) })
      } else {
        await apiJson(endpoints.produtos, { method: 'POST', body: JSON.stringify(corpo) })
      }
      setForm(produtoVazio)
      setEditando(null)
      setAviso(editando ? 'Produto atualizado.' : 'Produto cadastrado.')
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  function editar(produto) {
    setEditando(produto)
    setForm({
      categoriaId: String(produto.categoriaId),
      nome: produto.nome,
      precoAtual: String(produto.precoAtual),
      custoAtual: produto.custoAtual == null ? '' : String(produto.custoAtual),
    })
  }

  async function alternarProduto(produto) {
    setErro('')
    try {
      await apiJson(endpoints.produto(produto.id), {
        method: 'PUT',
        body: JSON.stringify({
          categoriaId: produto.categoriaId,
          nome: produto.nome,
          precoAtual: produto.precoAtual,
          custoAtual: produto.custoAtual,
          ativo: !produto.ativo,
        }),
      })
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  const opcoesCategoria = [
    { valor: '', rotulo: 'Selecione' },
    ...categorias.filter((categoria) => categoria.ativa).map((categoria) => ({
      valor: String(categoria.id),
      rotulo: categoria.nome,
    })),
  ]

  return (
    <TemplateAutenticado rotaAtual="/produtos">
      <div className="pagina">
        <CabecalhoPagina
          titulo="Produtos"
          descricao="Categorias e cardápio. O custo digitado aqui entra nos pedidos novos."
        />
        <MensagemErro texto={erro} />
        {aviso ? <p>{aviso}</p> : null}
        <div className="pagina__grade pagina__grade--lateral">
          <section className="pagina__painel">
            <form className="formulario" onSubmit={criarCategoria}>
              <FormField id="categoria" rotulo="Nova categoria" valor={nomeCategoria} onChange={setNomeCategoria} obrigatorio />
              <Botao texto="Salvar categoria" tipo="submit" />
            </form>
            <ul>
              {categorias.map((categoria) => (
                <li key={categoria.id}>
                  {categoria.nome} ({formatarBooleano(categoria.ativa)}){' '}
                  <Botao
                    texto={categoria.ativa ? 'Inativar' : 'Reativar'}
                    variante="fantasma"
                    onClick={() => alternarCategoria(categoria)}
                  />
                </li>
              ))}
            </ul>
          </section>
          <section className="pagina__painel">
            <form className="formulario" onSubmit={salvarProduto}>
              <label htmlFor="categoria-produto">Categoria</label>
              <Select id="categoria-produto" valor={form.categoriaId} onChange={(valor) => alterar('categoriaId', valor)} opcoes={opcoesCategoria} />
              <FormField id="nome" rotulo="Nome" valor={form.nome} onChange={(valor) => alterar('nome', valor)} obrigatorio />
              <div className="formulario__linha">
                <FormField id="preco" rotulo="Preço" tipo="number" valor={form.precoAtual} onChange={(valor) => alterar('precoAtual', valor)} obrigatorio />
                <FormField id="custo" rotulo="Custo" tipo="number" valor={form.custoAtual} onChange={(valor) => alterar('custoAtual', valor)} />
              </div>
              <div className="formulario__acoes">
                <Botao texto={editando ? 'Atualizar produto' : 'Cadastrar produto'} tipo="submit" />
                {editando ? (
                  <Botao texto="Cancelar" variante="fantasma" onClick={() => { setEditando(null); setForm(produtoVazio) }} />
                ) : null}
              </div>
            </form>
            <table className="tabela">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Categoria</th>
                  <th>Preço</th>
                  <th>Custo</th>
                  <th>Ativo</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {produtos.map((produto) => (
                  <tr key={produto.id}>
                    <td>{produto.nome}</td>
                    <td>{produto.categoriaNome}</td>
                    <td>{formatarMoeda(produto.precoAtual)}</td>
                    <td>{formatarMoeda(produto.custoAtual)}</td>
                    <td>{formatarBooleano(produto.ativo)}</td>
                    <td>
                      <Botao texto="Editar" variante="secundario" onClick={() => editar(produto)} />
                      <Botao texto={produto.ativo ? 'Inativar' : 'Reativar'} variante="fantasma" onClick={() => alternarProduto(produto)} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default Produtos
