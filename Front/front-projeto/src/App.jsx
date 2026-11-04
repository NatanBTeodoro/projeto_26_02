import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { RotaAutenticada } from './rotas/RotasProtegidas'
import Login from './componentes/paginas/paginasJSX/Login'
import Produtos from './componentes/paginas/paginasJSX/Produtos'
import Pedidos from './componentes/paginas/paginasJSX/Pedidos'
import Matriz from './componentes/paginas/paginasJSX/Matriz'
import Usuarios from './componentes/paginas/paginasJSX/Usuarios'

function proteger(pagina) {
  return <RotaAutenticada>{pagina}</RotaAutenticada>
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Navigate to="/produtos" replace />} />
        <Route path="/produtos" element={proteger(<Produtos />)} />
        <Route path="/pedidos" element={proteger(<Pedidos />)} />
        <Route path="/matriz" element={proteger(<Matriz />)} />
        <Route path="/usuarios" element={proteger(<Usuarios />)} />
        <Route path="*" element={<Navigate to="/produtos" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
