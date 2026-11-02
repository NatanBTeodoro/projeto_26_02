// moleculas/moleculasJSX/NavItem.jsx
//
// USO: item do menu lateral.
// NECESSIDADE: marca a rota atual e o item fora do perfil, sem repetir o link.
//
// PROPS:
//   href      (string)
//   texto     (string)
//   ativo     (boolean)
//   restrito  (boolean) — perfil atual não é o previsto para o método
//   onClick   (function)
//
// EXEMPLOS DE USO:
//   <NavItem href="/produtos" texto="Produtos" ativo={rotaAtual === '/produtos'} />

import { Link } from 'react-router-dom'
import '../moleculasCSS/NavItem.css'

function NavItem({ href, texto, ativo = false, restrito = false, onClick }) {
  const classe = [
    'nav-item',
    ativo ? 'nav-item--ativo' : '',
    restrito ? 'nav-item--restrito' : '',
  ].filter(Boolean).join(' ')

  return (
    <Link className={classe} to={href} onClick={onClick}>
      <span>{texto}</span>
      {restrito ? <small>outro perfil</small> : null}
    </Link>
  )
}

export default NavItem
