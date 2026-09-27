# OatGuard — QR Code Validator

A free, open-source Android app that validates QR codes for security threats using Google Safe Browsing API.

## 📱 Features

- **QR Code Scanning:** Fast, accurate QR detection via ML Kit
- **Security Validation:** Real-time threat detection powered by Google Safe Browsing
- **Trust Score:** Visual feedback (0-100) with color-coded risk levels
- **7 Languages:** Portuguese, English, Spanish, French, Russian, Hindi, Arabic
- **No Backend:** 100% client-side, zero tracking, zero data collection
- **Free Distribution:** Marketing tool for CapSEC information security consultancy

## 🚀 Quick Start

### Prerequisites
- Android 7.0+ (API 24)
- Camera permission
- Internet connection

### Download
- [Play Store](https://play.google.com/store/apps/details?id=com.capsec.oatguard) (coming soon)
- [GitHub Releases](https://github.com/capsec-br/oatguard/releases)

### Build from Source
```bash
git clone https://github.com/capsec-br/oatguard.git
cd oatguard
./gradlew assembleDebug
```

## 🔧 Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Scanning:** ML Kit Vision + CameraX
- **API Client:** Retrofit 2.11+ / OkHttp 4.12+
- **JSON:** Gson
- **Target:** Android 7.0+ (minSdk 24, targetSdk 37)

## 🔐 Security & Privacy

- **API Key Protection:** Restricted by package name + SHA-1 fingerprint (Google Cloud)
- **HTTPS Only:** NetworkSecurityConfig enforces TLS 1.2+
- **No Local Storage:** Stateless, no database, no cache
- **No User Tracking:** All processing is local, API calls are anonymous
- **Google Safe Browsing:** Powered by Google's threat intelligence

### Attribution
Malware protection provided by Google Safe Browsing. [Learn more](https://www.google.com/safebrowsing/).

## 📖 Documentation

- [Privacy Policy & Docs](https://www.capsec.com.br/oatguard/pt/docs.html)
- [Technical Specification](./oatguard-especificacao.md)
- [Development Journal](./validador-qr-diario.md)

## 🛣️ Roadmap

### Semana 1 ✅ (MVP Delivered)
- All UI screens (Splash, Home, Scanner, Result)
- QR scanning with ML Kit
- Mock Safe Browsing API
- Multi-language support (7 languages)
- Error handling
- Build APK debug

### Semana 2 (Hardware Testing)
- Google Cloud API integration
- Real API testing on device/emulator
- Performance validation

### Semana 3 (Play Store Release)
- Play Console submission
- App signing + SHA-1 registration
- Public release

### Future (Post-MVP)
- Firebase Analytics
- Validation history
- Educational content on phishing patterns
- B2B integration for CapSEC clients

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork this repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

OatGuard is released under the [MIT License](LICENSE). See LICENSE file for details.

⚠️ **Trademark Notice:** The "Oat" logo and brand are registered trademarks of CapSEC. Use of the logo in derivative works requires permission.

## 👨‍💼 About CapSEC

[CapSEC](https://www.capsec.com.br) is a Brazilian information security consultancy specializing in secure communications and threat protection.

**Related Projects:**
- [OatCall](https://play.google.com/store/apps/details?id=com.capsec.oatcall) — Call blocking app
- OatGuard — QR code validator (this repo)

## 📧 Contact

- Website: https://www.capsec.com.br
- Email: (from CapSEC website)
- GitHub: [@capsec-br](https://github.com/capsec-br)

---

**Build Status:** ✅ MVP (Semana 1) — Compilado e funcional  
**Last Updated:** 2026-09-27
