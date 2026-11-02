// paginas/paginasJSX/MetricasCliente.jsx

import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FiltroPeriodo from '../../organismos/organismosJSX/FiltroPeriodo'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/MetricasCliente.css'

function MetricasCliente() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()

  async function buscar({ inicio, fim }) {
    const params = new URLSearchParams({ periodoIni: inicio, periodoFim: fim })
    await executar(() => apiJson(`${endpoints.metricasCliente}?${params}`), '')
  }

  return (
    <TemplateAutenticado rotaAtual="/clientes">
      <div className="metricas-cliente">
        <CabecalhoPagina
          titulo="Métricas por cliente"
          descricao="Frequência, ticket médio e intervalo entre pedidos. O identificador na tela é o que a API devolver, nunca o dado original em agregados."
          perfis={['Administrador', 'Gestor']}
        />
        <section className="pagina__painel">
          <LegendaMetodo metodo="GET" caminho={`${endpoints.metricasCliente}?periodoIni&periodoFim`} />
          <FiltroPeriodo onAplicar={buscar} rotuloBotao={enviando ? 'Consultando...' : 'Consultar'} />
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default MetricasCliente
