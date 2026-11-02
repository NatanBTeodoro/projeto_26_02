const ROTULOS_CLASSE = {
  ESTRELA: 'Estrela',
  VACA_LEITEIRA: 'Vaca leiteira',
  ENIGMA: 'Enigma',
  CAO: 'Cão',
  EM_OBSERVACAO: 'Em observação',
  DADO_INCOMPLETO: 'Dado incompleto',
}

const ROTULOS_PERFIL = {
  ADMINISTRADOR: 'Administrador',
  GESTOR: 'Gestor',
  OPERADOR: 'Operador',
  ANALISTA: 'Analista',
}

const ROTULOS_STATUS = {
  ATIVO: 'Ativo',
  ESTORNADO: 'Estornado',
  PENDENTE: 'Pendente',
  EM_EXECUCAO: 'Em execução',
  CONCLUIDA: 'Concluída',
  FALHOU: 'Falhou',
  PROCESSANDO: 'Processando',
  CONCLUIDA_COM_ERROS: 'Concluída com erros',
  CAO_RECORRENTE: 'Cão recorrente',
  PONDERADA: 'Média ponderada',
  SIMPLES: 'Média simples',
  DIA: 'Dia',
  MES: 'Mês',
  TRIMESTRE: 'Trimestre',
  ANO: 'Ano',
}

export function formatarMoeda(valor) {
  if (valor === null || valor === undefined || valor === '') return '—'
  const numero = Number(valor)
  if (Number.isNaN(numero)) return String(valor)
  return numero.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

export function formatarNumero(valor, casas = 2) {
  if (valor === null || valor === undefined || valor === '') return '—'
  const numero = Number(valor)
  if (Number.isNaN(numero)) return String(valor)
  return numero.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas })
}

export function formatarDataHora(valor) {
  if (!valor) return '—'
  const data = new Date(valor)
  if (Number.isNaN(data.getTime())) return String(valor)
  return data.toLocaleString('pt-BR', { timeZone: 'America/Sao_Paulo' })
}

export function formatarBooleano(valor) {
  if (valor === true) return 'Sim'
  if (valor === false) return 'Não'
  return '—'
}

export function rotuloClasse(classe) {
  return ROTULOS_CLASSE[classe] || classe || '—'
}

export function rotuloPerfil(perfil) {
  return ROTULOS_PERFIL[perfil] || perfil || '—'
}

export function rotuloStatus(status) {
  return ROTULOS_STATUS[status] || status || '—'
}

export function paraInstantLocal(valorDatetimeLocal) {
  if (!valorDatetimeLocal) return null
  const data = new Date(valorDatetimeLocal)
  if (Number.isNaN(data.getTime())) return null
  return data.toISOString()
}

function paraInputLocal(data) {
  const deslocamento = data.getTimezoneOffset()
  const local = new Date(data.getTime() - deslocamento * 60000)
  return local.toISOString().slice(0, 16)
}

export function periodoMesAtual() {
  const agora = new Date()
  const inicio = new Date(agora.getFullYear(), agora.getMonth(), 1, 0, 0, 0)
  const fim = new Date(agora.getFullYear(), agora.getMonth() + 1, 0, 23, 59, 0)
  return { inicio: paraInputLocal(inicio), fim: paraInputLocal(fim) }
}

export function validarSenha(senha) {
  if (!senha || senha.length < 8 || senha.length > 72) {
    return 'A senha precisa ter entre 8 e 72 caracteres.'
  }
  if (!/[A-Za-z]/.test(senha) || !/\d/.test(senha)) {
    return 'A senha precisa de pelo menos uma letra e um número.'
  }
  return ''
}

export function numeroOuNulo(valor) {
  if (valor === '' || valor === null || valor === undefined) return null
  const numero = Number(valor)
  return Number.isNaN(numero) ? null : numero
}
