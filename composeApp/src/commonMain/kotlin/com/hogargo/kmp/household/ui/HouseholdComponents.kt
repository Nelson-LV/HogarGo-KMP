package com.hogargo.kmp.household.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.household.data.HouseholdCode
import com.hogargo.kmp.household.data.RecoveryUser
import com.hogargo.kmp.l10n.L10n

/** Paleta de HogarGo (la misma de la app iOS). */
object HogarGoColors {
    val background = Color(0xFFFEF8F5)
    val surface = Color.White
    val surfaceContainer = Color(0xFFFCEAE2)
    val surfaceContainerLow = Color(0xFFFFF1EB)
    val brandOrange = Color(0xFFFEAC67)
    val primaryText = Color(0xFF7A3E1D)
    val secondaryText = Color(0xFF8C7468)
    val brandOrangeContainer = Color(0xFFFFDBC9)
    val brandOrangeDeep = Color(0xFF974400)
    val borderSubtle = Color(0xFFEBDCD4)
    val success = Color(0xFF558B2F)
    val alertRed = Color(0xFFBA1A1A)
}

/** Tipo de teclado para los campos de los formularios de hogar. */
enum class HogarFieldKind {
    /** Mayuscula en cada palabra. */
    NAME,

    /** Sin autocapitalizacion ni autocorreccion. */
    RECOVERY_USER,
    CODE,

    /** Importes: teclado decimal. */
    AMOUNT,
}

/** Campo con etiqueta, error en vivo y texto de ayuda. */
@Composable
fun HogarTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    kind: HogarFieldKind = HogarFieldKind.NAME,
    error: String? = null,
    help: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = when (kind) {
                    HogarFieldKind.NAME -> KeyboardCapitalization.Words
                    HogarFieldKind.RECOVERY_USER -> KeyboardCapitalization.None
                    HogarFieldKind.CODE -> KeyboardCapitalization.Characters
                    HogarFieldKind.AMOUNT -> KeyboardCapitalization.None
                },
                keyboardType = if (kind == HogarFieldKind.AMOUNT) KeyboardType.Decimal else KeyboardType.Text,
                autoCorrectEnabled = kind == HogarFieldKind.NAME,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HogarGoColors.surfaceContainerLow,
                unfocusedContainerColor = HogarGoColors.surfaceContainerLow,
                unfocusedBorderColor = HogarGoColors.borderSubtle,
                focusedBorderColor = HogarGoColors.brandOrangeDeep,
                errorBorderColor = HogarGoColors.alertRed,
            ),
        )
        when {
            error != null -> Text(error, fontSize = 12.sp, color = HogarGoColors.alertRed)
            help != null -> Text(help, fontSize = 12.sp, color = HogarGoColors.secondaryText)
        }
    }
}

/** Boton principal. Deshabilitado se ve atenuado y no responde al toque. */
@Composable
fun HogarPrimaryButton(
    title: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val color = if (destructive) HogarGoColors.alertRed else HogarGoColors.brandOrangeDeep
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.45f),
        shape = CircleShape,
        contentPadding = PaddingValues(vertical = 15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color,
            disabledContentColor = Color.White,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

/** Codigo formateado (HGR-789) con boton para copiarlo. */
@Suppress("DEPRECATION") // LocalClipboard (nuevo) necesita ClipEntry especifico de cada plataforma.
@Composable
fun HouseholdCodeBadge(code: String, onCopied: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            HouseholdCode.format(code),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = HogarGoColors.primaryText,
        )
        TextButton(onClick = {
            clipboard.setText(AnnotatedString(HouseholdCode.format(code)))
            onCopied()
        }) {
            Text("⧉ " + L10n.copy, color = HogarGoColors.brandOrangeDeep, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Circulo con la inicial del integrante. */
@Composable
fun MemberAvatar(name: String, size: Dp = 40.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(HogarGoColors.brandOrangeContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.take(1).uppercase(),
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.Bold,
            color = HogarGoColors.primaryText,
        )
    }
}

/** Tarjeta blanca redondeada. */
@Composable
fun HogarCard(
    modifier: Modifier = Modifier,
    background: Color = HogarGoColors.surface,
    bordered: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .then(if (bordered) Modifier.border(1.dp, HogarGoColors.borderSubtle, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
    ) { content() }
}

/**
 * Contenedor comun de las pantallas de acceso y edicion: titulo con boton de cerrar,
 * contenido con scroll y boton principal fijo abajo que sube junto con el teclado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessScaffold(
    title: String,
    onClose: () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = HogarGoColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(HogarGoColors.surfaceContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✕", color = HogarGoColors.secondaryText, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HogarGoColors.background),
            )
        },
        bottomBar = {
            if (footer != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(HogarGoColors.background)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) { footer() }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // La barra inferior ya sube con el teclado: no sumar ese espacio dos veces.
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) { content() }
    }
}

/** Validacion en vivo del usuario de recuperacion al crear o unirse. Vacio: sin mensaje. */
fun liveRecoveryError(text: String, name: String): String? {
    if (RecoveryUser.normalize(text).isEmpty()) return null
    return RecoveryUser.validate(text, name)?.message
}
