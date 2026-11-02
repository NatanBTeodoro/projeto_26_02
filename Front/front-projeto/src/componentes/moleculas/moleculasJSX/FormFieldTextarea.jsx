// moleculas/moleculasJSX/FormFieldTextarea.jsx
//
// USO: motivo de estorno e textos longos.
// NECESSIDADE: rótulo + textarea + erro, no mesmo contrato do FormField.
//
// PROPS:
//   id, rotulo, valor, onChange, erro, obrigatorio, placeholder, linhas
//
// EXEMPLOS DE USO:
//   <FormFieldTextarea id="motivo" rotulo="Motivo" valor={motivo} onChange={setMotivo} obrigatorio />

import Label from '../../atomos/atomosJSX/Label'
import Textarea from '../../atomos/atomosJSX/Textarea'
import '../moleculasCSS/FormFieldTextarea.css'

function FormFieldTextarea({
  id,
  rotulo,
  valor,
  onChange,
  erro = '',
  obrigatorio = false,
  placeholder = '',
  linhas = 4,
}) {
  return (
    <div className="form-field">
      <Label htmlFor={id} texto={rotulo} obrigatorio={obrigatorio} />
      <Textarea
        id={id}
        valor={valor}
        onChange={onChange}
        erro={Boolean(erro)}
        placeholder={placeholder}
        linhas={linhas}
      />
      {erro ? <span className="form-field__erro">{erro}</span> : null}
    </div>
  )
}

export default FormFieldTextarea
