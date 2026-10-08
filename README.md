# Battleship2

Simulador do jogo **Batalha Naval Quinhentista**, desenvolvido no ambito da unidade curricular de
**Qualidade de Software** do **ISCTE-IUL**.

O jogo corre na consola. A nossa frota e a frota adversaria sao frotas de navios do seculo XVI
(galeao, fragata, nau, caravela e barca), e cada rajada vale tres tiros.

---

## Requisitos

| Ferramenta | Versao |
| :--- | :--- |
| JDK | **21** (e o nivel de compilacao definido no `pom.xml`) |
| Apache Maven | 3.9 ou superior |
| Git | qualquer versao recente |

> O projeto **nao** se compila com `javac Main.java`. Usa Maven: e o Maven que resolve as
> dependencias e produz o ficheiro executavel.

---

## Compilar e executar

```bash
# clonar
git clone https://github.com/LEI-122669/Battleship2.git
cd Battleship2

# correr os testes
mvn clean test

# empacotar (gera target/BattleshipGamePlayer-2.0.jar)
mvn clean package
```

O `maven-shade-plugin` produz um jar com todas as dependencias dentro, pelo que ha **dois** pontos
de entrada possiveis.

**1. Jogo na consola** (o que interessa para a Parte 1):

```bash
java -cp target/BattleshipGamePlayer-2.0.jar battleship.Main
```

**2. Servidor REST** (Parte 2) - e o `mainClass` declarado no `pom.xml`:

```bash
java -jar target/BattleshipGamePlayer-2.0.jar
```

---

## Comandos da consola

| Comando | O que faz |
| :--- | :--- |
| `gerafrota` | Gera uma frota aleatoria e comeca um jogo novo |
| `lefrota` | Le uma frota indicada pelo jogador |
| `estado` | Mostra o estado da frota (a flutuar / afundados) |
| `mapa` | Desenha o mapa da nossa frota |
| `rajada` | Le uma rajada de tres tiros e resolve-a |
| `simula` | Joga sozinho ate a frota toda ir ao fundo |
| `tiros` | Lista os tiros validos ja realizados |
| `ajuda` | Mostra a lista de comandos |
| `desisto` | Termina o programa |

Depois de `gerafrota` ou `lefrota`, escrever uma rajada na consola, por exemplo:

```
> rajada
A1 B2 C3
```

Cada rajada devolve um resumo em JSON com os campos `validShots`, `repeatedShots`,
`outsideShots`, `missedShots`, `sunkBoats` e `hitsOnBoats`. **Este JSON e o protocolo da Parte 2
e nao pode ser alterado.**

---

## A frota

Sao 11 navios, 21 celulas ocupadas, num tabuleiro 10x10 com linhas `A`-`J` e colunas `1`-`10`.

| Navio | Tamanho | Quantidade |
| :--- | :---: | :---: |
| Galeao | 5 | 1 |
| Fragata | 4 | 1 |
| Nau | 3 | 2 |
| Caravela | 2 | 3 |
| Barca | 1 | 4 |

---

## Simbolos do tabuleiro

| Simbolo | Significado |
| :---: | :--- |
| `.` | agua por descobrir |
| `#` | navio |
| `*` | tiro certeiro |
| `o` | tiro na agua |
| `-` | posicao adjacente a navio afundado |

---

## Documentacao tecnica

A documentacao Javadoc e gerada para a pasta `docs/` e publicada pelo **GitHub Pages**.

- Site: <https://LEI-122669.github.io/Battleship2/>
- Fonte da verdade: os comentarios Javadoc nas classes de `src/main/java/battleship/`
- Para regenerar: `mvn javadoc:javadoc` (o resultado fica em `target/reports/apidocs` e e depois
  copiado para `docs/`)

---

## Estado do projeto

**Implementado**

- Motor do jogo: frotas, navios, orientacoes, marcacao de tiros e deteccao de navio afundado
- Simulacao na consola com os comandos acima
- Servidor REST (Parte 2) e respetivo controlador de jogos
- Testes unitarios com JUnit 5 (`mvn clean test`)
- Documentacao Javadoc publicada em `docs/`

**Em desenvolvimento** (um ramo por funcionalidade, cada um com o respetivo *issue* e *pull request*)

- Visualizacao grafica dos tabuleiros
- Impressao das jogadas em PDF
- Armazenamento das jogadas numa base de dados
- Relogio com o tempo gasto em cada jogada

---

## Como contribuir

Cada funcionalidade vive no seu proprio ramo, com o numero de estudante no nome, e entra por
*pull request* revisto por outro membro do grupo.

```bash
git checkout main
git pull origin main
git checkout -b <numero-de-estudante>-<funcionalidade>
```

Antes de abrir o *pull request*:

1. `mvn clean test` tem de passar
2. O *issue* correspondente tem de existir e estar etiquetado
3. A descricao do *pull request* tem de referir o *issue*
