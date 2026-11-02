# Padrão de front-end

Documento de referência deste projeto (`front-projeto`, pizzaria Fogo & Lenha). Use-o como molde ao criar outro front com a mesma organização: React com Vite, componentes em camadas, CSS puro com variáveis de tema, rotas por perfil e uma única camada de acesso à API.

O domínio (pizzas, pedidos, admin) é só o exemplo. Em outro projeto, troque nomes de páginas, rotas e endpoints, e mantenha a estrutura, as convenções e o fluxo descritos aqui.

## Stack

| Peça | Escolha |
| --- | --- |
| Bundler | Vite |
| UI | React 19 (JSX, sem TypeScript) |
| Rotas | React Router DOM 7 (`BrowserRouter`) |
| Estilo | CSS puro, um arquivo por componente |
| HTTP | `fetch` nativo, centralizado em `src/services/api.js` |
| Estado | `useState` / `useEffect` no componente que precisa do dado |
| Sessão | `localStorage` (e `sessionStorage` só para flags pontuais) |
| Módulos | `"type": "module"` no `package.json` |
| Variáveis de ambiente | prefixo `VITE_` (ex.: `VITE_API_URL`) |

Scripts:

```bash
npm run dev       # desenvolvimento
npm run build     # gera dist/
npm run preview   # serve o build localmente
```

Não há biblioteca de componentes, CSS-in-JS, gerenciador de estado global nem cliente HTTP extra. O visual e o comportamento vêm dos componentes próprios.

## Estrutura de pastas

```text
/
├── index.html                 # ponto HTML; monta #root e carrega /src/index.jsx
├── vite.config.js
├── package.json
├── render.yaml                # deploy estático (opcional)
├── public/                    # arquivos servidos na raiz (favicon, manifest)
└── src/
    ├── index.jsx              # createRoot + StrictMode
    ├── index.css              # reset leve, fonte, fundo e imports globais
    ├── App.jsx                # única definição de rotas
    ├── images/                # imagens importadas pelos componentes
    ├── styles/                # tema e classes globais reutilizáveis
    │   ├── tema.css
    │   ├── imagem-cardapio.css
    │   └── aviso-claro.css
    ├── services/
    │   └── api.js             # URL base e helpers de fetch
    ├── utils/                 # funções puras (máscara, moeda, sessão, arquivos)
    ├── rotas/
    │   └── RotasProtegidas.jsx
    └── componentes/
        ├── atomos/
        │   ├── atomosJSX/
        │   └── atomosCSS/
        ├── moleculas/
        │   ├── moleculasJSX/
        │   └── moleculasCSS/
        ├── organismos/
        │   ├── organismosJSX/
        │   └── organismosCSS/
        ├── templates/
        │   ├── templatesJSX/
        │   └── templatesCSS/
        └── paginas/
            ├── paginasJSX/
            └── paginasCSS/
```

Cada camada tem duas pastas irmãs: uma só com `.jsx` e outra só com `.css`. O CSS é importado no topo do JSX correspondente. Não misture estilos de outro componente no mesmo arquivo.

## Atomic Design

A interface sobe em camadas. Uma camada só importa camadas abaixo dela (e `utils` / `services` quando fizer sentido). Página não vira átomo, e átomo não chama a API.

```text
Página
  └── Template (layout: navbar + main + footer)
        └── Organismo (bloco com regra de negócio de tela)
              └── Molécula (grupo pequeno de átomos)
                    └── Átomo (peça visual indivisível)
```

| Camada | O que é | O que não é | Exemplos neste projeto |
| --- | --- | --- | --- |
| Átomo | Uma tag com estilo e props. Sem composição de outros componentes de UI. | Formulário, lista, chamada HTTP | `Botao`, `Input`, `Label`, `Titulo`, `Texto`, `Link`, `Preco`, `Badge`, `Select`, `Logo`, `MensagemErro`, `MensagemSucesso` |
| Molécula | Dois ou mais átomos com um propósito único | Página inteira, layout | `FormField` (label + input + erro), `FormFieldSenha`, `FormFieldSelect`, `NavItem`, `CardPizza`, `CardBebida`, `ItemPedido` |
| Organismo | Seção autônoma da tela. Pode ter estado local e validação | Definição de rota | `Navbar`, `Footer`, `FormLogin`, `FormCadastro`, `ListaPizzas`, `ResumoPedido`, `FormularioEndereco` |
| Template | Esqueleto da página: navegação, área de conteúdo (`children`) e rodapé | Dados de negócio, fetch | `TemplatePublico`, `TemplateAutenticado`, `TemplateAdmin`, `TemplateLoginAdmin` |
| Página | Rota. Escolhe o template, busca dados, trata erro da API e passa callbacks | Estilo de botão ou input | `Home`, `Login`, `Pedido`, `Perfil`, `AdminCardapio` |

Regra prática: se o bloco aparece em mais de uma página com o mesmo comportamento, extraia para organismo ou molécula. Se é só o enquadramento (menu + conteúdo + rodapé), é template.

### Perfis de layout

Há três perfis de navegação, decididos pelo template:

| Perfil | Template | Quem vê | Itens de menu (neste projeto) |
| --- | --- | --- | --- |
| `visitante` | `TemplatePublico` | Sem login | âncoras do cardápio, Cadastro, Entrar |
| `cliente` | `TemplateAutenticado` | Cliente logado | Fazer Pedido, Meus Pedidos, Perfil, Sair |
| `admin` | `TemplateAdmin` | Administrador | Cardápio, Clientes, Pedidos, Sair |
| login admin | `TemplateLoginAdmin` | Tela de acesso do painel | só logo e selo, sem menu completo |

O `Navbar` recebe `perfil`, `rotaAtual` e `onSair`. O template só repassa essas props. A página é quem implementa o logout.

## Convenções de código

### Nomes

- Componentes, arquivos JSX e pastas de componente: PascalCase em português (`FormFieldSenha.jsx`, `MeusPedidos.jsx`).
- Funções, variáveis e props: camelCase em português (`rotaAtual`, `larguraTotal`, `onSair`, `desabilitado`).
- Evite inglês nas props de UI (`children` é a exceção do React).
- Um componente por arquivo. `export default` no final. Sem `index.js` de barril: o import aponta para o arquivo.

Import típico, sempre pelo caminho relativo até o arquivo:

```jsx
import Botao from '../../atomos/atomosJSX/Botao';
import FormLogin from '../../organismos/organismosJSX/FormLogin';
import { apiJson } from '../../../services/api';
import '../paginasCSS/Login.css';
```

### Cabeçalho do componente

Átomos, moléculas, organismos e templates começam com um comentário fixo. Páginas podem ser mais curtas (caminho do arquivo). Mantenha o bloco ao criar peça nova:

```jsx
// atomos/atomosJSX/Botao.jsx
//
// USO: onde o componente entra.
// NECESSIDADE: por que ele existe em vez de repetir markup.
//
// PROPS:
//   texto    (string) — o que mostra
//   onClick  (function) — o que dispara
//
// EXEMPLOS DE USO:
//   <Botao texto="Salvar" variante="primario" onClick={salvar} />
```

### Props e estado

- Componentes visuais são controlados: o pai guarda `valor` e recebe `onChange`.
- Valores padrão ficam na desestruturação (`variante = "primario"`, `erro = false`).
- Callbacks de ação usam o prefixo `on` (`onSubmit`, `onSair`, `onIrCadastro`).
- Validação de formulário vive no organismo (ou na página, se o formulário for único). A página trata a resposta da API e devolve a mensagem por prop (`erroExterno`).
- Listas usam `key` estável (`item.href`, `id`).

### Classes CSS (BEM simplificado)

- Bloco: nome do componente em kebab-case (`.botao`, `.form-login`, `.template-publico`).
- Elemento: `__` (`.navbar__menu`, `.form-login__campos`).
- Modificador: `--` (`.botao--primario`, `.botao--desabilitado`, `.nav-item--ativo`).
- O JSX monta a classe com template string. Modificador só entra quando a prop é verdadeira.

```jsx
className={`botao botao--${variante} ${larguraTotal ? "botao--largura-total" : ""}`}
```

- Cor, raio, sombra, transição e largura máxima vêm de variáveis. Não repita hex no componente se a cor já existe no tema.
- Hover e estado desabilitado ficam no CSS do próprio componente (`.botao:hover:not(.botao--desabilitado)`).

## Tema

`src/styles/tema.css` declara tudo em `:root`. `src/index.css` importa o tema, a fonte e os CSS globais, e define `box-sizing`, `body`, links e herança de fonte em `button`, `input`, `select` e `textarea`.

Grupos de variáveis que todo projeto neste padrão deve ter:

| Grupo | Exemplos | Uso |
| --- | --- | --- |
| Paleta bruta | `--cor-creme`, `--cor-oliva`, `--cor-terracota` | cores nomeadas da marca |
| Papéis | `--cor-marca`, `--cor-texto`, `--cor-fundo`, `--cor-erro`, `--cor-sucesso` | o que o componente consome |
| Texto | `--cor-texto-principal`, `--cor-texto-secundario` | hierarquia de leitura |
| Forma | `--raio`, `--raio-pequeno`, `--sombra-leve`, `--sombra-media`, `--transicao` | cartões, botões, foco |
| Layout | `--largura-conteudo`, `--padding-pagina` | coluna central das páginas |

Para outro projeto, troque os hex da paleta e mantenha os nomes de papel (`--cor-marca`, `--cor-erro`, …). Os componentes continuam iguais.

Classes globais (fora do BEM de um componente) ficam em `src/styles/` e são importadas uma vez em `index.css`. Use isso só para padrão visual repetido em várias telas, como proporção de imagem ou caixa de aviso.

## Rotas

Toda rota mora em `src/App.jsx`. Página não registra rota dentro de si.

```jsx
<BrowserRouter>
  <Routes>
    <Route path="/login" element={<Login />} />
    <Route path="/perfil" element={<RotaCliente><Perfil /></RotaCliente>} />
    <Route path="/admin" element={<Navigate to="/admin/login" replace />} />
    <Route path="/admin/cardapio" element={<RotaAdmin><AdminCardapio /></RotaAdmin>} />
  </Routes>
</BrowserRouter>
```

Convenções:

- Rotas públicas no topo, área logada em seguida, prefixo `/admin` para o painel.
- Rota índice de uma área (`/admin`) redireciona com `<Navigate replace />`.
- Guardas ficam em `src/rotas/RotasProtegidas.jsx` e só decidem “deixa passar” ou “manda para o login”. Não buscam dados.
- `RotaCliente` exige `localStorage.clienteId`. Sem isso, vai para `/login`.
- `RotaAdmin` exige `localStorage.adminLogado === 'true'`. Sem isso, vai para `/admin/login`.
- Depois do login bem-sucedido, a página grava a sessão e navega (`window.location.href` ou `useNavigate`).

Sessão deste projeto:

| Chave | Quem grava | Significado |
| --- | --- | --- |
| `clienteId` | login do cliente | id usado nas rotas protegidas e nos pedidos |
| `clienteNome` | login do cliente | nome exibido |
| `adminLogado` | login admin (`'true'`) | flag do painel |

`src/utils/sessaoCliente.js` concentra limpar essas chaves e o caso em que o usuário deve voltar como visitante. Logout remove as chaves do perfil e manda para a tela de login correspondente.

## API

Um único módulo, `src/services/api.js`:

- `API_BASE` lê `import.meta.env.VITE_API_URL` e cai em `http://localhost:8080` no desenvolvimento.
- `apiFetch(caminho, opcoes)` monta a URL, envia JSON e devolve o `Response`.
- `apiJson(caminho, opcoes)` faz o mesmo, lança `Error` com `mensagem` do corpo quando o status não é ok, devolve `null` em 204 e o JSON nos demais.

A página (ou o organismo, se o bloco for reutilizado) chama esses helpers. Átomo e molécula não conhecem a URL do back-end.

Padrão de tela que fala com a API:

```jsx
const [erroApi, setErroApi] = useState('');

async function handleLogin(dados) {
  setErroApi('');
  try {
    const response = await apiFetch('/clientes/login', {
      method: 'POST',
      body: JSON.stringify(dados),
    });
    if (response.ok) {
      const cliente = await response.json();
      localStorage.setItem('clienteId', cliente.id);
      window.location.href = '/pedido';
      return;
    }
    const body = await response.json().catch(() => ({}));
    setErroApi(body.mensagem || 'Usuário ou senha inválidos.');
  } catch {
    setErroApi('Erro de conexão. Tente novamente.');
  }
}
```

Listagens usam `useEffect` com função `async` interna e `apiJson`. O estado da lista começa em `[]`.

O back-end esperado devolve `{ mensagem }` em erro. Mantenha esse contrato ou adapte só `apiJson`, não cada tela.

## Utilitários

Funções sem JSX ficam em `src/utils/`. Elas não importam componentes.

| Arquivo | Responsabilidade |
| --- | --- |
| `formatacao.js` | máscara de CPF, validação simples, moeda `pt-BR` (input e exibição) |
| `imagens.js` | mapa de arquivos locais e fallback quando a API manda slug, URL ou data URL |
| `imagemArquivo.js` | leitura de arquivo do usuário, redimensionamento e data URL |
| `sessaoCliente.js` | limpar sessão e forçar modo visitante |

Máscaras de campo entram na molécula (`FormField` com prop `mascara`), não espalhadas em cada página.

## Anatomia de uma página

1. Importa um template.
2. Importa organismos (e átomos soltos, se a página tiver título próprio).
3. Importa o CSS da página (`paginasCSS/Nome.css`).
4. Guarda estado de dados, erro e loading local.
5. No `return`, o template recebe `rotaAtual` (e `onSair` se houver logout) e o miolo vai em `children`.

```jsx
function Login() {
  const [erroApi, setErroApi] = useState('');

  async function handleLogin(dados) {
    // chama apiFetch, grava sessão ou setErroApi
  }

  return (
    <TemplatePublico rotaAtual="/login">
      <div className="login">
        <FormLogin
          onSubmit={handleLogin}
          onIrCadastro={() => { window.location.href = '/cadastro'; }}
          erroExterno={erroApi}
        />
      </div>
    </TemplatePublico>
  );
}

export default Login;
```

O CSS da página estiliza só o invólucro (`.login`, grade, espaçamento). Botão, input e card já têm o próprio CSS.

Templates usam coluna flex de altura mínima da viewport: cabeçalho, `main` com `flex: 1` e `max-width: var(--largura-conteudo)`, rodapé no fim.

## Como criar outro projeto com este padrão

1. Criar o app com Vite + React e instalar `react-router-dom`.
2. Copiar a árvore `src/componentes` (as cinco camadas, cada uma com pasta JSX e pasta CSS), `src/styles`, `src/services`, `src/utils` e `src/rotas`. Pode começar só com os átomos genéricos (`Botao`, `Input`, `Label`, `Titulo`, `Texto`, `Link`, `MensagemErro`, `MensagemSucesso`) e um template.
3. Colocar o tema em `src/styles/tema.css` e importá-lo em `src/index.css`. Ajustar a paleta; manter os nomes de papel.
4. Apontar `index.html` para `/src/index.jsx` e montar `<App />` em `#root`.
5. Declarar as rotas só em `src/App.jsx`. Proteger com componentes no estilo de `RotaCliente` / `RotaAdmin`.
6. Configurar `VITE_API_URL`. Não espalhar a URL base pelas páginas.
7. Para cada tela nova:
   - criar `paginasJSX/Nome.jsx` e `paginasCSS/Nome.css`;
   - escolher o template pelo perfil;
   - se o bloco tiver estado ou validação e puder ser reusado, criar organismo;
   - se for só label + campo + erro, criar ou reusar molécula;
   - se for um controle visual novo, criar átomo com o comentário de USO / PROPS.
8. Nomear classes em BEM e consumir variáveis do tema.
9. Tratar erro de API na página e exibir com `MensagemErro`.

### Checklist de uma peça nova

- Arquivo JSX na pasta da camada e CSS irmão com o mesmo nome.
- Comentário de USO, NECESSIDADE, PROPS e exemplo.
- Props em português, com padrão na assinatura.
- Classes BEM; modificador via prop.
- Nenhuma cor solta se ela já existe no tema.
- Nenhuma chamada HTTP em átomo ou molécula.
- Rota registrada em `App.jsx`, com guarda se a tela exige login.
- `rotaAtual` igual ao `path` da rota, para o item do menu ficar ativo.

## O que adaptar e o que preservar

Preserve em qualquer projeto novo:

- separação átomo → molécula → organismo → template → página;
- pastas `*JSX` e `*CSS` lado a lado;
- um CSS por componente e BEM;
- tema só por variáveis em `:root`;
- rotas e guardas fora das páginas;
- `apiFetch` / `apiJson` como única porta do back-end;
- formulário controlado e mensagem de erro vinda de prop.

Adapte ao domínio novo:

- paleta e fonte em `tema.css` e `index.css`;
- lista de itens do `Navbar` e os templates de perfil;
- chaves de `localStorage` e as guardas;
- páginas, organismos de negócio e endpoints;
- utilitários de máscara, moeda ou arquivo, se o domínio não precisar deles.
