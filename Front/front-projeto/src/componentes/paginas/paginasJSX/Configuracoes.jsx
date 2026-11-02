// paginas/paginasJSX/Configuracoes.jsx

import { useEffect, useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormConfigAnalise from '../../organismos/organismosJSX/FormConfigAnalise'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Configuracoes.css'

function Configuracoes() {
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [config, setConfig] = useState(null)
  const [erroCarga, setErroCarga] = useState('')

  useEffect(() => {
    let ativo = true
    apiJson(endpoints.configAnalise)
      .then((corpo) => {
        if (ativo) setConfig(corpo)
      })
      .catch((falha) => {
        if (ativo) setErroCarga(falha.message)
      })
    return () => {
      ativo = false
    }
  }, [sucesso])

  async function salvar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.configAnalise, { method: 'PUT', body: JSON.stringify(dados) }),
      'Parâmetros gravados. A próxima análise usa estes valores.',
    )
    if (corpo) setConfig(corpo)
  }

  return (
    <TemplateAutenticado rotaAtual="/configuracoes">
      <div className="configuracoes">
        <CabecalhoPagina
          titulo="Parâmetros de análise"
          descricao="Suporte, confiança, lift, período mínimo, alerta de Cão e se a matriz é por categoria ou pelo cardápio inteiro."
          perfis={['Administrador', 'Gestor']}
        />
        <section className="pagina__painel">
          <LegendaMetodo metodo="GET" caminho={endpoints.configAnalise} />
          <FormConfigAnalise
            key={config?.id ?? 'padrao'}
            valoresIniciais={config}
            onSubmit={salvar}
            enviando={enviando}
          />
          <AvisosApi
            erro={erro || erroCarga}
            sucesso={sucesso}
            resposta={resposta !== undefined ? resposta : config || undefined}
          />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Configuracoes
