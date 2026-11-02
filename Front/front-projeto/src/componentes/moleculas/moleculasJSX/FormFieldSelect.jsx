// moleculas/moleculasJSX/FormFieldSelect.jsx
//
// USO: rótulo + select + erro.
// NECESSIDADE: perfil, granularidade e critério de popularidade no mesmo padrão do campo texto.
//
// PROPS:
//   id, rotulo, valor, onChange, opcoes, erro, obrigatorio, dica
//
// EXEMPLOS DE USO:
//   <FormFieldSelect id="perfil" rotulo="Perfil" valor={perfil} onChange={setPerfil} opcoes={opcoes} />

import Label from '../../atomos/atomosJSX/Label'
import Select from '../../atomos/atomosJSX/Select'
import Texto from '../../atomos/atomosJSX/Texto'
import '../moleculasCSS/FormFieldSelect.css'

function FormFieldSelect({
  id,
  rotulo,
  valor,
  onChange,
  opcoes = [],
  erro = '',
  obrigatorio = false,
  dica = '',
}) {
  return (
    <div className="form-field">
      <Label htmlFor={id} texto={rotulo} obrigatorio={obrigatorio} />
      <Select id={id} valor={valor} onChange={onChange} opcoes={opcoes} erro={Boolean(erro)} />
      {dica ? <Texto variante="dica">{dica}</Texto> : null}
      {erro ? <span className="form-field__erro">{erro}</span> : null}
    </div>
  )
}

export default FormFieldSelect
