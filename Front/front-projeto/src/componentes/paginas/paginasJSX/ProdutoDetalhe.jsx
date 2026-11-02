// paginas/paginasJSX/ProdutoDetalhe.jsx

import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormProduto from '../../organismos/organismosJSX/FormProduto'
import FormFichaTecnica from '../../organismos/organismosJSX/FormFichaTecnica'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import Link from '../../atomos/atomosJSX/Link'
import Texto from '../../atomos/atomosJSX/Texto'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { formatarDataHora, formatarMoeda } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/ProdutoDetalhe.css'

function ProdutoDetalhe() {
  const { id } = useParams()
  const sessao = lerSessao()
  const ocultarCusto = sessao.perfil === 'OPERADOR'
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [produto, setProduto] = useState(null)
  const [fichaTick, setFichaTick] = useState(0)
  const [erroCarga, setErroCarga] = useState('')

  useEffect(() => {
    let ativo = true
    apiJson(endpoints.produto(id))
      .then((corpo) => {
        if (ativo) setProduto(corpo)
      })
      .catch((falha) => {
        if (ativo) setErroCarga(falha.message)
      })
    return () => {
      ativo = false
    }
  }, [id, sucesso])

  async function salvar(dados) {
    await executar(
      () => apiJson(endpoints.produto(id), { method: 'PUT', body: JSON.stringify(dados) }),
      'Produto atualizado.',
    )
  }

  async function incluirFicha(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.fichaTecnica(id), { method: 'POST', body: JSON.stringify(dados) }),
      'Item incluído na ficha técnica.',
    )
    if (corpo) setFichaTick((valor) => valor + 1)
  }

  return (
    <TemplateAutenticado rotaAtual="/produtos">
      <div className="produto-detalhe">
        <CabecalhoPagina
          titulo={produto?.nome || `Produto ${id}`}
          descricao="Edição, histórico de preço e custo e ficha técnica. A recomendação da matriz fica no resultado da análise."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <Link href="/produtos" texto="Voltar à lista" />
        <Link href={`/simulador?produtoId=${id}`} texto="Simular preço" />
        {erroCarga ? <Texto>{erroCarga}</Texto> : null}
        <div className="pagina__grade pagina__grade--lateral">
          <section className="pagina__painel">
            {produto ? (
              <FormProduto
                key={`${produto.id}-${produto.precoAtual}-${produto.ativo}-${produto.nome}`}
                modo="editar"
                valoresIniciais={produto}
                ocultarCusto={ocultarCusto}
                onSubmit={salvar}
                enviando={enviando}
              />
            ) : (
              <Texto>{erroCarga || 'Carregando produto...'}</Texto>
            )}
            <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
          </section>
          <div className="produto-detalhe__coluna">
            <section className="pagina__painel">
              <Texto>Histórico de preço e custo</Texto>
              <ListaRecurso
                caminho={endpoints.historicoPreco(id)}
                ordenacaoPadrao="vigenciaIni,desc"
                colunas={[
                  { chave: 'preco', rotulo: 'Preço', formatar: formatarMoeda },
                  ...(ocultarCusto ? [] : [{ chave: 'custo', rotulo: 'Custo', formatar: formatarMoeda }]),
                  { chave: 'vigenciaIni', rotulo: 'Vigência', formatar: formatarDataHora },
                  { chave: 'vigenciaFim', rotulo: 'Fim', formatar: formatarDataHora },
                ]}
              />
            </section>
            <section className="pagina__painel">
              <Texto>Ficha técnica</Texto>
              <FormFichaTecnica produtoId={id} onSubmit={incluirFicha} enviando={enviando} />
              <ListaRecurso
                caminho={endpoints.fichaTecnica(id)}
                ordenacaoPadrao="id,asc"
                atualizacao={fichaTick}
                colunas={[
                  { chave: 'insumoId', rotulo: 'Insumo' },
                  { chave: 'quantidade', rotulo: 'Quantidade' },
                ]}
              />
            </section>
          </div>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default ProdutoDetalhe
