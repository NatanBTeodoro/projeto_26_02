// paginas/paginasJSX/Auditoria.jsx

import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import { endpoints } from '../../../services/endpoints'
import { formatarDataHora } from '../../../utils/formatacao'
import '../paginasCSS/Auditoria.css'

function Auditoria() {
  return (
    <TemplateAutenticado rotaAtual="/auditoria">
      <div className="auditoria">
        <CabecalhoPagina
          titulo="Auditoria"
          descricao="Quem alterou o quê, com o valor anterior e o novo. Ordenação inválida deve responder 400."
          perfis={['Administrador']}
        />
        <section className="pagina__painel">
          <ListaRecurso
            caminho={endpoints.auditoria}
            ordenacaoPadrao="dataHora,desc"
            colunas={[
              { chave: 'dataHora', rotulo: 'Quando', formatar: formatarDataHora },
              { chave: 'usuarioId', rotulo: 'Usuário' },
              { chave: 'acao', rotulo: 'Ação' },
              { chave: 'entidade', rotulo: 'Entidade' },
              { chave: 'entidadeId', rotulo: 'Id' },
              { chave: 'valorAnterior', rotulo: 'Antes' },
              { chave: 'valorNovo', rotulo: 'Depois' },
            ]}
          />
        </section>
      </div>
    </TemplateAutenticado>
  )
}

export default Auditoria
