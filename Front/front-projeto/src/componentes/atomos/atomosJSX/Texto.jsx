// atomos/atomosJSX/Texto.jsx
//
// USO: parágrafo de apoio, dica de campo ou texto corrido.
// NECESSIDADE: cor secundária e tamanho menores que o título.
//
// PROPS:
//   children  (node)
//   variante  (string) — corpo ou dica
//
// EXEMPLOS DE USO:
//   <Texto>Listagem paginada de produtos.</Texto>

import '../atomosCSS/Texto.css'

function Texto({ children, variante = 'corpo' }) {
  return <p className={`texto texto--${variante}`}>{children}</p>
}

export default Texto
