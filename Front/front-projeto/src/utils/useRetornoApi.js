import { useState } from 'react'
import { executarAcao } from './acao'

export function useRetornoApi() {
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [resposta, setResposta] = useState(undefined)
  const [enviando, setEnviando] = useState(false)

  async function executar(pedido, sucessoTexto) {
    setEnviando(true)
    try {
      return await executarAcao(pedido, {
        setErro,
        setSucesso,
        setResposta,
        sucesso: sucessoTexto,
      })
    } finally {
      setEnviando(false)
    }
  }

  return { erro, sucesso, resposta, enviando, executar }
}
