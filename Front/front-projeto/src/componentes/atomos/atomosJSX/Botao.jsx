// atomos/atomosJSX/Botao.jsx
//
// USO: ações de formulário, paginação e navegação secundária.
// NECESSIDADE: um único controle de clique, com variantes do tema.
//
// PROPS:
//   texto         (string) — rótulo visível
//   tipo          (string) — button, submit ou reset
//   variante      (string) — primario, secundario, perigo ou fantasma
//   onClick       (function) — disparo do clique
//   desabilitado  (boolean) — impede o clique
//   larguraTotal  (boolean) — ocupa a largura do pai
//
// EXEMPLOS DE USO:
//   <Botao texto="Salvar" variante="primario" tipo="submit" />

import '../atomosCSS/Botao.css'

function Botao({
  texto,
  tipo = 'button',
  variante = 'primario',
  onClick,
  desabilitado = false,
  larguraTotal = false,
}) {
  const classe = [
    'botao',
    `botao--${variante}`,
    desabilitado ? 'botao--desabilitado' : '',
    larguraTotal ? 'botao--largura-total' : '',
  ].filter(Boolean).join(' ')

  return (
    <button className={classe} type={tipo} onClick={onClick} disabled={desabilitado}>
      {texto}
    </button>
  )
}

export default Botao
