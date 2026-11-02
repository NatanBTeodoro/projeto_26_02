// atomos/atomosJSX/Badge.jsx
//
// USO: perfil, classe da matriz, status de job ou alerta.
// NECESSIDADE: um selo colorido reutilizável, sem hex na página.
//
// PROPS:
//   texto     (string)
//   variante  (string) — marca, oliva, estrela, vaca, enigma, cao, observacao, incompleto, neutro, sucesso, erro
//
// EXEMPLOS DE USO:
//   <Badge texto="Estrela" variante="estrela" />

import '../atomosCSS/Badge.css'

function Badge({ texto, variante = 'neutro' }) {
  return <span className={`badge badge--${variante}`}>{texto}</span>
}

export default Badge
