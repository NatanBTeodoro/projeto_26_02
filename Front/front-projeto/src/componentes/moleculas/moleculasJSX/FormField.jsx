// moleculas/moleculasJSX/FormField.jsx
//
// USO: rótulo + input + erro de um campo.
// NECESSIDADE: o trio se repete em todo formulário do projeto.
//
// PROPS:
//   id, rotulo, tipo, valor, onChange, erro, obrigatorio, placeholder, desabilitado, passo, minimo, dica
//
// EXEMPLOS DE USO:
//   <FormField id="nome" rotulo="Nome" valor={nome} onChange={setNome} obrigatorio />

import Label from '../../atomos/atomosJSX/Label'
import Input from '../../atomos/atomosJSX/Input'
import Texto from '../../atomos/atomosJSX/Texto'
import '../moleculasCSS/FormField.css'

function FormField({
  id,
  rotulo,
  tipo = 'text',
  valor,
  onChange,
  erro = '',
  obrigatorio = false,
  placeholder = '',
  desabilitado = false,
  passo,
  minimo,
  dica = '',
}) {
  return (
    <div className={`form-field ${erro ? 'form-field--erro' : ''}`}>
      <Label htmlFor={id} texto={rotulo} obrigatorio={obrigatorio} />
      <Input
        id={id}
        tipo={tipo}
        valor={valor}
        onChange={onChange}
        placeholder={placeholder}
        desabilitado={desabilitado}
        erro={Boolean(erro)}
        obrigatorio={obrigatorio}
        passo={passo}
        minimo={minimo}
      />
      {dica ? <Texto variante="dica">{dica}</Texto> : null}
      {erro ? <span className="form-field__erro">{erro}</span> : null}
    </div>
  )
}

export default FormField
