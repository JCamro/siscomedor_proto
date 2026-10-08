package com.example.siscomedor.domain

enum class QrRefreshFixture(val label: String) {
    SUCCESS("Consulta exitosa"), FAILURE("Consulta fallida"), OFFLINE("Sin conexión")
}

enum class QrRefreshStatus { SAVED, LOADING, REFRESHED, FAILED, OFFLINE }

// A local query fixture never regenerates content or verifies institutional validity.
data class QrRefreshState(
    val ticket: Ticket,
    val status: QrRefreshStatus = QrRefreshStatus.SAVED,
    val lastConsultedAt: String? = null
) {
    fun begin() = if (ticket.canShowQr) copy(status = QrRefreshStatus.LOADING) else this

    fun finish(fixture: QrRefreshFixture, online: Boolean, checkedAt: String): QrRefreshState {
        if (!ticket.canShowQr || status != QrRefreshStatus.LOADING) return this
        return when {
            !online || fixture == QrRefreshFixture.OFFLINE -> copy(status = QrRefreshStatus.OFFLINE)
            fixture == QrRefreshFixture.FAILURE -> copy(status = QrRefreshStatus.FAILED)
            else -> copy(status = QrRefreshStatus.REFRESHED, lastConsultedAt = checkedAt)
        }
    }
}
