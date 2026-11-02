// paginas/paginasJSX/Produtos.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormProduto from '../../organismos/organismosJSX/FormProduto'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import Link from '../../atomos/atomosJSX/Link'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { formatarBooleano, formatarMoeda } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Produtos.css'

function Produtos() {
  const sessao = lerSessao()
  const ocultarCusto = sessao.perfil === 'OPERADOR'
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [atualizacao, setAtualizacao] = useState(0)

  async function criar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.produtos, { method: 'POST', body: JSON.stringify(dados) }),
      'Produto cadastrado.',
    )
    if (corpo) setAtualizacao((valor) => valor + 1)
  }

  const colunas = [
    { chave: 'id', rotulo: 'Id' },
    { chave: 'nome', rotulo: 'Nome' },
    { chave: 'precoAtual', rotulo: 'Preço', formatar: formatarMoeda },
    ...(ocultarCusto ? [] : [{ chave: 'custoAtual', rotulo: 'Custo', formatar: formatarMoeda }]),
    { chave: 'sazonal', rotulo: 'Sazonal', formatar: formatarBooleano },
    { chave: 'ativo', rotulo: 'Ativo', formatar: formatarBooleano },
    { chave: 'abrir', rotulo: '', formatar: (_, linha) => <Link href={`/produtos/${linha.id}`} texto="Abrir" /> },
  ]

  return (
    <TemplateAutenticado rotaAtual="/produtos">
      <div className="produtos">
        <CabecalhoPagina
          titulo="Produtos"
          descricao="Cadastro com preço e custo. Inativar um item com venda preserva o histórico."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <div className="pagina__grade pagina__grade--lateral">
          <section className="pagina__painel">
            <FormProduto
              estabelecimentoId={sessao.estabelecimentoId}
              ocultarCusto={ocultarCusto}
              onSubmit={criar}
              enviando={enviando}
            />
            <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
          </section>
          <section className="pagina__painel">
            <ListaRecurso
              caminho={endpoints.produtos}
              colunas={colunas}
              ordenacaoPadrao="nome,asc"
              atualizacao={atualizacao}
            />
          </section>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default Produtos
