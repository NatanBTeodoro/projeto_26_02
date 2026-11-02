const TODOS = ['ADMINISTRADOR', 'GESTOR', 'OPERADOR', 'ANALISTA']
const LEITURA_CARDAPIO = ['ADMINISTRADOR', 'GESTOR', 'ANALISTA']
const VENDAS = ['ADMINISTRADOR', 'GESTOR', 'OPERADOR']
const ANALISE = ['ADMINISTRADOR', 'GESTOR', 'ANALISTA']
const GESTAO = ['ADMINISTRADOR', 'GESTOR']

export const SECOES_MENU = [
  {
    titulo: 'Geral',
    itens: [
      { href: '/', rotulo: 'Painel', perfis: TODOS },
      { href: '/alertas', rotulo: 'Alertas', perfis: ANALISE },
    ],
  },
  {
    titulo: 'Cadastros',
    itens: [
      { href: '/produtos', rotulo: 'Produtos', perfis: LEITURA_CARDAPIO },
      { href: '/categorias', rotulo: 'Categorias e insumos', perfis: LEITURA_CARDAPIO },
      { href: '/estabelecimento', rotulo: 'Estabelecimento', perfis: GESTAO },
      { href: '/usuarios', rotulo: 'Usuários', perfis: ['ADMINISTRADOR'] },
    ],
  },
  {
    titulo: 'Vendas',
    itens: [
      { href: '/vendas/nova', rotulo: 'Registrar venda', perfis: VENDAS },
      { href: '/vendas', rotulo: 'Histórico e estorno', perfis: VENDAS },
      { href: '/importacao', rotulo: 'Importar CSV', perfis: VENDAS },
    ],
  },
  {
    titulo: 'Análises',
    itens: [
      { href: '/matriz', rotulo: 'Matriz', perfis: ANALISE },
      { href: '/cross-sell', rotulo: 'Cross-sell', perfis: ANALISE },
      { href: '/abc', rotulo: 'Categoria e ABC', perfis: ANALISE },
      { href: '/comparativo', rotulo: 'Comparar períodos', perfis: ANALISE },
      { href: '/simulador', rotulo: 'Simulador de preço', perfis: GESTAO },
      { href: '/configuracoes', rotulo: 'Parâmetros', perfis: GESTAO },
      { href: '/unidades', rotulo: 'Unidades', perfis: GESTAO },
      { href: '/clientes', rotulo: 'Métricas por cliente', perfis: GESTAO },
    ],
  },
  {
    titulo: 'Governança',
    itens: [
      { href: '/auditoria', rotulo: 'Auditoria', perfis: ['ADMINISTRADOR'] },
    ],
  },
]

export function itemPermitido(item, perfil) {
  return item.perfis.includes(perfil)
}
