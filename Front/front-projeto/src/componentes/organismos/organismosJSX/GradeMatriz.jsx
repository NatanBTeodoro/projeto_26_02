// organismos/organismosJSX/GradeMatriz.jsx
//
// USO: quadrantes da matriz de Menu Engineering.
// NECESSIDADE: a classificação vem pronta da API; a tela só agrupa por classe.
//
// PROPS:
//   itens (array) — ClassificacaoResponse
//
// EXEMPLOS DE USO:
//   <GradeMatriz itens={classificacoes} />

import { Link } from 'react-router-dom'
import Badge from '../../atomos/atomosJSX/Badge'
import { formatarMoeda, formatarNumero, rotuloClasse } from '../../../utils/formatacao'
import '../organismosCSS/GradeMatriz.css'

const QUADRANTES = [
  { classe: 'ENIGMA', variante: 'enigma', eixo: 'Margem alta · pouca saída' },
  { classe: 'ESTRELA', variante: 'estrela', eixo: 'Margem alta · muita saída' },
  { classe: 'CAO', variante: 'cao', eixo: 'Margem baixa · pouca saída' },
  { classe: 'VACA_LEITEIRA', variante: 'vaca', eixo: 'Margem baixa · muita saída' },
]

function GradeMatriz({ itens = [] }) {
  const fora = itens.filter((item) => item.classe === 'EM_OBSERVACAO' || item.classe === 'DADO_INCOMPLETO')

  return (
    <div className="grade-matriz">
      <div className="grade-matriz__eixos">
        {QUADRANTES.map((quadrante) => {
          const grupo = itens.filter((item) => item.classe === quadrante.classe)
          return (
            <section key={quadrante.classe} className={`grade-matriz__quadrante grade-matriz__quadrante--${quadrante.variante}`}>
              <header>
                <Badge texto={rotuloClasse(quadrante.classe)} variante={quadrante.variante} />
                <p>{quadrante.eixo}</p>
              </header>
              <ul>
                {grupo.length === 0 ? <li className="grade-matriz__vazio">Nenhum item nesta página.</li> : grupo.map((item) => (
                  <li key={item.id}>
                    <Link to={`/produtos/${item.produtoId}`}>Produto {item.produtoId}</Link>
                    <span>pop. {formatarNumero(item.popularidade)}% · margem {formatarMoeda(item.margem)}</span>
                  </li>
                ))}
              </ul>
            </section>
          )
        })}
      </div>
      {fora.length ? (
        <ul className="grade-matriz__fora">
          {fora.map((item) => (
            <li key={item.id}>
              <Badge texto={rotuloClasse(item.classe)} variante={item.classe === 'DADO_INCOMPLETO' ? 'incompleto' : 'observacao'} />
              <Link to={`/produtos/${item.produtoId}`}>Produto {item.produtoId}</Link>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  )
}

export default GradeMatriz
