// paginas/paginasJSX/CategoriasInsumos.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormCategoria from '../../organismos/organismosJSX/FormCategoria'
import FormInsumo from '../../organismos/organismosJSX/FormInsumo'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import Titulo from '../../atomos/atomosJSX/Titulo'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { formatarBooleano, formatarMoeda } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/CategoriasInsumos.css'

function CategoriasInsumos() {
  const sessao = lerSessao()
  const categoria = useRetornoApi()
  const insumo = useRetornoApi()
  const [tickCategoria, setTickCategoria] = useState(0)
  const [tickInsumo, setTickInsumo] = useState(0)
  const [idCategoria, setIdCategoria] = useState('')
  const [idInsumo, setIdInsumo] = useState('')
  const [categoriaAtual, setCategoriaAtual] = useState(null)
  const [insumoAtual, setInsumoAtual] = useState(null)

  async function criarCategoria(dados) {
    const corpo = await categoria.executar(
      () => apiJson(endpoints.categorias, { method: 'POST', body: JSON.stringify(dados) }),
      'Categoria cadastrada.',
    )
    if (corpo) setTickCategoria((valor) => valor + 1)
  }

  async function salvarCategoria(dados) {
    const corpo = await categoria.executar(
      () => apiJson(endpoints.categoria(idCategoria), { method: 'PUT', body: JSON.stringify(dados) }),
      'Categoria atualizada.',
    )
    if (corpo) {
      setCategoriaAtual(corpo)
      setTickCategoria((valor) => valor + 1)
    }
  }

  async function carregarCategoria() {
    const corpo = await categoria.executar(
      () => apiJson(endpoints.categoria(idCategoria)),
      '',
    )
    if (corpo) setCategoriaAtual(corpo)
  }

  async function criarInsumo(dados) {
    const corpo = await insumo.executar(
      () => apiJson(endpoints.insumos, { method: 'POST', body: JSON.stringify(dados) }),
      'Insumo cadastrado.',
    )
    if (corpo) setTickInsumo((valor) => valor + 1)
  }

  async function salvarInsumo(dados) {
    const corpo = await insumo.executar(
      () => apiJson(endpoints.insumo(idInsumo), { method: 'PUT', body: JSON.stringify(dados) }),
      'Insumo atualizado.',
    )
    if (corpo) {
      setInsumoAtual(corpo)
      setTickInsumo((valor) => valor + 1)
    }
  }

  async function carregarInsumo() {
    const corpo = await insumo.executar(
      () => apiJson(endpoints.insumo(idInsumo)),
      '',
    )
    if (corpo) setInsumoAtual(corpo)
  }

  return (
    <TemplateAutenticado rotaAtual="/categorias">
      <div className="categorias-insumos">
        <CabecalhoPagina
          titulo="Categorias e insumos"
          descricao="A categoria agrupa o cardápio. O insumo entra na ficha técnica e no custo do produto."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <div className="pagina__duas-colunas">
          <section className="pagina__painel">
            <Titulo texto="Categorias" nivel={2} />
            <FormCategoria estabelecimentoId={sessao.estabelecimentoId} onSubmit={criarCategoria} enviando={categoria.enviando} />
            <div className="formulario__linha">
              <FormField id="id-categoria" rotulo="Id para editar" tipo="number" minimo="1" valor={idCategoria} onChange={setIdCategoria} />
              <Botao texto="Carregar" variante="secundario" onClick={carregarCategoria} desabilitado={!idCategoria} />
            </div>
            {categoriaAtual ? (
              <FormCategoria
                key={`${categoriaAtual.id}-${categoriaAtual.nome}-${categoriaAtual.ativa}`}
                modo="editar"
                valoresIniciais={categoriaAtual}
                onSubmit={salvarCategoria}
                enviando={categoria.enviando}
              />
            ) : null}
            <AvisosApi erro={categoria.erro} sucesso={categoria.sucesso} resposta={categoria.resposta} />
            <ListaRecurso
              caminho={endpoints.categorias}
              ordenacaoPadrao="nome,asc"
              atualizacao={tickCategoria}
              colunas={[
                { chave: 'id', rotulo: 'Id' },
                { chave: 'nome', rotulo: 'Nome' },
                { chave: 'ativa', rotulo: 'Ativa', formatar: formatarBooleano },
              ]}
            />
          </section>
          <section className="pagina__painel">
            <Titulo texto="Insumos" nivel={2} />
            <FormInsumo estabelecimentoId={sessao.estabelecimentoId} onSubmit={criarInsumo} enviando={insumo.enviando} />
            <div className="formulario__linha">
              <FormField id="id-insumo" rotulo="Id para editar" tipo="number" minimo="1" valor={idInsumo} onChange={setIdInsumo} />
              <Botao texto="Carregar" variante="secundario" onClick={carregarInsumo} desabilitado={!idInsumo} />
            </div>
            {insumoAtual ? (
              <FormInsumo
                key={`${insumoAtual.id}-${insumoAtual.nome}-${insumoAtual.ativo}`}
                modo="editar"
                valoresIniciais={insumoAtual}
                onSubmit={salvarInsumo}
                enviando={insumo.enviando}
              />
            ) : null}
            <AvisosApi erro={insumo.erro} sucesso={insumo.sucesso} resposta={insumo.resposta} />
            <ListaRecurso
              caminho={endpoints.insumos}
              ordenacaoPadrao="nome,asc"
              atualizacao={tickInsumo}
              colunas={[
                { chave: 'id', rotulo: 'Id' },
                { chave: 'nome', rotulo: 'Nome' },
                { chave: 'unidadeMedida', rotulo: 'Unidade' },
                { chave: 'custoUnitario', rotulo: 'Custo', formatar: formatarMoeda },
                { chave: 'ativo', rotulo: 'Ativo', formatar: formatarBooleano },
              ]}
            />
          </section>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default CategoriasInsumos
