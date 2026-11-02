// organismos/organismosJSX/Navbar.jsx
//
// USO: menu lateral de todas as telas autenticadas.
// NECESSIDADE: concentra os atalhos dos métodos e marca o que está fora do perfil.
//
// PROPS:
//   perfil     (string)
//   rotaAtual  (string)
//   onSair     (function)
//
// EXEMPLOS DE USO:
//   <Navbar perfil="GESTOR" rotaAtual="/produtos" onSair={sair} />

import { useState } from 'react'
import Logo from '../../atomos/atomosJSX/Logo'
import Badge from '../../atomos/atomosJSX/Badge'
import Botao from '../../atomos/atomosJSX/Botao'
import NavItem from '../../moleculas/moleculasJSX/NavItem'
import { SECOES_MENU, itemPermitido } from '../../../utils/menu'
import { lerSessao } from '../../../utils/sessao'
import { rotuloPerfil } from '../../../utils/formatacao'
import '../organismosCSS/Navbar.css'

function Navbar({ perfil, rotaAtual, onSair }) {
  const [aberto, setAberto] = useState(false)
  const sessao = lerSessao()

  return (
    <>
      <button className="navbar__abrir" type="button" onClick={() => setAberto(true)}>
        Menu
      </button>
      {aberto ? <button className="navbar__velo" type="button" aria-label="Fechar menu" onClick={() => setAberto(false)} /> : null}
      <aside className={`navbar ${aberto ? 'navbar--aberto' : ''}`}>
        <div className="navbar__topo">
          <Logo compacto />
          <button className="navbar__fechar" type="button" onClick={() => setAberto(false)}>
            Fechar
          </button>
        </div>
        <p className="navbar__usuario">
          <span>{sessao.usuarioEmail || 'Sessão local'}</span>
          <Badge texto={rotuloPerfil(perfil)} variante="marca" />
        </p>
        <nav className="navbar__menu">
          {SECOES_MENU.map((secao) => (
            <div key={secao.titulo} className="navbar__secao">
              <p className="navbar__secao-titulo">{secao.titulo}</p>
              {secao.itens.map((item) => (
                <NavItem
                  key={item.href}
                  href={item.href}
                  texto={item.rotulo}
                  ativo={rotaAtual === item.href}
                  restrito={!itemPermitido(item, perfil)}
                  onClick={() => setAberto(false)}
                />
              ))}
            </div>
          ))}
        </nav>
        <div className="navbar__sair">
          <Botao texto="Sair" variante="fantasma" larguraTotal onClick={onSair} />
        </div>
      </aside>
    </>
  )
}

export default Navbar
