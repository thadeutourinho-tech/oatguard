# OatGuard — Validador de QR Code

**Status:** 🟢 SEMANA 1 IMPLEMENTADA (build debug verificado)  
**Criado:** 2026-09-25  
**Última atualização:** 2026-09-27  
**Package:** `com.capsec.oatguard`  
**Especificação Completa:** `oatguard-especificacao.md` (ver seção 10 — Registro de Implementação)

---

## 📋 Visão Geral

**OatGuard** é um app B2C gratuito de validação de QR codes para Android. Usuário escaneia QR → app consulta Google Safe Browsing → retorna score de confiabilidade com cor e descrição. Serve como marketing/propaganda para a CapSEC.

Parte do ecossistema "Oat" (OatCall + OatGuard).

**Objetivo:** Ferramenta útil + retenção de marca + potencial lead generation para consultoria.

---

## 🎯 Decisões Arquiteturais

### API de Validação
- ✅ **Google Safe Browsing API** (gratuita, ~500k consultas/dia)
- Lookup online (não local cache) pra MVP — simplicidade > privacidade nesta fase
- Estrutura pronta pra migrar pra sync offline depois (tipo OatCall)

### UI/UX
- Score numérico: 0-100
- **Verde:** 75-100 (seguro)
- **Amarelo:** 40-74 (suspeito)
- **Vermelho:** 0-39 (perigoso)
- Descrição breve do resultado (ex: "Site seguro", "Possível phishing")
- Botão de ação: "Abrir link" ou "Voltar"
- **Banner CapSEC** embaixo (sempre visível, clicável → www.capsec.com.br)

### Stack Técnico
- **Linguagem:** Kotlin
- **UI Framework:** Jetpack Compose
- **QR Scan:** ML Kit Vision
- **API Client:** Retrofit
- **Release:** Google Play Store (CapSEC já tem presença)

### Permissões
- Câmera
- Internet
- Política clara de privacidade (Google Safe Browsing)

---

## 🚀 Roadmap

### Fase 1: MVP (Semana 1 — CONCLUÍDA 2026-09-27)
- [x] Projeto Kotlin + Compose setup
- [x] ML Kit QR scanner integrado (com fallback de permissão de câmera)
- [x] Google Safe Browsing API client (Retrofit) — pronto pra Semana 2, mockado agora
- [x] Tela de resultado (score + cor + descrição)
- [x] Banner CapSEC + OatCall clicáveis
- [x] Build APK debug interno (`BUILD SUCCESSFUL`, `app-debug.apk`) — testes manuais em device ainda pendentes (sem emulador/dispositivo nesta sessão)

### Fase 2: Play Store Release
- [ ] Play Console setup
- [ ] Screenshots + descrição store
- [ ] Teste beta (internal testing)
- [ ] Submit pra review
- [ ] Launch

### Fase 3: Analytics + Iteração
- [ ] Firebase Analytics (URLs validadas, taxa unsafe, cliques banner)
- [ ] Feedback de usuários
- [ ] Possivelmente cache offline (se necessário)

### Fase 4: Expansão (Post-MVP)
- [ ] Histórico de validações
- [ ] Educação em tempo real (padrões de phishing)
- [ ] Integração B2B (compliance/treinamento pra clientes CapSEC?)

---

## 💰 Modelo Financeiro Confirmado (2026-09-26)

**Custo Operacional: R$ 0**
- App client-to-Google (sem servidor backend próprio)
- Safe Browsing API 100% gratuita
- Cota padrão: ~10k requisições/dia (expansível sem custos)
- ROI: marketing institucional + leads corporativos

**Google Cloud Setup:**
- Projeto dedicado "capsec-oatguard"
- APIs & Services → Enable Safe Browsing API v4
- API Key com restrições: Package Name + SHA-1 + API restriction

---

## 🔐 Conformidade Google & Atribuição Obrigatória (2026-09-26)

**Atribuição Legal (Mandatory):**
- Footer Result Screen: "Proteção por Google Safe Browsing"
- Seção Sobre: menção com link
- Privacidade/Documentação: crédito completo
- **Falta de atribuição = rejeição Play Store + suspensão API**

**Linguagem Qualificada (Não-Absoluta):**
- ✅ "Nenhuma ameaça conhecida detectada" (não: "100% seguro")
- ✅ "Possível risco" (não: "Perigoso confirmado")
- ✅ "Potencialmente perigoso" (não: "Certamente malware")
- Incluir disclaimer de isenção

---

## 🔐 Refinamentos de Segurança (Adicionados 2026-09-26)

Baseado em feedback de análise de segurança (Google Gemini) + modelo client-side encryption:

### Segurança da Chave de API (Client-Side Encryption Model)
- [stated] API Key NUNCA hardcodificada diretamente no código (APK é descompilável)
- [stated] Armazenamento: `local.properties` (NÃO commitar ao Git) → ler com `Properties().load()` em `build.gradle.kts` → `buildConfigField`
- [stated] Chave fica compilada no APK MAS protegida por restrições Google Cloud criptográficas
- [stated] Proteção: Google Cloud Console registra Package Name + SHA-1 fingerprints + API restriction (Safe Browsing only)
- [stated] Android envia metadados automáticos: `X-Android-Package` e `X-Android-Cert` nas requisições HTTP
- [stated] Google valida origem e retorna 403 Forbidden se package ou certificado não corresponderem
- [stated] Proteção prática: terceiros que copiem chave recebem 403 (sem certificado de assinatura correto)
- [stated] Dois SHA-1s obrigatórios: SHA-1 do `debug.keystore` (desenvolvimento) + SHA-1 do Play App Signing (release, adicionado depois via Play Console)
- [stated] Rotação de chaves: rara em client-side (apenas incidente ou compliance), sem quebra de usuários — usar grace period 3-6 meses com ambas chaves ativas

### Certificate Pinning & Network Security
- [stated] REMOVER pinning SHA-256 fixo com Google (causa cascata de falhas em cert renewal)
- [stated] Usar HTTPS obrigatório + CAs de sistema via NetworkSecurityConfig
- [stated] Seguro + compatível com rotação de certificados da Google

### URL Resolution (Phishing Prevention)
- [stated] Resolver redirecionamentos HTTP 301/302 ANTES de enviar pra Safe Browsing API
- [stated] Implementar URLResolver.kt: HEAD requests, máx 5 hops, timeout 5s por request, loop detection
- [stated] Evitar validar apenas encurtadores (bit.ly, tinyurl) sem resolver destino final
- [stated] Fluxo: QR → extract URL → URLResolver.resolve() → validate final URL → result

## ✅ Decisões Finalizadas

- **Nome:** OatGuard ✅
- **Package:** `com.capsec.oatguard` ✅
- **API de Validação:** Google Safe Browsing (lookup online pra MVP) ✅
- **UI:** Score 0-100 + cores (verde/amarelo/vermelho) + descrição + botões (Abrir Link + Verificar Outro QR) ✅
- **Banners:** CapSEC + OatCall (clicáveis, sempre visíveis) ✅
- **Botão de Privacidade:** Ícone ℹ️ no header das telas (Home + Result) → abre documentação/política de privacidade ✅
- **Retrocompatibilidade:** minSdk 24 (Android 7.0), targetSdk **37** (atualizado de 35 — scaffold do Android Studio já veio com 37, decisão tomada com o usuário em 2026-09-27), segurança via NetworkSecurityConfig ✅
- **Conformidade Play Store:** 64-bit, targetSdk 37, Privacy Policy + Data Safety declaration (Privacy Policy/Data Safety ainda TBD, ver pendências) ✅ implementação / ⏳ documentação legal
- **Release:** Play Store ✅
- **Idiomas:** 7 línguas (PT, EN, ES, FR, RU, HI, AR) — mesmas do OatCall ✅
- **Assets:** oat-icon.png + logo_new.jpg fornecidos ✅
- **Cores:** Navy (#001F3F) + Azul Claro (#2196F3) + Verde/Amarelo/Vermelho (scores) ✅

## ❓ Pontos em Aberto (TBD - To Be Defined)

- **URL de Privacidade/Documentação:** ✅ implementada em `Constants.getDocsUrl(lang)` como `https://www.capsec.com.br/oatguard/{lang}/docs.html` (i18n, fallback PT) — falta confirmar se a página em si já existe/está publicada nesse domínio.
- **Analytics:** Rastrear validações, unsafe rate, cliques banner? (decisão pós-MVP, nada implementado)
- **Descrição Play Store:** Copy exato? (próximo passo)
- **Monetização futura?** (Agora é só marketing — mas pode evoluir)
- **Repositório GIT:** Onde hostar? (GitHub CapSEC?) — projeto local ainda não é um repositório git nesta máquina.
- **Google Cloud project + API Key real:** ainda não criados (Semana 2).
- **Teste em device/emulador físico:** ainda não realizado (só build de compilação verificado).

---

## 📝 Notas

- Padrão similar ao OatCall (gratuito + offline-first ready)
- Diferencial forte: educação em segurança + brand building
- Público alvo: qualquer um que leia QR (restaurante, conta, rua)
- Distribuição passiva = alto upside com baixo custo

---

## 📚 Documentação Finalizada (2026-09-26)

**3 Arquivos Sincronizados e Prontos:**

1. **`validador-qr-diario.md`** (este arquivo)
   - Status de projeto + decisões + roadmap
   - Modelo cliente-servidor documentado

2. **`oatguard-especificacao.md`**
   - Especificação técnica completa (914 linhas)
   - Setup Google Cloud Console com BuildConfig robusto
   - SHA-1s (debug + release) com keytool commands
   - Rotação de chaves + grace period

3. **`oatguard-prompt-claude-code.md`**
   - Prompt para Claude Code (512 linhas)
   - Instruções passo-a-passo para implementação
   - BuildConfig com Properties().load() method
   - Proteção criptográfica explicada

**Modelo de Segurança Completo:** Chave compilada no APK é segura via restrições Google Cloud + metadados Android (X-Android-Package, X-Android-Cert).

---

## ✅ Sessão 1: Definição de Requisitos (CONCLUÍDA)

**Data:** 2026-09-26  
**Resultado:** Especificação técnica + segurança + documentação dev, pronta para Claude Code

### Requisitos Finalizados:
- ✅ Fluxo de telas (Splash → Home → Scanner → Result)
- ✅ Integração Google Safe Browsing API
- ✅ UI/UX com cores, layout, banners
- ✅ 7 idiomas (PT, EN, ES, FR, RU, HI, AR)
- ✅ Assets (oat-icon.png + logo_new.jpg)
- ✅ Retrocompatibilidade (minSdk 24, targetSdk 35 na especificação original)
- ✅ Conformidade Play Store 2024
- ✅ Botão de Privacidade/Documentação
- ✅ NetworkSecurityConfig (HTTPS only)
- ✅ 64-bit support + Data Safety declaration

---

## ✅ Sessão 2: Desenvolvimento — Semana 1 MVP (CONCLUÍDA)

**Data:** 2026-09-27
**Executor:** Claude Code (agente desenvolvedor)
**Resultado:** App Android completo (UI + navegação + scanner + API client mockado), build debug verificado. Registro técnico detalhado em `oatguard-especificacao.md` seção 10.

### Entregado:
- ✅ Setup projeto Kotlin + Gradle (version catalog atualizado com CameraX/MLKit/Retrofit/OkHttp/Gson/Navigation-Compose)
- ✅ Todas as screens implementadas (Splash, Home, QRScanner, Result) em Compose
- ✅ Google Safe Browsing API client (Retrofit) implementado e pronto — **mockado** nesta fase via `MockSafeBrowsingRepository` (heurística por keyword na URL: `malware`/`phishing`/`unwanted`/`harmful`)
- ✅ QR Scanner (CameraX + ML Kit), com tratamento de permissão de câmera negada
- ✅ Navigation (NavHost) + Theme (cores de marca aplicadas)
- ✅ Tratamento de erros (QR inválido, sem internet, API indisponível, câmera negada)
- ✅ 7 idiomas traduzidos (pt/en/es/fr/ru/hi/ar)
- ⏳ Testes de compatibilidade em device/emulador real — **não realizados nesta sessão** (só build de compilação: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`)

### Decisões tomadas nesta sessão (com aprovação do usuário):
- Manter compileSdk/targetSdk **37** (scaffold do Android Studio) em vez de forçar 35 como na especificação original — reflete requisitos de Play Store mais atuais nesta data.
- Mock heurístico por keyword em vez de mock fixo "sempre seguro" — permite QA testar todos os estados visuais de score sem depender da API real.

### Bug encontrado e corrigido:
- Banner "Conheça o OatCall" apontava pra `www.capsec.com.br/oatcall` em vez da URL da Play Store especificada (`https://play.google.com/store/apps/details?id=com.capsec.oatcall`). Corrigido em `Constants.kt` antes do build final.

---

## 🔄 Próximas Ações (Ordem Sugerida)

### Sessão 3: Testes com Hardware + Google Cloud (Semana 2)
- [ ] Criar projeto "capsec-oatguard" no Google Cloud Console
- [ ] Habilitar Safe Browsing API v4 + gerar API Key restrita (package + SHA-1 debug)
- [ ] Preencher `SAFE_BROWSING_API_KEY` em `local.properties` (zero mudança de código — `RealSafeBrowsingRepository` assume automaticamente)
- [ ] Testar em device/emulador físico (câmera real, permissões runtime, redirecionamentos reais)

### Sessão 4: Copy + Preparação Play Store
- [ ] Confirmar/publicar a página de documentação/privacidade em `www.capsec.com.br/oatguard/{lang}/docs.html`
- [ ] Descrição da loja (título, short/full description, screenshots em ao menos 2 idiomas)
- [ ] Privacy Policy pública + Data Safety declaration

### Sessão 5: Play Store + GitHub (Semana 3)
- [ ] SHA-1 de release (Play Console → App Signing) adicionado ao Google Cloud
- [ ] Play Console setup + internal testing release
- [ ] Submit pra review
- [ ] Publicar repositório no GitHub (MIT License)

---

## 📄 Open Source & Licença (DECIDIDO: 2026-09-26)

### Decisão Final
- [stated] Abrir em GitHub como MIT License
- [stated] Logo "Oat" protegido via INPI existente (herdado de OatCall)
- [stated] ZERO ações adicionais no INPI necessárias
- [stated] Benefício duplo: open source (comunidade) + marca protegida (profissionalismo)

### Estrutura GitHub
```
github.com/capsec-br/oatguard
├── README.md (com licença MIT + aviso marca Oat)
├── LICENSE (MIT)
├── src/main/kotlin/com/capsec/oatguard/...
├── docs/ (links para www.capsec.com.br/oatguard/docs)
└── .github/workflows/ (CI/CD depois)
```

### README.md Header
```markdown
# OatGuard — QR Code Validator

Open source QR code safety validator powered by Google Safe Browsing.

**License:** MIT

⚠️ **Trademark:** The "Oat" logo and brand are registered trademarks of CapSEC.
Use of the logo in derivative works requires permission.
```

---

## ⏱️ TIMELINE DE DESENVOLVIMENTO

### Semana 1: MVP (Claude Code) — ✅ CONCLUÍDA 2026-09-27
- Projeto setup + UI/UX completa
- Google Cloud API Key: `""` (vazio) — Mock `SafeBrowsingRepository` (heurística por keyword)
- Todas as screens + navegação implementadas
- Resultado: APK debug compilável e verificado (`BUILD SUCCESSFUL`), sem dependência de API real

### Semana 2: Testes Hardware
- Google Cloud API Key: Chave real criada
- local.properties: `SAFE_BROWSING_API_KEY="AIzaSyD..."`
- Apenas recompila (build.gradle.kts já suporta)
- Testes em device/emulador com API real

### Semana 3: Play Store + GitHub
- SHA-1 release adicionado ao Google Cloud
- GitHub público com MIT License
- Play Store release
- Result: App em produção + comunidade

---

## 🔗 Referências

- [Google Safe Browsing API Docs](https://developers.google.com/safe-browsing/v4)
- OatCall architecture (referência de implementação offline)
- Play Store presence: CapSEC já tem account ativo
- Documentação: `www.capsec.com.br/oatguard/pt/docs.html` (7 idiomas)
- Diário local: `/home/claude/validador-qr-diario.md`
