// atomos/atomosJSX/Input.jsx
//
// USO: campo de texto, número, data ou arquivo controlado pelo pai.
// NECESSIDADE: um único estilo de entrada, sem repetir borda e foco.
//
// PROPS:
//   id             (string) — liga o campo ao rótulo
//   tipo           (string) — text, number, email, date, datetime-local, file
//   valor          (string) — valor controlado; vazio em arquivo
//   onChange       (function) — recebe o novo valor ou o arquivo
//   placeholder    (string)
//   desabilitado   (boolean)
//   erro           (boolean) — marca a borda de erro
//   obrigatorio    (boolean)
//   passo          (string) — step de número
//   minimo         (string)
//   maximo         (string)
//   nome           (string)
//
// EXEMPLOS DE USO:
//   <Input id="nome" valor={nome} onChange={setNome} />

import '../atomosCSS/Input.css'

function Input({
  id,
  tipo = 'text',
  valor = '',
  onChange,
  placeholder = '',
  desabilitado = false,
  erro = false,
  obrigatorio = false,
  passo,
  minimo,
  maximo,
  nome,
}) {
  const classe = `input ${erro ? 'input--erro' : ''} ${tipo === 'file' ? 'input--arquivo' : ''}`

  function aoMudar(evento) {
    if (tipo === 'file') {
      onChange(evento.target.files?.[0] || null)
      return
    }
    onChange(evento.target.value)
  }

  return (
    <input
      className={classe}
      id={id}
      name={nome || id}
      type={tipo}
      value={tipo === 'file' ? undefined : valor}
      onChange={aoMudar}
      placeholder={placeholder}
      disabled={desabilitado}
      aria-required={obrigatorio || undefined}
      step={passo}
      min={minimo}
      max={maximo}
    />
  )
}

export default Input
