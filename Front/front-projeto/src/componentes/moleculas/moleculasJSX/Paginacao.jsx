// moleculas/moleculasJSX/Paginacao.jsx
//
// USO: rodapé de toda listagem paginada (PageResponseDTO).
// NECESSIDADE: página base 0, tamanho e ordenação ficam testáveis na própria tela.
//
// PROPS:
//   pagina, totalPaginas, totalElementos, tamanho, ordenacao
//   onMudarPagina, onMudarTamanho, onMudarOrdenacao
//
// EXEMPLOS DE USO:
//   <Paginacao pagina={0} totalPaginas={3} tamanho={20} ordenacao="nome,asc" ... />

import { useState } from 'react'
import Botao from '../../atomos/atomosJSX/Botao'
import '../moleculasCSS/Paginacao.css'

function Paginacao({
  pagina = 0,
  totalPaginas = 0,
  totalElementos = 0,
  tamanho = 20,
  ordenacao = '',
  onMudarPagina,
  onMudarTamanho,
  onMudarOrdenacao,
}) {
  const [rascunhoOrdem, setRascunhoOrdem] = useState(ordenacao)
  const [ordemAplicada, setOrdemAplicada] = useState(ordenacao)
  if (ordenacao !== ordemAplicada) {
    setOrdemAplicada(ordenacao)
    setRascunhoOrdem(ordenacao)
  }
  const ultima = totalPaginas === 0 || pagina >= totalPaginas - 1

  function aplicarOrdem() {
    if (rascunhoOrdem !== ordenacao) onMudarOrdenacao(rascunhoOrdem)
  }

  return (
    <div className="paginacao">
      <p className="paginacao__resumo">
        Página {pagina + 1} de {Math.max(totalPaginas, 1)} · {totalElementos} registro(s)
      </p>
      <div className="paginacao__controles">
        <label className="paginacao__campo">
          Tamanho
          <select value={tamanho} onChange={(evento) => onMudarTamanho(Number(evento.target.value))}>
            {[10, 20, 50, 100, 200].map((opcao) => (
              <option key={opcao} value={opcao}>{opcao}</option>
            ))}
          </select>
        </label>
        <label className="paginacao__campo paginacao__campo--ordenacao">
          Ordenação
          <input
            value={rascunhoOrdem}
            onChange={(evento) => setRascunhoOrdem(evento.target.value)}
            onBlur={aplicarOrdem}
            onKeyDown={(evento) => {
              if (evento.key === 'Enter') aplicarOrdem()
            }}
            placeholder="nome,asc"
          />
        </label>
        <Botao texto="Anterior" variante="secundario" desabilitado={pagina <= 0} onClick={() => onMudarPagina(pagina - 1)} />
        <Botao texto="Próxima" variante="secundario" desabilitado={ultima} onClick={() => onMudarPagina(pagina + 1)} />
      </div>
    </div>
  )
}

export default Paginacao
