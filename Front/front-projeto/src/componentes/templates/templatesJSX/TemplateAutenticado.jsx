// templates/templatesJSX/TemplateAutenticado.jsx
//
// USO: todas as rotas depois do login.
// NECESSIDADE: menu, conteúdo e rodapé no mesmo esqueleto para os quatro perfis.
//
// PROPS:
//   rotaAtual (string)
//   children  (node)
//
// EXEMPLOS DE USO:
//   <TemplateAutenticado rotaAtual="/produtos">{conteudo}</TemplateAutenticado>

import Navbar from '../../organismos/organismosJSX/Navbar'
import Footer from '../../organismos/organismosJSX/Footer'
import { apiJson, esquecerCsrf } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao, limparSessao } from '../../../utils/sessao'
import '../templatesCSS/TemplateAutenticado.css'

async function sair() {
  try {
    await apiJson(endpoints.logout, { method: 'POST' })
  } catch {
    /* sessão já encerrada: segue para o login */
  }
  limparSessao()
  esquecerCsrf()
  window.location.href = '/login'
}

function TemplateAutenticado({ rotaAtual, children }) {
  const sessao = lerSessao()

  return (
    <div className="template-autenticado">
      <Navbar perfil={sessao.perfil} rotaAtual={rotaAtual} onSair={sair} />
      <div className="template-autenticado__corpo">
        <main className="template-autenticado__main">{children}</main>
        <Footer />
      </div>
    </div>
  )
}

export default TemplateAutenticado
