package com.hogargo.kmp.household.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.db.Member
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.household.data.HogarError
import com.hogargo.kmp.household.data.RecoveryUser
import com.hogargo.kmp.household.data.memberKey
import com.hogargo.kmp.l10n.L10n

/** 4. Perfil: mis datos, el hogar, integrantes y acciones del admin. */
@Composable
fun ProfileScreen(
    viewModel: HouseholdViewModel,
    onClose: () -> Unit,
    onEditName: () -> Unit,
    onEditRecovery: () -> Unit,
    onEditHousehold: () -> Unit,
) {
    val me by viewModel.me.collectAsState()
    val household by viewModel.household.collectAsState()
    val members by viewModel.members.collectAsState()
    var memberToRemove by remember { mutableStateOf<Member?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<HogarError?>(null) }

    val current = me
    val home = household
    // El rol se lee del integrante vivo, no de la sesion (puede cambiar por traspaso).
    val isAdmin = current?.isAdmin == true && current.isApproved
    val approved = members.filter { it.isApproved }
    val pending = members.filter { !it.isApproved }

    fun run(action: () -> HogarError?) {
        actionError = action()
    }

    AccessScaffold(title = L10n.profile, onClose = onClose) {
        if (current == null || home == null) return@AccessScaffold

        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MemberAvatar(current.name, 56.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EditableTitle(current.name, 22.sp, onEditName)
                if (isAdmin) AdminBadge(12.sp)
            }
        }
        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallCaps(L10n.homeName)
                if (isAdmin) {
                    EditableTitle(home.name, 18.sp, onEditHousehold)
                } else {
                    Text(home.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                }
                Spacer(Modifier.size(4.dp))
                SmallCaps(L10n.homeCode)
                HouseholdCodeBadge(home.code) { viewModel.showToast(L10n.copied) }
            }
        }

        // Solicitudes pendientes (solo admin)
        if (isAdmin && pending.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(L10n.pendingRequests(pending.size))
                pending.forEach { member ->
                    HogarCard(background = HogarGoColors.brandOrangeContainer.copy(alpha = 0.5f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MemberAvatar(member.name, 36.dp)
                            Text(member.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText)
                            Spacer(Modifier.weight(1f))
                            CircleAction("✕", HogarGoColors.alertRed, L10n.reject) {
                                run { viewModel.rejectRequest(member.id) }
                            }
                            CircleAction("✓", HogarGoColors.success, L10n.accept) {
                                run { viewModel.acceptRequest(member.id) }
                            }
                        }
                    }
                }
            }
        }

        // Integrantes
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle(L10n.members)
            approved.forEach { member ->
                HogarCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MemberAvatar(member.name, 36.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (member.id == current.id) "${member.name} (${L10n.you})" else member.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HogarGoColors.primaryText,
                            )
                            if (member.isAdmin) AdminBadge(11.sp)
                        }
                        if (isAdmin && !member.isAdmin) {
                            TextButton(onClick = { memberToRemove = member }) {
                                Text("🗑 " + L10n.remove, color = HogarGoColors.alertRed, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Usuario de recuperacion
        HogarCard(onClick = onEditRecovery) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🔑", fontSize = 16.sp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(L10n.recoveryLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText)
                    Text(
                        if (current.recoveryKey.isNotEmpty()) L10n.recoveryConfigured else L10n.recoveryNotConfigured,
                        fontSize = 12.sp,
                        color = HogarGoColors.secondaryText,
                    )
                }
                Text("›", fontSize = 20.sp, color = HogarGoColors.secondaryText)
            }
        }

        actionError?.let { Text(it.message, fontSize = 13.sp, color = HogarGoColors.alertRed) }

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HogarPrimaryButton(L10n.leaveHome, destructive = true) { confirmLeave = true }
            TextButton(onClick = viewModel::logout) {
                Text(L10n.logout, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText)
            }
        }
    }

    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text(L10n.removeTitle(member.name)) },
            text = { Text(L10n.removeBody) },
            confirmButton = {
                TextButton(onClick = {
                    memberToRemove = null
                    run { viewModel.removeMember(member.id) }
                }) { Text(L10n.remove, color = HogarGoColors.alertRed) }
            },
            dismissButton = { TextButton(onClick = { memberToRemove = null }) { Text(L10n.cancel) } },
        )
    }

    if (confirmLeave) {
        val body = when {
            !isAdmin -> L10n.leaveMemberBody
            approved.size > 1 -> L10n.leaveAdminBody
            else -> L10n.leaveLastBody
        }
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text(L10n.leaveHomeTitle(home?.name.orEmpty())) },
            text = { Text(body) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    run { viewModel.leaveHousehold() }
                }) { Text(L10n.leave, color = HogarGoColors.alertRed) }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text(L10n.cancel) } },
        )
    }
}

/** 7.1 Cambiar mi nombre (solo en este hogar). */
@Composable
fun ChangeNameScreen(viewModel: HouseholdViewModel, onClose: () -> Unit) {
    val me by viewModel.me.collectAsState()
    val members by viewModel.members.collectAsState()
    val current = me ?: return
    var newName by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<HogarError?>(null) }

    val clean = newName.trim()
    val liveError = when {
        clean.isEmpty() -> null
        current.recoveryKey.isNotEmpty() && RecoveryUser.hash(clean) == current.recoveryKey ->
            HogarError.NAME_SAME_AS_RECOVERY_USER.message
        members.any { it.id != current.id && it.nameKey == memberKey(clean) } -> HogarError.NAME_TAKEN.message
        else -> null
    }
    val canSave = clean.isNotEmpty() && liveError == null && clean != current.name

    AccessScaffold(
        title = L10n.changeMyName,
        onClose = onClose,
        footer = {
            HogarPrimaryButton(L10n.save, enabled = canSave) {
                error = viewModel.changeMyName(newName)
                if (error == null) onClose()
            }
        },
    ) {
        Text(L10n.currentName(current.name), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.secondaryText)
        HogarTextField(
            L10n.newName, newName, { newName = it; error = null }, current.name,
            error = liveError ?: error?.message,
            help = L10n.nameOnlyHere,
        )
    }
}

/** 7.2 Cambiar o crear el usuario de recuperacion. */
@Composable
fun ChangeRecoveryUserScreen(viewModel: HouseholdViewModel, onClose: () -> Unit) {
    val me by viewModel.me.collectAsState()
    val current = me ?: return
    var currentUser by rememberSaveable { mutableStateOf("") }
    var newUser by rememberSaveable { mutableStateOf("") }
    var repeatNew by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<HogarError?>(null) }

    val hasCurrent = current.recoveryKey.isNotEmpty()
    val currentError = if (hasCurrent && currentUser.isNotBlank() && RecoveryUser.hash(currentUser) != current.recoveryKey) {
        HogarError.RECOVERY_CURRENT_WRONG.message
    } else {
        null
    }
    val newError = when {
        RecoveryUser.normalize(newUser).isEmpty() -> null
        RecoveryUser.validate(newUser, current.name) != null -> RecoveryUser.validate(newUser, current.name)?.message
        hasCurrent && RecoveryUser.hash(newUser) == current.recoveryKey -> HogarError.RECOVERY_SAME_AS_CURRENT.message
        else -> null
    }
    // Aparece mientras se escribe la repeticion.
    val repeatError = if (repeatNew.isNotBlank() && RecoveryUser.normalize(repeatNew) != RecoveryUser.normalize(newUser)) {
        HogarError.RECOVERY_MISMATCH.message
    } else {
        null
    }
    val filled = (!hasCurrent || currentUser.isNotBlank()) && newUser.isNotBlank() && repeatNew.isNotBlank()
    val canSave = filled && currentError == null && newError == null && repeatError == null

    AccessScaffold(
        title = L10n.recoveryLabel,
        onClose = onClose,
        footer = {
            HogarPrimaryButton(L10n.save, enabled = canSave) {
                error = viewModel.changeRecoveryUser(currentUser, newUser, repeatNew)
                if (error == null) onClose()
            }
        },
    ) {
        Text(if (hasCurrent) L10n.recoveryChangeHelp else L10n.recoveryHelp, fontSize = 13.sp, color = HogarGoColors.secondaryText)
        if (hasCurrent) {
            HogarTextField(
                L10n.recoveryCurrent, currentUser, { currentUser = it; error = null },
                kind = HogarFieldKind.RECOVERY_USER, error = currentError,
            )
        }
        HogarTextField(
            L10n.recoveryNew, newUser, { newUser = it; error = null }, L10n.recoveryPlaceholder,
            kind = HogarFieldKind.RECOVERY_USER, error = newError,
        )
        HogarTextField(
            L10n.recoveryRepeat, repeatNew, { repeatNew = it; error = null },
            kind = HogarFieldKind.RECOVERY_USER, error = repeatError ?: error?.message,
        )
    }
}

/** 5. Cambiar el nombre del hogar (solo admin). */
@Composable
fun RenameHouseholdScreen(viewModel: HouseholdViewModel, onClose: () -> Unit) {
    val household by viewModel.household.collectAsState()
    val home = household ?: return
    var name by rememberSaveable { mutableStateOf(home.name) }
    var error by remember { mutableStateOf<HogarError?>(null) }

    AccessScaffold(
        title = L10n.renameHome,
        onClose = onClose,
        footer = {
            HogarPrimaryButton(L10n.save, enabled = name.isNotBlank() && name.trim() != home.name) {
                error = viewModel.renameHousehold(name)
                if (error == null) onClose()
            }
        },
    ) {
        HogarTextField(L10n.homeName, name, { name = it; error = null }, home.name, error = error?.message)
    }
}

@Composable
private fun EditableTitle(text: String, size: TextUnit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.clip(CircleShape).clickable(onClick = onEdit).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text, fontSize = size, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
        Text("✎", fontSize = 15.sp, color = HogarGoColors.accentText)
    }
}

@Composable
private fun AdminBadge(size: TextUnit) {
    Text(L10n.adminBadge, fontSize = size, fontWeight = FontWeight.Bold, color = HogarGoColors.accentText)
}

@Composable
private fun SmallCaps(text: String) {
    Text(text.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.secondaryText)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
}

@Composable
private fun CircleAction(symbol: String, color: Color, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(CircleShape).background(color).clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(symbol, color = Color.White, fontWeight = FontWeight.Bold) }
}
