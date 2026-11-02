// moleculas/moleculasJSX/CabecalhoPagina.jsx
//
// USO: topo de cada rota autenticada.
// NECESSIDADE: título, texto e perfis previstos ficam iguais em todas as telas.
//
// PROPS:
//   titulo, descricao, perfis
//
// EXEMPLOS DE USO:
//   <CabecalhoPagina titulo="Produtos" descricao="..." perfis={['Gestor']} />

import { useEffect } from 'react'
import Titulo from '../../atomos/atomosJSX/Titulo'
import Texto from '../../atomos/atomosJSX/Texto'
import Badge from '../../atomos/atomosJSX/Badge'
import '../moleculasCSS/CabecalhoPagina.css'

function CabecalhoPagina({ titulo, descricao = '', perfis = [] }) {
  useEffect(() => {
    document.title = `${titulo} — Quadrante`
  }, [titulo])

  return (
    <header className="cabecalho-pagina">
      <Titulo texto={titulo} />
      {descricao ? <Texto>{descricao}</Texto> : null}
      {perfis.length ? (
        <div className="cabecalho-pagina__perfis">
          <span>Perfis previstos</span>
          {perfis.map((perfil) => (
            <Badge key={perfil} texto={perfil} variante="oliva" />
          ))}
        </div>
      ) : null}
    </header>
  )
}

export default CabecalhoPagina
