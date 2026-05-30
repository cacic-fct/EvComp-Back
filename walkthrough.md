# Walkthrough — Backend Sistema de Eventos da Computação (Spring Boot)

## Requisitos Técnicos
* **Java:** Criado com JDK 25 (compatível com Java 17).
* **Framework:** Spring Boot.

---

## Como Rodar o Servidor
Execute o seguinte comando na pasta raiz do projeto:
```powershell
.\gradlew bootRun
```
O servidor será inicializado em **localhost:8080** e o console de administração do banco H2 estará acessível em **http://localhost:8080/h2-console** (URL JDBC: `jdbc:h2:mem:seccompdb`, sem senha).

---

## Roteiro de Testes Automatizados via `curl`

Devido à carga de dados inicial criada por `DataInitializer.java` (pense como uma simulação do banco de dados), você pode realizar os seguintes testes via terminal:

### 1. Login e Controle de Acesso
```bash
# Login do Administrador
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\": \"admin@unesp.br\", \"senha\": \"Admin1234\"}"

# Login do Participante Comum
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\": \"joao@gmail.com\", \"senha\": \"Joao1234\"}"
```

### 2. Cadastro de Participante (Regras de Negócio e RA Condicional)
```bash
# Cadastro Inválido: Email UNESP sem informar o RA (Deve retornar erro 400)
curl -X POST http://localhost:8080/api/cadastro -H "Content-Type: application/json" -d "{\"nome\": \"Bruno Unesp\", \"email\": \"bruno@unesp.br\", \"senha\": \"Senha1234\", \"ra\": \"\"}"

# Cadastro Válido: Email comum sem RA
curl -X POST http://localhost:8080/api/cadastro -H "Content-Type: application/json" -d "{\"nome\": \"Carlos Silva\", \"email\": \"carlos@gmail.com\", \"senha\": \"Senha1234\"}"

# Cadastro Válido: Email UNESP com RA
curl -X POST http://localhost:8080/api/cadastro -H "Content-Type: application/json" -d "{\"nome\": \"Bruno Unesp\", \"email\": \"bruno@unesp.br\", \"senha\": \"Senha1234\", \"ra\": \"221234567\"}"
```

### 3. Gerenciamento de Coletores de Presença
```bash
# Promover um participante (Maria, ID: 3) como Coletor no Evento SECCOMP 2026 (ID: 1)
curl -X POST http://localhost:8080/api/eventos/1/coletores/3

# Listar coletores do evento SECCOMP 2026
curl -X GET http://localhost:8080/api/eventos/1/coletores
```

### 4. Processamento de Inscrição (Com validação de Capacidade e Conflito de Horários)
```bash
# Realizar inscrição com conflito de horário
# (O sistema rejeitará porque as duas atividades ocorrem no mesmo horário)
# Atividade 4 e Atividade 5 no banco de dados.
```

### 5. Download de Certificado em PDF Real (Com cálculo de horas de presença)
> **ATENÇÃO:** A geração de PDF ainda não está feita!
```bash
# Baixar Certificado Geral para João (ID: 2) no evento SECCOMP 2026 (ID: 1)
# O evento SECCOMP 2026 terminou em 24/05/2026, logo está elegível para emissão
curl -o "Certificado_Joao.pdf" "http://localhost:8080/api/certificados/download?participanteId=2&eventoId=1"

# Baixar Certificado de Atividade Individual (Atividade ID: 1 - Minicurso Spring Boot)
curl -o "Certificado_Atividade_Joao.pdf" "http://localhost:8080/api/certificados/download?participanteId=2&eventoId=1&atividadeId=1"
```

### 6. Geração de Relatórios em PDF Real
> **ATENÇÃO:** A geração de PDF ainda não está feita!
```bash
# Gerar PDF com a lista de todos os participantes do evento SECCOMP 2026 (ID: 1)
curl -o "Relatorio_Participantes.pdf" "http://localhost:8080/api/relatorios/gerar?eventoId=1&tipo=participantes"

# Gerar PDF com estatísticas e gráfico comparativo (Internos vs Externos)
curl -o "Grafico_Comparativo.pdf" "http://localhost:8080/api/relatorios/gerar?eventoId=1&tipo=grafico"
```

## Autores
<table>
	<tr>
		<td align="center">
			<a href="https://github.com/Gabriel-Ciriaco">
				<img src="https://avatars.githubusercontent.com/u/66225865" width="100px;" alt=""/>
				<br>
				<sub>
					<b>Gabriel C. de Carvalho</b>
				</sub>
		</td>
		<td align="center">
			<a href="https://github.com/Carol-Nunes">
				<img src="https://avatars.githubusercontent.com/u/18383333" width="100px;" alt=""/>
				<br>
				<sub>
					<b>Caroline N. Araujo</b>
				</sub>
		</td>
	</tr>
</table>