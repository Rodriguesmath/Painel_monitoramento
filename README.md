# 💧 Painel de Monitoramento CAGEPA (PMG) v3.1

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Concluído-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## 📋 Visão Geral
Sistema corporativo de monitoramento automatizado para leitura de hidrômetros, desenvolvido com foco em **Arquitetura Limpa** e **Padrões de Projeto (GoF)**. O sistema integra monitoramento contínuo via OCR, gestão centralizada de usuários, alertas proativos e relatórios gerenciais, operando através de uma interface de linha de comando (CLI/TUI) robusta.

---

## 🏗️ Arquitetura e Padrões de Projeto (Design Patterns)

O sistema foi concebido para demonstrar a aplicação prática de diversos padrões de projeto, garantindo modularidade, extensibilidade e manutenibilidade.

| Padrão | Categoria | Aplicação no Projeto | Arquivos e Papéis |
| :--- | :--- | :--- | :--- |
| **Facade** | Estrutural | Simplifica a complexidade dos subsistemas fornecendo uma interface única. | • `src/com/cagepa/pmg/FachadaSistema.java` (Facade) |
| **Singleton** | Criacional | Garante instância única para recursos compartilhados. | • `src/com/cagepa/pmg/infra/Logger.java` (Singleton)<br>• `src/com/cagepa/pmg/infra/ConexaoDB.java` (Resource Access) |
| **Adapter** | Estrutural | Padroniza o processamento de imagens de diferentes modelos. | • `src/com/cagepa/pmg/smc/adapter/IProcessadorImagem.java` (Target)<br>• `src/com/cagepa/pmg/smc/adapter/AdaptadorAnalogicoModeloA.java` (Adapter)<br>• `src/com/cagepa/pmg/smc/adapter/AdaptadorAnalogicoModeloB.java` (Adapter) |
| **Observer** | Comportamental | Notifica alertas quando uma nova leitura é processada. | • `src/com/cagepa/pmg/smc/SMC.java` (Subject)<br>• `src/com/cagepa/pmg/san/SAN.java` (Observer) |
| **Strategy** | Comportamental | Alterna dinamicamente entre estratégias de notificação. | • `src/com/cagepa/pmg/san/INotificador.java` (Strategy Interface)<br>• `src/com/cagepa/pmg/san/NotificadorEmail.java` (Concrete Strategy)<br>• `src/com/cagepa/pmg/san/NotificadorSMS.java` (Concrete Strategy) |
| **State** | Comportamental | Gerencia o ciclo de vida do processamento de leitura. | • `src/com/cagepa/pmg/smc/state/LeituraContext.java` (Context)<br>• `src/com/cagepa/pmg/smc/state/EstadoLeitura.java` (State Interface)<br>• `src/com/cagepa/pmg/smc/state/EstadoProcessando.java` (Concrete State) |
| **Template Method** | Comportamental | Define o esqueleto da geração de relatórios. | • `src/com/cagepa/pmg/sgr/GeradorRelatorio.java` (Abstract Template)<br>• `src/com/cagepa/pmg/sgr/RelatorioPDF.java` (Concrete Class)<br>• `src/com/cagepa/pmg/sgr/RelatorioCSV.java` (Concrete Class) |

---

## 🚀 Status dos Módulos

### 👤 SGU - Sistema de Gerenciamento de Usuários
**Status:** `(Concluído)` (100%)
> Gerenciamento completo com persistência em SQLite e validação robusta.
- [x] CRUD de Usuários e Hidrômetros
- [x] Validação de Duplicidade e Fluxo de "Tente Novamente"
- [x] Persistência Relacional (SQLite)
- [x] Autenticação e Controle de Acesso (RBAC)
- [x] Lógica de Offset para resiliência a resets de hardware

### 📷 SMC - Sistema de Monitoramento e Controle
**Status:** `(Concluído)` (100%)
> Arquitetura reativa implementada. OCR funcional para múltiplos modelos.
- [x] Monitoramento de Diretórios (WatchService)
- [x] Integração com Tesseract OCR
- [x] Suporte a Múltiplos Modelos (A, B, C)
- [x] Refinamento do OCR para Modelo B (Contraste)
- [x] Implementação Real do OCR para Modelo C

### 🔔 SAN - Sistema de Alerta e Notificação
**Status:** `(Concluído)` (100%) (MOCK)
> Lógica de detecção ativa. Bufferização de alertas para UI limpa.
- [x] Detecção de Anomalias (Consumo > Limite)
- [x] Visualização Não-Intrusiva na Monitorização (Buffer)
- [x] Integração com Servidor SMTP (E-mail)
- [x] Integração com Gateway SMS

### 📊 SGR - Sistema de Geração de Relatórios
**Status:** `(Concluído)` (100%) (MOCK)
> Geração de arquivos com feedback visual na CLI.
- [x] Exportação Polimórfica (PDF/CSV)
- [x] Criação de Arquivos e Feedback ao Usuário
- [x] Geração de PDF Binário Real (iText/PDFBox)
- [x] Geração de CSV com Dados Reais

### 🖥️ Interface (CLI & TUI)
**Status:** `(Concluído)` (100%)
> Interface de terminal rica e interativa.
- [x] Menus Interativos (Lanterna)
- [x] Monitoramento em Tempo Real
- [x] Validação de Entradas e Diálogos de Confirmação

---
*Desenvolvido por Rodrigues Matheus Lima - IFPB Campus Campina Grande*
