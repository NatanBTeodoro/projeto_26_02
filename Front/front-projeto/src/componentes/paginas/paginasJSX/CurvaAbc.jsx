// paginas/paginasJSX/CurvaAbc.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FiltroPeriodo from '../../organismos/organismosJSX/FiltroPeriodo'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import Botao from '../../atomos/atomosJSX/Botao'
import { apiJson, baixarArquivo } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/CurvaAbc.css'

function CurvaAbc() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [periodo, setPeriodo] = useState(null)

  async function buscar({ inicio, fim }) {
    setPeriodo({ inicio, fim })
    const params = new URLSearchParams({ periodoIni: inicio, periodoFim: fim })
    await executar(() => apiJson(`${endpoints.abc}?${params}`), '')
  }

  async function exportar(tipo) {
    if (!periodo) return
    const params = new URLSearchParams({ periodoIni: periodo.inicio, periodoFim: periodo.fim })
    const base = tipo === 'pdf' ? endpoints.exportarAbcPdf : endpoints.exportarAbcCsv
    await executar(
      () => baixarArquivo(`${base}?${params}`, `abc.${tipo}`),
      `Arquivo ${tipo.toUpperCase()} baixado.`,
    )
  }

  return (
    <TemplateAutenticado rotaAtual="/abc">
      <div className="curva-abc">
        <CabecalhoPagina
          titulo="Categoria e curva ABC"
          descricao="Participação da categoria no faturamento e na margem, mais a curva ABC do período."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <section className="pagina__painel">
          <LegendaMetodo metodo="GET" caminho={`${endpoints.abc}?periodoIni&periodoFim`} />
          <FiltroPeriodo onAplicar={buscar} rotuloBotao={enviando ? 'Consultando...' : 'Gerar análise'} />
          <div className="formulario__acoes">
            <Botao texto="Exportar CSV" variante="secundario" onClick={() => exportar('csv')} desabilitado={!periodo} />
            <Botao texto="Exportar PDF" variante="secundario" onClick={() => exportar('pdf')} desabilitado={!periodo} />
          </div>
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default CurvaAbc
