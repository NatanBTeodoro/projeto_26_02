// paginas/paginasJSX/Dashboard.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import CartaoMetrica from '../../moleculas/moleculasJSX/CartaoMetrica'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import FiltroPeriodo from '../../organismos/organismosJSX/FiltroPeriodo'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarMoeda, rotuloClasse } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Dashboard.css'

function textoClasses(mapa) {
  if (!mapa || typeof mapa !== 'object') return '—'
  return Object.entries(mapa).map(([chave, valor]) => `${rotuloClasse(chave)} ${valor}`).join(' · ') || '—'
}

function Dashboard() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [painel, setPainel] = useState(null)

  async function buscar({ inicio, fim }) {
    const params = new URLSearchParams({ periodoIni: inicio, periodoFim: fim })
    const corpo = await executar(
      () => apiJson(`${endpoints.dashboard}?${params}`),
      '',
    )
    if (corpo) setPainel(corpo)
  }

  return (
    <TemplateAutenticado rotaAtual="/">
      <div className="dashboard">
        <CabecalhoPagina
          titulo="Painel"
          descricao="Receita, ticket médio, classificação, alertas e regras. Os números vêm prontos da API."
          perfis={['Administrador', 'Gestor', 'Operador', 'Analista']}
        />
        <section className="pagina__painel">
          <LegendaMetodo metodo="GET" caminho={`${endpoints.dashboard}?periodoIni&periodoFim`} />
          <FiltroPeriodo onAplicar={buscar} rotuloBotao={enviando ? 'Consultando...' : 'Atualizar painel'} />
          <div className="dashboard__metricas">
            <CartaoMetrica rotulo="Receita" valor={formatarMoeda(painel?.receita)} />
            <CartaoMetrica rotulo="Ticket médio" valor={formatarMoeda(painel?.ticketMedio)} />
            <CartaoMetrica rotulo="Alertas" valor={painel?.alertas != null ? String(Array.isArray(painel.alertas) ? painel.alertas.length : painel.alertas) : '—'} />
            <CartaoMetrica rotulo="Por classificação" valor={textoClasses(painel?.itensPorClasse)} />
          </div>
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Dashboard
