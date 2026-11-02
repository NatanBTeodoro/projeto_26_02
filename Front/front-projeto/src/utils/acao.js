export async function executarAcao(pedido, { setErro, setSucesso, setResposta, sucesso }) {
  setErro('')
  setSucesso('')
  try {
    const corpo = await pedido()
    setResposta(corpo)
    if (sucesso) setSucesso(sucesso)
    return corpo
  } catch (erro) {
    setErro(erro?.message || 'Erro de conexão. Tente novamente.')
    setResposta(erro?.corpo ?? null)
    return null
  }
}
