package com.ipb.castelobranco.features.gallery.domain.usecase

import com.ipb.castelobranco.features.gallery.domain.download.GalleryDownloadScheduler
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import javax.inject.Inject

class GalleryAutoDownloadUseCase @Inject constructor(
    private val scheduler: GalleryDownloadScheduler,
    private val repository: GalleryRepository,
) {
    /**
     * Enfileira o download automático (WiFi only) se a galeria estiver vazia localmente.
     * Deve ser chamado APÓS preloadDataUseCase() para que albumsFlow reflita o disco, e SOMENTE
     * com sessão ativa — a galeria é restrita a membros, então sem login o trabalho só pode
     * terminar em 401 e deixar esse erro registrado para a tela.
     */
    fun triggerIfNeeded() {
        if (repository.albumsFlow.value.isEmpty()) {
            scheduler.enqueueWifiOnly()
        }
    }

    /**
     * Re-enfileira após o login. Usa REPLACE de propósito: substituir o trabalho é o que descarta
     * um 401 registrado enquanto o usuário ainda estava deslogado.
     */
    fun onLoginSuccess() = scheduler.enqueueWifiOnly(replaceExisting = true)

    /** Download manual via botão — WiFi only, mantém se já estiver rodando. */
    fun enqueueWifiOnly(replaceExisting: Boolean = false) = scheduler.enqueueWifiOnly(replaceExisting)

    /** Download forçado com dados móveis — substitui qualquer trabalho pendente. */
    fun enqueueAnyNetwork() = scheduler.enqueueAnyNetwork()

    /** Cancela o trabalho pendente e apaga o acervo — conteúdo de membro não sobrevive à sessão. */
    suspend fun clearOnLogout() {
        scheduler.cancel()
        repository.clearAllPhotos()
    }
}
