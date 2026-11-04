const AMBOS = ['ADMINISTRADOR', 'GESTOR']

export const SECOES_MENU = [
  {
    titulo: 'Cardápio',
    itens: [
      { href: '/produtos', rotulo: 'Produtos', perfis: AMBOS },
      { href: '/pedidos', rotulo: 'Pedidos', perfis: AMBOS },
      { href: '/matriz', rotulo: 'Matriz', perfis: AMBOS },
      { href: '/usuarios', rotulo: 'Usuários', perfis: ['ADMINISTRADOR'] },
    ],
  },
]

export function itemPermitido(item, perfil) {
  return item.perfis.includes(perfil)
}
