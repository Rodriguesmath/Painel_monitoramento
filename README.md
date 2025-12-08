# Painel de Monitoramento CAGEPA (PMG) v3.0

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow?style=for-the-badge)
![Version](https://img.shields.io/badge/Versão-3.0-blue?style=for-the-badge)

## 📋 Visão Geral
Sistema de monitoramento automatizado para leitura de hidrômetros, estruturado sobre padrões de projeto (GoF) e arquitetura modular. O sistema integra monitoramento contínuo, gestão de usuários, alertas automáticos e relatórios.

## 🚀 Progresso dos Módulos

O progresso abaixo reflete a implementação de funcionalidades **reais** (produção). Funcionalidades simuladas (mocks) são contabilizadas como 0%.

### 👤 SGU - Sistema de Gerenciamento de Usuários
**Progresso:** 10%
`█░░░░░░░░░`
> O sistema possui a estrutura de classes e gerenciamento em memória, mas carece de persistência real e autenticação segura.
- [x] Estrutura de Classes (`Usuario`, `SGU`)
- [x] Cadastro em Memória
- [ ] Persistência (Banco de Dados)
- [ ] Criptografia e Autenticação Real

### 📷 SMC - Sistema de Monitoramento e Controle
**Progresso:** 40%
`████░░░░░░`
> O núcleo de monitoramento e a varredura de arquivos são reais. O processamento da imagem (leitura do valor) é simulado.
- [x] Loop de Monitoramento Contínuo (Thread)
- [x] Adapters com Varredura de Diretórios e Validação de Arquivos
- [x] Padrões State e Observer
- [ ] Processamento de Imagem (OCR/Computer Vision)

### 🔔 SAN - Sistema de Alerta e Notificação
**Progresso:** 20%
`██░░░░░░░░`
> A lógica de detecção de anomalias funciona, mas o envio das notificações é apenas logado no console.
- [x] Regras de Negócio (Limites de Consumo)
- [x] Estrutura Strategy (`Email`, `SMS`)
- [ ] Integração com Servidor SMTP (E-mail Real)
- [ ] Integração com Gateway SMS (SMS Real)

### 📊 SGR - Sistema de Geração de Relatórios
**Progresso:** 5%
`▌░░░░░░░░░`
> A estrutura está pronta, mas a geração física dos arquivos (PDF/CSV) ainda é simulada.
- [x] Estrutura Template Method
- [ ] Biblioteca de Geração de PDF (iText/PDFBox)
- [ ] Escrita de Arquivo CSV Real

### 🖥️ Infraestrutura (CLI & Fachada)
**Progresso:** 90%
`█████████░`
> A interface e a orquestração estão quase completas para o escopo atual.
- [x] CLI Interativa
- [x] Fachada (Facade Pattern)
- [x] Sistema de Logs (Singleton)

---
*Desenvolvido como projeto final da disciplina de padrões de projeto no Campus Campina Grande do IFPB.*
