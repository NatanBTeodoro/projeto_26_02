// atomos/atomosJSX/MensagemSucesso.jsx
//
// USO: confirmação depois de um POST ou PUT bem-sucedido.
// NECESSIDADE: feedback positivo separado da mensagem de erro.
//
// PROPS:
//   texto  (string)
//
// EXEMPLOS DE USO:
//   <MensagemSucesso texto="Produto cadastrado." />

import '../atomosCSS/MensagemSucesso.css'

function MensagemSucesso({ texto }) {
  if (!texto) return null
  return (
    <p className="mensagem-sucesso" role="status">
      {texto}
    </p>
  )
}

export default MensagemSucesso
