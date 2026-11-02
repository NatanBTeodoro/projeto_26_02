// organismos/organismosJSX/Footer.jsx
//
// USO: rodapé dos templates, com o estado de /actuator/health.
// NECESSIDADE: o testador vê se a API está no ar sem sair da tela.
//
// PROPS: nenhuma.
//
// EXEMPLOS DE USO:
//   <Footer />

import { useEffect, useState } from 'react'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/Footer.css'

function Footer() {
  const [status, setStatus] = useState('consultando')

  useEffect(() => {
    let ativo = true
    apiJson(endpoints.health)
      .then((corpo) => {
        if (ativo) setStatus(corpo?.status || 'UP')
      })
      .catch(() => {
        if (ativo) setStatus('INDISPONÍVEL')
      })
    return () => {
      ativo = false
    }
  }, [])

  const classe = status === 'UP' ? 'footer__status--ok' : 'footer__status--falha'

  return (
    <footer className="footer">
      <span>Quadrante · testes dos métodos da API</span>
      <span className={`footer__status ${classe}`}>Saúde {status}</span>
    </footer>
  )
}

export default Footer
