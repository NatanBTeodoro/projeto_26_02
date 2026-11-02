// organismos/organismosJSX/AvisosApi.jsx
//
// USO: bloco de erro, sucesso e JSON depois de uma ação.
// NECESSIDADE: toda página de teste mostra o mesmo retorno do back.
//
// PROPS:
//   erro, sucesso, resposta
//
// EXEMPLOS DE USO:
//   <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />

import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import MensagemSucesso from '../../atomos/atomosJSX/MensagemSucesso'
import ResultadoApi from './ResultadoApi'
import '../organismosCSS/AvisosApi.css'

function AvisosApi({ erro = '', sucesso = '', resposta }) {
  return (
    <div className="avisos-api">
      <MensagemErro texto={erro} />
      <MensagemSucesso texto={sucesso} />
      <ResultadoApi resposta={resposta} />
    </div>
  )
}

export default AvisosApi
