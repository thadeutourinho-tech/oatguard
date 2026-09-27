# OatGuard — Prompt para Claude Code

**Versão:** 1.0  
**Data:** 2026-09-26  

---

## 📌 INSTRUÇÃO PRINCIPAL

Você é um desenvolvedor Android experiente. Seu objetivo é implementar o **OatGuard**, um app Android de validação de QR codes usando Google Safe Browsing API.

O projeto tem especificação técnica completa anexada. Seu trabalho é:
1. Criar a estrutura Kotlin + Jetpack Compose
2. Implementar todas as screens na ordem correta
3. Integrar Google Safe Browsing API **com segurança crítica**
4. Garantir retrocompatibilidade (minSdk 24) + conformidade Play Store (targetSdk 35)

⚠️ **CRÍTICO:** Leia a seção "Segurança da Chave de API" e "Tratamento de Redirecionamentos" da especificação ANTES de codar.

---

## 📋 RESUMO EXECUTIVO

**O que é OatGuard:**
- App Android B2C gratuito que valida QR codes
- Usuário aponta câmera → escaneia QR → verifica segurança via Google Safe Browsing API → retorna score (0-100) com cores e descrição
- Ferramenta de marketing/educação para CapSEC
- Parte do ecossistema "Oat" (junto com OatCall)

**Stack:**
- Kotlin + Jetpack Compose (UI moderna)
- ML Kit Vision (QR scanning)
- CameraX (câmera)
- Retrofit (HTTP client)
- Google Safe Browsing API v4

**Requisitos Críticos:**
- minSdk: 24 (Android 7.0) | targetSdk: 35 (Android 15)
- 7 idiomas: PT, EN, ES, FR, RU, HI, AR
- NetworkSecurityConfig (HTTPS only)
- Sem banco de dados, sem cadastros, stateless
- Play Store ready (64-bit, Privacy Policy, Data Safety)

---

## 🎯 FLUXO DE TELAS

```
SplashScreen (2s, logo Oat)
    ↓
HomeScreen
  • Header: Logo Oat + "OatGuard" + InfoButton (ℹ️)
  • Botão grande: "Verificar se QR Code é Seguro"
  • 2 Banners clicáveis (CapSEC + OatCall)
    ↓ (ao clicar botão)
QRScannerScreen
  • CameraX + ML Kit barcode scanning
  • Captura automática
    ↓ (QR lido → URL extraído)
ResultScreen
  • Header: Logo Oat + "OatGuard" + InfoButton
  • Círculo grande: Score (0-100) com cor (verde/amarelo/vermelho)
  • Breve explicação (ex: "Site Seguro", "Possível Phishing")
  • Descrição do destino (extraída da API)
  • Botões de ação:
    - Botão primário: "Abrir Link" → dispara Intent ACTION_VIEW com URL (navegador padrão)
    - Botão secundário: "Verificar Outro QR Code" → volta para HomeScreen
  • 2 Banners (CapSEC + OatCall)
```

---

## 📐 ESTRUTURA DE PACOTES

```
com.capsec.oatguard/
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── QRScannerScreen.kt
│   │   └── ResultScreen.kt
│   ├── components/
│   │   ├── OatHeader.kt (com InfoButton integrado)
│   │   ├── InfoButton.kt
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
│   │   ├── SafeBrowsingService.kt (Retrofit interface)
│   │   ├── SafeBrowsingClient.kt
│   │   └── models/
│   │       ├── SafeBrowsingRequest.kt
│   │       └── SafeBrowsingResponse.kt
│   ├── network/
│   │   └── URLResolver.kt (resolve redirects até URL final, máx 5 hops)
│   └── repository/
│       └── SafeBrowsingRepository.kt (resolve URL → validate contra API)
├── viewmodel/
│   └── QRValidatorViewModel.kt
├── utils/
│   ├── URLValidator.kt
│   ├── ColorMapper.kt
│   └── Constants.kt
├── MainActivity.kt
└── MyApp.kt (Application class)
```

---

## 🎨 CORES & DESIGN

**Brand Colors:**
- Navy Escuro: `#001F3F` (headers, botões)
- Azul Claro: `#2196F3` (destaques, banners)
- Branco: `#FFFFFF` (fundo)

**Score Colors:**
- Verde: `#4CAF50` (75-100, safe)
- Amarelo: `#FFC107` (40-74, suspicious)
- Vermelho: `#F44336` (0-39, dangerous)

---

## 🔐 SEGURANÇA CRÍTICA (LEIA ANTES DE CODAR)

### 1. Proteção da Chave de API (Google Safe Browsing)

**NUNCA hardcodificar a chave no código.** APK é público (descompilável), MAS chave é protegida por restrições Google.

**Fluxo Correto:**

```kotlin
// 1. local.properties (NÃO COMMITAR AO GIT):
SAFE_BROWSING_API_KEY=AIzaSyD...sua_chave_aqui

// 2. .gitignore:
local.properties

// 3. build.gradle.kts (método robusto):
android {
    buildFeatures {
        buildConfig = true
    }
    
    defaultConfig {
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

// 4. Usar via BuildConfig (Retrofit):
val apiKey = BuildConfig.SAFE_BROWSING_API_KEY
val response = safeBrowsingService.checkThreat(apiKey, request)
```

**Por que é seguro mesmo compilada no APK:**

1. **Google Cloud Console:** Restrições por Package Name + SHA-1 fingerprint
2. **Android valida:** Envia `X-Android-Package` e `X-Android-Cert` nas requisições
3. **Resultado:** Terceiros que copiem a chave recebem `403 Forbidden` (sem certificado correto)
4. **Dois SHA-1s:** Um para debug (desenvolvimento), outro para release (Play Console)

**Google Cloud Console (OBRIGATÓRIO):**
- API Key → Restrict Key
  - Android apps only
  - Package: com.capsec.oatguard
  - SHA-1 fingerprint: (debug + release depois)
  - API restrictions: Safe Browsing API ONLY

### 2. URL Resolution (Antes de Validar)

Phishing usa encurtadores (bit.ly, tinyurl) e redirecionamentos. Resolver ANTES de enviar pra API.

**Implementar URLResolver:**
- HEAD request pra seguir Location header
- Máximo 5 hops (evitar loops)
- Timeout 5s por request
- Validar apenas HTTP/HTTPS
- Enviar URL FINAL pra Safe Browsing API

```kotlin
// Fluxo:
val qrUrl = "https://bit.ly/malicioso"
val finalUrl = URLResolver.resolve(qrUrl) // → "https://phishing.com"
val result = safeBrowsingService.checkThreat(finalUrl)
```

### 3. NetworkSecurityConfig

**NÃO usar Certificate Pinning fixo com Google.** Causa falhas quando certificado é renovado.

Solução: HTTPS obrigatório + confiar em CAs de sistema.

### 4. Conformidade com Google (OBRIGATÓRIO)

**Atribuição Legal (Mandatory):**
```
Incluir em:
- Footer da tela de resultado
- Seção "Sobre" do app
- Documentação/Privacidade

Texto exemplo:
"Proteção contra malware fornecida por Google Safe Browsing.
Saiba mais: https://www.google.com/safebrowsing/"
```

**Linguagem Qualificada (NÃO ABSOLUTA):**

Safe Browsing usa telemetria e listas conhecidas. Usar:
- ✅ "Nenhuma ameaça conhecida detectada" (não: "100% seguro")
- ✅ "Possível risco detectado" (não: "Perigoso confirmado")
- ✅ "Potencialmente perigoso" (não: "Certamente malware")

Vide seção 6.0.7 da especificação para detalhes completos.

---

## 🔧 CONFIGURAÇÃO TÉCNICA OBRIGATÓRIA

### build.gradle.kts
```kotlin
android {
    namespace = "com.capsec.oatguard"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.capsec.oatguard"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

### AndroidManifest.xml (crítico)
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:supportsRtl="true"
    ... />
```

### NetworkSecurityConfig (res/xml/network_security_config.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">safebrowsing.googleapis.com</domain>
        <domain includeSubdomains="true">www.capsec.com.br</domain>
        <domain includeSubdomains="true">play.google.com</domain>
    </domain-config>
</network-security-config>
```

---

## 📦 DEPENDÊNCIAS (build.gradle.kts)

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    
    // Compose
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // ML Kit Vision (QR Code)
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // CameraX
    implementation("androidx.camera:camera-core:1.4.0-beta02")
    implementation("androidx.camera:camera-camera2:1.4.0-beta02")
    implementation("androidx.camera:camera-lifecycle:1.4.0-beta02")
    implementation("androidx.camera:camera-view:1.4.0-beta02")
    
    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}
```

---

## 🔐 GOOGLE SAFE BROWSING API

### Integração
- **Endpoint:** `POST https://safebrowsing.googleapis.com/v4/threatMatches:find?key={API_KEY}`
- **Request body:** Validar URL contra threat types (MALWARE, SOCIAL_ENGINEERING, UNWANTED_SOFTWARE, POTENTIALLY_HARMFUL_APPLICATION)
- **Response:** Array de matches (vazio = seguro)

### Score Mapping
- **Seguro (matches vazio):** Score 100, Verde, "Site Seguro"
- **MALWARE:** Score 20, Vermelho, "Link com Malware Detectado"
- **SOCIAL_ENGINEERING:** Score 30, Vermelho, "Possível Phishing"
- **UNWANTED_SOFTWARE:** Score 45, Amarelo, "Software Suspeito"
- **POTENTIALLY_HARMFUL_APPLICATION:** Score 50, Amarelo, "Aplicação Potencialmente Perigosa"

### SafeBrowsingService (Retrofit)
```kotlin
interface SafeBrowsingService {
    @POST("v4/threatMatches:find")
    suspend fun checkThreat(
        @Query("key") apiKey: String,
        @Body request: SafeBrowsingRequest
    ): SafeBrowsingResponse
}
```

---

## 🌐 SUPORTE A 7 IDIOMAS

Criar `res/values-XX/strings.xml` para:
- `values/` → Português (padrão)
- `values-en/` → English
- `values-es/` → Español
- `values-fr/` → Français
- `values-ru/` → Русский
- `values-hi/` → हिंदी
- `values-ar/` → العربية (suporte RTL automático)

**Strings a traduzir (com linguagem qualificada - obrigatório):**
```
app_name = "OatGuard"
btn_scan_qr = "Verificar se QR Code é Seguro"
btn_scan_another = "Verificar Outro QR Code"
btn_know_capsec = "Conheça a CapSEC"
btn_know_oatcall = "Conheça o OatCall"
error_no_url = "QR code não contém um link válido"
error_no_internet = "Sem conexão com a internet"
error_api_unavailable = "Não conseguimos validar o link agora. Tente novamente"

// SCORE MESSAGES (Linguagem Qualificada - Conformidade Google)
score_safe = "Nenhuma ameaça conhecida detectada"
score_phishing = "Possível ataque de phishing"
score_malware = "Risco potencial de malware detectado"
score_suspicious = "Possível software suspeito"
score_harmful_app = "Aplicação potencialmente perigosa"

// ATTRIBUTION (Obrigatório - não traduzir marca)
attribution = "Proteção contra malware fornecida por Google Safe Browsing"
learn_more = "Saiba mais sobre Google Safe Browsing"
disclaimer = "O OatGuard fornece análise informativa baseada em ameaças conhecidas. Não constitui garantia absoluta de segurança."
```

---

## 🎬 IMPLEMENTAÇÃO — ORDEM SUGERIDA

### Fase 1: Setup
- [ ] Criar projeto Kotlin + Compose
- [ ] Configurar build.gradle.kts (dependências, compileSdk, targetSdk, minSdk)
- [ ] Configurar AndroidManifest.xml (permissões, supportRtl)
- [ ] Criar NetworkSecurityConfig.xml

### Fase 2: UI Base
- [ ] Theme (Color.kt, Typography.kt)
- [ ] SplashScreen (logo Oat, 2s delay)
- [ ] OatHeader component (com InfoButton)
- [ ] SafetyScoreCard component
- [ ] CapSecBanner + OatCallBanner components
- [ ] InfoButton component
- [ ] Navigation setup (NavController)

### Fase 3: Screens
- [ ] HomeScreen (header + botão + 2 banners)
- [ ] QRScannerScreen (CameraX + ML Kit)
- [ ] ResultScreen (score + descrição + botões + banners)
  - [ ] Botão "Abrir Link" (primário, destaque visual)
  - [ ] Botão "Verificar Outro QR Code" (secundário)

### Fase 4: API & ViewModel
- [ ] Constants.kt (URLs, configurações)
- [ ] SafeBrowsingRequest/Response models
- [ ] SafeBrowsingService (Retrofit interface)
- [ ] **URLResolver.kt** (resolver redirects, máx 5 hops, timeout 5s)
  - [ ] Implementar HEAD request com loop detection
  - [ ] Validar apenas HTTP/HTTPS
  - [ ] Retornar URL final ou original em caso de erro
- [ ] SafeBrowsingRepository (resolve URL → valida contra API)
- [ ] QRValidatorViewModel (gerencia estado)

### Fase 5: Funcionalidades
- [ ] URLValidator.kt (regex validation)
- [ ] ColorMapper.kt (score → cor)
- [ ] Conectar ViewModel às screens
- [ ] Tratamento de erros (offline, timeout, invalid QR)

### Fase 6: Localização & Finais
- [ ] Strings.xml para 7 idiomas
- [ ] Testes em múltiplas versões (API 24, API 29, API 35)
- [ ] Build APK e Bundle
- [ ] Verificação de conformidade Play Store

---

## ⚙️ DETALHES TÉCNICOS IMPORTANTES

### QR Scanner (CameraX + ML Kit)
- Use `CameraProvider` para gerenciar câmera
- `ImageAnalyzer` pra processar frames
- `BarcodeScanner.scan()` pra cada frame
- Feche scanner quando QR for detectado
- Trate permissão negada com graceful fallback

### ViewModel & State Management
```kotlin
class QRValidatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(UIState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun validateQRCode(url: String) {
        // chamada repo.checkUrl(url)
        // atualiza state com resultado
    }
    
    fun reset() {
        // volta pra Idle
    }
}
```

### Tratamento de Erros
- **QR não é URL:** regex validation antes de chamar API
- **Sem conexão:** catch IOException → mostrar mensagem
- **Timeout:** OkHttp timeout + retry logic
- **API erro:** parse SafeBrowsingResponse de erro
- **Câmera negada:** mostrar mensagem + permissão request

### Threading
- Retrofit com suspend fun = coroutine automático
- UI updates via Compose State
- Sem Thread.sleep() ou blocking operations
- LaunchedEffect pra side effects (delay, navegação)

### Intents & Navegação de URLs
- **Padrão geral:** `context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))`
- **Botão "Abrir Link" (Result Screen):**
  ```kotlin
  Button(onClick = {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validatedUrl))
      context.startActivity(intent)
  }) {
      Text("Abrir Link")
  }
  ```
- **Banners (CapSEC + OatCall):**
  - Mesmo padrão com URLs pré-definidas em Constants.kt
- **InfoButton (Privacidade):**
  - Mesmo padrão com URL de documentação
- **Nota:** Sempre envolver em try-catch para casos onde não há navegador instalado (raro em Android)

---

## ✅ CHECKLIST DE ENTREGA

- [ ] Projeto compila sem erros
- [ ] Todas as screens implementadas e navegáveis
- [ ] Google Safe Browsing API integrada
- [ ] QR scanner funcional (detecta e extrai URL)
- [ ] Score calculation correto (0-100)
- [ ] Cores aplicadas conforme score
- [ ] Banners clicáveis abrem URLs corretas
- [ ] InfoButton abre documentação (placeholder URL)
- [ ] 7 idiomas configurados (mínimo 2 testados)
- [ ] Sem permissões negadas (tratado gracefully)
- [ ] Offline detection + mensagem apropriada
- [ ] APK debug buildável
- [ ] Bundle release buildável
- [ ] Testes em API 24, 29, 35 (se possível)
- [ ] NetworkSecurityConfig aplicado (HTTPS only)
- [ ] targetSdk 35, minSdk 24 confirmados

---

## 📝 NOTAS FINAIS

1. **Simplicidade é chave:** Sem BD, sem cache, sem analytics ainda. Stateless.
2. **Segurança:** HTTPS only, TLS 1.2+, Privacy Policy obrigatória
3. **Retrocompatibilidade:** Funciona bem em Android 7.0+, mas com targetSdk 35
4. **Play Store:** Pronto para submit (64-bit, Privacy Policy, Data Safety)
5. **Marca:** Usar ícone "Oat" e logo "CapSEC" conforme fornecidos
6. **URL de Privacidade (i18n):** `https://www.capsec.com.br/oatguard/{lang}/docs.html`
   - Detecção automática de idioma do device
   - Função `Constants.getDocsUrl(language: String)` implementa fallback para PT
   - Idiomas: `pt`, `en`, `es`, `fr`, `ru`, `hi`, `ar`

---

## ⏱️ TIMELINE DE DESENVOLVIMENTO (IMPORTANTE)

### Semana 1: Desenvolvimento (Agora - Claude Code)
- **Google Cloud API Key:** VAZIO (`""` em `local.properties`)
- **SafeBrowsingService:** Mock (retorna scores fake)
- **Foco:** UI/UX completa, navegação, testes sem dependência API real
- **Resultado:** APK debug compilável e testável sem chave real

### Semana 2: Testes com Hardware
- **Google Cloud API Key:** Chave real criada em Google Cloud Console
- **local.properties:** `SAFE_BROWSING_API_KEY="AIzaSyD..."`
- **Build.gradle.kts:** Já suporta leitura de `local.properties`
- **Ação:** Apenas recompila (sem mudanças de código)
- **Testes:** Device/emulador com API real Google

### Semana 3: Play Store Release
- **SHA-1 de Release:** Extraído do Play Console (App signing)
- **Google Cloud:** Adiciona SHA-1 de release ao lado do debug
- **Resultado:** App pronto para publicação

**Vantagem:** Não há bloqueios em Semana 1 — código está pronto para ambas as fases desde o início.

---

## 🔗 ESPECIFICAÇÃO COMPLETA

**Veja o arquivo:** `oatguard-especificacao.md` para detalhes técnicos completos, configurações adicionais, e justificativas arquiteturais.

---

**ESTÁ PRONTO? Comece pelo setup do projeto e implemente as screens na ordem sugerida. Sucesso! 🚀**
