package com.pavlo.regionsmapdownloader.domain.model

/**
 * Одиниця роботи черги завантажень.
 *
 * [downloadName] — ідентифікатор завантаження: з нього будується і URL, і ім'я файлу на диску,
 * тому два регіони з однаковим [downloadName] — це та сама карта.
 * [displayName] потрібен лише для сповіщення та повідомлень про помилку.
 */
data class DownloadQueueItem(
    val downloadName: String,
    val displayName: String
)
