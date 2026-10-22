# TugaKidsPlayer

![Licença GPL v3](https://img.shields.io/badge/Licen%C3%A7a-GPLv3-blue.svg)
![Android minSdk](https://img.shields.io/badge/Android-7.0%2B%20%28API%2024%29-green.svg)
![Target SDK](https://img.shields.io/badge/Target%20SDK-API%2037-brightgreen.svg)

Aplicação Android para ver os filmes do TugaKids.

---

## Funcionalidades

- **Catálogo de Filmes:** Navegação pelos conteúdos disponíveis.
- **Pesquisa Integrada:** Pesquisa rápida por título.
- **Detalhes Ricos:** Exibição de pósteres, ano de lançamento e classificação etária (quando disponíveis).
- **Reprodução de Vídeo:** Leitor de vídeo integrado.
- **Personalização:** Suporte a Tema Claro / Escuro e ajuste do tamanho dos cartazes.
- **Atualizações:** Verificação de novas versões da aplicação.

---

## Instalação (Para Utilizadores Finais)

Esta secção aplica-se apenas quando existir uma GitHub Release com APK publicado.

1. Aceda à secção de Releases na página Releases do repositório.
2. Transfira o ficheiro `app-release.apk` da versão mais recente (quando disponível).
3. No seu dispositivo Android, abra o ficheiro transferido e autorize a instalação de aplicações de fontes desconhecidas, caso seja solicitado.

---

## Arquitetura e Comunicação em Rede

A aplicação estabelece ligação à internet para obter o catálogo de filmes, metadados e ficheiros de vídeo.

- **Persistência Local:** Utiliza preferências locais para guardar configurações de tema e preferências de visualização.
- **Rede:** Os dados são obtidos diretamente das fontes configuradas pela aplicação.

---

## Requisitos de Desenvolvimento

- **JDK:** 17
- **Android SDK:** API 37
- **Ferramentas:** Android Studio ou Gradle 9.6.0+

> **Compatibilidade:** Dispositivos com Android 7.0 (API 24) ou superior. O código é compilado com compatibilidade para Java 11.

---

## Compilar e Executar (Debug)

### Gerar APK de Debug

```powershell
.\gradlew.bat assembleDebug
```

> **Localização do ficheiro gerado:** `app/build/outputs/apk/debug/app-debug.apk`

### Instalar no Dispositivo / Emulador

```powershell
.\gradlew.bat installDebug
```

---

## Testes Unitários

Os testes unitários encontram-se na pasta `app/src/test/`.

Para executar os testes unitários:
```powershell
.\gradlew.bat testDebugUnitTest
```

Para executar os testes unitários e compilar a build de debug em conjunto:
```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

---

## Build Release Assinada

A variante release requer uma assinatura configurada localmente.

### Passos para Configuração:
1. Crie e guarde o seu keystore fora do repositório.
2. Copie `keystore.properties.example` para `keystore.properties`.
3. Preencha os dados com o caminho do keystore, alias e palavras-passe.

> **Aviso de Segurança:** Nunca adicione o ficheiro `keystore.properties`, ficheiros `.keystore` / `.jks` ou palavras-passe ao controlo de versão (Git).

Também pode utilizar **variáveis de ambiente**:
- `KEYSTORE_FILE`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

### Comandos de Compilação:

Gerar APK assinado:
```powershell
.\gradlew.bat assembleRelease
```

Gerar Android App Bundle (.aab):
```powershell
.\gradlew.bat bundleRelease
```

---

## Estrutura do Projeto

```text
TugaKidsPlayer/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/clr89/tugakidsplayer/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/          # Preferências, modelos e repositórios
│       │   │   └── ui/            # Catálogo, leitor e tema
│       │   └── res/
│       ├── test/                  # Testes unitários
│       └── androidTest/           # Testes de instrumentação/UI
├── gradle/
├── keystore.properties.example
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

---

## Desenvolvido com IA

Este projeto contou com assistência de Inteligência Artificial no desenvolvimento, refatoração de código e documentação.

---

## Conteúdos de Terceiros e Licença

A licença GPL-3.0 aplica-se exclusivamente ao código fonte deste projeto. Não concede direitos sobre logótipos, cartazes, vídeos, marcas ou outros conteúdos de terceiros apresentados ou referenciados pela aplicação. Esses materiais pertencem aos respetivos titulares de direitos autorais.

Este projeto é distribuído sob a licença GNU General Public License v3.0. Consulte o [LICENSE](LICENSE) para mais detalhes.
