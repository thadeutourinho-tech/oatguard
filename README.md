# OatGuard

**Check a QR code before you open the link.**

OatGuard is a free, open-source Android app that scans a QR code and checks the link it contains against Google Safe Browsing before you open it. It has no ads, no account, no analytics and no backend run by CapSEC.

Built by a Brazilian developer at [CapSEC](https://capsec.com.br), an information security consultancy.

[Português (BR)](README.pt-BR.md)

<a href="https://play.google.com/store/apps/details?id=com.capsec.oatguard">Get it on Google Play</a>

## Why

Scammers place fake QR codes over real ones (parking meters, menus, payment slips). The destination is only visible after you open the link. OatGuard shows you where the code leads and whether Google flags it, before you open anything.

## Features

- QR code scanning on the device (CameraX and ML Kit)
- Link check against the Google Safe Browsing API
- Trust score from 0 to 100 with color-coded risk levels
- Recognizes PIX key QR codes (since v1.0.1) and handles them separately, without sending them to Safe Browsing
- Seven languages
- Free, no ads, no sign-up

## Requirements

- Android 7.0 (API 24) or later
- Camera permission
- Internet connection (needed for the Safe Browsing check)

## Privacy

- No account, no ads, no analytics.
- CapSEC does not run a server for this app. It does not receive, collect or store the links you scan.
- The app does not save scanned links on the device.
- Scanning the QR code (camera and decoding) happens on your device.
- To check a link, the app sends a request directly from your device to Google's Safe Browsing API, over HTTPS. Google handles that request under its own terms and privacy policy, and, as with any network request, can see your IP address.
  - The app uses the Safe Browsing v4 Lookup API (`threatMatches:find`). The request contains the full link, including path and query string, after redirects have been followed. It is not reduced to hash prefixes.
- Before the check, the app follows the link's redirects to find the final destination. To do this, your device sends an HTTP `HEAD` request to the scanned link and to each redirect it points to, including the final destination, up to a fixed maximum number of redirects and with a short timeout per request. These requests use OkHttp's default headers and no cookies. This means the server behind the link can see your IP address and that a request was made, even though you did not open the link.
- If the QR code contains a PIX key or a PIX copy-and-paste code, the app does not send it to Safe Browsing. It opens a separate PIX screen, which copies the key or code to the clipboard only when you tap the copy button.
- Requests carry the app's API key (restricted to this app by package name and signing certificate), not a user identifier.

Full policy: [privacy policy](https://www.capsec.com.br/oatguard/pt/docs.html#privacy) (in Portuguese).

## What the result means

- "No threat found" means Google Safe Browsing has no match for that link right now. It does not guarantee the destination is safe.
- A new malicious link may not be listed yet.
- Only HTTP redirects are followed. Redirects done with JavaScript or HTML meta refresh are not.
- Only the final destination is checked against Safe Browsing, not the intermediate redirects.
- A malicious server can answer the app's `HEAD` request differently from how it answers a browser.
- Links written without `http://` or `https://` (for example `www.example.com`) are not checked. The app reports that no link was found.
- PIX: OatGuard only recognizes the format of PIX keys and codes. It makes no network request for this, does not query the central PIX directory, and cannot tell whether a key exists or who owns it. It does not verify who receives the payment. For dynamic PIX codes, the charge URL at the payment provider is not checked against Safe Browsing. Always confirm the recipient in your bank app before paying.
- The 0 to 100 score is not an independent analysis. It is a fixed mapping of the threat type returned by Google Safe Browsing: malware 20, social engineering (phishing) 30, unwanted software 45, potentially harmful application 50, and 100 when Google reports no known threat. If several threats are returned, the lowest score is shown. OatGuard does not add its own signals to this score.

## Build from source

```bash
git clone https://github.com/thadeutourinho-tech/oatguard.git
cd oatguard
./gradlew assembleDebug
```

You need your own Safe Browsing API key. Put it in `local.properties` as `SAFE_BROWSING_API_KEY`.

Warning: if the key is empty, the app falls back to a mock that only looks for the words "malware", "phishing", "unwanted" or "harmful" in the link and reports every other link as having no known threat. It exists only for UI testing. Never distribute a build made without a key.

Stack: Kotlin, Jetpack Compose, CameraX with ML Kit, Retrofit and OkHttp, Gson.

## Status and roadmap

- Released on Google Play: v1.0.0 (QR scanning and Safe Browsing check) and v1.0.1 (PIX key detection).
- Ideas under consideration: check links received from other apps through the share menu, a local-only scan history, short phishing education tips.

## Contributing

Issues and pull requests are welcome. Fork the repository, create a branch, commit your changes and open a pull request.

## License

MIT. See [LICENSE](LICENSE).

The "Oat" name and logo are trademarks of CapSEC. Using them in derivative works requires permission.

## Contact

CapSEC: https://capsec.com.br | contato@capsec.com.br
