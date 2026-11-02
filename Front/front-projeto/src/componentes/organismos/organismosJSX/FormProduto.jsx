// organismos/organismosJSX/FormProduto.jsx
//
// USO: criar e editar produto (RF01, RF26).
// NECESSIDADE: o corpo segue ProdutoDTO.Create e ProdutoDTO.Update.
//
// PROPS:
//   valoresIniciais, estabelecimentoId, modo (criar|editar), ocultarCusto, onSubmit, enviando
//
// EXEMPLOS DE USO:
//   <FormProduto estabelecimentoId="1" modo="criar" onSubmit={criar} />

import { useState } from 'react'
import FormField from '../../moleculas/moleculasJSX/FormField'
import Checkbox from '../../atomos/atomosJSX/Checkbox'
import Botao from '../../atomos/atomosJSX/Botao'
import LegendaMetodo from '../../moleculas/moleculasJSX/LegendaMetodo'
import { endpoints } from '../../../services/endpoints'
import '../organismosCSS/FormProduto.css'

function vazio(valores, estabelecimentoId) {
  return {
    estabelecimentoId: String(valores?.estabelecimentoId ?? estabelecimentoId ?? ''),
    categoriaId: valores?.categoriaId != null ? String(valores.categoriaId) : '',
    nome: valores?.nome || '',
    precoAtual: valores?.precoAtual != null ? String(valores.precoAtual) : '',
    custoAtual: valores?.custoAtual != null ? String(valores.custoAtual) : '',
    sazonal: Boolean(valores?.sazonal),
    janelaIni: valores?.janelaIni || '',
    janelaFim: valores?.janelaFim || '',
    ativo: valores?.ativo !== false,
  }
}

function FormProduto({
  valoresIniciais = null,
  estabelecimentoId = '',
  modo = 'criar',
  ocultarCusto = false,
  onSubmit,
  enviando = false,
}) {
  const [form, setForm] = useState(() => vazio(valoresIniciais, estabelecimentoId))
  const [erros, setErros] = useState({})

  function alterar(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }))
  }

  function validar() {
    const proximos = {}
    if (modo === 'criar' && !form.estabelecimentoId) proximos.estabelecimentoId = 'Informe o estabelecimento.'
    if (!form.nome.trim()) proximos.nome = 'Informe o nome.'
    if (form.precoAtual === '' || Number(form.precoAtual) < 0) proximos.precoAtual = 'Preço precisa ser zero ou maior.'
    if (form.sazonal && form.janelaIni && form.janelaFim && form.janelaFim < form.janelaIni) {
      proximos.janelaFim = 'A janela final precisa ser depois da inicial.'
    }
    setErros(proximos)
    return Object.keys(proximos).length === 0
  }

  function aoEnviar(evento) {
    evento.preventDefault()
    if (!validar()) return
    const corpo = {
      categoriaId: form.categoriaId ? Number(form.categoriaId) : null,
      nome: form.nome.trim(),
      precoAtual: form.precoAtual,
      sazonal: form.sazonal,
      janelaIni: form.sazonal && form.janelaIni ? form.janelaIni : null,
      janelaFim: form.sazonal && form.janelaFim ? form.janelaFim : null,
    }
    if (!ocultarCusto) corpo.custoAtual = form.custoAtual === '' ? null : form.custoAtual
    if (modo === 'criar') corpo.estabelecimentoId = Number(form.estabelecimentoId)
    else corpo.ativo = form.ativo
    onSubmit(corpo)
  }

  const caminho = modo === 'criar' ? endpoints.produtos : endpoints.produto(':id')

  return (
    <form className="form-produto formulario" onSubmit={aoEnviar}>
      <LegendaMetodo metodo={modo === 'criar' ? 'POST' : 'PUT'} caminho={caminho} />
      {modo === 'criar' ? (
        <FormField
          id="produto-estabelecimento"
          rotulo="Estabelecimento"
          tipo="number"
          valor={form.estabelecimentoId}
          onChange={(valor) => alterar('estabelecimentoId', valor)}
          erro={erros.estabelecimentoId}
          obrigatorio
          minimo="1"
          dica="Troque o id para testar isolamento entre estabelecimentos."
        />
      ) : null}
      <div className="formulario__linha">
        <FormField id="produto-nome" rotulo="Nome" valor={form.nome} onChange={(valor) => alterar('nome', valor)} erro={erros.nome} obrigatorio />
        <FormField id="produto-categoria" rotulo="Categoria" tipo="number" valor={form.categoriaId} onChange={(valor) => alterar('categoriaId', valor)} minimo="1" dica="Opcional." />
      </div>
      <div className="formulario__linha">
        <FormField id="produto-preco" rotulo="Preço de venda" tipo="number" passo="0.01" minimo="0" valor={form.precoAtual} onChange={(valor) => alterar('precoAtual', valor)} erro={erros.precoAtual} obrigatorio />
        {ocultarCusto ? null : (
          <FormField id="produto-custo" rotulo="Custo de produção" tipo="number" passo="0.01" minimo="0" valor={form.custoAtual} onChange={(valor) => alterar('custoAtual', valor)} dica="Vazio marca dado incompleto na matriz." />
        )}
      </div>
      <Checkbox id="produto-sazonal" texto="Produto sazonal" marcado={form.sazonal} onChange={(valor) => alterar('sazonal', valor)} />
      {form.sazonal ? (
        <div className="formulario__linha">
          <FormField id="produto-janela-ini" rotulo="Início da janela" tipo="date" valor={form.janelaIni} onChange={(valor) => alterar('janelaIni', valor)} />
          <FormField id="produto-janela-fim" rotulo="Fim da janela" tipo="date" valor={form.janelaFim} onChange={(valor) => alterar('janelaFim', valor)} erro={erros.janelaFim} />
        </div>
      ) : null}
      {modo === 'editar' ? (
        <Checkbox id="produto-ativo" texto="Ativo no cardápio" marcado={form.ativo} onChange={(valor) => alterar('ativo', valor)} />
      ) : null}
      <div className="formulario__acoes">
        <Botao texto={enviando ? 'Salvando...' : modo === 'criar' ? 'Cadastrar produto' : 'Salvar alterações'} tipo="submit" desabilitado={enviando} />
      </div>
    </form>
  )
}

export default FormProduto
