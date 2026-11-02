// atomos/atomosJSX/Checkbox.jsx
//
// USO: marcas booleanas (sazonal, ativo, escopo por categoria).
// NECESSIDADE: checkbox com rótulo clicável, controlado pelo pai.
//
// PROPS:
//   id            (string)
//   texto         (string)
//   marcado       (boolean)
//   onChange      (function) — recebe boolean
//   desabilitado  (boolean)
//
// EXEMPLOS DE USO:
//   <Checkbox id="sazonal" texto="Sazonal" marcado={sazonal} onChange={setSazonal} />

import '../atomosCSS/Checkbox.css'

function Checkbox({ id, texto, marcado = false, onChange, desabilitado = false }) {
  return (
    <label className="checkbox" htmlFor={id}>
      <input
        id={id}
        type="checkbox"
        checked={marcado}
        disabled={desabilitado}
        onChange={(evento) => onChange(evento.target.checked)}
      />
      <span>{texto}</span>
    </label>
  )
}

export default Checkbox
