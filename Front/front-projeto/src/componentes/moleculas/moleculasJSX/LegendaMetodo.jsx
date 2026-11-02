// moleculas/moleculasJSX/LegendaMetodo.jsx
//
// USO: acima de cada formulário ou lista, mostra o verbo e o caminho HTTP.
// NECESSIDADE: o testador precisa saber qual método do back aquela ação dispara.
//
// PROPS:
//   metodo   (string) — GET, POST, PUT
//   caminho  (string)
//
// EXEMPLOS DE USO:
//   <LegendaMetodo metodo="POST" caminho="/api/produtos" />

import '../moleculasCSS/LegendaMetodo.css'

function LegendaMetodo({ metodo, caminho }) {
  return (
    <p className="legenda-metodo">
      <span className={`legenda-metodo__verbo legenda-metodo__verbo--${metodo.toLowerCase()}`}>{metodo}</span>
      <code>{caminho}</code>
    </p>
  )
}

export default LegendaMetodo
