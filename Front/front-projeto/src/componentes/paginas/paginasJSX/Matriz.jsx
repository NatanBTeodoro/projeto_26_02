import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { formatarMoeda, rotuloClasse } from '../../../utils/formatacao'

function iso(data) {
  const mes = String(data.getMonth() + 1).padStart(2, '0')
  const dia = String(data.getDate()).padStart(2, '0')
  return `${data.getFullYear()}-${mes}-${dia}`
}

function Matriz() {
  const hoje = new Date()
  const [de, setDe] = useState(iso(new Date(hoje.getFullYear(), hoje.getMonth(), 1)))
  const [ate, setAte] = useState(iso(hoje))
  const [resultado, setResultado] = useState(null)
  const [erro, setErro] = useState('')

  async function consultar(evento) {
    evento.preventDefault()
    setErro('')
    try {
      const corpo = await apiJson(`${endpoints.analise}?de=${de}&ate=${ate}`)
      setResultado(corpo)
    } catch (falha) {
      setResultado(null)
      setErro(falha.message)
    }
  }

  return (
    <TemplateAutenticado rotaAtual="/matriz">
      <div className="pagina">
        <CabecalhoPagina
          titulo="Matriz"
          descricao="Popularidade, margem e classe são calculadas no servidor para o período."
        />
        <MensagemErro texto={erro} />
        <form className="formulario pagina__painel" onSubmit={consultar}>
          <div className="formulario__linha">
            <FormField id="de" rotulo="De" tipo="date" valor={de} onChange={setDe} obrigatorio />
            <FormField id="ate" rotulo="Até" tipo="date" valor={ate} onChange={setAte} obrigatorio />
          </div>
          <Botao texto="Calcular" tipo="submit" />
        </form>
        {resultado ? (
          <section className="pagina__painel">
            <p>
              {resultado.quantidadePedidos} pedidos · receita {formatarMoeda(resultado.receita)} · ticket médio {formatarMoeda(resultado.ticketMedio)}
            </p>
            <table className="tabela">
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Quantidade</th>
                  <th>Popularidade</th>
                  <th>Margem</th>
                  <th>Classe</th>
                </tr>
              </thead>
              <tbody>
                {resultado.itens.map((item) => (
                  <tr key={item.produtoId}>
                    <td>{item.nome}</td>
                    <td>{item.quantidade}</td>
                    <td>{item.popularidade}%</td>
                    <td>{formatarMoeda(item.margem)}</td>
                    <td>{rotuloClasse(item.classe)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        ) : null}
      </div>
    </TemplateAutenticado>
  )
}

export default Matriz
