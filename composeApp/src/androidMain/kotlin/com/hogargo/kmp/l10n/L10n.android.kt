package com.hogargo.kmp.l10n

import java.util.Locale

internal actual fun deviceLanguageIsEnglish(): Boolean = Locale.getDefault().language == "en"
