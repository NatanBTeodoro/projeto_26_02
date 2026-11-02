// templates/templatesJSX/TemplatePublico.jsx
//
// USO: login e recuperação de senha.
// NECESSIDADE: enquadra a marca e o rodapé sem o menu autenticado.
//
// PROPS:
//   children (node)
//
// EXEMPLOS DE USO:
//   <TemplatePublico><FormLogin /></TemplatePublico>

import Logo from '../../atomos/atomosJSX/Logo'
import Footer from '../../organismos/organismosJSX/Footer'
import '../templatesCSS/TemplatePublico.css'

function TemplatePublico({ children }) {
  return (
    <div className="template-publico">
      <header className="template-publico__topo">
        <Logo />
      </header>
      <main className="template-publico__main">{children}</main>
      <Footer />
    </div>
  )
}

export default TemplatePublico
