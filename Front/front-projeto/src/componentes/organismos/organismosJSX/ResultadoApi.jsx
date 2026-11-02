// organismos/organismosJSX/ResultadoApi.jsx
//
// USO: abaixo da ação, mostra o JSON devolvido pelo back.
// NECESSIDADE: o teste precisa ver o corpo, não só a mensagem traduzida na tela.
//
// PROPS:
//   resposta  (any) — objeto, lista, null ou undefined para ocultar
//
// EXEMPLOS DE USO:
//   <ResultadoApi resposta={corpo} />

import '../organismosCSS/ResultadoApi.css'

function ResultadoApi({ resposta }) {
  if (resposta === undefined) return null
  const texto = resposta === null ? 'Sem corpo (204).' : JSON.stringify(resposta, null, 2)
  const aberto = Boolean(resposta && (resposta.mensagem || resposta.status))

  return (
    <details className="resultado-api" open={aberto}>
      <summary>Resposta da API</summary>
      <pre>{texto}</pre>
    </details>
  )
}

export default ResultadoApi
