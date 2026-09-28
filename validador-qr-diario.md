# OatGuard — Validador de QR Code

**Status:** ✅ SEMANA 1 IMPLEMENTADA + ✅ API REAL INTEGRADA (Semana 2 Fase 1) — próximo: testes em hardware  
**Criado:** 2026-09-25  
**Última atualização:** 2026-09-28  
**Package:** `com.capsec.oatguard`  
**GitHub:** https://github.com/thadeutourinho-tech/oatguard (Public, MIT License)  
**Build:** ✅ BUILD SUCCESSFUL (app-debug.apk com chave real, 2026-09-28)  
**Google Cloud:** ✅ Projeto "capsec-oatguard" + Safe Browsing API v4 + API Key  
**SHA-1 Debug:** `89358E7C17384F914E9F944192EFC9A28AE17A64`  
**Especificação Completa:** `oatguard-especificacao.md` (v1.2)

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

### Fase 1: MVP (Esta semana?)
- [ ] Projeto Kotlin + Compose setup
- [ ] ML Kit QR scanner integrado
- [ ] Google Safe Browsing API client
- [ ] Tela de resultado (score + cor + descrição)
- [ ] Banner CapSEC clicável
- [ ] Build APK interno + testes

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
- [stated] Chave fica compilada no APK MAS protegida por restrições Google Cloud (package + SHA-1). **Correção 2026-09-28:** a restrição NÃO é criptográfica — headers são texto e o SHA-1 é público; quem extrair a chave e enviar os headers certos consegue usá-la. Impacto limitado: API restriction (só Safe Browsing, gratuita) → abuso consome cota, não gera custo. Monitorar métricas no Cloud Console
- [stated] Proteção: Google Cloud Console registra Package Name + SHA-1 fingerprints + API restriction (Safe Browsing only)
- [stated] ~~Android envia metadados automáticos~~ **CORRIGIDO 2026-09-28:** chamadas REST via OkHttp NÃO enviam `X-Android-Package`/`X-Android-Cert` automaticamente (só libs do Play Services). O app adiciona via `AndroidAppIdentityInterceptor` (SHA-1 calculado em runtime do certificado de assinatura). Verificado: sem headers → 403; com headers → 200
- [stated] Google valida origem e retorna 403 Forbidden se package ou certificado não corresponderem
- [stated] Proteção prática: terceiros que copiem só a chave recebem 403 (barreira contra uso casual, não garantia — ver correção acima)
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
- **Retrocompatibilidade:** minSdk 24 (Android 7.0), targetSdk 35, segurança via NetworkSecurityConfig ✅
- **Conformidade Play Store:** 64-bit, targetSdk 35, Privacy Policy + Data Safety declaration ✅
- **Release:** Play Store ✅
- **Idiomas:** 7 línguas (PT, EN, ES, FR, RU, HI, AR) — mesmas do OatCall ✅
- **Assets:** oat-icon.png + logo_new.jpg fornecidos ✅
- **Cores:** Navy (#001F3F) + Azul Claro (#2196F3) + Verde/Amarelo/Vermelho (scores) ✅

## ❓ Pontos em Aberto (TBD - To Be Defined)

- **URL de Privacidade/Documentação:** Confirmar URL exata (atualmente em Constants.kt como `https://www.capsec.com.br/oatguard/privacidade`)
- **Analytics:** Rastrear validações, unsafe rate, cliques banner? (decisão pós-MVP)
- **Descrição Play Store:** Copy exato? (próximo passo)
- **Monetização futura?** (Agora é só marketing — mas pode evoluir)
- **Repositório GIT:** Onde hostar? (GitHub CapSEC?)

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

**Modelo de Segurança Completo:** Chave compilada no APK, restrita no Google Cloud (package + SHA-1 + API restriction). O app envia `X-Android-Package`/`X-Android-Cert` via `AndroidAppIdentityInterceptor`. Restrição = barreira contra uso casual; impacto de abuso limitado a cota (API gratuita). Detalhes: especificação v1.2, Passo 2.5 e seção 11.

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
- ✅ Retrocompatibilidade (minSdk 24, targetSdk 35)
- ✅ Conformidade Play Store 2024
- ✅ Botão de Privacidade/Documentação
- ✅ NetworkSecurityConfig (HTTPS only)
- ✅ 64-bit support + Data Safety declaration

---

## 🔄 Próximas Ações (Ordem Sugerida)

### Sessão 2: Desenvolvimento — Semana 1 MVP (CONCLUÍDA 2026-09-27)
**Executor:** Claude Code  
**Resultado:** ✅ App Android MVP 100% funcional, build debug verificado

#### Entregáveis Concluídos:
- [x] Setup projeto Kotlin + Gradle (version catalog, dependencies)
- [x] Todas as 4 screens implementadas em Jetpack Compose (Splash, Home, QRScanner, Result)
- [x] Google Safe Browsing API client (Retrofit) — mockado com heurística por keyword
- [x] QR Scanner funcional (CameraX + ML Kit barcode detection)
- [x] Navigation completa (NavHost com routing)
- [x] Theme aplicado (cores de marca Navy + Azul Claro)
- [x] Tratamento de erros (QR inválido, offline, API erro, câmera negada)
- [x] 7 idiomas traduzidos (PT/EN/ES/FR/RU/HI/AR)
- [x] Build APK debug verificado (`BUILD SUCCESSFUL`)
- [x] Repositório GitHub criado (Public, MIT License, Android .gitignore)

#### Não realizados nesta sessão:
- [ ] Testes em device/emulador físico (pendente Semana 2)

---

### Sessão 3: Google Cloud Setup + Integração API Real (SEMANA 2 — Fase 1 CONCLUÍDA 2026-09-28)
**Data:** 2026-09-28  
**Foco:** Google Cloud API + configuração de segurança

#### ✅ Concluído:
- [x] Projeto "capsec-oatguard" criado no Google Cloud Console
- [x] Safe Browsing API v4 habilitada
- [x] API Key gerada ("Chave de API 1")
- [x] SHA-1 debug obtido: `89358E7C17384F914E9F944192EFC9A28AE17A64`
- [x] Application restrictions configuradas:
   - Android apps: `com.capsec.oatguard`
   - SHA-1: `89358E7C17384F914E9F944192EFC9A28AE17A64`
- [x] API restrictions configuradas: Safe Browsing API only
- [x] Chave pronta para integração

#### ✅ Fase 2 — Integração API real (2026-09-28):
- [x] Chave API em local.properties (fora do Git) → `BuildConfig.SAFE_BROWSING_API_KEY`
- [x] `RealSafeBrowsingRepository` ativo automaticamente (provider escolhe Real quando a chave não está vazia)
- [x] `AndroidAppIdentityInterceptor`: envia `X-Android-Package` + `X-Android-Cert` (sem isso a API retorna 403)
- [x] ViewModel criado via factory com Application context; log de erros de API só em debug (tag `QRValidatorViewModel`)
- [x] `./gradlew clean assembleDebug` → BUILD SUCCESSFUL
- [x] Chave validada via curl contra a API real: URL limpa → `{}`; URLs de teste do Google → MALWARE / SOCIAL_ENGINEERING
- [x] SHA-1 do diário corrigido (tinha 41 chars; valor real conferido com keytool)
- [x] `oatguard-especificacao.md` atualizada para v1.2 (seção 11 + correções em 5.1 e Passo 2.5)
- [x] Avisos da IDE tratados (detalhes: especificação, seção 11.5):
   - `SafeBrowsingRepository.kt`: `delay` com `Long` → `Duration` (`800.milliseconds`)
   - Especificação: exemplos de `AndroidManifest.xml` com `... />` (XML inválido) corrigidos; exemplo `if`/`else` vazio trocado por código real do interceptor
   - Regra adotada: blocos de código nos `.md` devem ser sintaticamente válidos (a IDE os analisa)
   - Restante (inofensivo): `@OptIn(ExperimentalGetImage)` sem efeito em `QRScannerScreen.kt:242`
   - Build após correções: ✅ BUILD SUCCESSFUL

**Arquivos alterados:** `data/api/AndroidAppIdentityInterceptor.kt` (novo), `data/api/SafeBrowsingClient.kt`, `data/repository/SafeBrowsingRepository.kt`, `viewmodel/QRValidatorViewModel.kt`, `ui/navigation/Navigation.kt`

**Decisões:** mantida a arquitetura da Semana 1 em vez dos snippets do `oatguard-prompt-api-real.md`: strings i18n via resources; erro de API → tela de erro (não "score 50"); `platformTypes: ANY_PLATFORM` (não só `ANDROID`)

#### ⏳ Próximos passos:
- [ ] Testes em device/emulador físico (URLs de teste: `http://malware.testing.google.test/testing/malware/`, `http://testsafebrowsing.appspot.com/s/phishing.html`)
- [ ] Teste de redirecionamentos (URLResolver) com encurtador real

---

### Sessão 4: Testes Hardware (SEMANA 2 — Fase 2 — Planejado)
- [ ] Instalar APK debug em device/emulador
- [ ] QR com URL limpa → verde (100); URLs de teste do Google → vermelho (MALWARE 20 / PHISHING 30)
- [ ] Encurtador real (bit.ly/tinyurl) → URLResolver resolve o destino
- [ ] Modo avião → tela "sem internet"
- [ ] Se aparecer erro de API: `adb logcat -s QRValidatorViewModel` (403 = package/SHA-1 não batem)

### Sessão 5: Play Store Preparation (SEMANA 3)
- [ ] Descrição Play Store (copy em PT + EN)
- [ ] Screenshots (mínimo 2-3 idiomas)
- [ ] Privacy Policy URL confirmada e publicada
- [ ] Data Safety declaration preenchida

### Sessão 6: Release (SEMANA 3)
- [ ] SHA-1 de release extraído (Play Console → App Signing)
- [ ] SHA-1 release adicionado ao Google Cloud
- [ ] Play Console setup finalizado
- [ ] Internal testing release
- [ ] Submit pra review
- [ ] Launch (public release)

---

## 📄 Open Source & Licença (DECIDIDO: 2026-09-26)

### Decisão Final
- [stated] Abrir em GitHub como MIT License
- [stated] Logo "Oat" protegido via INPI existente (herdado de OatCall)
- [stated] ZERO ações adicionais no INPI necessárias
- [stated] Benefício duplo: open source (comunidade) + marca protegida (profissionalismo)

### Repositório GitHub (CRIADO 2026-09-27)
- **URL:** https://github.com/thadeutourinho-tech/oatguard
- **Visibilidade:** Public
- **Licença:** MIT (auto-gerado GitHub)
- **.gitignore:** Android (auto-gerado GitHub)
- **Status:** Pronto para `git push` do código local

#### Estrutura (após push)
```
github.com/thadeutourinho-tech/oatguard/
├── README.md (descritivo com features, stack, roadmap, attribution)
├── LICENSE (MIT — auto-gerado)
├── .gitignore (Android — auto-gerado)
│
├── app/src/main/
│   ├── kotlin/com/capsec/oatguard/
│   │   ├── ui/ (screens, components, theme, navigation)
│   │   ├── data/ (api, network, repository)
│   │   ├── viewmodel/
│   │   └── utils/
│   ├── res/
│   │   ├── drawable/ (oat_icon.png, logo_capsec.png)
│   │   ├── values/ (strings.xml + 6 values-XX/)
│   │   └── ...
│   └── AndroidManifest.xml
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/ (wrapper)
│
└── local.properties (⚠️ NÃO commitar — .gitignore)
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

### Semana 1: MVP (Claude Code)
- Projeto setup + UI/UX completa
- Google Cloud API Key: `""` (vazio) — Mock SafeBrowsingService
- Todas as screens + navegação
- Result: APK debug compilável sem dependência API real

### Semana 2: API Real + Testes Hardware
- ✅ Google Cloud API Key: Chave real criada e restrita
- ✅ local.properties: `SAFE_BROWSING_API_KEY=AIzaSy...` (sem aspas)
- ✅ Recompilado — exigiu uma mudança de código: headers `X-Android-*` via interceptor
- ✅ API validada via curl (200 com headers / 403 sem)
- ⏳ Testes em device/emulador com API real

### Semana 3: Play Store + GitHub
- SHA-1 release adicionado ao Google Cloud
- GitHub público com MIT License
- Play Store release
- Result: App em produção + comunidade

---

## 🔗 Referências

- **GitHub Repository:** https://github.com/thadeutourinho-tech/oatguard (Public, MIT License)
- [Google Safe Browsing API Docs](https://developers.google.com/safe-browsing/v4)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- [CameraX Documentation](https://developer.android.com/training/camerax)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- OatCall architecture (referência de implementação offline)
- Play Store presence: CapSEC já tem account ativo
- Documentação: `www.capsec.com.br/oatguard/pt/docs.html` (7 idiomas)
- Diário local: `/home/claude/validador-qr-diario.md`