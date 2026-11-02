// atomos/atomosJSX/Logo.jsx
//
// USO: marca no menu e na tela de login.
// NECESSIDADE: o quadrante da matriz vira a identidade, sem imagem externa.
//
// PROPS:
//   compacto  (boolean) — só o símbolo e o nome curto
//
// EXEMPLOS DE USO:
//   <Logo />

import '../atomosCSS/Logo.css'

function Logo({ compacto = false }) {
  return (
    <span className={`logo ${compacto ? 'logo--compacto' : ''}`}>
      <span className="logo__marca" aria-hidden="true">
        <i /><i /><i /><i />
      </span>
      <span className="logo__textos">
        <strong>Quadrante</strong>
        {compacto ? null : <small>engenharia de cardápio</small>}
      </span>
    </span>
  )
}

export default Logo
