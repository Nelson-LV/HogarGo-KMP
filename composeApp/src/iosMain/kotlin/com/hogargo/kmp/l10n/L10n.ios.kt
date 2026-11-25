package com.hogargo.kmp.l10n

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

internal actual fun deviceLanguageIsEnglish(): Boolean =
    (NSLocale.preferredLanguages.firstOrNull() as? String ?: "es").startsWith("en")
