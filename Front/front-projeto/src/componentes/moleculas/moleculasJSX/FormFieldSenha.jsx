// moleculas/moleculasJSX/FormFieldSenha.jsx
//
// USO: senha de login e de cadastro de usuário.
// NECESSIDADE: o tipo password fica isolado, com a mesma casca do FormField.
//
// PROPS:
//   id, rotulo, valor, onChange, erro, obrigatorio, dica
//
// EXEMPLOS DE USO:
//   <FormFieldSenha id="senha" rotulo="Senha" valor={senha} onChange={setSenha} obrigatorio />

import FormField from './FormField'
import '../moleculasCSS/FormFieldSenha.css'

function FormFieldSenha(props) {
  return <FormField {...props} tipo="password" />
}

export default FormFieldSenha
