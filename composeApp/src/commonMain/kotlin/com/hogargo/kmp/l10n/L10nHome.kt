package com.hogargo.kmp.l10n

val L10n.homeTitle get() = tr("Inicio", "Home")
val L10n.homeSubtitle get() = tr("Así va tu hogar hoy.", "How your home is doing today.")
val L10n.homeSpentThisMonth get() = tr("Gastado este mes", "Spent this month")
val L10n.homeNextEvent get() = tr("Próximo evento", "Next event")
val L10n.homeNoEvents get() = tr("Sin eventos próximos", "No upcoming events")
fun L10n.homeUnpaidBills(n: Int) = tr("Facturas por pagar: $n", "Bills to pay: $n")
val L10n.homeAbout get() = tr("Acerca de y créditos", "About and credits")
