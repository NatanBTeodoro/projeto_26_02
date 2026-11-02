// paginas/paginasJSX/Estabelecimento.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormEstabelecimento from '../../organismos/organismosJSX/FormEstabelecimento'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarBooleano } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Estabelecimento.css'

function Estabelecimento() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [tick, setTick] = useState(0)
  const [id, setId] = useState('')
  const [atual, setAtual] = useState(null)

  async function criar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.estabelecimentos, { method: 'POST', body: JSON.stringify(dados) }),
      'Estabelecimento cadastrado.',
    )
    if (corpo) setTick((valor) => valor + 1)
  }

  async function salvar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.estabelecimento(id), { method: 'PUT', body: JSON.stringify(dados) }),
      'Estabelecimento atualizado.',
    )
    if (corpo) {
      setAtual(corpo)
      setTick((valor) => valor + 1)
    }
  }

  async function carregar() {
    const corpo = await executar(() => apiJson(endpoints.estabelecimento(id)), '')
    if (corpo) setAtual(corpo)
  }

  return (
    <TemplateAutenticado rotaAtual="/estabelecimento">
      <div className="estabelecimento">
        <CabecalhoPagina
          titulo="Estabelecimento"
          descricao="Todo dado do sistema pertence a um estabelecimento. Use outro id nas telas de cadastro para testar o isolamento."
          perfis={['Administrador', 'Gestor']}
        />
        <div className="pagina__grade pagina__grade--lateral">
          <section className="pagina__painel">
            <FormEstabelecimento onSubmit={criar} enviando={enviando} />
            <div className="formulario__linha">
              <FormField id="estab-id" rotulo="Id para editar" tipo="number" minimo="1" valor={id} onChange={setId} />
              <Botao texto="Carregar" variante="secundario" onClick={carregar} desabilitado={!id} />
            </div>
            {atual ? (
              <FormEstabelecimento
                key={`${atual.id}-${atual.nome}-${atual.ativo}`}
                modo="editar"
                valoresIniciais={atual}
                onSubmit={salvar}
                enviando={enviando}
              />
            ) : null}
            <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
          </section>
          <section className="pagina__painel">
            <ListaRecurso
              caminho={endpoints.estabelecimentos}
              ordenacaoPadrao="nome,asc"
              atualizacao={tick}
              colunas={[
                { chave: 'id', rotulo: 'Id' },
                { chave: 'nome', rotulo: 'Nome' },
                { chave: 'ativo', rotulo: 'Ativo', formatar: formatarBooleano },
              ]}
            />
          </section>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default Estabelecimento
