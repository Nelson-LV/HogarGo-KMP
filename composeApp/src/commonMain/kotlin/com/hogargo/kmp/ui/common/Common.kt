package com.hogargo.kmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.l10n.longDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Titulo y subtitulo de una pantalla principal. */
@Composable
fun ScreenHeader(title: String, subtitle: String, centered: Boolean = false) {
    Column(
        modifier = Modifier,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
        Text(subtitle, fontSize = 14.sp, color = HogarGoColors.secondaryText)
    }
}

/** Boton de capsula pequeno (acciones secundarias dentro de tarjetas). */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    container: Color = HogarGoColors.brandOrangeDeep,
    content: Color = Color.White,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (enabled) container else container.copy(alpha = 0.4f))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = content, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Campo de fecha: muestra la fecha larga y abre un selector al tocarlo. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun HogarDateField(label: String, date: LocalDate, onChange: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
        Row(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .background(HogarGoColors.surfaceContainerLow)
                .clickable { open = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Text("📅  " + L10n.longDate(date), color = HogarGoColors.primaryText, fontSize = 15.sp)
        }
    }
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        onChange(Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.UTC).date)
                    }
                    open = false
                }) { Text(L10n.accept) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text(L10n.cancel) } },
        ) { DatePicker(state = state) }
    }
}
