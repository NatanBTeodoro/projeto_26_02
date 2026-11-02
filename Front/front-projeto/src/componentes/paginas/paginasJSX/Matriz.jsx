// paginas/paginasJSX/Matriz.jsx

import { useCallback, useEffect, useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormAnalise from '../../organismos/organismosJSX/FormAnalise'
import GradeMatriz from '../../organismos/organismosJSX/GradeMatriz'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import Botao from '../../atomos/atomosJSX/Botao'
import Badge from '../../atomos/atomosJSX/Badge'
import Texto from '../../atomos/atomosJSX/Texto'
import { apiJson, baixarArquivo } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { formatarMoeda, formatarNumero, rotuloClasse, rotuloStatus } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Matriz.css'

function varianteClasse(classe) {
  const mapa = {
    ESTRELA: 'estrela',
    VACA_LEITEIRA: 'vaca',
    ENIGMA: 'enigma',
    CAO: 'cao',
    EM_OBSERVACAO: 'observacao',
    DADO_INCOMPLETO: 'incompleto',
  }
  return mapa[classe] || 'neutro'
}

function Matriz() {
  const sessao = lerSessao()
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [execucao, setExecucao] = useState(null)
  const [itens, setItens] = useState([])

  const aoCarregar = useCallback((corpo) => {
    setItens(Array.isArray(corpo?.conteudo) ? corpo.conteudo : [])
  }, [])

  useEffect(() => {
    if (!execucao?.id) return undefined
    if (execucao.status === 'CONCLUIDA' || execucao.status === 'FALHOU') return undefined
    let ativo = true
    const timer = setInterval(async () => {
      try {
        const corpo = await apiJson(endpoints.analise(execucao.id))
        if (ativo) setExecucao(corpo)
      } catch {
        /* a navegação continua; o próximo ciclo tenta de novo */
      }
    }, 2000)
    return () => {
      ativo = false
      clearInterval(timer)
    }
  }, [execucao?.id, execucao?.status])

  async function iniciar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.analises, { method: 'POST', body: JSON.stringify(dados) }),
      'Análise enfileirada. A tela continua navegável enquanto o job roda.',
    )
    if (corpo?.id) setExecucao(corpo)
  }

  async function reexecutar() {
    if (!execucao?.id) return
    const corpo = await executar(
      () => apiJson(endpoints.reexecutar(execucao.id), { method: 'POST' }),
      'Nova execução pedida.',
    )
    if (corpo?.id) setExecucao(corpo)
  }

  async function exportar(tipo) {
    const caminho = tipo === 'pdf' ? endpoints.exportarPdf(execucao.id) : endpoints.exportarCsv(execucao.id)
    await executar(
      () => baixarArquivo(caminho, `matriz-${execucao.id}.${tipo}`),
      `Arquivo ${tipo.toUpperCase()} baixado.`,
    )
  }

  return (
    <TemplateAutenticado rotaAtual="/matriz">
      <div className="matriz">
        <CabecalhoPagina
          titulo="Matriz"
          descricao="Estrela, vaca leiteira, enigma e cão. Itens sem custo ou sem histórico mínimo ficam fora dos quadrantes."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <section className="pagina__painel">
          <FormAnalise estabelecimentoId={sessao.estabelecimentoId} onSubmit={iniciar} enviando={enviando} />
          {execucao ? (
            <div className="matriz__status">
              <Texto>Execução {execucao.id}</Texto>
              <Badge texto={rotuloStatus(execucao.status)} variante={execucao.status === 'FALHOU' ? 'erro' : 'oliva'} />
              <div className="formulario__acoes">
                <Botao texto="Reexecutar" variante="secundario" onClick={reexecutar} desabilitado={enviando} />
                <Botao texto="Exportar CSV" variante="secundario" onClick={() => exportar('csv')} desabilitado={!execucao.id} />
                <Botao texto="Exportar PDF" variante="secundario" onClick={() => exportar('pdf')} desabilitado={!execucao.id} />
              </div>
            </div>
          ) : null}
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta ?? execucao} />
        </section>
        {execucao?.id ? (
          <>
            <GradeMatriz itens={itens} />
            <section className="pagina__painel">
              <ListaRecurso
                caminho={endpoints.classificacoes(execucao.id)}
                ordenacaoPadrao="classe,asc"
                aoCarregar={aoCarregar}
                colunas={[
                  { chave: 'produtoId', rotulo: 'Produto' },
                  { chave: 'popularidade', rotulo: 'Popularidade %', formatar: (valor) => formatarNumero(valor) },
                  { chave: 'margem', rotulo: 'Margem', formatar: formatarMoeda },
                  {
                    chave: 'classe',
                    rotulo: 'Classe',
                    formatar: (valor) => <Badge texto={rotuloClasse(valor)} variante={varianteClasse(valor)} />,
                  },
                ]}
              />
            </section>
          </>
        ) : null}
      </div>
    </TemplateAutenticado>
  )
}

export default Matriz
