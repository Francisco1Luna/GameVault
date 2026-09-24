# Sistema GameVault

## Integrantes do grupo
* Arthur Cavalcanti Medeiros de Barros - arthur.cavalcanti@ufrpe.br
* Caio Duarte Amaral Espinola de Carvalho - caio.duarte@ufrpe.br
* Carlos Henrique Silva - carlos.hsilva4@ufrpe.br
* Francisco Luna de Moraes - francisco.luna@ufrpe.br
* Matheus José Mendonça de Lima Vasconcelos - matheus.mendonca@ufrpe.br

## Diagrama de Classes UML
![Diagrama UML](diagramaUml.png)

## Descrição

Sistema para gestão de uma locadora/loja de jogos eletrônicos, controlando catálogo de jogos, plataformas suportadas, clientes e operações de locação e venda. O sistema deve respeitar classificação indicativa dos jogos em relação à idade dos clientes e controlar limites de locações simultâneas conforme o plano do cliente.

Como diferencial, o cadastro de jogos é enriquecido automaticamente com dados reais obtidos de uma API pública de catálogo de jogos, trazendo informações como gênero, plataformas disponíveis e desenvolvedora/publicadora, evitando digitação manual e mantendo o catálogo mais realista.

## Requisitos Funcionais

### 1. Catálogo e Plataformas
- REQ01: Cadastrar plataformas (nome: PC, PlayStation, Xbox, Switch)
- REQ02: Cadastrar jogos (título, gênero, classificação indicativa, plataformas compatíveis)
- REQ03: Buscar jogos pelo título usando a API pública **FreeToGame** (endpoint `https://www.freetogame.com/api/games`) para pré-preencher gênero, plataforma e link do jogo no cadastro

### 2. Clientes
- REQ04: Cadastrar clientes (nome, data de nascimento, CPF)
- REQ05: Implementar herança para tipos de cliente: Comum e Assinante (plano mensal com benefícios)

### 3. Estoque
- REQ06: Controlar exemplares/licenças disponíveis por jogo e plataforma
- REQ07: Consultar disponibilidade de um jogo para locação em tempo real

### 4. Locações e Vendas
- REQ08: Registrar locação de jogo (cliente, jogo, data de retirada, prazo de devolução)
- REQ09: Registrar devolução, atualizando o estoque disponível
- REQ10: Registrar venda de jogo (composição: item de venda referenciando jogo e preço)
- REQ11: Aplicar desconto automático para clientes Assinantes em vendas

### 5. Financeiro
- REQ12: Calcular multa por atraso na devolução, proporcional aos dias de atraso
- REQ13: Consultar pendências financeiras de um cliente (multas não pagas)

### 6. Relatórios
- REQ14: Relatório dos jogos mais locados por período, exportável em CSV
- REQ15: Relatório de receita (locações + vendas) por mês
- REQ16: Listar jogos importados da API por gênero, para conferência do catálogo sincronizado

### 7. Regras e Restrições
- REQ17: **Bloquear** a locação de jogo cuja classificação indicativa seja incompatível com a idade do cliente calculada a partir da data de nascimento
- REQ18: **Não permitir** que um cliente Comum tenha mais de 1 locação ativa simultânea, e um Assinante mais de 3
- REQ19: **Impedir** nova locação para cliente com multa pendente não paga
- REQ20: **Validar** disponibilidade de exemplar/licença antes de confirmar qualquer locação
- REQ21: **Bloquear** a devolução de uma locação já finalizada anteriormente
- REQ22: **Garantir** que toda venda tenha ao menos um item antes de ser confirmada

## Possíveis APIs/Bibliotecas

JavaFX, FreeToGame API (https://www.freetogame.com/api/games — pública, sem chave), `java.net.http.HttpClient`, Jackson/Gson, Java Time API, JUnit.

**Requisito bônus (opcional, fora da contagem oficial):** gerar uma página HTML estática com o "ranking" dos jogos mais locados do mês (tabela + capa/thumbnail vindos da API), aberta automaticamente no navegador via `Desktop.getDesktop().browse()`.
