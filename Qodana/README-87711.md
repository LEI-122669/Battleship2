# Qodana — Large Class

**Aluno:** José Gil  
**Número:** 87711  
**Projeto:** BattleShip2  
**Code smell:** Large Class

## 1. Mapeamento do code smell

| Code smell | Inspeção Qodana | Métrica / critério | Limite configurado |
|---|---|---|---|
| Large Class | `ClassWithTooManyMethods` | Número de métodos da classe | 10 métodos |
| Large Class | `OverlyComplexClass` | Complexidade total da classe | 80 |

O perfil `profile-87711.yaml` utiliza uma configuração inicial vazia e ativa explicitamente estas duas inspeções. A primeira identifica classes com mais de 10 métodos e a segunda identifica classes cuja complexidade ultrapassa o limite de 80.

## 2. Testes do quality gate

### Teste 1 — Falha

Foi criada uma branch de teste e introduzida uma classe que ultrapassa um dos limites definidos no perfil de inspeção do Qodana.

**URL da execução que falhou:** 

### Teste 2 — Sucesso

A classe foi corrigida, deixando de ultrapassar os limites configurados. Após o novo commit, o Qodana voltou a executar e o quality gate passou.

**URL da execução que passou:** 

## 3. Reflexão crítica

O Qodana detetou classes que ultrapassam os limites definidos para o número de métodos e para a complexidade total. Podem existir falsos positivos, quando uma classe ultrapassa um limite mas tem responsabilidades justificadas, e falsos negativos, quando uma classe tem demasiadas responsabilidades sem ultrapassar os limites configurados. Os resultados são sensíveis aos valores escolhidos, uma vez que alterar os limites de 10 métodos ou 80 de complexidade pode alterar as classes sinalizadas. Estas inspeções são aproximações ao cheiro Large Class e não avaliam todas as estratégias e métricas propostas no livro, nem substituem uma análise da responsabilidade e coesão das classes.
