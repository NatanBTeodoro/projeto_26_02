// paginas/paginasJSX/Comparativo.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FiltroPeriodo from '../../organismos/organismosJSX/FiltroPeriodo'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import Titulo from '../../atomos/atomosJSX/Titulo'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Comparativo.css'

function Comparativo() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [periodoA, setPeriodoA] = useState(null)
  const [periodoB, setPeriodoB] = useState(null)

  async function comparar() {
    if (!periodoA || !periodoB) return
    const params = new URLSearchParams({
      periodoIniA: periodoA.inicio,
      periodoFimA: periodoA.fim,
      periodoIniB: periodoB.inicio,
      periodoFimB: periodoB.fim,
    })
    await executar(() => apiJson(`${endpoints.comparar}?${params}`), '')
  }

  return (
    <TemplateAutenticado rotaAtual="/comparativo">
      <div className="comparativo">
        <CabecalhoPagina
          titulo="Comparar períodos"
          descricao="Dois recortes separados. A API devolve os itens que mudaram de classe."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <div className="pagina__duas-colunas">
          <section className="pagina__painel">
            <Titulo texto="Período A" nivel={3} />
            <FiltroPeriodo idPrefixo="periodo-a" rotuloBotao="Guardar período A" onAplicar={setPeriodoA} />
          </section>
          <section className="pagina__painel">
            <Titulo texto="Período B" nivel={3} />
            <FiltroPeriodo idPrefixo="periodo-b" rotuloBotao="Guardar período B" onAplicar={setPeriodoB} />
          </section>
        </div>
        <section className="pagina__painel">
          <LegendaMetodo metodo="GET" caminho={`${endpoints.comparar}?periodoIniA&periodoFimA&periodoIniB&periodoFimB`} />
          <Botao texto={enviando ? 'Comparando...' : 'Comparar'} onClick={comparar} desabilitado={!periodoA || !periodoB || enviando} />
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Comparativo
