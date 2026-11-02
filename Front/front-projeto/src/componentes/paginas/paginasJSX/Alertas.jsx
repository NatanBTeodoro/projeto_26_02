// paginas/paginasJSX/Alertas.jsx

import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import Badge from '../../atomos/atomosJSX/Badge'
import { endpoints } from '../../../services/endpoints'
import { formatarBooleano, rotuloStatus } from '../../../utils/formatacao'
import '../paginasCSS/Alertas.css'

function Alertas() {
  return (
    <TemplateAutenticado rotaAtual="/alertas">
      <div className="alertas">
        <CabecalhoPagina
          titulo="Alertas"
          descricao="Item que permanece Cão por N períodos consecutivos. Em observação não conta."
          perfis={['Administrador', 'Gestor', 'Analista']}
        />
        <section className="pagina__painel">
          <ListaRecurso
            caminho={endpoints.alertas}
            ordenacaoPadrao="id,desc"
            colunas={[
              { chave: 'id', rotulo: 'Id' },
              { chave: 'produtoId', rotulo: 'Produto' },
              { chave: 'tipo', rotulo: 'Tipo', formatar: (valor) => <Badge texto={rotuloStatus(valor)} variante="cao" /> },
              { chave: 'periodos', rotulo: 'Períodos' },
              { chave: 'ativo', rotulo: 'Ativo', formatar: formatarBooleano },
            ]}
          />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Alertas
