// atomos/atomosJSX/Titulo.jsx
//
// USO: título de página ou de bloco.
// NECESSIDADE: hierarquia tipográfica da marca, sem h1 solto em cada tela.
//
// PROPS:
//   texto    (string)
//   nivel    (number) — 1, 2 ou 3
//
// EXEMPLOS DE USO:
//   <Titulo texto="Produtos" />

import '../atomosCSS/Titulo.css'

function Titulo({ texto, nivel = 1 }) {
  const Tag = nivel === 2 ? 'h2' : nivel === 3 ? 'h3' : 'h1'
  return <Tag className={`titulo titulo--${nivel}`}>{texto}</Tag>
}

export default Titulo
