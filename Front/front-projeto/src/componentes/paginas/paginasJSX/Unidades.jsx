// paginas/paginasJSX/Unidades.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormUnidade from '../../organismos/organismosJSX/FormUnidade'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import FiltroPeriodo from '../../organismos/organismosJSX/FiltroPeriodo'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Unidades.css'

function Unidades() {
  const sessao = lerSessao()
  const cadastro = useRetornoApi()
  const relatorio = useRetornoApi()
  const [tick, setTick] = useState(0)

  async function criar(dados) {
    const corpo = await cadastro.executar(
      () => apiJson(endpoints.unidades, { method: 'POST', body: JSON.stringify(dados) }),
      'Unidade cadastrada.',
    )
    if (corpo) setTick((valor) => valor + 1)
  }

  async function comparar({ inicio, fim }) {
    const params = new URLSearchParams({ periodoIni: inicio, periodoFim: fim })
    await relatorio.executar(() => apiJson(`${endpoints.comparativoUnidades}?${params}`), '')
  }

  return (
    <TemplateAutenticado rotaAtual="/unidades">
      <div className="unidades">
        <CabecalhoPagina
          titulo="Unidades"
          descricao="Comparativo entre unidades da mesma rede. Se a rota ainda não existir no back, a resposta 404 aparece aqui."
          perfis={['Administrador', 'Gestor']}
        />
        <div className="pagina__duas-colunas">
          <section className="pagina__painel">
            <FormUnidade estabelecimentoId={sessao.estabelecimentoId} onSubmit={criar} enviando={cadastro.enviando} />
            <AvisosApi erro={cadastro.erro} sucesso={cadastro.sucesso} resposta={cadastro.resposta} />
          </section>
          <section className="pagina__painel">
            <LegendaMetodo metodo="GET" caminho={`${endpoints.comparativoUnidades}?periodoIni&periodoFim`} />
            <FiltroPeriodo idPrefixo="unidades" onAplicar={comparar} rotuloBotao={relatorio.enviando ? 'Comparando...' : 'Comparar unidades'} />
            <AvisosApi erro={relatorio.erro} sucesso={relatorio.sucesso} resposta={relatorio.resposta} />
          </section>
        </div>
        <section className="pagina__painel">
          <ListaRecurso
            caminho={endpoints.unidades}
            ordenacaoPadrao="nome,asc"
            atualizacao={tick}
            colunas={[
              { chave: 'id', rotulo: 'Id' },
              { chave: 'nome', rotulo: 'Nome' },
              { chave: 'estabelecimentoId', rotulo: 'Estabelecimento' },
            ]}
          />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Unidades
