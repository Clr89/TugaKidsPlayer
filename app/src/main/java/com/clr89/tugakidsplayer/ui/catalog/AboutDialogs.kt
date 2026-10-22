package com.clr89.tugakidsplayer.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clr89.tugakidsplayer.data.repository.PublishedRelease
import com.clr89.tugakidsplayer.data.repository.ReleaseCheckResult

@Composable
fun AboutDialog(
    installedVersion: String,
    updateState: ReleaseCheckResult,
    onDismiss: () -> Unit,
    onOpenRepository: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenContact: () -> Unit,
    onCheckUpdates: () -> Unit,
) {
    var showLegalInfo by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showLegalInfo) "Informação legal e privacidade" else "Sobre TugaKidsPlayer") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showLegalInfo) {
                    LegalAndPrivacyText()
                } else {
                    Text("TugaKidsPlayer · versão $installedVersion")
                    TextButton(onClick = onOpenRepository) { Text("Repositório no GitHub") }
                    TextButton(onClick = onOpenWebsite) { Text("Site TugaKids") }
                    TextButton(onClick = onOpenContact) { Text("Contacto: tugakids.com@proton.me") }
                    Text(
                        "O contacto destina-se apenas a assuntos relacionados com o site TugaKids.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = { showLegalInfo = true }) { Text("Informação legal e privacidade") }
                    TextButton(onClick = onCheckUpdates) { Text("Procurar atualizações") }
                    when (updateState) {
                        ReleaseCheckResult.NotChecked -> Text("Ainda não foi verificada nesta sessão.")
                        ReleaseCheckResult.NoUpdate -> Text("Não foi encontrada uma versão publicada mais recente.")
                        ReleaseCheckResult.Unavailable -> Text("Não foi possível verificar atualizações. Tenta novamente mais tarde.")
                        ReleaseCheckResult.Checking -> Text("A verificar atualizações…")
                        is ReleaseCheckResult.UpdateAvailable -> Text("Versão ${updateState.release.tagName} disponível.")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = if (showLegalInfo) ({ showLegalInfo = false }) else onDismiss) {
                Text(if (showLegalInfo) "Voltar" else "Fechar")
            }
        },
    )
}

@Composable
private fun LegalAndPrivacyText() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "TugaKidsPlayer é uma aplicação independente, não oficial e não afiliada aos responsáveis pelo site TugaKids.",
        )
        Text("Os conteúdos, imagens, nomes e marcas pertencem aos respetivos titulares.")
        Text(
            "A aplicação consulta um catálogo remoto e apresenta os endereços fornecidos pelas fontes. A reprodução é feita a partir desses endereços; a aplicação não aloja os vídeos.",
        )
        Text(
            "Para funcionar, a aplicação comunica com o serviço do catálogo, os servidores de imagens e vídeo indicados pelas fontes e, para a procura de atualizações, a API pública do GitHub.",
        )
        Text(
            "A aplicação não pede contas nem campos de dados pessoais. O código atual não integra um serviço remoto de análise ou telemetria nem transmite explicitamente dados pessoais. Como em qualquer pedido de rede, os servidores contactados podem receber o endereço IP e dados técnicos da ligação.",
        )
        Text(
            "O tamanho dos pósteres, a preferência de tema e a versão de atualização dispensada são guardados nas preferências locais da aplicação. A configuração Android permite cópias de segurança da aplicação; a inclusão destas preferências depende do comportamento de cópia de segurança do dispositivo.",
        )
        Text(
            "Esta informação descreve o funcionamento atual da aplicação. Não constitui uma licença dos conteúdos nem uma garantia ou autorização dos respetivos titulares.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun AvailableUpdateDialog(
    installedVersion: String,
    release: PublishedRelease,
    onLater: () -> Unit,
    onViewRelease: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        title = { Text("Nova versão disponível") },
        text = {
            Text("Instalada: $installedVersion\nDisponível: ${release.tagName}")
        },
        confirmButton = {
            TextButton(onClick = onViewRelease) { Text("Ver atualização") }
        },
        dismissButton = {
            TextButton(onClick = onLater) { Text("Mais tarde") }
        },
    )
}
