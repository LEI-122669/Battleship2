# Battleship2

Simulador do jogo **Batalha Naval Quinhentista**

O jogo corre na consola. A nossa frota e a frota adversaria sao frotas de navios do seculo XVI
(galeao, fragata, nau, caravela e barca), e cada rajada vale tres tiros.

---

## Requisitos

| Ferramenta | Versao |
| :--- | :--- |
| JDK | **21** (e o nivel de compilacao definido no `pom.xml`) |
| Apache Maven | 3.9 ou superior |
| Git | qualquer versao recente |



---

## Compilar e executar

```bash
# clonar
git clone https://github.com/LEI-122669/Battleship2.git
cd Battleship2

```



**1. Jogo na consola** (o que interessa para a Parte 1):

```bash
java -cp target/BattleshipGamePlayer-2.0.jar battleship.Main
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


