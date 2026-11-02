// paginas/paginasJSX/RegistroVenda.jsx

import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormPedido from '../../organismos/organismosJSX/FormPedido'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/RegistroVenda.css'

function RegistroVenda() {
  const sessao = lerSessao()
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()

  async function registrar(dados) {
    await executar(
      () => apiJson(endpoints.pedidos, { method: 'POST', body: JSON.stringify(dados) }),
      'Venda registrada. O servidor grava o preço e o custo vigentes.',
    )
  }

  return (
    <TemplateAutenticado rotaAtual="/vendas/nova">
      <div className="registro-venda">
        <CabecalhoPagina
          titulo="Registrar venda"
          descricao="O pedido precisa de ao menos um item, quantidade inteira e data que não seja futura. O custo não sai desta tela."
          perfis={['Administrador', 'Gestor', 'Operador']}
        />
        <section className="pagina__painel">
          <FormPedido estabelecimentoId={sessao.estabelecimentoId} onSubmit={registrar} enviando={enviando} />
          <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default RegistroVenda
