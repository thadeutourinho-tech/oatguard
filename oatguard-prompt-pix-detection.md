# Prompt: PIX Detection Feature para OatGuard (v1.0.1)

## 📋 Contexto do Projeto

**App:** OatGuard (validador de QR codes)
**Versão:** v1.0.0 → v1.0.1 (adicionar PIX detection)
**Linguagem:** Kotlin + Jetpack Compose
**Framework:** Android Studio (AGP 8.2+, Kotlin 1.9+)

---

## 🎯 Objetivo

Implementar detecção automática de chaves PIX em QR codes scaneados e exibir uma tela de resultado específica com orientações educacionais.

---

## 📊 Especificação de Requisitos

### 1. Detecção de Formato PIX

Implementar `PixKeyDetector` que identifique os 5 tipos de chaves PIX do BACEN:

| Tipo | Padrão | Regex | Exemplo |
|------|--------|-------|---------|
| **CPF** | 11 dígitos | `^\d{11}$` | `12345678901` |
| **CNPJ** | 14 dígitos | `^\d{14}$` | `12345678901234` |
| **Email** | Email válido | `^[^@\s]+@[^@\s]+\.[^@\s]+$` | `user@domain.com` |
| **Telefone** | +55 + 11 dígitos | `^\+?55\d{11}$` ou `^\d{11}$` | `5511987654321` ou `11987654321` |
| **EVP** | UUID v4 | `^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$` | `123e4567-e89b-12d3-a456-426655440000` |

**Validação adicional:**
- Aceitar strings "limpas" (com ou sem formatação)
- Detectar CPF/CNPJ mesmo com pontuação (extrair apenas dígitos)
- Telefone: aceitar com/sem +55

---

### 2. Fluxo de Detecção

**Ordem de execução (após scanning do QR):**

```
QR Code scaneado
    ↓
String extraída do QR
    ↓
┌─ PixKeyDetector.detect(string) retorna PixKeyType?
│
├─ Se PixKeyType encontrado → tela ResultadoPix
│  (não fazer validação de URL)
│
└─ Se null → tentar Safe Browsing API (comportamento atual)
```

---

### 3. Resultado PIX - Tela Compose

**Componente:** `PixResultScreen(pixKey: String, pixType: PixKeyType)`

**Layout:**
```
┌─────────────────────────────────────────┐
│  [< Voltar]             [⚙️ Configurações]
├─────────────────────────────────────────┤
│                                         │
│  ⚠️ (Ícone âmbar)                       │
│                                         │
│  CHAVE PIX DETECTADA                    │
│                                         │
│  Identificamos que este QR Code é       │
│  uma chave PIX.                         │
│                                         │
│  Aqui está o problema: a gente não      │
│  consegue validar se essa chave é       │
│  real ou falsificada. Essa informação   │
│  só as instituições financeiras têm     │
│  acesso.                                │
│                                         │
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓    │
│  ┃ O que você deve fazer:          ┃    │
│  ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫    │
│  ┃ ✓ Entre em contato com o        ┃    │
│  ┃   prestador de serviço pra      ┃    │
│  ┃   confirmar                     ┃    │
│  ┃ ✓ Veja se o CPF/CNPJ bate      ┃    │
│  ┃ ✓ Quando tiver dúvida,         ┃    │
│  ┃   NÃO pague                     ┃    │
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛    │
│                                         │
│  [COPIAR CHAVE PIX]                     │
│  [VOLTAR À CÂMERA]                      │
│                                         │
└─────────────────────────────────────────┘
```

**Cores:**
- Background alerta: `Color(0xFFFFF3CD)` (âmbar claro, #FFF3CD)
- Borda/ícone: `Color(0xFFFFC107)` (âmbar, #FFC107)
- Texto alerta: `Color(0xFF856404)` (marrom escuro, #856404)

**Botões:**
- `COPIAR CHAVE PIX` → copia a chave para clipboard (Toast: "Chave copiada!")
- `VOLTAR À CÂMERA` → volta ao scanner (dismiss current screen)

---

### 4. Integração com fluxo existente

**Localização atual esperada:**
- `MainActivity.kt` ou `CameraScreen.kt` — onde QR é processado
- `ResultScreen.kt` (ou similar) — tela de resultado de URL

**Mudanças:**
1. No handler de QR scaneado, chamar `PixKeyDetector.detect()` **antes** de fazer Safe Browsing
2. Se PIX detectado → navegar para `PixResultScreen`
3. Se não PIX → continuar com fluxo atual (Safe Browsing)

---

### 5. Estrutura de Código Esperada

```
app/src/main/java/com/capsec/oatguard/
├── pix/
│   ├── PixKeyType.kt                 (enum)
│   ├── PixKeyDetector.kt             (object com lógica)
│   └── PixDetectionResult.kt         (data class)
├── ui/
│   ├── screens/
│   │   ├── CameraScreen.kt           (existente - modificar)
│   │   ├── ResultScreen.kt           (existente - sem mudanças)
│   │   └── PixResultScreen.kt        (NOVO)
│   └── components/
│       └── PixAlertCard.kt           (componente reutilizável)
└── ...
```

---

### 6. Casos de Teste

**Unit Tests (`PixKeyDetectorTest.kt`):**

```kotlin
// Casos válidos
✓ detecta CPF válido (11 dígitos)
✓ detecta CNPJ válido (14 dígitos)
✓ detecta email válido
✓ detecta telefone com +55
✓ detecta telefone sem +55
✓ detecta UUID v4 (EVP)

// Casos inválidos
✗ rejeita CPF com menos de 11 dígitos
✗ rejeita CNPJ com menos de 14 dígitos
✗ rejeita email sem @
✗ rejeita telefone com menos dígitos
✗ rejeita UUID inválido

// Edge cases
✓ ignora espaços antes/depois
✓ aceita strings com pontuação (remove antes de validar)
✓ retorna null para string vazia
```

**Integration Tests (screenshot/visual):**

```kotlin
// Tela PIX
✓ exibe âmbar claro como background
✓ ícone ⚠️ fica visível
✓ botão COPIAR funciona
✓ botão VOLTAR funciona
✓ texto alerta está legível
```

---

## 🔧 Implementação

### Tarefas:

- [ ] Criar `PixKeyType.kt` (enum com 5 tipos)
- [ ] Criar `PixKeyDetector.kt` (lógica de detecção)
- [ ] Criar `PixResultScreen.kt` (UI)
- [ ] Criar `PixAlertCard.kt` (componente)
- [ ] Modificar `CameraScreen.kt` (integração)
- [ ] Adicionar testes unitários
- [ ] Testar visualmente no emulador
- [ ] Atualizar navegação (adicionar rota para PIX)

---

## 📝 Notas Importantes

1. **Não fazer chamadas DICT** — apenas detectar formato PIX, não validar
2. **Copy deve ser exato** — use o texto especificado acima
3. **Cores âmbar** — não vermelho (que significa perigo/malware)
4. **Privacidade** — não logar a chave PIX completa
5. **Acessibilidade** — certifique-se de que o alerta é visível para daltônicos (não usar apenas cor)

---

## 🎯 Definição de Pronto

A feature está pronta quando:

- ✅ QR codes com PIX são detectados automaticamente
- ✅ Tela de resultado específica é exibida (não confundir com URL perigoso)
- ✅ Copy está 100% conforme especificação
- ✅ Botão COPIAR funciona e mostra toast
- ✅ Botão VOLTAR retorna à câmera
- ✅ Testes unitários passam (>80% coverage)
- ✅ App compila sem warnings
- ✅ Nenhum crash em casos edge
- ✅ Screenshots visuais conferidas

---

## 📚 Referências

- BACEN PIX documentação: https://www.bcb.gov.br/pix
- Tipos de chave: CPF (11 dígitos), CNPJ (14), Email, Telefone (+55), EVP (UUID)
- Projeto OatGuard: `com.capsec.oatguard` (Play Store)
- Versão Target: Android 9+ (API 28+)

---

**Autor:** Thadeu | CapSEC
**Data:** 01/10/2026
**Versão do Prompt:** 1.0
