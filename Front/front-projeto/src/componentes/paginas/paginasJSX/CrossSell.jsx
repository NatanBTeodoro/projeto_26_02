// paginas/paginasJSX/CrossSell.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import Badge from '../../atomos/atomosJSX/Badge'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { apiJson, baixarArquivo } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarNumero } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/CrossSell.css'

function CrossSell() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [execucaoId, setExecucaoId] = useState('')
  const [consultaId, setConsultaId] = useState('')

  async function verCombos() {
    await executar(
      () => apiJson(endpoints.combos(consultaId)),
      '',
    )
  }

  async function exportar(tipo) {
    const caminho = tipo === 'pdf' ? endpoints.exportarPdf(consultaId) : endpoints.exportarCsv(consultaId)
    await executar(() => baixarArquivo(caminho, `cross-sell-${consultaId}.${tipo}`), `Arquivo ${tipo.toUpperCase()} baixado.`)
  }

  return (
    <TemplateAutenticado rotaAtual="/cross-sell">
      <div className="cross-sell">
        <CabecalhoPagina
          titulo="Cross-sell e combos"
          descricao="Suporte, confiança e lift da execução escolhida. Regra frágil fica marcada. Combos sugeridos vêm da API."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <section className="pagina__painel">
          <div className="formulario__linha">
            <FormField id="execucao-cross" rotulo="Execução" tipo="number" minimo="1" valor={execucaoId} onChange={setExecucaoId} dica="Use o id gerado na matriz." />
            <Botao texto="Carregar regras" onClick={() => setConsultaId(execucaoId)} desabilitado={!execucaoId} />
          </div>
          {consultaId ? (
            <div className="formulario__acoes">
              <Botao texto={enviando ? 'Consultando...' : 'Ver combos'} variante="secundario" onClick={verCombos} />
              <Botao texto="CSV" variante="fantasma" onClick={() => exportar('csv')} />
              <Botao texto="PDF" variante="fantasma" onClick={() => exportar('pdf')} />
            </div>
          ) : null}
          <LegendaMetodo metodo="GET" caminho={endpoints.combos(consultaId || ':id')} />
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
        {consultaId ? (
          <section className="pagina__painel">
            <ListaRecurso
              caminho={endpoints.regras(consultaId)}
              ordenacaoPadrao="lift,desc"
              colunas={[
                { chave: 'antecedente', rotulo: 'Antecedente' },
                { chave: 'consequente', rotulo: 'Consequente' },
                { chave: 'suporte', rotulo: 'Suporte', formatar: (valor) => formatarNumero(valor, 4) },
                { chave: 'confianca', rotulo: 'Confiança', formatar: (valor) => formatarNumero(valor, 4) },
                { chave: 'lift', rotulo: 'Lift', formatar: (valor) => formatarNumero(valor, 4) },
                {
                  chave: 'fragil',
                  rotulo: 'Leitura',
                  formatar: (valor) => <Badge texto={valor ? 'Frágil' : 'Estável'} variante={valor ? 'incompleto' : 'sucesso'} />,
                },
              ]}
            />
          </section>
        ) : null}
      </div>
    </TemplateAutenticado>
  )
}

export default CrossSell
