# Painel de Monitoramento CAGEPA (PMG) v3.0

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow?style=for-the-badge)
![Version](https://img.shields.io/badge/Versão-3.0-blue?style=for-the-badge)

## 📋 Visão Geral
Sistema de monitoramento automatizado para leitura de hidrômetros, estruturado sobre padrões de projeto (GoF) e arquitetura modular. O sistema integra monitoramento contínuo, gestão de usuários, alertas automáticos e relatórios.

## 🚀 Progresso dos Módulos

O progresso abaixo reflete a implementação de funcionalidades **reais** (produção). Funcionalidades simuladas (mocks) são contabilizadas como 0%.

### 👤 SGU - Sistema de Gerenciamento de Usuários
**Progresso:** 80%
`████████░░`
> Persistência real (SQLite) e RBAC implementados. Faltam testes unitários e validações de segurança mais robustas.
- [x] Estrutura de Classes (`Usuario`, `SGU`)
- [x] Cadastro e Persistência (SQLite)
- [x] Autenticação e RBAC (Admin/Padrão)
- [ ] Testes Unitários Automatizado

### 📷 SMC - Sistema de Monitoramento e Controle
**Progresso:** 40%
`████░░░░░░`
> A arquitetura (State, Observer) e varredura de pastas são reais. **O processamento de imagem (OCR) é MOCK (simulado).**
- [x] Loop de Monitoramento Contínuo
- [x] Adapters e Varredura de Diretórios
- [x] Padrões State e Observer
- [ ] **Processamento de Imagem Real (OCR)**

### 🔔 SAN - Sistema de Alerta e Notificação
**Progresso:** 20%
`██░░░░░░░░`
> A lógica de detecção funciona. **O envio de E-mail e SMS é MOCK (apenas log).**
- [x] Regras de Negócio (Limites)
- [x] Estrutura Strategy
- [ ] **Envio Real de E-mail (SMTP)**
- [ ] **Envio Real de SMS (Gateway)**

### 📊 SGR - Sistema de Geração de Relatórios
**Progresso:** 10%
`█░░░░░░░░░`
> Estrutura Template Method definida. **A geração dos arquivos físicos (PDF/CSV) é MOCK.**
- [x] Estrutura Template Method
- [ ] **Geração de Arquivo PDF Real**
- [ ] **Geração de Arquivo CSV Real**

### 🖥️ Infraestrutura (CLI & Fachada)
**Progresso:** 80%
`████████░░`
> Interface funcional e integrada.
- [x] CLI Interativa
- [x] Fachada (Facade Pattern)
- [x] Sistema de Logs
- [ ] Tratamento de Exceções Complexas

---
*Desenvolvido como projeto final da disciplina de padrões de projeto no Campus Campina Grande do IFPB.*
