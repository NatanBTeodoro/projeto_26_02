// atomos/atomosJSX/MensagemErro.jsx
//
// USO: falha de validação ou mensagem vinda da API.
// NECESSIDADE: o contrato de erro do back aparece sempre no mesmo bloco.
//
// PROPS:
//   texto  (string)
//
// EXEMPLOS DE USO:
//   <MensagemErro texto={erroApi} />

import '../atomosCSS/MensagemErro.css'

function MensagemErro({ texto }) {
  if (!texto) return null
  return (
    <p className="mensagem-erro" role="alert">
      {texto}
    </p>
  )
}

export default MensagemErro
