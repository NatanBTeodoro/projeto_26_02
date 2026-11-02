// organismos/organismosJSX/ListaRecurso.jsx
//
// USO: qualquer GET paginado (PageResponseDTO).
// NECESSIDADE: página, tamanho e sort inválido são testados uma vez, em todas as listas.
//
// PROPS:
//   caminho           (string)
//   colunas           (array) — { chave, rotulo, formatar(valor, linha) }
//   ordenacaoPadrao   (string)
//   filtros           (object)
//   atualizacao       (number) — incrementa para recarregar
//   tamanhoInicial    (number)
//
// EXEMPLOS DE USO:
//   <ListaRecurso caminho="/api/produtos" colunas={colunas} ordenacaoPadrao="nome,asc" />

import { useEffect, useState } from 'react'
import { apiJson } from '../../../services/api'
import { comConsulta, parametrosPagina } from '../../../utils/consulta'
import { formatarBooleano } from '../../../utils/formatacao'
import Texto from '../../atomos/atomosJSX/Texto'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import Paginacao from '../../moleculas/moleculasJSX/Paginacao'
import '../organismosCSS/ListaRecurso.css'

function textoCelula(valor) {
  if (valor === null || valor === undefined || valor === '') return '—'
  if (typeof valor === 'boolean') return formatarBooleano(valor)
  if (typeof valor === 'object') return JSON.stringify(valor)
  return String(valor)
}

function ListaRecurso({
  caminho,
  colunas,
  ordenacaoPadrao = 'id,desc',
  filtros = {},
  atualizacao = 0,
  tamanhoInicial = 20,
  aoCarregar,
}) {
  const [pagina, setPagina] = useState(0)
  const [tamanho, setTamanho] = useState(tamanhoInicial)
  const [ordenacao, setOrdenacao] = useState(ordenacaoPadrao)
  const [dados, setDados] = useState(null)
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)
  const filtrosChave = JSON.stringify(filtros)
  const chaveConsulta = `${ordenacaoPadrao}|${filtrosChave}`
  const [chaveVista, setChaveVista] = useState(chaveConsulta)
  if (chaveConsulta !== chaveVista) {
    setChaveVista(chaveConsulta)
    setPagina(0)
    setOrdenacao(ordenacaoPadrao)
  }

  useEffect(() => {
    let ativo = true

    async function carregar() {
      setCarregando(true)
      setErro('')
      try {
        const consulta = parametrosPagina({ pagina, tamanho, ordenacao, filtros: JSON.parse(filtrosChave) })
        const corpo = await apiJson(comConsulta(caminho, consulta))
        if (ativo) {
          setDados(corpo)
          aoCarregar?.(corpo)
        }
      } catch (falha) {
        if (!ativo) return
        setErro(falha.message || 'Erro de conexão. Tente novamente.')
        setDados(falha.corpo || null)
      } finally {
        if (ativo) setCarregando(false)
      }
    }

    carregar()
    return () => {
      ativo = false
    }
  }, [caminho, pagina, tamanho, ordenacao, filtrosChave, atualizacao, aoCarregar])

  const linhas = Array.isArray(dados?.conteudo)
    ? dados.conteudo
    : Array.isArray(dados)
      ? dados
      : []
  const ehPagina = Boolean(dados && Array.isArray(dados.conteudo))

  return (
    <section className="lista-recurso">
      <LegendaMetodo metodo="GET" caminho={`${caminho}?page&size&sort`} />
      {carregando ? <Texto variante="dica">Carregando página...</Texto> : null}
      <MensagemErro texto={erro} />
      <div className="lista-recurso__tabela-wrap">
        <table className="lista-recurso__tabela">
          <thead>
            <tr>
              {colunas.map((coluna) => (
                <th key={coluna.chave}>{coluna.rotulo}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {linhas.length === 0 ? (
              <tr>
                <td colSpan={colunas.length}>Nenhum registro nesta página.</td>
              </tr>
            ) : linhas.map((linha, indice) => (
              <tr key={linha.id ?? indice}>
                {colunas.map((coluna) => (
                  <td key={coluna.chave}>
                    {coluna.formatar
                      ? coluna.formatar(linha[coluna.chave], linha)
                      : textoCelula(linha[coluna.chave])}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {ehPagina ? (
        <Paginacao
          pagina={dados.pagina ?? pagina}
          totalPaginas={dados.totalPaginas ?? 0}
          totalElementos={dados.totalElementos ?? linhas.length}
          tamanho={tamanho}
          ordenacao={ordenacao}
          onMudarPagina={setPagina}
          onMudarTamanho={(novo) => {
            setTamanho(novo)
            setPagina(0)
          }}
          onMudarOrdenacao={(nova) => {
            setOrdenacao(nova)
            setPagina(0)
          }}
        />
      ) : null}
    </section>
  )
}

export default ListaRecurso
