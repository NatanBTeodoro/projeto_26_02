// paginas/paginasJSX/Usuarios.jsx

import { useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormUsuario from '../../organismos/organismosJSX/FormUsuario'
import ListaRecurso from '../../organismos/organismosJSX/ListaRecurso'
import AvisosApi from '../../organismos/organismosJSX/AvisosApi'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Botao from '../../atomos/atomosJSX/Botao'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { formatarBooleano, rotuloPerfil } from '../../../utils/formatacao'
import { useRetornoApi } from '../../../utils/useRetornoApi'
import '../paginasCSS/Usuarios.css'

function Usuarios() {
  const sessao = lerSessao()
  const { erro, sucesso, resposta, enviando, executar } = useRetornoApi()
  const [tick, setTick] = useState(0)
  const [id, setId] = useState('')
  const [atual, setAtual] = useState(null)

  async function criar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.usuarios, { method: 'POST', body: JSON.stringify(dados) }),
      'Usuário cadastrado.',
    )
    if (corpo) setTick((valor) => valor + 1)
  }

  async function salvar(dados) {
    const corpo = await executar(
      () => apiJson(endpoints.usuario(id), { method: 'PUT', body: JSON.stringify(dados) }),
      'Usuário atualizado.',
    )
    if (corpo) {
      setAtual(corpo)
      setTick((valor) => valor + 1)
    }
  }

  async function carregar() {
    const corpo = await executar(() => apiJson(endpoints.usuario(id)), '')
    if (corpo) setAtual(corpo)
  }

  return (
    <TemplateAutenticado rotaAtual="/usuarios">
      <div className="usuarios">
        <CabecalhoPagina
          titulo="Usuários e perfis"
          descricao="Um perfil por usuário. Inativar impede o próximo login. A resposta nunca traz a senha."
          perfis={['Administrador']}
        />
        <div className="pagina__grade pagina__grade--lateral">
          <section className="pagina__painel">
            <FormUsuario estabelecimentoId={sessao.estabelecimentoId} onSubmit={criar} enviando={enviando} />
            <div className="formulario__linha">
              <FormField id="usuario-id" rotulo="Id para editar" tipo="number" minimo="1" valor={id} onChange={setId} />
              <Botao texto="Carregar" variante="secundario" onClick={carregar} desabilitado={!id} />
            </div>
            {atual ? (
              <FormUsuario
                key={`${atual.id}-${atual.email}-${atual.perfil}-${atual.ativo}`}
                modo="editar"
                valoresIniciais={atual}
                onSubmit={salvar}
                enviando={enviando}
              />
            ) : null}
            <AvisosApi erro={erro} sucesso={sucesso} resposta={resposta} />
          </section>
          <section className="pagina__painel">
            <ListaRecurso
              caminho={endpoints.usuarios}
              ordenacaoPadrao="email,asc"
              atualizacao={tick}
              colunas={[
                { chave: 'id', rotulo: 'Id' },
                { chave: 'email', rotulo: 'E-mail' },
                { chave: 'perfil', rotulo: 'Perfil', formatar: rotuloPerfil },
                { chave: 'ativo', rotulo: 'Ativo', formatar: formatarBooleano },
              ]}
            />
          </section>
        </div>
      </div>
    </TemplateAutenticado>
  )
}

export default Usuarios
