// paginas/paginasJSX/Importacao.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormImportacao from '../../organismos/organismosJSX/FormImportacao'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { rotuloStatus } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Importacao.css'

function Importacao() {
  const sessao = lerSessao()
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [tick, setTick] = useState(0)

  async function previsualizar(formulario) {
    await executar(
      () => apiJson(endpoints.previsualizarCsv, { method: 'POST', body: formulario }),
      'Pré-visualização pronta. Confira o relatório antes de confirmar.',
    )
  }

  async function confirmar(formulario) {
    const corpo = await executar(
      () => apiJson(endpoints.importacoes, { method: 'POST', body: formulario }),
      'Importação enviada. Linhas inválidas não barram as válidas.',
    )
    if (corpo) setTick((valor) => valor + 1)
  }

  return (
    <TemplateAutenticado rotaAtual="/importacao">
      <div className="importacao">
        <CabecalhoPagina
          titulo="Importar CSV"
          descricao="Pré-visualize o arquivo, mapeie as linhas com erro e só então confirme. Identificador externo repetido é ignorado."
          perfis={['Administrador', 'Gestor', 'Operador']}
        />
        <section className="pagina__painel">
          <FormImportacao
            estabelecimentoId={sessao.estabelecimentoId}
            onPrevisualizar={previsualizar}
            onConfirmar={confirmar}
            enviando={enviando}
          />
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
        <section className="pagina__painel">
          <ListaRecurso
            caminho={endpoints.importacoes}
            ordenacaoPadrao="id,desc"
            atualizacao={tick}
            colunas={[
              { chave: 'id', rotulo: 'Id' },
              { chave: 'arquivo', rotulo: 'Arquivo' },
              { chave: 'status', rotulo: 'Status', formatar: rotuloStatus },
              { chave: 'linhasOk', rotulo: 'Válidas' },
              { chave: 'linhasErro', rotulo: 'Erros' },
              { chave: 'relatorioErros', rotulo: 'Relatório' },
            ]}
          />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Importacao
