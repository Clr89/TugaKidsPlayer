# TugaKidsPlayer

App Android para ver filmes hospedados no [TugaKids](https://www.tugakids.com).

## Funcionalidades

- Catálogo remoto de filmes (JSON via HTTPS)
- Catálogo de demonstração como fallback
- Pesquisa local no catálogo
- Tamanhos de póster configuráveis
- Tema claro / escuro (com preferência persistida)
- Player com Media3 (ExoPlayer)
  - Modo imersivo
  - Ajuste de imagem (fit, fill, etc.)
  - Abrir vídeo noutra app
  - Retoma da posição após pausa/rotação
- Verificação de atualizações via GitHub Releases
- Links externos restritos (segurança)

## Requisitos

- Android Studio Ladybug (ou mais recente)
- JDK 11+
- minSdk 24 / targetSdk 37

## Como construir

1. Clona o repositório:
   ```bash
   git clone https://github.com/Clr89/TugaKidsPlayer.git
   cd TugaKidsPlayer
   ```

2. Abre o projeto no Android Studio e deixa o Gradle sincronizar.

3. Para builds de **debug**:
   ```bash
   ./gradlew assembleDebug
   ```

4. Para builds de **release** (assinados):
   - Copia `keystore.properties.example` para `keystore.properties`
   - Preenche com os dados do teu keystore
   - Gera o APK/AAB:
     ```bash
     ./gradlew assembleRelease
     # ou
     ./gradlew bundleRelease
     ```

## Configuração do catálogo

O endpoint do catálogo está definido em:

```
app/src/main/java/com/clr89/tugakidsplayer/data/repository/MovieCatalogConfig.kt
```

Atualmente aponta para um JSON remoto servido por HTTPS. O formato esperado é um array de objetos com pelo menos:

| Campo          | Tipo   | Obrigatório |
|----------------|--------|-------------|
| `id`           | string | sim         |
| `title`        | string | sim         |
| `thumbnailUrl` | string | sim         |
| `videoUrl`     | string | sim         |
| `year`         | int    | não         |
| `ageRating`    | string | não         |

## Estrutura principal

```
app/src/main/java/com/clr89/tugakidsplayer/
├── MainActivity.kt
├── data/
│   ├── AppPreferences.kt
│   ├── DemoMovieCatalog.kt
│   ├── model/Movie.kt
│   └── repository/
│       ├── GitHubReleaseChecker.kt
│       ├── MovieCatalogConfig.kt
│       ├── MovieCatalogJsonParser.kt
│       └── RemoteMovieCatalogRepository.kt
└── ui/
    ├── catalog/          # ecrã do catálogo, pesquisa, diálogos
    ├── player/           # PlayerScreen + apresentação de vídeo
    └── theme/
```

## Tecnologias

- Kotlin
- Jetpack Compose + Material 3
- Media3 (ExoPlayer)
- DataStore Preferences
- Coroutines

## Contacto

- Site: [tugakids.com](https://www.tugakids.com)
- Email: tugakids.com@proton.me

---

Projeto pessoal · Android
