package com.hogargo.kmp.household.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.household.data.HogarError
import com.hogargo.kmp.household.data.HouseholdRepository
import com.hogargo.kmp.household.data.RecoveryUser
import com.hogargo.kmp.household.data.messageFor
import com.hogargo.kmp.l10n.L10n
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Bienvenida: crear un hogar, unirse o iniciar sesion. */
@Composable
fun WelcomeScreen(onCreate: () -> Unit, onJoin: () -> Unit, onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(120.dp).clip(CircleShape).background(HogarGoColors.brandOrangeContainer),
            contentAlignment = Alignment.Center,
        ) { Text("🏠", fontSize = 52.sp) }
        Spacer(Modifier.size(14.dp))
        Text("HogarGo", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
        Spacer(Modifier.size(8.dp))
        Text(
            L10n.welcomeSubtitle,
            fontSize = 15.sp,
            color = HogarGoColors.secondaryText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            HogarPrimaryButton(title = L10n.createHome, onClick = onCreate)
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = PaddingValues(vertical = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HogarGoColors.brandOrangeContainer,
                    contentColor = HogarGoColors.accentText,
                ),
            ) { Text(L10n.joinHome, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onLogin) {
                Text(L10n.signIn, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText)
            }
        }
    }
}

/** 3.1 Crear hogar: quien lo crea queda como admin. */
@Composable
fun CreateHouseholdScreen(viewModel: HouseholdViewModel, onClose: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var householdName by rememberSaveable { mutableStateOf("") }
    var recoveryUser by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf(viewModel.suggestCode()) }
    var error by remember { mutableStateOf<HogarError?>(null) }

    val recoveryError = liveRecoveryError(recoveryUser, name)
    val canSubmit = name.isNotBlank() && householdName.isNotBlank() &&
        RecoveryUser.normalize(recoveryUser).isNotEmpty() && recoveryError == null

    AccessScaffold(
        title = L10n.createHome,
        onClose = onClose,
        footer = {
            HogarPrimaryButton(L10n.createAction, enabled = canSubmit) {
                error = viewModel.createHousehold(name, householdName, recoveryUser, code)
                if (error == HogarError.CODE_TAKEN) code = viewModel.suggestCode()
            }
        },
    ) {
        HogarTextField(
            L10n.yourName, name, { name = it; error = null }, L10n.yourNamePlaceholder,
            error = error.messageFor(HogarError.Field.NAME),
        )
        HogarTextField(
            L10n.homeName, householdName, { householdName = it; error = null }, L10n.homeNamePlaceholder,
            error = error.messageFor(HogarError.Field.HOUSEHOLD),
        )
        HogarTextField(
            L10n.recoveryLabel, recoveryUser, { recoveryUser = it; error = null }, L10n.recoveryPlaceholder,
            kind = HogarFieldKind.RECOVERY_USER,
            error = recoveryError ?: error.messageFor(HogarError.Field.RECOVERY),
            help = L10n.recoveryHelp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(L10n.homeCode, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
            HogarCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HouseholdCodeBadge(code) { viewModel.showToast(L10n.copied) }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { code = viewModel.suggestCode() }) {
                        Text(L10n.regenerateCode, color = HogarGoColors.accentText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            error.messageFor(HogarError.Field.CODE)?.let {
                Text(it, fontSize = 12.sp, color = HogarGoColors.alertRed)
            }
        }
    }
}

/** 3.2 Unirse a un hogar: queda pendiente hasta que el admin acepte. */
@Composable
fun JoinHouseholdScreen(viewModel: HouseholdViewModel, onClose: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var recoveryUser by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<HogarError?>(null) }
    var requestSent by rememberSaveable { mutableStateOf(false) }

    fun edited() {
        error = null
        requestSent = false
    }

    val recoveryError = liveRecoveryError(recoveryUser, name)
    val canSubmit = name.isNotBlank() && RecoveryUser.normalize(recoveryUser).isNotEmpty() &&
        recoveryError == null && code.isNotBlank()

    AccessScaffold(
        title = L10n.joinHome,
        onClose = onClose,
        footer = {
            HogarPrimaryButton(L10n.joinAction, enabled = canSubmit) {
                error = viewModel.joinHousehold(name, recoveryUser, code)
                requestSent = error == null
            }
        },
    ) {
        if (requestSent) {
            HogarCard(background = HogarGoColors.success.copy(alpha = 0.1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("✈️", fontSize = 20.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(L10n.requestSentTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                        Text(L10n.requestSentBody, fontSize = 13.sp, color = HogarGoColors.secondaryText)
                    }
                }
            }
        }
        HogarTextField(
            L10n.yourName, name, { name = it; edited() }, L10n.yourNamePlaceholder,
            error = error.messageFor(HogarError.Field.NAME),
        )
        HogarTextField(
            L10n.recoveryLabel, recoveryUser, { recoveryUser = it; edited() }, L10n.recoveryPlaceholder,
            kind = HogarFieldKind.RECOVERY_USER,
            error = recoveryError ?: error.messageFor(HogarError.Field.RECOVERY),
            help = L10n.recoveryHelp,
        )
        HogarTextField(
            L10n.homeCode, code, { code = it; edited() }, "HGR-789",
            kind = HogarFieldKind.CODE,
            error = error.messageFor(HogarError.Field.CODE),
        )
    }
}

/** 3.3 Iniciar sesion con el nombre en el hogar o el usuario de recuperacion. */
@Composable
fun LoginScreen(viewModel: HouseholdViewModel, onClose: () -> Unit, onForgotCode: () -> Unit) {
    var nameOrUser by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<HogarError?>(null) }

    AccessScaffold(
        title = L10n.signIn,
        onClose = onClose,
        footer = {
            HogarPrimaryButton(L10n.signIn, enabled = nameOrUser.isNotBlank() && code.isNotBlank()) {
                error = viewModel.login(nameOrUser, code)
            }
        },
    ) {
        HogarTextField(
            L10n.loginLabel, nameOrUser, { nameOrUser = it; error = null }, L10n.loginPlaceholder,
            kind = HogarFieldKind.RECOVERY_USER,
            error = error.messageFor(HogarError.Field.NAME) ?: error.messageFor(HogarError.Field.GENERAL),
        )
        HogarTextField(
            L10n.homeCode, code, { code = it; error = null }, "HGR-789",
            kind = HogarFieldKind.CODE,
            error = error.messageFor(HogarError.Field.CODE),
        )
        TextButton(onClick = onForgotCode) {
            Text(L10n.forgotCode, fontWeight = FontWeight.SemiBold, color = HogarGoColors.accentText)
        }
    }
}

/** 3.4 Ver los codigos de mis hogares con el usuario de recuperacion. */
@Composable
fun RecoverCodesScreen(viewModel: HouseholdViewModel, onClose: () -> Unit) {
    var recoveryUser by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<HouseholdRepository.RecoveredHome>?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AccessScaffold(title = L10n.recoverTitle, onClose = onClose) {
        Text(L10n.recoverDescription, fontSize = 14.sp, color = HogarGoColors.secondaryText)
        HogarTextField(
            L10n.recoveryLabel, recoveryUser, { recoveryUser = it }, L10n.recoveryPlaceholder,
            kind = HogarFieldKind.RECOVERY_USER,
        )
        HogarPrimaryButton(L10n.showCodes, enabled = recoveryUser.isNotBlank(), loading = loading) {
            loading = true
            scope.launch {
                // Pequena pausa para que se vea el indicador.
                delay(250)
                results = viewModel.recoverHomes(recoveryUser)
                loading = false
            }
        }
        results?.let { homes ->
            if (homes.isEmpty()) {
                Text(L10n.recoverEmpty, fontSize = 14.sp, color = HogarGoColors.secondaryText)
            } else {
                homes.forEach { home ->
                    RecoveredHomeCard(home, onCopied = { viewModel.showToast(L10n.copied) }) {
                        if (!viewModel.enter(home)) results = emptyList()
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveredHomeCard(home: HouseholdRepository.RecoveredHome, onCopied: () -> Unit, onEnter: () -> Unit) {
    HogarCard(bordered = true) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (home.isAdmin) "${home.householdName} · ${L10n.adminBadge}" else home.householdName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HogarGoColors.primaryText,
            )
            Text(L10n.registeredAs(home.memberName), fontSize = 13.sp, color = HogarGoColors.secondaryText)
            Row(verticalAlignment = Alignment.CenterVertically) {
                HouseholdCodeBadge(home.code, onCopied)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onEnter,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = HogarGoColors.brandOrangeDeep, contentColor = Color.White),
                ) { Text(L10n.enter, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
