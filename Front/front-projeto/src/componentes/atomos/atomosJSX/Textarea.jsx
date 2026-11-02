// atomos/atomosJSX/Textarea.jsx
//
// USO: textos longos, como motivo de estorno ou relatório de erros.
// NECESSIDADE: área de várias linhas com o mesmo foco do Input.
//
// PROPS:
//   id            (string)
//   valor         (string)
//   onChange      (function)
//   linhas        (number)
//   desabilitado  (boolean)
//   erro          (boolean)
//   placeholder   (string)
//
// EXEMPLOS DE USO:
//   <Textarea id="motivo" valor={motivo} onChange={setMotivo} />

import '../atomosCSS/Textarea.css'

function Textarea({
  id,
  valor = '',
  onChange,
  linhas = 4,
  desabilitado = false,
  erro = false,
  placeholder = '',
}) {
  return (
    <textarea
      className={`textarea ${erro ? 'textarea--erro' : ''}`}
      id={id}
      rows={linhas}
      value={valor}
      placeholder={placeholder}
      disabled={desabilitado}
      onChange={(evento) => onChange(evento.target.value)}
    />
  )
}

export default Textarea
