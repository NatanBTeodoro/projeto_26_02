// atomos/atomosJSX/Select.jsx
//
// USO: escolha única entre opções (perfil, granularidade, ordenação).
// NECESSIDADE: o mesmo visual do Input para listas fechadas.
//
// PROPS:
//   id            (string)
//   valor         (string)
//   onChange      (function)
//   opcoes        (array) — { valor, rotulo }
//   desabilitado  (boolean)
//   erro          (boolean)
//
// EXEMPLOS DE USO:
//   <Select id="perfil" valor={perfil} onChange={setPerfil} opcoes={opcoes} />

import '../atomosCSS/Select.css'

function Select({
  id,
  valor = '',
  onChange,
  opcoes = [],
  desabilitado = false,
  erro = false,
}) {
  return (
    <select
      className={`select ${erro ? 'select--erro' : ''}`}
      id={id}
      value={valor}
      disabled={desabilitado}
      onChange={(evento) => onChange(evento.target.value)}
    >
      {opcoes.map((opcao) => (
        <option key={opcao.valor} value={opcao.valor}>
          {opcao.rotulo}
        </option>
      ))}
    </select>
  )
}

export default Select
