# OatGuard — Especificação Técnica Completa

**Versão:** 1.2  
**Data:** 2026-09-26 (atualizado 2026-09-28)  
**Status:** ✅ Semana 1 implementada + ✅ Semana 2 Fase 1 (API real integrada) — ver seções 10 e 11 "Registro de Implementação"

---

## 1. Visão Geral

**OatGuard** é um app Android B2C gratuito que valida QR codes contra a Google Safe Browsing API, retornando uma nota de segurança com explicação. Estrutura simples, stateless, sem BD local ou cadastros. Funciona como ferramenta de marketing/educação para a CapSEC.

---

## 2. Requisitos Funcionais

### 2.1 Splash Screen
- Exibe apenas o logo "Oat" (idêntico ao OatCall)
- Duração: ~2 segundos
- Navega automaticamente para Home

### 2.2 Tela Principal (Home)
**Layout (de cima pra baixo):**
1. **Header:**
   - Logo Oat + texto "OatGuard" (lado esquerdo, ~30%)
   - Ícone "ℹ️" (info) clicável no canto superior direito
     - Ao clicar: abre documentação/política de privacidade
2. Espaçador
3. **Botão central grande:** "Verificar se QR Code é Seguro"
   - Ocupa ~50% da tela
   - Ao clicar: abre câmera (intent)
4. Espaçador
5. **Banner 1:** "Conheça a CapSEC"
   - Clicável → abre `https://www.capsec.com.br` no navegador
6. **Banner 2:** "Conheça o OatCall"
   - Clicável → abre página Play Store do OatCall (`https://play.google.com/store/apps/details?id=com.capsec.oatcall`)

### 2.3 QR Code Scanner
- Ao clicar em "Verificar QR Code", abre câmera via **ML Kit Vision**
- Captura QR code automaticamente
- Extrai URL do QR
- Valida se é URL válida (regex básico)
- Se não for URL válida: mostra erro e volta para Home
- Se for válida: dispara requisição Google Safe Browsing API

### 2.4 Tela de Resultado (Result)
**Layout (de cima pra baixo):**
1. **Header (idêntico à Home):**
   - Logo Oat + texto "OatGuard" (lado esquerdo, ~30%)
   - Ícone "ℹ️" (info) clicável no canto superior direito
     - Ao clicar: abre documentação/política de privacidade
2. Espaçador
3. **Área central de resultado:**
   - **Círculo com score:** (ex: "87/100")
     - Cor de fundo: Verde (75-100), Amarelo (40-74), Vermelho (0-39)
     - Texto grande, legível
   - **Breve explicação:** (ex: "Site Seguro", "Possível Phishing", "Link Perigoso")
   - **Descrição do destino:** (extraída da Google Safe Browsing API)
4. Espaçador
5. **Botões de ação:**
   - **Botão primário:** "Abrir Link" (principal, destaque visual)
     - Ao clicar: dispara Intent ACTION_VIEW com a URL
     - Abre no navegador padrão do sistema
   - **Botão secundário:** "Verificar Outro QR Code"
     - Ao clicar: volta para Home
6. Espaçador
7. **Banners idênticos à Home:**
   - "Conheça a CapSEC"
   - "Conheça o OatCall"

### 2.5 Botão "Abrir Link" (Result Screen)
- **Localização:** Result Screen, área central (abaixo do score)
- **Texto:** "Abrir Link"
- **Ação:** Dispara `Intent(Intent.ACTION_VIEW, Uri.parse(url))`
- **Comportamento:** Abre a URL no navegador padrão do sistema
- **Estilo:** Botão primário, destaque visual (cores da marca)
- **Disponibilidade:** Sempre habilitado (URL já foi validada)
- **Nota:** Responsabilidade é do usuário decidir abrir após ver o score de segurança

### 2.6 Botão de Informações (Info Button)
- **Ícone:** "ℹ️" (info) ou `ic_info` personalizado
- **Localização:** Header, canto superior direito (em ambas Home + Result)
- **Ação ao clicar:** Abre documentação/política de privacidade em navegador
- **URL:** `https://www.capsec.com.br/oatguard/{lang}/docs.html` (i18n automático)
  - Suporta: `pt`, `en`, `es`, `fr`, `ru`, `hi`, `ar`
  - Exemplo PT: `https://www.capsec.com.br/oatguard/pt/docs.html`
  - Implementação: função `getDocsUrl(language: String)` em `Constants.kt`
- **Tamanho:** Pequeno, ~24-32dp
- **Cor:** Navy Escuro (#001F3F) ou Azul Claro (#2196F3) com contraste
- **Ripple:** Feedback visual ao tocar
- **Responsável por:** Acesso a documentação oficial e política de privacidade

### 2.7 Tratamento de Erros
- **QR não é URL:** "QR code não contém um link válido"
- **Sem conexão:** "Sem conexão com a internet"
- **API indisponível:** "Não conseguimos validar o link agora. Tente novamente"
- **URL vazia ou inválida:** Mensagem genérica de erro

---

## 3. Requisitos Técnicos

### 3.1 Stack
- **Linguagem:** Kotlin
- **Compile SDK:** 37 — Latest (⚠️ atualizado de 35 pra 37; ver nota de implementação abaixo)
- **Target SDK:** 37 — Play Store requirement (⚠️ atualizado de 35 pra 37)
- **Mínimo SDK:** Android 7.0 (API 24) — Retrocompatibilidade com segurança
- **UI Framework:** Jetpack Compose (suporta API 21+)
- **QR Scan:** ML Kit Vision (suporta API 21+)
- **HTTP Client:** Retrofit 2.11+ com OkHttp 4.12+ (suporta TLS 1.2+)
- **JSON:** Gson

> **Nota de implementação (2026-09-27):** o projeto foi criado pelo Android Studio já com compileSdk/targetSdk 37 e Compose BOM 2026.02.01 (mais recentes que os 35 originalmente especificados aqui, que datavam de 2024/Android 15). Decisão tomada em conjunto com o Claude Code: manter o scaffold atual (37) em vez de forçar o rebaixamento pra 35, já que reflete os requisitos de Play Store vigentes nesta data. minSdk 24 permanece inalterado. Ver seção 10 para o registro completo.

### 3.1.1 Justificativa de Versões

**minSdkVersion = 24 (Android 7.0, 2016)**
- ✅ Suporta ~95% de dispositivos em uso
- ✅ Oferece suporte seguro a TLS 1.2+ (essencial pra API HTTPS)
- ✅ ML Kit + CameraX + Compose funcionam estáveis
- ✅ Reduz fragmentação sem perder segurança
- ✅ Equilibra alcance vs. maintenance burden

**targetSdkVersion = 35 (Android 15, 2024)**
- ✅ Atende requisitos Play Store atuais
- ✅ Acesso a APIs modernas (permissões, storage, etc)
- ✅ Suporte a 64-bit (obrigatório)
- ✅ Certificação de privacidade e segurança

**compileSdkVersion = 35**
- ✅ Usa features mais recentes na compilação
- ✅ Detecta deprecated APIs em tempo de build
- ✅ Garante compatibilidade com Play Store

### 3.2 Assets (Icons e Logos)
Os arquivos a seguir estão na raiz do projeto Android:

- **oat-icon.png** — Ícone "Oat" (cadeado azul + navy)
  - Uso: Splash screen, Header das telas (Home + Result), Botão "Conheça o OatCall"
  - Dimensões: Originalmente fornecido, usar em múltiplas resoluções via `drawable-*` folders
  
- **logo_new.jpg** — Logo CapSEC completo ("CAPSEC" + ícone)
  - Uso: Botão/Banner "Conheça a CapSEC"
  - Dimensões: Originalmente fornecido, adaptar para altura do banner

**Estrutura de drawable:**
```
res/drawable/
├── ic_oat.png        (oat-icon.png - redimensionado)
└── ic_capsec_logo.png (logo_new.jpg - redimensionado)
```

### 3.3 Permissões
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 3.4 Conformidade Play Store (2024+)

**Requisitos Obrigatórios:**
- ✅ **64-bit support:** ABI `arm64-v8a` (obrigatório desde 2021)
- ✅ **targetSdkVersion:** 35 (obrigatório, atualizado anualmente)
- ✅ **Privacy Policy:** URL pública acessível
- ✅ **Data Safety:** Declarar coleta de dados (SafeBrowsing API usage)
- ✅ **Runtime Permissions:** Solicitar camera + internet com justificativa
- ✅ **Signature:** Assinado com chave de release
- ✅ **No deprecated APIs:** Remover APIs descontinuadas no compileSdk 35

**Certificações Recomendadas:**
- App signing via Play Console (obrigatório)
- Suporte a Android 15 (target 35)
- Testing em mínimo 2 versões (API 24 + API 35)

### 3.5 Compatibilidade Regressiva

**Tratamento de APIs por versão:**
```kotlin
// Exemplo real (AndroidAppIdentityInterceptor): API moderna quando disponível, fallback em versões antigas
val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    // Android 9+: API nova, com suporte a key rotation
    pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo?.apkContentsSigners
} else {
    // Android 7-8: API antiga (deprecated, mas é a única disponível)
    @Suppress("DEPRECATION")
    pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
}
```

**Bibliotecas com suporte regressivo:**
- **Retrofit:** 2.11+ suporta Android 7.0+
- **OkHttp:** 4.12+ força TLS 1.2+ (seguro mesmo em Android antigo)
- **ML Kit:** Detecta versão em runtime, adapta recursos
- **CameraX:** Compatível via androidx (suporta API 21+)
- **Compose:** Runtime compatibility library para APIs antigas

### 3.6 Suporte a Idiomas
App disponível em: **Português, Inglês, Espanhol, Francês, Russo, Hindi, Árabe**

Estrutura de localizações:
```
res/
├── values/                 (Português - padrão)
│   └── strings.xml
├── values-en/             (English)
│   └── strings.xml
├── values-es/             (Español)
│   └── strings.xml
├── values-fr/             (Français)
│   └── strings.xml
├── values-ru/             (Русский)
│   └── strings.xml
├── values-hi/             (हिंदी)
│   └── strings.xml
└── values-ar/             (العربية - RTL)
    └── strings.xml
```

**Configuração RTL para Árabe:**
```xml
<!-- AndroidManifest.xml -->
<application
    android:supportsRtl="true">
    <!-- ... demais atributos e componentes ... -->
</application>
```

**Strings a traduzir:**
- App name: "OatGuard"
- Botão principal: "Verificar se QR Code é Seguro"
- Botão secundário: "Verificar Outro QR Code"
- Banner 1: "Conheça a CapSEC"
- Banner 2: "Conheça o OatCall"
- Explicações de score (Seguro, Phishing, Malware, etc)
- Mensagens de erro (sem conexão, QR inválido, timeout, etc)

### 3.3 Dependências (build.gradle.kts)

> **Atualizado 2026-09-27** com as versões realmente usadas na implementação (via `gradle/libs.versions.toml`), mais recentes que as originalmente listadas aqui (que eram de ~2024). Retrofit, OkHttp e Gson mantiveram-se praticamente iguais; CameraX saiu de beta (1.4.0-beta02 → 1.4.1 estável); foram adicionadas `navigation-compose`, `lifecycle-runtime/viewmodel-compose` e `material-icons-core` (necessárias pra Navigation e para os ícones usados no InfoButton/scanner, não previstas na lista original).

```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.10.1")
implementation("androidx.appcompat:appcompat:1.7.0")

// Compose (via BOM 2026.02.01)
implementation("androidx.activity:activity-compose:1.8.0")
implementation("androidx.compose.material3:material3") // via BOM
implementation("androidx.compose.material:material-icons-core") // via BOM — ícones (Info, ArrowBack)
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.navigation:navigation-compose:2.8.5") // NavHost — não listado na v1.0 desta especificação

// ML Kit Vision (QR Code)
implementation("com.google.mlkit:vision-common:17.3.0")
implementation("com.google.mlkit:barcode-scanning:17.3.0")

// CameraX (estável, saiu de beta)
implementation("androidx.camera:camera-core:1.4.1")
implementation("androidx.camera:camera-camera2:1.4.1")
implementation("androidx.camera:camera-lifecycle:1.4.1")
implementation("androidx.camera:camera-view:1.4.1")

// Retrofit + OkHttp
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson
implementation("com.google.code.gson:gson:2.11.0")
```

---

## 4. Arquitetura

### 4.1 Estrutura de Pacotes
```
com.capsec.oatguard/
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ResultScreen.kt
│   │   └── QRScannerScreen.kt
│   ├── components/
│   │   ├── OatHeader.kt (com InfoButton integrado)
│   │   ├── InfoButton.kt (botão de privacidade/documentação)
│   │   ├── SafetyScoreCard.kt
│   │   ├── CapSecBanner.kt
│   │   └── OatCallBanner.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   └── Theme.kt
│   └── navigation/
│       └── Navigation.kt
├── data/
│   ├── api/
│   │   ├── SafeBrowsingService.kt
│   │   ├── SafeBrowsingClient.kt (create(context) — OkHttp + Retrofit)
│   │   ├── AndroidAppIdentityInterceptor.kt (X-Android-Package + X-Android-Cert; ver seção 11)
│   │   └── models/ (request/response)
│   ├── network/
│   │   └── URLResolver.kt (resolve redirects, máx 5 hops, timeout 5s)
│   └── repository/
│       └── SafeBrowsingRepository.kt (interface + RealSafeBrowsingRepository + MockSafeBrowsingRepository + provider; ver seção 10)
├── viewmodel/
│   └── QRValidatorViewModel.kt
├── utils/
│   ├── URLValidator.kt
│   ├── ColorMapper.kt
│   ├── Constants.kt (URLs, configurações)
│   ├── ThreatScoreMapper.kt (threatType → score + string resources; não previsto na v1.0)
│   └── IntentUtils.kt (openUrl() compartilhado por InfoButton/banners/ResultScreen; não previsto na v1.0)
├── MainActivity.kt
└── (MyApp.kt Application class não foi necessária — sem estado global, dispensada)

### 4.1.1 Constants.kt (implementado 2026-09-27)
```kotlin
object Constants {
    const val SAFE_BROWSING_BASE_URL = "https://safebrowsing.googleapis.com/"

    const val CAPSEC_URL = "https://www.capsec.com.br"
    const val OATCALL_URL = "https://play.google.com/store/apps/details?id=com.capsec.oatcall"
    const val SAFE_BROWSING_LEARN_MORE_URL = "https://www.google.com/safebrowsing/"

    const val SPLASH_DELAY_MS = 2000L
    const val MAX_REDIRECT_HOPS = 5
    const val REDIRECT_TIMEOUT_SECONDS = 5L

    const val SCORE_SAFE_MIN = 75
    const val SCORE_SUSPICIOUS_MIN = 40

    private val SUPPORTED_DOC_LANGUAGES = setOf("pt", "en", "es", "fr", "ru", "hi", "ar")
    private const val DEFAULT_DOC_LANGUAGE = "pt"

    fun getDocsUrl(language: String): String {
        val lang = language.lowercase().takeIf { it in SUPPORTED_DOC_LANGUAGES } ?: DEFAULT_DOC_LANGUAGE
        return "https://www.capsec.com.br/oatguard/$lang/docs.html"
    }
}
```
Os threat types (`MALWARE`, `SOCIAL_ENGINEERING`, `UNWANTED_SOFTWARE`, `POTENTIALLY_HARMFUL_APPLICATION`) foram implementados em `data/api/models/ThreatTypes` (junto ao request model), não em `Constants`, para ficar ao lado do payload da API que os usa.

### 4.2 Data Flow

```
Home Screen
   ↓ (clica "Verificar QR Code")
QR Scanner Screen (CameraX + ML Kit)
   ↓ (QR lido + URL extraído)
ViewModel.validateQRCode(url)
   ↓
SafeBrowsingRepository.checkUrl(url)
   ↓
URLResolver.resolve(url) (segue redirects, máx 5 hops)
   ↓
SafeBrowsingService (Retrofit + AndroidAppIdentityInterceptor)
   ↓ (chamada HTTP)
Google Safe Browsing API
   ↓ (resposta)
SafeBrowsingRepository
   ↓
ViewModel.updateResult()
   ↓
Result Screen (exibe score + descrição)
   ↓ (clica "Verificar outro QR")
Home Screen
```

### 4.3 Componentes Principais

#### ViewModel (QRValidatorViewModel)
- Gerencia estado da validação
- Holds: `url`, `score`, `explanation`, `description`, `isLoading`, `error`
- Expõe: `validateQRCode(url)`, `reset()`

#### SafeBrowsingRepository
- Abstração sobre SafeBrowsingService
- Trata erros de rede/API
- Mapeia resposta API pra modelo interno

#### SafeBrowsingService (Retrofit)
- Interface HTTP pra Google Safe Browsing API
- Single POST request: `POST https://safebrowsing.googleapis.com/v4/threatMatches:find?key={API_KEY}`

#### UI Screens (Compose)
- **SplashScreen:** Logo apenas, 2s delay
- **HomeScreen:** Logo + nome + botão grande + 2 banners
- **QRScannerScreen:** CameraX + ML Kit barcode scanning
- **ResultScreen:** Score circle + explicação + banners

---

## 5. Google Safe Browsing API

### 5.1 Integração
- **Endpoint:** `POST https://safebrowsing.googleapis.com/v4/threatMatches:find?key={API_KEY}`
- **Request body (JSON):**
  ```json
  {
    "client": {
      "clientId": "com.capsec.oatguard",
      "clientVersion": "1.0.0"
    },
    "threatInfo": {
      "threatTypes": [
        "MALWARE",
        "SOCIAL_ENGINEERING",
        "UNWANTED_SOFTWARE",
        "POTENTIALLY_HARMFUL_APPLICATION"
      ],
      "platformTypes": ["ANY_PLATFORM"],
      "threatEntryTypes": ["URL"],
      "threatEntries": [
        {"url": "https://example.com"}
      ]
    }
  }
  ```
- **Response:**
  - Se nenhuma ameaça conhecida: `{}` (objeto vazio, sem `matches` — verificado contra a API real em 2026-09-28)
  - Se ameaça: `{"matches": [{"threatType": "MALWARE", "platformType": "ANY_PLATFORM", "threat": {"url": "..."}, "cacheDuration": "300s", "threatEntryType": "URL"}]}`
- **`platformTypes: ANY_PLATFORM`** (implementado): cobre ameaças listadas para qualquer plataforma. Restringir a `ANDROID` perderia phishing/malware catalogados para outras plataformas.
- **Headers obrigatórios:** `X-Android-Package` e `X-Android-Cert`. Sem eles, a chave com restrição "Android apps" responde `403`. Ver Passo 2.5 e seção 11.

### 5.2 Score Mapping (Linguagem Qualificada - Conformidade Google)

- **Seguro (matches vazio):** Score = 100, Cor = Verde, Explicação = "Nenhuma ameaça conhecida detectada"
- **MALWARE:** Score = 20, Cor = Vermelho, Explicação = "Risco potencial de malware detectado"
- **SOCIAL_ENGINEERING:** Score = 30, Cor = Vermelho, Explicação = "Possível ataque de phishing"
- **UNWANTED_SOFTWARE:** Score = 45, Cor = Amarelo, Explicação = "Possível software suspeito"
- **POTENTIALLY_HARMFUL_APPLICATION:** Score = 50, Cor = Amarelo, Explicação = "Aplicação potencialmente perigosa"

**Nota:** Usar "possível", "potencial", "suspeito" em vez de afirmações absolutas (obrigatório pelas políticas da Google)

---

## 6. Instruções de Build

### 6.0.4 Segurança da Chave de API (Google Safe Browsing)

**⚠️ CRÍTICO: Proteção contra Engenharia Reversa**

A Google Safe Browsing API v4 recebe a chave via query parameter (?key=...). Como o APK é público, a chave pode ser extraída via descompilação se não for protegida.

**Mitigação Obrigatória — Google Cloud Console:**

1. **Restrição de Aplicação (Application Restriction):**
   - Ir para: Google Cloud Console → APIs & Services → Credentials → Selecionar API Key
   - Seção "Application restrictions"
   - Selecionar "Android apps"
   - Adicionar Package Name: `com.capsec.oatguard`
   - Adicionar SHA-1 Fingerprint **de debug** (pra desenvolvimento)
   - Adicionar SHA-1 Fingerprint **de release** (pra Play Store - será gerado depois)

2. **Restrição de API (API Restriction):**
   - Seção "API restrictions"
   - Selecionar "Custom" → Restrict key to selected APIs
   - Habilitar **APENAS** "Safe Browsing API"

**Como Gerar SHA-1 Fingerprint:**

Debug (desenvolvimento local):
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

Release (Play Console - depois):
- Google Play Console → App signing → Certificados → Copiar SHA-1

**Resultado:** Mesmo que a chave seja extraída do APK, ela funcionará APENAS:
- No package `com.capsec.oatguard`
- Com o certificado específico (debug ou release)
- Exclusivamente para Safe Browsing API

### 6.0.5 Certificate Pinning & Network Security Config

**⚠️ IMPORTANTE: Não usar Certificate Pinning fixo com Google**

A seção anterior sugeria pinning SHA-256 estático. **REVERTER esta abordagem.**

Motivo: Google rotaciona certificados e CAs periodicamente. Pin fixo sem backup dinâmico causa:
- Falhas em cascata quando certificado é renovado
- App inteiro fica inoperável até atualização Play Store
- Risco de cascata de falhas no production

**Solução Recomendada:** Confiar nas CAs de sistema padrão + HTTPS estrito

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Google Safe Browsing: confiar em CAs de sistema, HTTPS obrigatório -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">safebrowsing.googleapis.com</domain>
    </domain-config>
    
    <!-- CapSEC website e banners: HTTPS obrigatório -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">www.capsec.com.br</domain>
        <domain includeSubdomains="true">play.google.com</domain>
    </domain-config>
</network-security-config>
```

**Configuração em AndroidManifest.xml:**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:supportsRtl="true">
    <!-- ... demais atributos e componentes ... -->
</application>
```

**Benefícios:**
- ✅ HTTPS forçado em todas as chamadas
- ✅ Compatível com rotação de certificados da Google
- ✅ Seguro contra downgrade attacks
- ✅ Sem risco de cascata de falhas por cert renewal

### 6.0.6 Tratamento de Redirecionamentos & URL Resolution

**⚠️ IMPORTANTE: Phishing via URL Encurtadas**

Golpes via QR code frequentemente usam encurtadores (bit.ly, tinyurl, t.co, shortlinks próprios) ou múltiplos saltos HTTP 301/302.

**Problema:** Se enviar apenas URL encurtada pra Safe Browsing API, ela pode classificar o domínio do encurtador como inofensivo, ignorando o destino final malicioso.

**Solução: Resolver URLs antes de validar**

Fluxo implementado no `SafeBrowsingRepository`:

```kotlin
// 1. Extrair URL do QR
val qrUrl = "https://bit.ly/abc123"

// 2. Resolver redirecionamentos (max 5 saltos)
val finalUrl = resolveRedirects(qrUrl, maxHops = 5)
// Resultado: "https://site-malicioso.com/phishing"

// 3. Validar URL final contra Safe Browsing API
val result = safeBrowsingService.checkThreat(finalUrl)
```

**Implementação Técnica:**

```kotlin
private suspend fun resolveRedirects(
    url: String,
    maxHops: Int = 5,
    currentHop: Int = 0
): String {
    if (currentHop >= maxHops) {
        return url // Limite atingido, retornar URL atual
    }
    
    return try {
        val request = Request.Builder()
            .url(url)
            .head() // Apenas headers, sem body
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        // Seguir redirect?
        if (response.code in 301..302) {
            val redirectUrl = response.header("Location")
            if (redirectUrl != null) {
                return resolveRedirects(redirectUrl, maxHops, currentHop + 1)
            }
        }
        
        // Sem redirect, retornar URL
        response.request.url.toString()
        
    } catch (e: Exception) {
        // Timeout ou erro de rede: retornar URL original
        url
    }
}
```

**Restrições de Segurança:**
- Máximo 5 saltos (evitar loops/ataques)
- Timeout de 5 segundos por request HEAD
- Validar que cada redirect tem Location válido
- Apenas seguir HTTP/HTTPS (não file://, data://, etc)
- Após resolver, enviar URL FINAL pra Safe Browsing API

**Casos de Borda:**
- URL não redireciona: usar original
- Timeout em algum hop: usar última URL válida
- Loop detectado: usar última URL única
- Mais de 5 hops: usar URL no hop 5

### 6.0.7 Conformidade com Políticas da Google (OBRIGATÓRIO)

**⚠️ CRÍTICO: Atribuição Legal & Linguagem Qualificada**

Para evitar suspensão de chave de API e rejeição na Play Store, a documentação exige:

#### 1. Atribuição da Google (Mandatory Attribution)

Incluir crédito legal em:
- **Na tela de resultado:** Um texto pequeno no footer
- **Na seção Sobre do app:** Menção explicativa
- **Na documentação de privacidade:** Link para Google Safe Browsing Policy

Exemplo de texto a incluir:
```
"Proteção contra malware fornecida por Google.
Advisory provided by Google Safe Browsing.
Saiba mais: https://www.google.com/safebrowsing/"
```

#### 2. Linguagem Qualificada (Não-Absoluta)

Safe Browsing baseia-se em telemetria e listas de ameaças conhecidas. Google exige:
- **NÃO dizer:** "100% seguro", "garantidamente seguro", "sem ameaças"
- **USAR:** "Nenhuma ameaça conhecida detectada", "Sem registro de problema"
- **NÃO dizer:** "Perigoso", "Malwares confirmados"
- **USAR:** "Possível risco", "Suspeito", "Potencialmente perigoso"

**Exemplos de Resultados (Linguagem Corrigida):**

```
Score 100 (Verde):
✅ "Nenhuma ameaça conhecida detectada"
(não: "100% seguro")

Score 45 (Amarelo):
⚠️ "Possível risco detectado"
(não: "Perigoso confirmado")

Score 20 (Vermelho):
🚫 "Risco potencial detectado"
(não: "Certamente malwares")
```

#### 3. Isenção de Responsabilidade (Disclaimer)

Obrigatório na documentação/privacidade:
```
"O OatGuard fornece análise de segurança baseada em dados 
de ameaças conhecidas pela Google Safe Browsing. A análise 
é informativa e não constitui garantia absoluta de segurança. 
Os usuários devem exercer julgamento crítico ao abrir links."
```

#### 4. Conformidade Contínua

- Google pode revisar a chave e exigir ajustes a qualquer momento
- Manter documentação atualizada com termos de uso
- Responder rapidamente a avisos ou suspensões
- Não revender ou compartilhar dados da API

### 6.0.8 Segurança de Rede (NetworkSecurityConfig)

**File: res/xml/network_security_config.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Google Safe Browsing API: HTTPS only, pin certificate -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">safebrowsing.googleapis.com</domain>
        <pin-set>
            <pin digest="SHA-256">BASE64_ENCODED_PIN_HERE</pin>
        </pin-set>
    </domain-config>
    
    <!-- Geral: HTTPS only, sem cleartext -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">www.capsec.com.br</domain>
        <domain includeSubdomains="true">play.google.com</domain>
    </domain-config>
</network-security-config>
```

**Referência no AndroidManifest.xml:**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config">
    <!-- ... demais atributos e componentes ... -->
</application>
```

**Justificativa:**
- Força HTTPS mesmo em Android 7+ (que suporta cleartext por padrão)
- Evita Man-in-the-Middle attacks em conexões com API
- Retrocompatível (ignora em versões que já forçam HTTPS)

### 6.1 Pré-requisitos
- Android Studio Jellyfish+ (2024.1+)
- JDK 17+
- Android SDK 35+
- Gradle 8.0+
- Google Cloud Console com projeto CapSEC criado

### 6.1.5 Modelo Financeiro & Viabilidade (GRATUITO)

**Custo Operacional: R$ 0 (ZERO)**

O OatGuard funciona em modelo **100% stateless client-to-Google**:
- Sem servidor backend próprio
- Sem banco de dados remoto
- Sem hosting de infraestrutura
- Sem custos de computação em nuvem (CapSEC)

**Safe Browsing API é Completamente Gratuita:**
- Google não cobra por chamadas à Safe Browsing API v4
- API é pública, voltada pra proteção de utilizadores (não comercial)
- Não há versão paga do Safe Browsing (existe Web Risk API paga, mas é pra uso corporativo diferente)

**Quotas & Limites (Padrão):**
- Cota padrão inicial: ~10.000 requisições/dia
- Limite de taxa: proteção contra picos (rate limiting por minuto)
- Aumento de cota: submeter formulário no Google Cloud (aprovado sem custos, desde que cumpra política de uso)

**Único custo fixo incorrido:**
- Taxa anual da conta de programador Google Play Console (~25 USD/ano, já paga pra OatCall)

**ROI (Retorno sobre Investimento):**
- App funciona como **canal de marketing institucional** para CapSEC
- Visibilidade em restaurantes, eventos, locais públicos
- Banners integrados (CapSEC + OatCall) canalizam usuários pra leads corporativos
- Sem gasto em publicidade paga (organic reach via download)
- **Conversão potencial:** utilização prática → confiança de marca → leads de consultoria

### 6.2 Setup da Google Safe Browsing API no Google Cloud (CRÍTICO)

**Passo 0: Setup Inicial no Google Cloud Console**

1. Ir para [Google Cloud Console](https://console.cloud.google.com/)
2. Criar novo projeto:
   - Nome: "capsec-oatguard" (ou similar)
   - ID do projeto será auto-gerado
3. Ativar Safe Browsing API:
   - Menu lateral: APIs & Services → Library
   - Pesquisar "Safe Browsing API"
   - Clicar em "Safe Browsing API v4"
   - Clicar em "Enable"
4. Verificar quotas (opcional):
   - APIs & Services → Quotas
   - Procurar "Safe Browsing API"
   - Ver limite padrão (~10.000 req/dia)

**Passo 1: Gerar API Key com Restrições**

1. Google Cloud Console → APIs & Services → Credentials
2. Clicar em "+ Create Credentials" → "API Key"
3. Copiar a chave gerada (guardar com segurança)
4. **Nomear:** "OatGuard Safe Browsing Key"
5. Clicar em "Restrict Key"
5. **Application Restrictions:**
   - Tipo: Android apps
   - Package name: `com.capsec.oatguard`
   - SHA-1 Fingerprint de debug (ver comando abaixo)
   - **IMPORTANTE:** Adicionar SHA-1 de release DEPOIS (quando certificado for gerado no Play Console)
6. **API Restrictions:**
   - Custom → Restrict to APIs
   - Selecionar APENAS "Safe Browsing API"
7. Salvar e copiar a chave

**Gerar SHA-1 Fingerprint (Debug):**
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android | grep "SHA1"
```

**Passo 2: Adicionar API Key ao Projeto (Protegida)**

⚠️ **CRÍTICO:** A chave será compilada no APK (descompilável), mas protegida por restrições criptográficas.

### Timeline de Desenvolvimento

| Fase | Semana | Chave | Ação |
|------|--------|-------|------|
| **Desenvolvimento (Claude Code)** | Semana 1 | `""` (vazio/placeholder) | Mock SafeBrowsingService, testa UI/UX sem API real |
| **Testes com Hardware** | Semana 2 | Chave real Google Cloud | Ativa chave real, testa com device/emulador |
| **Play Store Release** | Semana 3 | Chave + SHA-1 release | Adiciona SHA-1 de release, publica |

**Fluxo prático:**
- Semana 1: `SAFE_BROWSING_API_KEY=""` (vazio) → SafeBrowsingService retorna mock scores
- Semana 2: `SAFE_BROWSING_API_KEY="AIzaSyD..."` → recompila, testa com Google de verdade
- Nenhuma mudança no código — apenas atualiza `local.properties`

---

1. Adicionar ao `local.properties` (NÃO commitar ao Git):
   ```
   SAFE_BROWSING_API_KEY=AIzaSyD...sua_chave_aqui
   ```

2. Adicionar ao `.gitignore`:
   ```
   local.properties
   ```

3. Adicionar ao `build.gradle.kts` (método robusto):
   ```kotlin
   android {
       buildFeatures {
           buildConfig = true
       }
       
       defaultConfig {
           // Ler da local.properties
           val apiKey = project.rootProject.file("local.properties").let { file ->
               if (file.exists()) {
                   val properties = java.util.Properties().apply { 
                       load(file.inputStream()) 
                   }
                   properties.getProperty("SAFE_BROWSING_API_KEY", "")
               } else ""
           }
           
           buildConfigField("String", "SAFE_BROWSING_API_KEY", "\"$apiKey\"")
       }
   }
   ```

4. Usar no código Kotlin:
   ```kotlin
   // No SafeBrowsingService (Retrofit)
   val apiKey = BuildConfig.SAFE_BROWSING_API_KEY
   val response = safeBrowsingService.checkThreat(apiKey, request)
   ```

### Passo 2.5: Protecção Criptográfica (Crucial)

**Como a chave fica segura mesmo compilada no APK:**

1. **No Google Cloud Console:** Ao configurar a chave:
   - Restrição: "Android apps"
   - Package Name: `com.capsec.oatguard`
   - SHA-1 Fingerprint: Certificado de assinatura (veja passo abaixo)

2. **Quando o app faz requisição:** o app precisa enviar os headers:
   - `X-Android-Package: com.capsec.oatguard`
   - `X-Android-Cert: <SHA-1 do certificado, hex maiúsculo sem ":">`

   ⚠️ **Correção (2026-09-28):** o Android **não** envia esses headers automaticamente em chamadas REST (OkHttp/Retrofit). Só as bibliotecas do Google Play Services fazem isso. O OatGuard os adiciona via `AndroidAppIdentityInterceptor`, que calcula o SHA-1 em runtime a partir do certificado que assinou o APK instalado (debug.keystore em dev, Play App Signing em release). Nenhum código muda entre os dois.

3. **Google valida:** Se os headers estiverem ausentes ou o pacote/certificado não corresponderem, retorna `403 Forbidden`

**Resultado prático (verificado com curl em 2026-09-28):**
- ✅ App chama a API com a chave + headers → `200`
- ❌ Chave usada sem os headers → `403 Forbidden`
- ⚠️ **Limitação:** a restrição **não é criptográfica**. Os headers são texto e o SHA-1 do certificado é público (extraível de qualquer APK). Quem extrair a chave do APK *e* enviar os headers corretos consegue usá-la. A restrição é uma barreira contra uso casual, não uma garantia.

**Mitigações que limitam o impacto de abuso:**
- API restriction: chave só funciona na Safe Browsing API (sem acesso a APIs pagas)
- Safe Browsing é gratuita: abuso consome cota, não gera custo
- Monitorar uso em Google Cloud Console → APIs & Services → Safe Browsing → Metrics; se houver abuso, rotacionar (Passo 4)
- Opcional: definir um limite de cota por minuto/dia abaixo do padrão, para que abuso não esgote a cota dos usuários reais

### Passo 3: Cadastrar SHA-1 Fingerprints (OBRIGATÓRIO)

**Debug (Desenvolvimento Local):**
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android | grep SHA1
```
Copiar o SHA-1 e adicionar ao Google Cloud Console → API Key → Application Restrictions

**Release (Play Console - Depois):**
1. Google Play Console → App signing
2. Ir em "Certificados" → "App Signing Key"
3. Copiar "SHA-1 certificate fingerprint"
4. Voltar a Google Cloud Console → API Key → Application Restrictions
5. Adicionar este SHA-1 (coexistindo com o SHA-1 debug)

### Passo 4: Rotação de Chaves (Plano de Segurança)

Em apps client-side, rotação é **rara** (não é web backend), mas importante ter um plano:

**Quando Rotar:**
- Incidente: configuração de restrição falha (permite abuso)
- Compliance: política interna de rotação periódica (ex: anual)
- **Não é necessário:** simplesmente por estar compilada no APK

**Como Rotar (Sem quebrar usuários):**

1. **Google Cloud Console:** Criar segunda chave
   - Nome: `OatGuard_Key_v2`
   - Aplicar exatamente as mesmas restrições (Package Name + SHA-1s)

2. **Update do App:**
   - Atualizar `local.properties`: `SAFE_BROWSING_API_KEY=AIzaSyD...v2`
   - Compilar nova versão (ex: v1.1.0)
   - Upload na Play Store

3. **Grace Period (3-6 meses):**
   - Manter AMBAS as chaves ativas no Google Cloud
   - Usuários atualizam o app gradualmente

4. **Desativação:**
   - Monitorar métricas de tráfego na chave v1
   - Quando volume cair a ~0, deletar a chave v1 no Google Cloud

**Benefício:** Usuários com versão antiga continuam funcionando durante a transição

### 6.3 Build
```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew bundleRelease
```

### 6.4 Run
```bash
./gradlew installDebug
./gradlew :app:connectedAndroidTest
```

---

## 7. Notas de Implementação

### 7.1 ML Kit Vision + CameraX
- Use `com.google.mlkit.vision.barcode.BarcodeScanning.getClient()`
- Integre CameraX para captura de vídeo
- Processe frame-by-frame
- Se múltiplas barcodes: use primeira ou maior

### 7.2 URL Validation
- Regex básico: `^https?://[^\s/$.?#].[^\s]*$`
- Tratamento: se inválido, mostra erro
- Se válido: dispara API

### 7.3 Threading
- Requisição API: Coroutine + Retrofit (automático com suspend fun)
- UI Updates: LaunchedEffect + State
- Sem bloquear thread principal

### 7.4 Paleta de Cores

**Brand Colors (Oat/CapSEC):**
- **Navy Escuro:** `#001F3F` ou `Color(0xFF001F3F)` — Cor primária (header, botões)
- **Azul Claro:** `#2196F3` ou `Color(0xFF2196F3)` — Cor secundária (destaques)
- **Branco:** `#FFFFFF` — Fundo principal

**Score Safety Colors:**
- **Verde (Seguro):** `#4CAF50` ou `Color(0xFF4CAF50)` — Score 75-100
- **Amarelo (Suspeito):** `#FFC107` ou `Color(0xFFFFC107)` — Score 40-74
- **Vermelho (Perigoso):** `#F44336` ou `Color(0xFFF44336)` — Score 0-39

**Aplicação:**
- Header background: Navy Escuro
- Botões: Navy Escuro + Azul Claro (ripple)
- Banners: Azul Claro background com texto Navy
- Score circle: Verde/Amarelo/Vermelho conforme resultado

### 7.5 Tratamento de Edge Cases
- QR não é URL: mensagem de erro, volta para Home
- Timeout API (>10s): erro genérico, retry button
- URL vazia: mensagem "Código inválido"
- Sem permissão câmera: aviso + request novamente
- Offline: "Sem conexão, verifique sua internet"

### 7.6 Navegação
- **Splash → Home:** Após 2s (LaunchedEffect com delay)
- **Home → QRScanner:** NavController.navigate("scanner")
- **Scanner → Result:** NavController.navigate("result") — **implementado sem argumento na rota** (`result?score=X` original foi descartado). O `QRValidatorViewModel` é escopado no NavHost (compartilhado entre Scanner e Result) e a URL/resultado trafegam pelo `StateFlow<UiState>` dele, evitando problemas de encoding com URLs arbitrárias contendo `?`, `&`, etc.
- **Result → Home:** navigate("home") com `popUpTo(home)` (limpa Scanner+Result da pilha) + `viewModel.reset()`

### 7.7 Banners Clicáveis
- Use `Intent(Intent.ACTION_VIEW, Uri.parse(url))`
- CapSEC: `https://www.capsec.com.br`
- OatCall: `https://play.google.com/store/apps/details?id=com.capsec.oatcall`

---

## 8. Checklist Final de Implementação

### Core Features
- [ ] Splash screen (logo Oat, 2s)
- [ ] Home screen (header com InfoButton + botão + 2 banners)
- [ ] QR scanner (CameraX + ML Kit)
- [ ] Google Safe Browsing API client (Retrofit)
- [ ] Result screen (header com InfoButton + score + explicação + botões + banners)
- [ ] Botão "Abrir Link" (Result screen, abre URL no navegador via Intent)
- [ ] InfoButton (ícone ℹ️ → abre URL privacidade/documentação)
- [ ] Error handling (todas as telas)
- [ ] Navigation (Splash → Home → Scanner → Result → Home)
- [ ] URL validation (regex)
- [ ] Permission requests (câmera + internet)
- [ ] Theme (colors + typography)
- [ ] Suporte a 7 idiomas (strings.xml traduzidas)
- [ ] Constants.kt com URLs e configurações

### Segurança & Conformidade Play Store
- [ ] NetworkSecurityConfig (HTTPS only)
- [ ] 64-bit ABI support (arm64-v8a)
- [ ] targetSdkVersion = 35
- [ ] minSdkVersion = 24
- [ ] compileSdkVersion = 35
- [ ] Privacy Policy URL definida
- [ ] Data Safety declarada (SafeBrowsing API)
- [ ] App Signing via Play Console
- [ ] Sem APIs deprecadas (compileSdk 35)

### Testes de Compatibilidade
- [ ] Teste em Android 7.0 (API 24) — minSdk
- [ ] Teste em Android 10 (API 29) — mid-range
- [ ] Teste em Android 15 (API 35) — target/latest
- [ ] Teste com e sem conexão (error handling)
- [ ] Teste de QR inválido/vazio
- [ ] Teste de timeout na API
- [ ] Teste de permissões (câmera negada)
- [ ] Teste de múltiplos idiomas (ao menos 2)

### Build & Release
- [ ] Debug APK build
- [ ] Release Bundle (AAB) build
- [ ] Play Console internal testing
- [ ] Play Console beta testing (opcional)
- [ ] Screenshots para Play Store (mín. 2 idiomas)
- [ ] Descrição da loja (título, short desc, full desc)

---

## 9. Referências

- [Google Safe Browsing API v4](https://developers.google.com/safe-browsing/v4)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning/android)
- [CameraX Guide](https://developer.android.com/training/camerax)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit Documentation](https://square.github.io/retrofit/)

---

## 10. Registro de Implementação — Semana 1 (Claude Code, 2026-09-27)

Todos os itens do "Core Features" e boa parte de "Segurança & Conformidade" do checklist da seção 8 foram implementados e o build debug foi verificado (`./gradlew :app:assembleDebug` → `BUILD SUCCESSFUL`, `app-debug.apk` gerado). Detalhes:

### 10.1 O que foi entregue
- Todas as 4 telas (Splash, Home, QRScanner, Result) + `Navigation.kt` (NavHost) + tema com as cores de marca.
- Scanner CameraX + ML Kit com tratamento de permissão de câmera negada (tela de fallback com botão pra Configurações).
- Camada de dados completa: `SafeBrowsingRequest`/`Response`, `SafeBrowsingService` (Retrofit), `SafeBrowsingClient`, `URLResolver` (5 hops, timeout 5s, HEAD request, `followRedirects(false)` manual pra loop detection).
- `SafeBrowsingRepository`: interface com **duas implementações** — `RealSafeBrowsingRepository` (produção) e `MockSafeBrowsingRepository` (Semana 1) — selecionadas automaticamente por `SafeBrowsingRepositoryProvider` com base em `BuildConfig.SAFE_BROWSING_API_KEY.isBlank()`. Isso não estava detalhado na v1.0 desta especificação (que só previa "SafeBrowsingService Mock" no prompt) — foi uma decisão de arquitetura pra deixar a troca Semana 1 → Semana 2 zero-code-change, conforme timeline da seção 6.2.
- `QRValidatorViewModel` com `UiState` (Idle/Loading/Success/Error), escopado no NavHost e compartilhado entre Scanner e Result (ver desvio da seção 7.6).
- `network_security_config.xml` conforme seção 6.0.5 (HTTPS only, sem pinning fixo — a versão com `<pin-set>` da seção 6.0.8, que o próprio doc já classificava como não recomendada na 6.0.5, não foi usada).
- `strings.xml` completo nos 7 idiomas (pt/en/es/fr/ru/hi/ar), com linguagem qualificada nas mensagens de score.

### 10.2 Mock heurístico (Semana 1 — decisão tomada com o usuário)
Com `SAFE_BROWSING_API_KEY` vazia, `MockSafeBrowsingRepository` não faz chamada de rede real. Decide o score por palavra-chave na URL escaneada:
- URL contém `malware` → MALWARE (score 20)
- URL contém `phishing` → SOCIAL_ENGINEERING (score 30)
- URL contém `unwanted` → UNWANTED_SOFTWARE (score 45)
- URL contém `harmful` → POTENTIALLY_HARMFUL_APPLICATION (score 50)
- Qualquer outra URL real → seguro (score 100)

Isso permite ao QA testar todos os estados visuais da ResultScreen gerando QR codes com essas palavras-chave na URL, sem depender da API real. `URLResolver` não é usado no caminho mock (usa a URL bruta), pois o mock não faz I/O de rede.

### 10.3 Desvios em relação à v1.0 desta especificação
1. **compileSdk/targetSdk 35 → 37** (seção 3.1) — o scaffold do Android Studio já veio com 37; decisão tomada com o usuário de manter a versão atual em vez de rebaixar.
2. **Versões de dependências atualizadas** (seção 3.3) — CameraX saiu de beta (1.4.1 estável), Gson 2.10.1→2.11.0, além de `navigation-compose`, `lifecycle-viewmodel/runtime-compose` e `material-icons-core`, que a v1.0 não listava.
3. **Navegação Scanner→Result sem argumento na rota** (seção 7.6) — ViewModel compartilhado no NavHost em vez de `result?score=X`.
4. **Bug corrigido durante a implementação:** o banner "Conheça o OatCall" estava apontando pra `www.capsec.com.br/oatcall` em vez da URL da Play Store especificada nas seções 2.2 e 7.7 (`https://play.google.com/store/apps/details?id=com.capsec.oatcall`). Corrigido em `Constants.kt` antes do build final.
5. **`MyApp.kt` (Application class) não foi criada** — não havia necessidade de estado/inicialização global na Semana 1 (sem DI framework, sem SDKs que exigem `Application.onCreate()`).

### 10.4 Pendências reais para Semana 2 e 3 (nada bloqueado no código)
- Criar projeto "capsec-oatguard" no Google Cloud Console + habilitar Safe Browsing API v4 (seção 6.2, Passo 0).
- Gerar API Key com restrição por Package Name + SHA-1 debug + API restriction (seção 6.2, Passo 1).
- Preencher `SAFE_BROWSING_API_KEY` em `local.properties` — o app já troca automaticamente pra `RealSafeBrowsingRepository`. ⚠️ Na prática foi necessária uma mudança de código (headers Android — ver seção 11).
- Testar em device/emulador físico com a API real (câmera real, permissões em runtime, redirecionamentos reais via `URLResolver`).
- Semana 3: SHA-1 de release (Play Console), submissão à Play Store, publicação do repositório no GitHub (MIT License).
- Ainda não testado neste ambiente: fluxo de câmera real (sem emulador/dispositivo conectado durante a implementação) — apenas o build de compilação foi verificado.

> ✅ Itens de Google Cloud + chave concluídos em 2026-09-28 — ver seção 11.

---

## 11. Registro de Implementação — Semana 2, Fase 1: API Real (Claude Code, 2026-09-28)

Executado conforme `oatguard-prompt-api-real.md`. Build verificado: `./gradlew clean assembleDebug` → `BUILD SUCCESSFUL` (único warning: `@OptIn(ExperimentalGetImage)` em `QRScannerScreen.kt`, preexistente e inofensivo).

### 11.1 Estado encontrado
A maior parte do prompt já estava implementada na Semana 1: `RealSafeBrowsingRepository`, request/response models, `SafeBrowsingService`, `URLResolver` e a troca automática Mock→Real via `SafeBrowsingRepositoryProvider`. A arquitetura existente foi mantida em vez de aplicar os snippets do prompt:
- **Textos via string resources** (`ThreatScoreMapper`) em vez de mensagens PT hardcoded: mantém os 7 idiomas.
- **Erro de rede/API → tela de erro** (`UiState.Error`) em vez de um "score 50 amarelo": exibir score quando não houve validação seria enganoso (seção 6.0.7).
- **`ANY_PLATFORM`** em vez de `ANDROID` (seção 5.1).

### 11.2 Problemas encontrados e corrigidos
1. **403 garantido na API real.** A premissa de que "o Android envia `X-Android-Package`/`X-Android-Cert` automaticamente" era falsa para chamadas OkHttp. Novo arquivo `data/api/AndroidAppIdentityInterceptor.kt`:
   - `X-Android-Package` = `context.packageName`
   - `X-Android-Cert` = SHA-1 (hex maiúsculo sem ":") do certificado de assinatura, lido via `PackageManager` (`GET_SIGNING_CERTIFICATES` no API 28+ com suporte a key rotation; `GET_SIGNATURES` no API 24–27)
2. **SHA-1 debug com erro de digitação na documentação.** O diário registrava 41 caracteres (`…9441992EF…`). O valor real, conferido com `keytool`, é `89358E7C17384F914E9F944192EFC9A28AE17A64`. O valor cadastrado no Google Cloud estava correto.

### 11.3 Mudanças de código
- `SafeBrowsingClient`: de `object` com `service` lazy → `create(context)`, que adiciona o interceptor.
- `SafeBrowsingRepositoryProvider.create(context)`; `RealSafeBrowsingRepository` recebe o `service` por parâmetro.
- `QRValidatorViewModel`: criado via `QRValidatorViewModel.Factory` (`viewModelFactory` + `APPLICATION_KEY`); `Navigation.kt` usa `viewModel(factory = QRValidatorViewModel.Factory)`. Ainda sem Application class própria.
- Log de exceções de rede/API só em build debug (`Log.w`, tag `QRValidatorViewModel`), para diagnosticar 403/timeout nos testes em hardware.

### 11.4 Verificação contra a API real (curl, mesmos headers do app)
| URL | Resultado |
|---|---|
| `https://www.google.com/` | `200` — `{}` (nenhuma ameaça conhecida) |
| `http://malware.testing.google.test/testing/malware/` | `200` — `MALWARE` |
| `http://testsafebrowsing.appspot.com/s/phishing.html` | `200` — `SOCIAL_ENGINEERING` |
| Qualquer URL **sem** headers Android | `403` |

### 11.5 Limpeza de avisos da IDE (2026-09-28)
Avisos apontados pela inspeção do Android Studio. Nenhum afetava o build.

| Arquivo | Aviso | Correção |
|---|---|---|
| `SafeBrowsingRepository.kt:61` | "Legacy Long overload can be converted to Duration" | `delay(MOCK_LATENCY_MS)` com `800L` → `delay(MOCK_LATENCY)` com `800.milliseconds` (`kotlin.time.Duration`). Mesmo comportamento. |
| `oatguard-especificacao.md` (seção 3.5) | "'if'/'else' has empty body" | Exemplo genérico com `if`/`else` só com comentários trocado pelo trecho real do `AndroidAppIdentityInterceptor` (API 28+ vs. API 24–27). |
| `oatguard-especificacao.md` (seções 3.6, 6.0.5, 6.0.8) | "Tag start is not closed" | Exemplos de `AndroidManifest.xml` terminavam em `... />` (XML inválido). Agora fecham com `</application>` e usam `<!-- ... demais atributos e componentes ... -->`. |

Por que blocos de Markdown geram erros: o Android Studio analisa os blocos de código (```` ```kotlin ````, ```` ```xml ````) como código de verdade. Regra para este documento: exemplos devem ser sintaticamente válidos, com reticências só dentro de comentários.

Aviso restante, conhecido e inofensivo: `QRScannerScreen.kt:242`. `@OptIn(ExperimentalGetImage)` não tem efeito porque a anotação do CameraX não usa `@RequiresOptIn`.

Build após as correções: `./gradlew assembleDebug` → `BUILD SUCCESSFUL`.

### 11.6 Pendências
- Testes em device/emulador (câmera real + API real). Usar QR codes com as URLs de teste da tabela 11.4.
- Testar `URLResolver` com encurtador real (bit.ly/tinyurl).
- Semana 3: adicionar SHA-1 do Play App Signing no Google Cloud. O interceptor já envia o SHA-1 correto em release, sem mudança de código.
