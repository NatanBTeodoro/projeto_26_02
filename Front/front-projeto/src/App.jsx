import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { RotaAutenticada } from './rotas/RotasProtegidas'
import Login from './componentes/paginas/paginasJSX/Login'
import RecuperarSenha from './componentes/paginas/paginasJSX/RecuperarSenha'
import Dashboard from './componentes/paginas/paginasJSX/Dashboard'
import Produtos from './componentes/paginas/paginasJSX/Produtos'
import ProdutoDetalhe from './componentes/paginas/paginasJSX/ProdutoDetalhe'
import CategoriasInsumos from './componentes/paginas/paginasJSX/CategoriasInsumos'
import RegistroVenda from './componentes/paginas/paginasJSX/RegistroVenda'
import HistoricoVendas from './componentes/paginas/paginasJSX/HistoricoVendas'
import Importacao from './componentes/paginas/paginasJSX/Importacao'
import Matriz from './componentes/paginas/paginasJSX/Matriz'
import CrossSell from './componentes/paginas/paginasJSX/CrossSell'
import Simulador from './componentes/paginas/paginasJSX/Simulador'
import Alertas from './componentes/paginas/paginasJSX/Alertas'
import CurvaAbc from './componentes/paginas/paginasJSX/CurvaAbc'
import Comparativo from './componentes/paginas/paginasJSX/Comparativo'
import Configuracoes from './componentes/paginas/paginasJSX/Configuracoes'
import Usuarios from './componentes/paginas/paginasJSX/Usuarios'
import Auditoria from './componentes/paginas/paginasJSX/Auditoria'
import MetricasCliente from './componentes/paginas/paginasJSX/MetricasCliente'
import Estabelecimento from './componentes/paginas/paginasJSX/Estabelecimento'
import Unidades from './componentes/paginas/paginasJSX/Unidades'

function proteger(pagina) {
  return <RotaAutenticada>{pagina}</RotaAutenticada>
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/recuperar-senha" element={<RecuperarSenha />} />
        <Route path="/" element={proteger(<Dashboard />)} />
        <Route path="/produtos" element={proteger(<Produtos />)} />
        <Route path="/produtos/:id" element={proteger(<ProdutoDetalhe />)} />
        <Route path="/categorias" element={proteger(<CategoriasInsumos />)} />
        <Route path="/estabelecimento" element={proteger(<Estabelecimento />)} />
        <Route path="/usuarios" element={proteger(<Usuarios />)} />
        <Route path="/vendas/nova" element={proteger(<RegistroVenda />)} />
        <Route path="/vendas" element={proteger(<HistoricoVendas />)} />
        <Route path="/importacao" element={proteger(<Importacao />)} />
        <Route path="/matriz" element={proteger(<Matriz />)} />
        <Route path="/cross-sell" element={proteger(<CrossSell />)} />
        <Route path="/abc" element={proteger(<CurvaAbc />)} />
        <Route path="/comparativo" element={proteger(<Comparativo />)} />
        <Route path="/simulador" element={proteger(<Simulador />)} />
        <Route path="/configuracoes" element={proteger(<Configuracoes />)} />
        <Route path="/unidades" element={proteger(<Unidades />)} />
        <Route path="/clientes" element={proteger(<MetricasCliente />)} />
        <Route path="/alertas" element={proteger(<Alertas />)} />
        <Route path="/auditoria" element={proteger(<Auditoria />)} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
