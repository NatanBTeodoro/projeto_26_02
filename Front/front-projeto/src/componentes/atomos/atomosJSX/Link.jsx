// atomos/atomosJSX/Link.jsx
//
// USO: navegação interna (React Router) ou âncora externa.
// NECESSIDADE: o mesmo visual de link em menu, tabela e rodapé.
//
// PROPS:
//   href      (string)
//   texto     (string)
//   variante  (string) — texto ou discreto
//
// EXEMPLOS DE USO:
//   <Link href="/produtos/1" texto="Abrir" />

import { Link as LinkRota } from 'react-router-dom'
import '../atomosCSS/Link.css'

function Link({ href, texto, variante = 'texto' }) {
  const classe = `link link--${variante}`
  if (href.startsWith('/')) {
    return (
      <LinkRota className={classe} to={href}>
        {texto}
      </LinkRota>
    )
  }
  return (
    <a className={classe} href={href}>
      {texto}
    </a>
  )
}

export default Link
