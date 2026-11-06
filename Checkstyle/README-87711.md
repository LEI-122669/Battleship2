# Checkstyle — PackageName

**Aluno:** José Gil
**Número:** 87711  
**Projeto:** BattleShip2

## 1. Convenção escolhida

Foi escolhida a convenção de nomenclatura de packages Java, utilizando a inspeção `PackageName` do Checkstyle.

A expressão regular utilizada foi:

`^[a-z]+(\.[a-z][a-z0-9]*)*$`

Esta expressão exige que cada componente do package comece por uma letra minúscula. Os restantes caracteres podem ser letras minúsculas ou números. Os diferentes componentes são separados por pontos.

### Exemplos

| Package | Resultado |
|---|---|
| `battleship` | Válido |
| `battleship.model` | Válido |
| `battleship.game.logic` | Válido |
| `BattleShip` | Inválido |
| `battleship.Game` | Inválido |
| `battle_ship` | Inválido |

## 2. Testes do quality gate

### Teste 1 — Falha

Foi introduzido o package `BattleShip`, que não respeita a convenção definida. O Checkstyle identificou a violação e o quality gate falhou.

**URL da execução que falhou:** [Colocar URL da execução]

### Teste 2 — Sucesso

O package foi corrigido para `battleship`, passando a respeitar a convenção definida. Após a correção, o workflow foi executado novamente e o quality gate passou.

**URL da execução que passou:** [Colocar URL da execução]

## 3. Limitações do Checkstyle

O Checkstyle consegue verificar se os nomes dos packages respeitam a expressão regular definida, mas não consegue determinar se o nome escolhido é significativo ou representa corretamente a funcionalidade do package. Também não consegue avaliar se a organização dos packages é adequada à arquitetura do projeto. Assim, um nome sintaticamente correto não garante uma boa organização do código.
