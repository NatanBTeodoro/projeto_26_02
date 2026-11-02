// atomos/atomosJSX/Label.jsx
//
// USO: rótulo de campo, associado pelo htmlFor.
// NECESSIDADE: tipografia e asterisco de obrigatório num só lugar.
//
// PROPS:
//   htmlFor      (string)
//   texto        (string)
//   obrigatorio  (boolean)
//
// EXEMPLOS DE USO:
//   <Label htmlFor="email" texto="E-mail" obrigatorio />

import '../atomosCSS/Label.css'

function Label({ htmlFor, texto, obrigatorio = false }) {
  return (
    <label className="label" htmlFor={htmlFor}>
      {texto}
      {obrigatorio ? <span className="label__obrigatorio"> *</span> : null}
    </label>
  )
}

export default Label
