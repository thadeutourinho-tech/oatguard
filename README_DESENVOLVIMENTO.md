# OatGuard — Pronto para Desenvolvimento em Claude Code

**Status:** ✅ ESPECIFICAÇÃO FINALIZADA  
**Data:** 2026-09-26  
**Package:** `com.capsec.oatguard`  
**Linguagem:** Kotlin + Jetpack Compose  

---

## 📋 **Resumo Executivo para Claude Code**

### O que é OatGuard?
App Android B2C (gratuito) que valida QR codes via Google Safe Browsing API. Usuário escaneia → app consulta Google → retorna score (0-100) com cor (verde/amarelo/vermelho).

**Objetivo:** Ferramenta de marketing + segurança para CapSEC.

### Stack Técnico
- **Kotlin** + Jetpack Compose (UI)
- **ML Kit Vision** + **CameraX** (QR scanning)
- **Retrofit 2.11+** + OkHttp 4.12+ (HTTP)
- **Google Safe Browsing API v4** (gratuita)
- **Build:** minSdk 24, targetSdk 35, compileSdk 35
- **Permissões:** CAMERA, INTERNET

### 7 Idiomas
- Português (padrão), Inglês, Espanhol, Francês, Russo, Hindi, Árabe (RTL)

---

## 📚 **3 Arquivos de Documentação Sincronizados**

| Arquivo | Descrição | Para Quem |
|---------|-----------|-----------|
| **oatguard-especificacao.md** | Especificação técnica completa (914 linhas) | Developers (referência detalhada) |
| **oatguard-prompt-claude-code.md** | Prompt executável para Claude Code (534 linhas) | Claude Code (instruções diretas) |
| **validador-qr-diario.md** | Diário de projeto + decisões + roadmap | Product Managers + stakeholders |

**Todos atualizados com:**
- ✅ URL docs.html com i18n: `https://www.capsec.com.br/oatguard/{lang}/docs.html`
- ✅ Timeline de desenvolvimento (Semana 1: placeholder → Semana 2: chave real)
- ✅ Licença MIT + GitHub (zero ações INPI adicionais necessárias)

---

## 🔐 **Modelo de Segurança (Client-Side Encryption)**

### Google Cloud API Key
- **Semana 1:** Placeholder (`""`) — Mock SafeBrowsingService
- **Semana 2:** Chave real — `local.properties` → `BuildConfig`
- **Proteção:** Restrições Google Cloud (Package Name + SHA-1 + API restriction)
- **Android valida:** Metadados `X-Android-Package` + `X-Android-Cert`
- **Resultado:** Terceiros recebem 403 Forbidden (sem certificado correto)

### SHA-1 Fingerprints
- **Debug:** Gerar com `keytool -list -v -keystore ~/.android/debug.keystore`
- **Upload (release local):** `F5:A1:F9:44:1E:A1:22:F4:52:D9:3F:0D:A6:22:1F:28:1C:3F:30:25`
  - Keystore `keystore/oatguard-release.jks`, alias `oatguard-release`, válido até 2056 (senhas em `local.properties`)
  - Conferir com `./gradlew signingReport` (variante `release`)
- **App signing (Play):** Extrair de Play Console > Integridade do app > Assinatura de apps (Google re-assina o AAB com esta chave — é a que roda nos aparelhos)
- **Google Cloud:** Registrar debug + upload + app signing do Play

---

## 🚀 **3 Fases de Desenvolvimento**

### **Semana 1: MVP (Claude Code)**
- Setup projeto Kotlin + Gradle
- Todas as 4 screens (Splash → Home → Scanner → Result)
- UI/UX com Jetpack Compose
- Navigation + Theme (cores + typography)
- Google Safe Browsing API client (mock com chave vazia)
- QR scanning (CameraX + ML Kit)
- Banners clicáveis (CapSEC + OatCall)
- InfoButton → docs.html com i18n automático
- Error handling + permissões
- **APK debug compilável** (sem chave real necessária)

### **Semana 2: Testes Hardware**
- Criar projeto Google Cloud ("capsec-oatguard")
- Enable Safe Browsing API v4
- Gerar API Key com restrições (Package Name + SHA-1 debug + Safe Browsing only)
- Atualizar `local.properties`: `SAFE_BROWSING_API_KEY="AIzaSyD..."`
- Recompila (build.gradle.kts já suporta)
- Testa em device/emulador com API real
- **Nenhuma mudança no código** — apenas valor da chave

### **Semana 3: Play Store + Open Source**
- Extrair SHA-1 de release do Play Console
- Adicionar SHA-1 release no Google Cloud
- Publicar em GitHub (MIT License)
- Submit Play Store
- Launch

---

## ✅ **Checklist Pronto para Claude Code**

### Decisões Finalizadas
- ✅ Nome: OatGuard
- ✅ Package: `com.capsec.oatguard`
- ✅ API: Google Safe Browsing (lookup online)
- ✅ UI: Score 0-100 + cores (verde/amarelo/vermelho)
- ✅ Banners: CapSEC + OatCall (clicáveis)
- ✅ InfoButton: docs.html com i18n
- ✅ Assets: oat-icon.png + logo_new.jpg (fornecidos)
- ✅ Retrocompatibilidade: minSdk 24, targetSdk 35
- ✅ Play Store 2024: 64-bit, Privacy Policy, Data Safety
- ✅ Conformidade: HTTPS only, NetworkSecurityConfig
- ✅ 7 idiomas: PT, EN, ES, FR, RU, HI, AR
- ✅ Open Source: MIT License + GitHub
- ✅ INPI: Zero ações adicionais (logo Oat já registrado)

### Pontos em Aberto (TBD)
- [x] SHA-1 upload/release local (`F5:A1:F9:…:30:25`)
- [ ] SHA-1 app signing (gerado no Play Console, Semana 3)
- [ ] Descrição Play Store (copy oficial)
- [ ] GitHub repo (quando público)

---

## 📖 **Como Usar Esta Documentação**

### Para Começar Semana 1 (Agora)
1. Leia `oatguard-prompt-claude-code.md` (prompt completo)
2. Abra no Claude Code
3. Cole o prompt
4. Siga as instruções

### Para Referência Técnica
- Abra `oatguard-especificacao.md` durante o desenvolvimento
- Seções: Arquitetura, API, Segurança, Conformidade, Checklist

### Para Status de Projeto
- Abra `validador-qr-diario.md` para decisões + roadmap

---

## 🎯 **Estado Final**

**OatGuard está 100% especificado e pronto para desenvolvimento.**

Todos os 3 documentos estão sincronizados, detalhados e prontos.

Quando entrar no Claude Code:
1. Tenha `oatguard-prompt-claude-code.md` à mão
2. Execute desenvolvimento sem bloqueadores
3. Semana 1: Mock API (chave vazia)
4. Semana 2: API real (chave do Google Cloud)
5. Semana 3: Play Store + GitHub

**Vamos codar! 🚀**

---

*Documentação: 2026-09-27*  
*Mantida em: `/home/claude/README_DESENVOLVIMENTO.md`*
