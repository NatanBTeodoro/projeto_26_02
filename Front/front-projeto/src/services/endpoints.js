// Caminhos alinhados ao projeto-menu-engineering.md (seções 14 e 15) e aos DTOs do back.
// O front só monta a URL; o cálculo de negócio fica no servidor.

export const endpoints = {
  csrf: '/api/auth/csrf',
  login: '/api/auth/login',
  logout: '/api/auth/logout',
  recuperarSenha: '/api/auth/recuperar-senha',
  sessao: '/api/auth/sessao',
  health: '/actuator/health',

  estabelecimentos: '/api/estabelecimentos',
  estabelecimento: (id) => `/api/estabelecimentos/${id}`,

  usuarios: '/api/usuarios',
  usuario: (id) => `/api/usuarios/${id}`,

  categorias: '/api/categorias',
  categoria: (id) => `/api/categorias/${id}`,

  insumos: '/api/insumos',
  insumo: (id) => `/api/insumos/${id}`,

  produtos: '/api/produtos',
  produto: (id) => `/api/produtos/${id}`,
  historicoPreco: (id) => `/api/produtos/${id}/historico-preco`,
  fichaTecnica: (id) => `/api/produtos/${id}/ficha-tecnica`,

  pedidos: '/api/pedidos',
  pedido: (id) => `/api/pedidos/${id}`,
  estorno: (id) => `/api/pedidos/${id}/estorno`,

  importacoes: '/api/importacoes',
  importacao: (id) => `/api/importacoes/${id}`,
  previsualizarCsv: '/api/importacoes/pre-visualizar',

  analises: '/api/analises',
  analise: (id) => `/api/analises/${id}`,
  classificacoes: (id) => `/api/analises/${id}/classificacoes`,
  regras: (id) => `/api/analises/${id}/regras`,
  reexecutar: (id) => `/api/analises/${id}/reexecutar`,
  combos: (id) => `/api/analises/${id}/combos`,
  simular: '/api/analises/simular',
  comparar: '/api/analises/comparar',
  exportarCsv: (id) => `/api/analises/${id}/exportar.csv`,
  exportarPdf: (id) => `/api/analises/${id}/exportar.pdf`,

  configAnalise: '/api/config-analise',
  alertas: '/api/alertas',
  dashboard: '/api/dashboard',
  abc: '/api/relatorios/abc',
  exportarAbcCsv: '/api/relatorios/abc/exportar.csv',
  exportarAbcPdf: '/api/relatorios/abc/exportar.pdf',
  metricasCliente: '/api/relatorios/clientes',
  unidades: '/api/unidades',
  comparativoUnidades: '/api/relatorios/unidades',
  auditoria: '/api/auditoria',
}
