# 💧 Painel de Monitoramento CAGEPA (PMG) v3.1

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Beta-yellow?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## 📋 Visão Geral
Sistema corporativo de monitoramento automatizado para leitura de hidrômetros, desenvolvido com foco em **Arquitetura Limpa** e **Padrões de Projeto (GoF)**. O sistema integra monitoramento contínuo via OCR, gestão centralizada de usuários, alertas proativos e relatórios gerenciais, operando através de uma interface de linha de comando (CLI/TUI) robusta.

---

## 🏗️ Arquitetura e Padrões de Projeto (Design Patterns)

O sistema foi concebido para demonstrar a aplicação prática de diversos padrões de projeto, garantindo modularidade, extensibilidade e manutenibilidade.

| Padrão | Categoria | Aplicação no Projeto | Localização no Código |
| :--- | :--- | :--- | :--- |
| **Facade** | Estrutural | Simplifica a complexidade dos subsistemas (SGU, SMC, SAN, SGR) fornecendo uma interface única para o cliente (CLI). | [`FachadaSistema.java`](src/com/cagepa/pmg/FachadaSistema.java) |
| **Singleton** | Criacional | Garante instância única para o gerenciador de logs e conexão com banco de dados. | [`Logger.java`](src/com/cagepa/pmg/infra/Logger.java), [`ConexaoDB.java`](src/com/cagepa/pmg/infra/ConexaoDB.java) |
| **Adapter** | Estrutural | Permite que o sistema processe imagens de diferentes modelos de hidrômetros (A, B, C) através de uma interface comum. | [`IProcessadorImagem.java`](src/com/cagepa/pmg/smc/adapter/IProcessadorImagem.java) |
| **Observer** | Comportamental | Notifica automaticamente o subsistema de alertas (SAN) quando uma nova leitura é processada pelo monitoramento (SMC). | [`SMC.java`](src/com/cagepa/pmg/smc/SMC.java) (Subject) → [`SAN.java`](src/com/cagepa/pmg/san/SAN.java) (Observer) |
| **Strategy** | Comportamental | Permite alternar dinamicamente entre estratégias de notificação (E-mail ou SMS) sem alterar o cliente. | [`INotificador.java`](src/com/cagepa/pmg/san/INotificador.java) |
| **State** | Comportamental | Gerencia o ciclo de vida do processamento de leitura (Processando → Concluído/Erro) de forma organizada. | [`LeituraContext.java`](src/com/cagepa/pmg/smc/state/LeituraContext.java) |
| **Template Method** | Comportamental | Define o esqueleto do algoritmo de geração de relatórios, delegando a formatação específica (PDF/CSV) para as subclasses. | [`GeradorRelatorio.java`](src/com/cagepa/pmg/sgr/GeradorRelatorio.java) |

---

## 🚀 Status dos Módulos

### 👤 SGU - Sistema de Gerenciamento de Usuários
**Status:** `PRODUÇÃO` (100%)
> Gerenciamento completo com persistência em SQLite.
- [x] CRUD de Usuários e Hidrômetros
- [x] Persistência Relacional (SQLite)
- [x] Autenticação e Controle de Acesso (RBAC)
- [x] Lógica de Offset para resiliência a resets de hardware

### 📷 SMC - Sistema de Monitoramento e Controle
**Status:** `PARCIAL` (70%)
> Arquitetura reativa implementada. OCR funcional para Modelo A.
- [x] Monitoramento de Diretórios (WatchService)
- [x] Integração com Tesseract OCR
- [x] Suporte a Múltiplos Modelos (A, B, C)
- [ ] Refinamento do OCR para Modelo B (Contraste)
- [ ] Implementação Real do OCR para Modelo C

### 🔔 SAN - Sistema de Alerta e Notificação
**Status:** `MOCK` (Funcional)
> Lógica de detecção ativa. Envio simulado com feedback visual.
- [x] Detecção de Anomalias (Consumo > Limite)
- [x] Feedback Visual no Console (Mock)
- [ ] Integração com Servidor SMTP (E-mail)
- [ ] Integração com Gateway SMS

### 📊 SGR - Sistema de Geração de Relatórios
**Status:** `MOCK` (Funcional)
> Estrutura pronta. Geração de arquivos fictícios para validação de fluxo.
- [x] Exportação Polimórfica (PDF/CSV)
- [x] Criação de Arquivos Físicos (Mock Content)
- [ ] Geração de PDF Binário Real (iText/PDFBox)
- [ ] Geração de CSV com Dados Reais

### 🖥️ Interface (CLI & TUI)
**Status:** `PRODUÇÃO` (95%)
> Interface de terminal rica e interativa.
- [x] Menus Interativos (Lanterna)
- [x] Monitoramento em Tempo Real
- [x] Validação de Entradas e Diálogos de Confirmação

---
*Desenvolvido por Rodrigues Matheus Lima - IFPB Campus Campina Grande*
