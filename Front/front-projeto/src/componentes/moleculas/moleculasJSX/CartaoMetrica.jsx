// moleculas/moleculasJSX/CartaoMetrica.jsx
//
// USO: número de destaque no painel (receita, ticket, alertas).
// NECESSIDADE: o dashboard repete o mesmo bloco de rótulo + valor.
//
// PROPS:
//   rotulo  (string)
//   valor   (string)
//   detalhe (string)
//
// EXEMPLOS DE USO:
//   <CartaoMetrica rotulo="Receita" valor="R$ 1.200,00" />

import '../moleculasCSS/CartaoMetrica.css'

function CartaoMetrica({ rotulo, valor, detalhe = '' }) {
  return (
    <article className="cartao-metrica">
      <p className="cartao-metrica__rotulo">{rotulo}</p>
      <strong className="cartao-metrica__valor">{valor}</strong>
      {detalhe ? <p className="cartao-metrica__detalhe">{detalhe}</p> : null}
    </article>
  )
}

export default CartaoMetrica
