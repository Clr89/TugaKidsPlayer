# TugaKidsPlayer

Aplicação Android para consultar um catálogo de filmes e reproduzir os vídeos a partir dos endereços fornecidos pelas fontes.

## Funcionalidades

- Catálogo de filmes
- Pesquisa no catálogo
- Cartazes com ano e classificação etária, quando disponíveis
- Reprodução de vídeo
- Tema claro e escuro
- Ajuste do tamanho dos cartazes
- Verificação de atualizações
- Secção Sobre e informação legal

## Requisitos de desenvolvimento

Confirmados na configuração Gradle do módulo `app`:

- JDK 11 (`sourceCompatibility` / `targetCompatibility` = JavaVersion.VERSION_11)
- Android SDK
- `minSdk` 24
- `targetSdk` 37
- `compileSdk` 37

## Compilar e executar (debug)

Na raiz do projeto:

```bash
./gradlew assembleDebug
```

No Windows (PowerShell ou cmd):

```bat
gradlew.bat assembleDebug
```

O APK de debug fica em `app/build/outputs/apk/debug/`.

Para instalar num dispositivo ou emulador ligado:

```bash
./gradlew installDebug
```

## Testes unitários

Na raiz do projeto:

```bash
./gradlew test
```

No Windows:

```bat
gradlew.bat test
```

Os testes unitários estão em `app/src/test/`.

## Build release assinada

1. Prepare um keystore **fora** do repositório (ficheiros `.jks`, `.keystore` e `keystore.properties` estão no `.gitignore`).
2. Copie `keystore.properties.example` para `keystore.properties` na raiz do projeto e preencha os campos com os dados do seu keystore.
3. Não inclua palavras-passe, tokens nem o ficheiro `keystore.properties` no controlo de versões.
4. Em alternativa às propriedades do ficheiro, a configuração de assinatura também aceita as variáveis de ambiente `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`.
5. Gere a build:

```bash
./gradlew assembleRelease
```

ou

```bash
./gradlew bundleRelease
```

Se a assinatura de release não estiver configurada, a tarefa de release falha com uma mensagem a indicar a necessidade de `keystore.properties` ou das variáveis de ambiente.

## Estrutura do projeto

```
TugaKidsPlayer/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/clr89/tugakidsplayer/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/          # preferências, modelos, repositórios
│       │   │   └── ui/            # catálogo, leitor, tema
│       │   └── res/
│       ├── test/                  # testes unitários
│       └── androidTest/
├── gradle/
├── keystore.properties.example
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## Conteúdos de terceiros

A licença GPL-3.0 aplica-se ao código-fonte deste projeto. Não concede quaisquer direitos sobre logótipos, cartazes, vídeos, marcas ou outros conteúdos de terceiros apresentados ou referenciados pela aplicação. Esses materiais pertencem aos respetivos titulares.

## Licença

Este projeto está licenciado sob a GNU General Public License versão 3 (GPL-3.0). O texto integral encontra-se no ficheiro [LICENSE](LICENSE).
