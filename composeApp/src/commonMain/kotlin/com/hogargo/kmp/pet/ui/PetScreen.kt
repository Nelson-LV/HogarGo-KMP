package com.hogargo.kmp.pet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.household.ui.HogarCard
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.l10n.petBuy
import com.hogargo.kmp.l10n.petCoinsBalance
import com.hogargo.kmp.l10n.petCooldownAvailableIn
import com.hogargo.kmp.l10n.petDecorate
import com.hogargo.kmp.l10n.petEquippedBadge
import com.hogargo.kmp.l10n.petFeed
import com.hogargo.kmp.l10n.petHappiness
import com.hogargo.kmp.l10n.petItemName
import com.hogargo.kmp.l10n.petLevel
import com.hogargo.kmp.l10n.petLevelRequired
import com.hogargo.kmp.l10n.petPlay
import com.hogargo.kmp.l10n.petRemove
import com.hogargo.kmp.l10n.petSatiety
import com.hogargo.kmp.l10n.petStatusTitle
import com.hogargo.kmp.l10n.petTipSubtitle
import com.hogargo.kmp.l10n.petTipTitle
import com.hogargo.kmp.l10n.petWardrobeSubtitle
import com.hogargo.kmp.l10n.petWardrobeTitle
import com.hogargo.kmp.l10n.petWear
import com.hogargo.kmp.l10n.petWearing
import com.hogargo.kmp.l10n.petWearingNothing
import com.hogargo.kmp.pet.PetRules
import com.hogargo.kmp.pet.PetViewModel
import com.hogargo.kmp.pet.WardrobeCard
import com.hogargo.kmp.pet.WardrobeCatalog
import com.hogargo.kmp.ui.common.PillButton
import kotlinx.coroutines.launch

@Composable
fun PetScreen(viewModel: PetViewModel) {
    val state by viewModel.state.collectAsState()
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var wardrobeTop by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel) { viewModel.onOpen() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Escenario
        HogarCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ZoriAvatar(state.equippedIds, 200.dp)
                val wearing = WardrobeCatalog.filter { it.id in state.equippedIds }
                Text(
                    if (wearing.isEmpty()) L10n.petWearingNothing
                    else L10n.petWearing + " " + wearing.joinToString(" ") { it.emoji },
                    fontSize = 14.sp,
                    color = HogarGoColors.secondaryText,
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillButton(L10n.petFeed, onClick = viewModel::feed)
                    PillButton(L10n.petPlay, onClick = viewModel::play, container = HogarGoColors.brandOrange, content = HogarGoColors.onBrandOrange)
                    PillButton(
                        L10n.petDecorate,
                        onClick = { scope.launch { scroll.animateScrollTo(wardrobeTop) } },
                        container = HogarGoColors.brandOrangeContainer,
                        content = HogarGoColors.primaryText,
                    )
                }
                cooldownText(state.feedRemainingMs, state.playRemainingMs)?.let {
                    Text(it, fontSize = 13.sp, color = HogarGoColors.secondaryText)
                }
            }
        }

        // Estado
        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(L10n.petStatusTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                    Text(
                        L10n.petLevel(state.level),
                        modifier = Modifier.clip(CircleShape).background(HogarGoColors.brandOrangeContainer).padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                }
                StatBar(L10n.petHappiness, state.happiness, HogarGoColors.brandOrange)
                StatBar(L10n.petSatiety, state.satiety, HogarGoColors.brandOrangeDeep)
            }
        }

        // Armario
        HogarCard(modifier = Modifier.onGloballyPositioned { wardrobeTop = it.positionInParent().y.toInt() }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(L10n.petWardrobeTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                    Text(L10n.petCoinsBalance(state.coins), fontWeight = FontWeight.Bold, color = HogarGoColors.accentText)
                }
                Text(L10n.petWardrobeSubtitle, fontSize = 13.sp, color = HogarGoColors.secondaryText)
                state.items.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { card ->
                            ItemCard(
                                card,
                                Modifier.weight(1f),
                                onBuy = { viewModel.buy(card.entry.id) },
                                onToggle = { viewModel.toggleEquipped(card.entry.id) },
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // Consejo
        HogarCard(background = HogarGoColors.brandOrangeDeep) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(L10n.petTipTitle, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(L10n.petTipSubtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Aviso de cooldown: si ambos esperan, el mayor sin prefijo; si solo uno, con su nombre. */
private fun cooldownText(feedMs: Long, playMs: Long): String? = when {
    feedMs <= 0 && playMs <= 0 -> null
    feedMs > 0 && playMs > 0 -> L10n.petCooldownAvailableIn(PetRules.minutesLabelValue(maxOf(feedMs, playMs)))
    feedMs > 0 -> L10n.petFeed + ": " + L10n.petCooldownAvailableIn(PetRules.minutesLabelValue(feedMs))
    else -> L10n.petPlay + ": " + L10n.petCooldownAvailableIn(PetRules.minutesLabelValue(playMs))
}

@Composable
private fun StatBar(label: String, value: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, color = HogarGoColors.primaryText)
            Text("${(value * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText)
        }
        Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(HogarGoColors.surfaceContainer)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(value.coerceIn(0f, 1f)).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun ItemCard(card: WardrobeCard, modifier: Modifier, onBuy: () -> Unit, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (card.equipped) HogarGoColors.brandOrangeContainer else HogarGoColors.surfaceContainerLow)
            .border(if (card.equipped) 2.dp else 1.dp, if (card.equipped) HogarGoColors.brandOrangeDeep else HogarGoColors.borderSubtle, shape)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(card.entry.emoji, fontSize = 36.sp, modifier = Modifier.alpha(if (card.locked) 0.35f else 1f))
        Text(L10n.petItemName(card.entry.id), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText, textAlign = TextAlign.Center)
        when {
            card.locked -> Text("🔒 " + L10n.petLevelRequired(card.entry.unlockLevel), fontSize = 12.sp, color = HogarGoColors.secondaryText, textAlign = TextAlign.Center)
            card.equipped -> {
                Text("✓ " + L10n.petEquippedBadge, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.accentText)
                PillButton(L10n.petRemove, onClick = onToggle, container = HogarGoColors.surface, content = HogarGoColors.primaryText)
            }
            !card.owned -> PillButton(L10n.petBuy(card.entry.price), onClick = onBuy, enabled = card.canBuy)
            else -> PillButton(L10n.petWear, onClick = onToggle)
        }
    }
}
