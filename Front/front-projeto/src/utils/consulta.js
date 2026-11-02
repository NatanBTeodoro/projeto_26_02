export function parametrosPagina({
  pagina = 0,
  tamanho = 20,
  ordenacao = '',
  filtros = {},
} = {}) {
  const params = new URLSearchParams()
  params.set('page', String(pagina))
  params.set('size', String(tamanho))
  if (ordenacao) params.set('sort', ordenacao)
  Object.entries(filtros).forEach(([chave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') {
      params.set(chave, String(valor))
    }
  })
  return params.toString()
}

export function comConsulta(caminho, consulta) {
  if (!consulta) return caminho
  return `${caminho}?${consulta}`
}
