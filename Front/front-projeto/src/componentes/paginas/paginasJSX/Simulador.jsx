// paginas/paginasJSX/Simulador.jsx

import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormSimulacao from '../../organismos/organismosJSX/FormSimulacao'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import Badge from '../../atomos/atomosJSX/Badge'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { rotuloClasse } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Simulador.css'

function Simulador() {
  const [params] = useSearchParams()
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [classe, setClasse] = useState('')

  async function simular(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.simular, { method: 'POST', body: JSON.stringify(dados) }),
      'Simulação calculada. O cardápio não foi alterado.',
    )
    setClasse(corpo?.classe || corpo?.novaClasse || '')
  }

  return (
    <TemplateAutenticado rotaAtual="/simulador">
      <div className="simulador">
        <CabecalhoPagina
          titulo="Simulador de preço"
          descricao="Mostra a classificação com o preço novo, sem gravar o produto."
          perfis={['Administrador', 'Gestor']}
        />
        <section className="pagina__painel">
          <FormSimulacao produtoId={params.get('produtoId') || ''} onSubmit={simular} enviando={enviando} />
          {classe ? <Badge texto={rotuloClasse(classe)} variante="marca" /> : null}
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Simulador
