# OatGuard — Integração API Real (Google Safe Browsing v4)

**Objetivo:** Substituir o MockSafeBrowsingRepository pelo RealSafeBrowsingRepository, integrando a Google Safe Browsing API real.

**Status Atual:**
- ✅ MVP com mock API completo (Semana 1)
- ✅ Google Cloud setup concluído (chave gerada, restrições aplicadas)
- ✅ SHA-1 debug registrado no Google Cloud
- ⏳ Aguardando: Integração API real + testes em hardware

---

## 🎯 Tarefas Prioritárias

### 1. Confirmar `local.properties` com Chave Real

**Arquivo:** `local.properties` (raiz do projeto)

```properties
SAFE_BROWSING_API_KEY=AIzaSyD...sua_chave_aqui
```

⚠️ **CRÍTICO:** Certifique-se de que:
- A chave foi copiada do Google Cloud Console
- Não tem espaços extras
- Está na linha exata `SAFE_BROWSING_API_KEY=`
- O arquivo **NÃO é commitado** (já está em .gitignore)

---

### 2. Integração API Real (Substituir Mock)

**Arquivo:** `app/src/main/kotlin/com/capsec/oatguard/data/repository/SafeBrowsingRepository.kt`

**Contexto Atual:**
- MockSafeBrowsingRepository: retorna scores fake por heurística de keyword
- RealSafeBrowsingRepository: deve fazer requisições reais via Retrofit

**O que Fazer:**

#### A. Atualizar `SafeBrowsingService.kt` (Retrofit)

```kotlin
// app/src/main/kotlin/com/capsec/oatguard/data/api/SafeBrowsingService.kt

interface SafeBrowsingService {
    @POST("/v4/threatMatches:find")
    suspend fun checkThreat(
        @Query("key") apiKey: String,
        @Body request: SafeBrowsingRequest
    ): SafeBrowsingResponse
}
```

**Request Model:**
```kotlin
// Já deve estar implementado, validar estrutura:
data class SafeBrowsingRequest(
    val client: ClientInfo,
    val threatInfo: ThreatInfo
)

data class ClientInfo(
    val clientId: String = "com.capsec.oatguard",
    val clientVersion: String = "1.0.0"
)

data class ThreatInfo(
    val threatTypes: List<String>,           // ["MALWARE", "SOCIAL_ENGINEERING", ...]
    val platformTypes: List<String>,         // ["ANDROID"]
    val threatEntries: List<ThreatEntry>
)

data class ThreatEntry(
    val url: String
)
```

**Response Model:**
```kotlin
// Validar se estrutura já está correta:
data class SafeBrowsingResponse(
    val matches: List<ThreatMatch>? = null
)

data class ThreatMatch(
    val threat: ThreatInfo,
    val threatType: String,
    val platformType: String
)
```

#### B. Implementar `RealSafeBrowsingRepository.kt`

```kotlin
// app/src/main/kotlin/com/capsec/oatguard/data/repository/SafeBrowsingRepository.kt

class RealSafeBrowsingRepository(
    private val safeBrowsingService: SafeBrowsingService
) : SafeBrowsingRepository {
    
    override suspend fun validateUrl(url: String): SafetyScore {
        return try {
            // 1. Resolver redirects antes de validar
            val finalUrl = URLResolver.resolve(url)
            
            // 2. Montar request
            val request = buildRequest(finalUrl)
            
            // 3. Chamar API real (chave injetada via BuildConfig)
            val response = safeBrowsingService.checkThreat(
                apiKey = BuildConfig.SAFE_BROWSING_API_KEY,
                request = request
            )
            
            // 4. Processar resposta
            return processThreatResponse(response, finalUrl)
            
        } catch (e: Exception) {
            // Erro de rede/API → score UNKNOWN, mensagem de erro
            SafetyScore(
                score = 50,
                color = Color.YELLOW,
                category = "API_ERROR",
                message = "Erro ao validar URL. Tente novamente.",
                recommendation = "error"
            )
        }
    }
    
    private fun buildRequest(url: String): SafeBrowsingRequest {
        return SafeBrowsingRequest(
            client = ClientInfo(
                clientId = "com.capsec.oatguard",
                clientVersion = "1.0.0"
            ),
            threatInfo = ThreatInfo(
                threatTypes = listOf(
                    "MALWARE",
                    "SOCIAL_ENGINEERING",
                    "UNWANTED_SOFTWARE",
                    "POTENTIALLY_HARMFUL_APPLICATION"
                ),
                platformTypes = listOf("ANDROID"),
                threatEntries = listOf(ThreatEntry(url = url))
            )
        )
    }
    
    private fun processThreatResponse(
        response: SafeBrowsingResponse,
        url: String
    ): SafetyScore {
        // Se matches vazio → seguro
        if (response.matches == null || response.matches.isEmpty()) {
            return SafetyScore(
                score = 100,
                color = Color.GREEN,
                category = "SAFE",
                message = "Nenhuma ameaça conhecida detectada",
                recommendation = "open",
                destination = extrairDominio(url)
            )
        }
        
        // Análise de threats detectados
        val threatTypes = response.matches.map { it.threatType }.toSet()
        
        return when {
            threatTypes.contains("MALWARE") -> SafetyScore(
                score = 20,
                color = Color.RED,
                category = "MALWARE",
                message = "Risco potencial de malware detectado",
                recommendation = "warn",
                destination = extrairDominio(url)
            )
            threatTypes.contains("SOCIAL_ENGINEERING") -> SafetyScore(
                score = 30,
                color = Color.RED,
                category = "PHISHING",
                message = "Possível ataque de phishing detectado",
                recommendation = "warn",
                destination = extrairDominio(url)
            )
            threatTypes.contains("UNWANTED_SOFTWARE") -> SafetyScore(
                score = 45,
                color = Color.YELLOW,
                category = "UNWANTED",
                message = "Software suspeito ou potencialmente indesejado",
                recommendation = "caution",
                destination = extrairDominio(url)
            )
            threatTypes.contains("POTENTIALLY_HARMFUL_APPLICATION") -> SafetyScore(
                score = 50,
                color = Color.YELLOW,
                category = "HARMFUL",
                message = "Aplicação potencialmente perigosa detectada",
                recommendation = "caution",
                destination = extrairDominio(url)
            )
            else -> SafetyScore(
                score = 40,
                color = Color.YELLOW,
                category = "SUSPICIOUS",
                message = "Possível risco detectado",
                recommendation = "caution",
                destination = extrairDominio(url)
            )
        }
    }
    
    private fun extrairDominio(url: String): String {
        return try {
            URL(url).host ?: url
        } catch (e: Exception) {
            url
        }
    }
}
```

---

### 3. Ativar `RealSafeBrowsingRepository` em `MainActivity`

**Arquivo:** `app/src/main/kotlin/com/capsec/oatguard/MainActivity.kt`

```kotlin
// ANTES (Mock):
val repository = MockSafeBrowsingRepository()

// DEPOIS (Real):
val safeBrowsingService = createSafeBrowsingService(
    baseUrl = "https://safebrowsing.googleapis.com",
    apiKey = BuildConfig.SAFE_BROWSING_API_KEY
)
val repository = RealSafeBrowsingRepository(safeBrowsingService)

fun createSafeBrowsingService(baseUrl: String, apiKey: String): SafeBrowsingService {
    val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
    
    return retrofit.create(SafeBrowsingService::class.java)
}
```

---

### 4. Validar URLResolver (Importante!)

**Arquivo:** `app/src/main/kotlin/com/capsec/oatguard/utils/URLResolver.kt`

**Requisitos:**
- [x] Resolver redirects 301/302 ANTES de validar
- [x] Máximo 5 hops
- [x] Timeout 5s por request
- [x] Loop detection
- [x] Retornar URL final

**Teste:**
```kotlin
// Testar com URL encurtada (bit.ly, tinyurl, etc)
val url = "https://bit.ly/example"
val finalUrl = URLResolver.resolve(url)
// Deve retornar a URL final após redirecionamentos
```

---

### 5. Compilar e Testar

```bash
# 1. Confirmar local.properties tem a chave
cat local.properties | grep SAFE_BROWSING_API_KEY

# 2. Recompilar com chave real
./gradlew clean assembleDebug

# 3. Se compilar com sucesso, está pronto para testes em hardware
```

---

## ⚠️ Checklist Crítico

- [ ] Chave API está em `local.properties` (não em código)
- [ ] Chave injetada via `BuildConfig.SAFE_BROWSING_API_KEY`
- [ ] `RealSafeBrowsingRepository` substituiu `MockSafeBrowsingRepository`
- [ ] `SafeBrowsingService` está configurado com endpoint correto
- [ ] Models (Request/Response) estão sincronizados com Google API
- [ ] URLResolver resolve redirects antes de validar
- [ ] Tratamento de erros implementado (API indisponível, offline, etc)
- [ ] Compilação com sucesso: `BUILD SUCCESSFUL`
- [ ] APK gerado sem warnings críticos

---

## 📖 Referências

- [Google Safe Browsing API v4](https://developers.google.com/safe-browsing/v4)
- API Endpoint: `https://safebrowsing.googleapis.com/v4/threatMatches:find?key={API_KEY}`
- Threat Types: MALWARE, SOCIAL_ENGINEERING, UNWANTED_SOFTWARE, POTENTIALLY_HARMFUL_APPLICATION
- Platform Types: ANDROID, WINDOWS, LINUX, OS_X, ALL_PLATFORMS

---

## 🎯 Resultado Esperado

Após implementação:
1. App carrega a chave real do `BuildConfig`
2. QR scanner scaneia código → URL extraída
3. URLResolver resolve redirects
4. RealSafeBrowsingRepository faz requisição à Google API
5. Score (0-100) retorna com cor e descrição
6. Resultado exibido em ResultScreen com attribution Google
7. APK buildado com sucesso e pronto para testes em device/emulador

---

**Status:** Pronto para Claude Code  
**Próximo Passo:** Implementar, compilar, testar em hardware
