# OatGuard

**Confira o QR code antes de abrir o link.**

O OatGuard é um app Android gratuito e de código aberto que lê um QR code e verifica o link dele no Google Safe Browsing antes de você abrir. Não tem anúncios, cadastro, análise de uso nem servidor próprio da CapSEC.

Criado por um desenvolvedor brasileiro na [CapSEC](https://capsec.com.br), consultoria de segurança da informação.

[English](README.md)

<a href="https://play.google.com/store/apps/details?id=com.capsec.oatguard">Baixar no Google Play</a>

## Por que existe

Golpistas colam QR codes falsos sobre os verdadeiros (parquímetros, cardápios, boletos). O destino só aparece depois que você abre o link. O OatGuard mostra para onde o código leva e se o Google o marca como ameaça, antes de você abrir qualquer coisa.

## Recursos

- Leitura do QR code no próprio aparelho (CameraX e ML Kit)
- Verificação do link no Google Safe Browsing
- Pontuação de 0 a 100 com níveis de risco por cor
- Reconhece QR codes de chave PIX (desde a v1.0.1) e os trata à parte, sem enviá-los ao Safe Browsing
- Sete idiomas
- Gratuito, sem anúncios, sem cadastro

## Requisitos

- Android 7.0 (API 24) ou superior
- Permissão de câmera
- Conexão com a internet (necessária para a verificação no Safe Browsing)

## Privacidade

- Sem conta, sem anúncios, sem análise de uso.
- A CapSEC não mantém servidor para este app. Ela não recebe, coleta nem armazena os links que você escaneia.
- O app não salva os links escaneados no aparelho.
- A leitura do QR code (câmera e decodificação) acontece no seu aparelho.
- Para verificar um link, o app envia uma requisição direto do seu aparelho para a API do Google Safe Browsing, por HTTPS. O Google trata essa requisição sob os próprios termos e política de privacidade e, como em qualquer requisição de rede, pode ver o seu endereço IP.
  - O app usa a API de consulta v4 do Safe Browsing (`threatMatches:find`). A requisição contém o link completo, com caminho e parâmetros, depois de seguidos os redirecionamentos. Não é reduzida a prefixos de hash.
- Antes da verificação, o app segue os redirecionamentos do link para achar o destino final. Para isso, o seu aparelho envia uma requisição HTTP `HEAD` ao link escaneado e a cada redirecionamento, incluindo o destino final, até um número máximo fixo de redirecionamentos e com tempo limite curto por requisição. Essas requisições usam os cabeçalhos padrão do OkHttp e não enviam cookies. Isso significa que o servidor por trás do link pode ver o seu IP e que uma requisição foi feita, mesmo que você não tenha aberto o link.
- Se o QR code contiver uma chave PIX ou um código PIX copia e cola, o app não o envia ao Safe Browsing. Ele abre uma tela PIX separada, que só copia a chave ou o código para a área de transferência quando você toca no botão de copiar.
- As requisições levam a chave de API do app (restrita a este app por nome do pacote e certificado de assinatura), e não um identificador de usuário.

Política completa: [política de privacidade](https://www.capsec.com.br/oatguard/pt/docs.html#privacy).

## O que o resultado significa

- "Nenhuma ameaça encontrada" quer dizer que o Google Safe Browsing não tem registro desse link agora. Não garante que o destino seja seguro.
- Um link malicioso novo pode ainda não estar na lista.
- Só redirecionamentos HTTP são seguidos. Redirecionamentos feitos por JavaScript ou por meta refresh em HTML não são.
- Só o destino final é verificado no Safe Browsing, e não os redirecionamentos intermediários.
- Um servidor malicioso pode responder à requisição `HEAD` do app de forma diferente da que responde a um navegador.
- Links escritos sem `http://` ou `https://` (por exemplo `www.exemplo.com`) não são verificados. O app informa que nenhum link foi encontrado.
- PIX: o OatGuard só reconhece o formato de chaves e códigos PIX. Não faz requisição de rede para isso, não consulta o diretório central do PIX e não sabe dizer se uma chave existe nem de quem é. Também não verifica quem recebe o pagamento. Nos códigos PIX dinâmicos, a URL da cobrança no provedor de pagamento não é verificada no Safe Browsing. Confirme sempre o destinatário no app do seu banco antes de pagar.
- A pontuação de 0 a 100 não é uma análise independente. Ela é um mapeamento fixo do tipo de ameaça devolvido pelo Google Safe Browsing: malware 20, engenharia social (phishing) 30, software indesejado 45, aplicativo potencialmente nocivo 50, e 100 quando o Google não reporta ameaça conhecida. Se várias ameaças forem devolvidas, vale a menor pontuação. O OatGuard não soma sinais próprios a essa pontuação.

## Compilar a partir do código

```bash
git clone https://github.com/thadeutourinho-tech/oatguard.git
cd oatguard
./gradlew assembleDebug
```

Você precisa de uma chave própria da API do Safe Browsing. Coloque-a em `local.properties` como `SAFE_BROWSING_API_KEY`.

Atenção: se a chave estiver vazia, o app usa um simulador que só procura as palavras "malware", "phishing", "unwanted" ou "harmful" no link e informa que qualquer outro link não tem ameaça conhecida. Ele existe apenas para testar a interface. Nunca distribua uma versão compilada sem chave.

Tecnologias: Kotlin, Jetpack Compose, CameraX com ML Kit, Retrofit e OkHttp, Gson.

## Status e roteiro

- Publicado no Google Play: v1.0.0 (leitura de QR code e verificação no Safe Browsing) e v1.0.1 (detecção de chave PIX).
- Ideias em avaliação: verificar links recebidos de outros apps pelo menu de compartilhar, histórico de leituras apenas local, dicas curtas de educação contra phishing.

## Contribuindo

Issues e pull requests são bem-vindos. Faça um fork, crie uma branch, faça seus commits e abra um pull request.

## Licença

MIT. Veja [LICENSE](LICENSE).

O nome e o logotipo "Oat" são marcas da CapSEC. Usá-los em obras derivadas exige permissão.

## Contato

CapSEC: https://capsec.com.br | contato@capsec.com.br
