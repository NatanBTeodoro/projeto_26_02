// paginas/paginasJSX/HistoricoVendas.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormEstorno from '../../organismos/organismosJSX/FormEstorno'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarDataHora, rotuloStatus } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/HistoricoVendas.css'

function HistoricoVendas() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [tick, setTick] = useState(0)

  async function estornar(id, dados) {
    const corpo = await executar(
      () => apiJson(endpoints.estorno(id), { method: 'POST', body: JSON.stringify(dados) }),
      'Estorno registrado. A venda original permanece e sai dos cálculos.',
    )
    if (corpo) setTick((valor) => valor + 1)
  }

  return (
    <TemplateAutenticado rotaAtual="/vendas">
      <div className="historico-vendas">
        <CabecalhoPagina
          titulo="Histórico e estorno"
          descricao="Vendas estornadas continuam na lista, com status próprio, e não entram na matriz."
          perfis={['Administrador', 'Gestor', 'Operador']}
        />
        <div className="pagina__grade pagina__grade--lateral">
          <section className="pagina__painel">
            <FormEstorno onSubmit={estornar} enviando={enviando} />
            <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
          </section>
          <section className="pagina__painel">
            <ListaRecurso
              caminho={endpoints.pedidos}
              ordenacaoPadrao="dataHora,desc"
              atualizacao={tick}
              colunas={[
                { chave: 'id', rotulo: 'Id' },
                { chave: 'externalId', rotulo: 'Externo' },
                { chave: 'dataHora', rotulo: 'Data', formatar: formatarDataHora },
                { chave: 'status', rotulo: 'Status', formatar: rotuloStatus },
                { chave: 'motivoEstorno', rotulo: 'Motivo' },
                {
                  chave: 'itens',
                  rotulo: 'Itens',
                  formatar: (itens) => Array.isArray(itens)
                    ? itens.map((item) => `#${item.produtoId} × ${item.quantidade}`).join(', ')
                    : '—',
                },
              ]}
            />
          </section>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default HistoricoVendas
