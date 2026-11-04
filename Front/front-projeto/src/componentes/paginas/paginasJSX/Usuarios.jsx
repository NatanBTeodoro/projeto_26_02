import { useEffect, useState } from 'react'
import TemplateAutenticado from '../../templates/templatesJSX/TemplateAutenticado'
import CabecalhoPagina from '../../moleculas/moleculasJSX/CabecalhoPagina'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Select from '../../atomos/atomosJSX/Select'
import Botao from '../../atomos/atomosJSX/Botao'
import MensagemErro from '../../atomos/atomosJSX/MensagemErro'
import { apiJson } from '../../../services/api'
import { endpoints } from '../../../services/endpoints'
import { lerSessao } from '../../../utils/sessao'
import { formatarBooleano, rotuloPerfil } from '../../../utils/formatacao'

const perfis = [
  { valor: 'GESTOR', rotulo: 'Gestor' },
  { valor: 'ADMINISTRADOR', rotulo: 'Administrador' },
]

function Usuarios() {
  const sessao = lerSessao()
  const [usuarios, setUsuarios] = useState([])
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [perfil, setPerfil] = useState('GESTOR')
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  async function carregar() {
    setUsuarios(await apiJson(endpoints.usuarios))
  }

  useEffect(() => {
    if (sessao.perfil !== 'ADMINISTRADOR') return
    carregar().catch((falha) => setErro(falha.message))
  }, [sessao.perfil])

  async function criar(evento) {
    evento.preventDefault()
    setErro('')
    try {
      await apiJson(endpoints.usuarios, {
        method: 'POST',
        body: JSON.stringify({ email, senha, perfil }),
      })
      setEmail('')
      setSenha('')
      setAviso('Usuário cadastrado.')
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  async function atualizar(usuario, campos) {
    setErro('')
    try {
      await apiJson(endpoints.usuario(usuario.id), {
        method: 'PUT',
        body: JSON.stringify(campos),
      })
      setAviso('Usuário atualizado.')
      await carregar()
    } catch (falha) {
      setErro(falha.message)
    }
  }

  if (sessao.perfil !== 'ADMINISTRADOR') {
    return (
      <TemplateAutenticado rotaAtual="/usuarios">
        <div className="pagina">
          <CabecalhoPagina titulo="Usuários" />
          <MensagemErro texto="Acesso negado" />
        </div>
      </TemplateAutenticado>
    )
  }

  return (
    <TemplateAutenticado rotaAtual="/usuarios">
      <div className="pagina">
        <CabecalhoPagina
          titulo="Usuários"
          descricao="O administrador redefine a senha. A resposta da API não devolve o hash."
        />
        <MensagemErro texto={erro} />
        {aviso ? <p>{aviso}</p> : null}
        <form className="formulario pagina__painel" onSubmit={criar}>
          <FormField id="email" rotulo="E-mail" tipo="email" valor={email} onChange={setEmail} obrigatorio />
          <FormField id="senha" rotulo="Senha" tipo="password" valor={senha} onChange={setSenha} obrigatorio />
          <label htmlFor="perfil">Perfil</label>
          <Select id="perfil" valor={perfil} onChange={setPerfil} opcoes={perfis} />
          <Botao texto="Cadastrar" tipo="submit" />
        </form>
        <table className="tabela">
          <thead>
            <tr>
              <th>E-mail</th>
              <th>Perfil</th>
              <th>Ativo</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {usuarios.map((usuario) => (
              <tr key={usuario.id}>
                <td>{usuario.email}</td>
                <td>{rotuloPerfil(usuario.perfil)}</td>
                <td>{formatarBooleano(usuario.ativo)}</td>
                <td>
                  <Botao
                    texto={usuario.perfil === 'ADMINISTRADOR' ? 'Tornar gestor' : 'Tornar administrador'}
                    variante="secundario"
                    onClick={() => atualizar(usuario, {
                      perfil: usuario.perfil === 'ADMINISTRADOR' ? 'GESTOR' : 'ADMINISTRADOR',
                    })}
                  />
                  <Botao
                    texto={usuario.ativo ? 'Inativar' : 'Reativar'}
                    variante="fantasma"
                    onClick={() => atualizar(usuario, { ativo: !usuario.ativo })}
                  />
                  <Botao
                    texto="Redefinir senha"
                    variante="fantasma"
                    onClick={() => {
                      const nova = window.prompt('Nova senha')
                      if (nova) atualizar(usuario, { senha: nova })
                    }}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </TemplateAutenticado>
  )
}

export default Usuarios
