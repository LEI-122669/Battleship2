# Qodana — Large Class

**Aluno:** [Nome completo]  
**Número:** 87711  
**Projeto:** BattleShip2  
**Code smell:** Large Class

## 1. Mapeamento do code smell

| Code smell | Inspeção Qodana | Métrica utilizada | Limite |
|---|---|---|---|
| Large Class | `OverlyComplexClass` | Soma das complexidades ciclomáticas dos métodos e inicializadores da classe | 46 |

A inspeção `OverlyComplexClass` identifica classes cuja complexidade total ultrapassa o limite configurado. O limite 46 deve corresponder ao valor definido no perfil de inspeção utilizado.

## 2. Testes do quality gate

### Teste 1 — Falha

Foi criada uma branch de teste e introduzida uma classe cuja complexidade ultrapassa o limite configurado.

**URL da execução que falhou:** [Colocar aqui o URL do GitHub Actions]

### Teste 2 — Sucesso

A classe foi corrigida, reduzindo a sua complexidade para um valor abaixo do limite configurado.

**URL da execução que passou:** [Colocar aqui o URL do GitHub Actions]

## 3. Reflexão crítica

O Qodana detetou a classe cuja complexidade ciclomática total ultrapassava o limite definido. A deteção pode produzir falsos positivos, por exemplo, quando uma classe tem uma complexidade elevada, mas essa complexidade é justificada pela sua responsabilidade, e falsos negativos quando uma classe tem demasiadas responsabilidades, mas não ultrapassa o limite. O resultado é sensível ao valor-limite configurado, pois a alteração desse valor pode fazer com que uma classe deixe de ser sinalizada ou passe a ser sinalizada. Esta inspeção é apenas uma aproximação ao cheiro Large Class e não avalia todas as estratégias de deteção e métricas propostas no livro.
