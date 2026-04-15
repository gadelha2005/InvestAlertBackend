# InvestAlert

Um sistema completo de monitoramento de investimentos com alertas inteligentes, desenvolvido com tecnologias modernas para fornecimento de análises em tempo real sobre ativos no mercado financeiro.

## 🎯 Objetivo

InvestAlert é uma plataforma web que permite investidores monitorar seus ativos financeiros, receber alertas sobre mudanças significativas no mercado, gerenciar sua carteira de investimentos e acompanhar suas metas financeiras através de um dashboard intuitivo e responsivo.

## 📋 Funcionalidades

- **Dashboard Interativo**: Visualização em tempo real do desempenho da carteira
- **Gerenciamento de Alertas**: Crie alertas personalizados para monitorar seus ativos favoritos
- **Scanner de Mercado**: Identifica oportunidades de investimento com critérios definidos
- **Gestão de Carteira**: Acompanhe seus ativos e posições de investimento
- **Metas Financeiras**: Defina e acompanhe suas metas de investimento
- **Notificações**: Receba notificações em tempo real sobre eventos importantes
- **Autenticação Segura**: Sistema de login com JWT
- **Gestão de Ativos**: Consulte informações detalhadas sobre ativos financeiros

## 🏗️ Arquitetura

### Stack Tecnológico

#### Frontend

- **React 19** - Biblioteca UI
- **TypeScript** - Tipagem estática
- **Vite** - Build tool moderno
- **Tailwind CSS** - Estilização
- **React Router** - Roteamento de páginas
- **React Hook Form** - Gerenciamento de formulários
- **Zod** - Validação de schemas
- **Zustand** - Gerenciamento de estado
- **Axios** - Cliente HTTP
- **React Query** - Gerenciamento de cache de dados
- **Recharts** - Gráficos e visualizações

#### Backend

- **Spring Boot 3.4.3** - Framework Java
- **Java 17** - Linguagem de programação
- **Spring Data JPA** - Acesso a dados
- **Spring Security** - Autenticação e autorização
- **JWT (JJWT)** - Tokens seguros
- **MySQL** - Banco de dados relacional
- **Flyway** - Migrações de banco de dados
- **MapStruct** - Mapeamento de DTOs
- **Docker** - Containerização

## 📁 Estrutura do Projeto

```
InvestAlert/
├── InvestAlertFrontend/          # Aplicação React
│   ├── src/
│   │   ├── api/                  # Chamadas HTTP para o backend
│   │   ├── components/           # Componentes reutilizáveis
│   │   ├── pages/                # Páginas da aplicação
│   │   ├── routes/               # Configuração de rotas
│   │   ├── store/                # Estado global (Zustand)
│   │   ├── types/                # Tipos TypeScript
│   │   ├── hooks/                # Custom React hooks
│   │   └── utils/                # Funções utilitárias
│   └── package.json
│
└── InvestAlertBackend/           # Aplicação Spring Boot
    └── investalert/
        ├── src/
        │   ├── main/java/com/investalert/
        │   │   ├── controller/    # Endpoints REST
        │   │   ├── service/       # Lógica de negócio
        │   │   ├── repository/    # Acesso a dados
        │   │   ├── model/         # Entidades JPA
        │   │   ├── dto/           # DTOs (Data Transfer Objects)
        │   │   ├── security/      # Configuração de segurança
        │   │   └── config/        # Configurações gerais
        │   └── main/resources/
        │       └── db/migration/  # Scripts SQL (Flyway)
        ├── dockerfile            # Containerização
        ├── docker-compose.yml    # Orquestração de containers
        └── pom.xml
```

## 🚀 Como Executar

### Pré-requisitos

- **Node.js** 18+ (para frontend)
- **Java 17+** (para backend)
- **Docker** e **Docker Compose** (opcional, para ambiente completo)

### Frontend

1. Navegue até a pasta do frontend:

   ```bash
   cd InvestAlertFrontend
   ```

2. Instale as dependências:

   ```bash
   npm install
   ```

3. Execute em modo desenvolvimento:

   ```bash
   npm run dev
   ```

   A aplicação estará disponível em `http://localhost:5173`

4. Para build de produção:

   ```bash
   npm run build
   ```

5. Para visualizar o build:

   ```bash
   npm run preview
   ```

6. Para executar linter:
   ```bash
   npm run lint
   ```

### Backend

1. Navegue até a pasta do backend:

   ```bash
   cd InvestAlertBackend/investalert
   ```

2. Compile e execute a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   Ou no Windows:

   ```bash
   mvnw.cmd spring-boot:run
   ```

3. Para apenas compilar:

   ```bash
   ./mvnw clean package
   ```

4. A aplicação estará disponível em `http://localhost:8080`

### Com Docker Compose (Completo)

Execute a aplicação inteira com um único comando. Certifique-se de estar na pasta `InvestAlertBackend/investalert`:

```bash
docker-compose up
```

Este comando inicia:

- Backend Spring Boot na porta 8080
- Banco de dados MySQL
- Rede Docker para comunicação entre containers

Para parar os containers:

```bash
docker-compose down
```

## 🔌 Endpoints da API

### Autenticação

- `POST /api/auth/login` - Login de usuário
- `POST /api/auth/register` - Registro de novo usuário
- `POST /api/auth/refresh` - Refresh token JWT

### Dashboard

- `GET /api/dashboard/summary` - Resumo do dashboard

### Alertas

- `GET /api/alertas` - Listar alertas
- `POST /api/alertas` - Criar novo alerta
- `PUT /api/alertas/{id}` - Atualizar alerta
- `DELETE /api/alertas/{id}` - Deletar alerta

### Ativos

- `GET /api/ativos` - Listar ativos
- `GET /api/ativos/{id}` - Detalhes de um ativo
- `POST /api/ativos` - Criar ativo

### Carteira

- `GET /api/carteira` - Visualizar carteira do usuário
- `POST /api/carteira/adicionar` - Adicionar ativo à carteira
- `DELETE /api/carteira/{id}` - Remover ativo da carteira

### Metas

- `GET /api/metas` - Listar metas
- `POST /api/metas` - Criar meta
- `PUT /api/metas/{id}` - Atualizar meta
- `DELETE /api/metas/{id}` - Deletar meta

### Scanner

- `GET /api/scanner` - Executar scanner de mercado

### Notificações

- `GET /api/notificacoes` - Listar notificações
- `PUT /api/notificacoes/{id}/lida` - Marcar notificação como lida

## 🔐 Autenticação

A aplicação utiliza **JWT (JSON Web Tokens)** para autenticação:

1. O usuário faz login com credenciais
2. Backend retorna um token JWT
3. Frontend armazena o token localmente
4. Token é incluído em todas as requisições no header `Authorization: Bearer {token}`
5. Backend valida o token antes de processar requisições

## 📝 Variáveis de Ambiente

O backend requer a configuração de variáveis de ambiente para funcionar corretamente.

## 🧪 Testes

### Frontend

```bash
# Adicionar testes conforme necessário
```

### Backend

```bash
# Executar testes
./mvnw test
```

## 📚 Documentação Adicional

Consulte os arquivos `HELP.md` no backend para mais informações sobre Spring Boot.

## 🤝 Contribuição

Para contribuir com o projeto:

1. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
2. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
3. Push para a branch (`git push origin feature/AmazingFeature`)
4. Abra um Pull Request

## 📄 Licença

Este projeto é privado.

## 👨‍💻 Desenvolvedor

Pedro Gadelha Paula

---

