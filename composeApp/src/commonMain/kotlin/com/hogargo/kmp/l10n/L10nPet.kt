package com.hogargo.kmp.l10n

// Textos de la mascota Zori (ES/EN).

val L10n.petFeed get() = tr("Alimentar", "Feed")
val L10n.petPlay get() = tr("Jugar", "Play")
val L10n.petDecorate get() = tr("Decorar", "Decorate")
val L10n.petStatusTitle get() = tr("Estado de Zori", "Zori's Status")
fun L10n.petLevel(level: Int) = tr("Nvl $level", "Lvl $level")
val L10n.petHappiness get() = tr("Felicidad", "Happiness")
val L10n.petSatiety get() = tr("Saciedad", "Fullness")
val L10n.petWardrobeTitle get() = tr("Armario", "Wardrobe")
val L10n.petWardrobeSubtitle
    get() = tr(
        "¡Cuida a Zori para subir de nivel y gana monedas con tareas para comprar nuevos objetos!",
        "Take care of Zori to level up and earn coins from tasks to buy new items!",
    )
val L10n.petWear get() = tr("Poner", "Wear")
val L10n.petRemove get() = tr("Quitar", "Remove")
fun L10n.petBuy(price: Int) = tr("Comprar · $price 🪙", "Buy · $price 🪙")
fun L10n.petCoinsBalance(coins: Int) = "🪙 $coins"
val L10n.petEquippedBadge get() = tr("Puesto", "Worn")
val L10n.petWearing get() = tr("Zori lleva puesto:", "Zori is wearing:")
val L10n.petWearingNothing get() = tr("Nada todavía. ¡Pulsa Decorar!", "Nothing yet. Tap Decorate!")
fun L10n.petLevelRequired(level: Int) = tr("Nivel $level requerido", "Level $level required")
val L10n.petTipTitle get() = tr("Sigue cuidando a Zori", "Keep taking care of Zori")
val L10n.petTipSubtitle
    get() = tr(
        "Aliméntalo y juega con él para subir de nivel y desbloquear nuevos objetos.",
        "Feed and play with Zori to level up and unlock new items.",
    )
fun L10n.petCooldownAvailableIn(minutes: Long) = tr("Disponible en $minutes min", "Available in $minutes min")

fun L10n.petItemName(id: String): String = when (id) {
    "scarf" -> tr("Bufanda Acogedora", "Cozy Scarf")
    "ball" -> tr("Pelota de Juguete", "Toy Ball")
    "glasses" -> tr("Gafas Inteligentes", "Smart Glasses")
    "skateboard" -> tr("Mini Monopatín", "Mini Skateboard")
    else -> id
}
