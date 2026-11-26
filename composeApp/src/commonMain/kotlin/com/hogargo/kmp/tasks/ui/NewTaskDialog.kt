package com.hogargo.kmp.tasks.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hogargo.kmp.db.Member
import com.hogargo.kmp.household.ui.HogarGoColors

val CATEGORIES = listOf("Cocina", "Jardín", "Limpieza", "General")

@Composable
fun NewTaskDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        category: String,
        assignedMemberId: String,
        assignedMemberName: String,
        assignedToOther: Boolean,
        coinReward: Int,
        dueDate: String,
        dueTime: String,
    ) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CATEGORIES.first()) }
    var selectedMember by remember { mutableStateOf<Member?>(members.firstOrNull()) }
    var assignToOther by remember { mutableStateOf(false) }
    var otherAssigneeName by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("18:00") }
    var coinReward by remember { mutableStateOf(10) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var memberExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Proponer nueva tarea", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea") },
                    placeholder = { Text("Ej: Lavar los platos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Selector de categoría
                Column {
                    Text("Categoría", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        trailingIcon = {
                            Text("▼", modifier = Modifier.clickable { categoryExpanded = true })
                        },
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }

                // Asignado a
                Column {
                    Text("Responsable", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = !assignToOther,
                            onClick = { assignToOther = false },
                            label = { Text("Integrante") },
                        )
                        FilterChip(
                            selected = assignToOther,
                            onClick = { assignToOther = true },
                            label = { Text("Otra persona") },
                        )
                    }

                    if (!assignToOther) {
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedMember?.name ?: "Sin asignar",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { memberExpanded = true },
                            trailingIcon = {
                                Text("▼", modifier = Modifier.clickable { memberExpanded = true })
                            },
                        )
                        DropdownMenu(
                            expanded = memberExpanded,
                            onDismissRequest = { memberExpanded = false },
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        selectedMember = member
                                        memberExpanded = false
                                    },
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = otherAssigneeName,
                            onValueChange = { otherAssigneeName = it },
                            label = { Text("Nombre del responsable") },
                            placeholder = { Text("Ej: Visita / Primo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Horario y recompensa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Hora") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = coinReward.toString(),
                        onValueChange = { input -> input.toIntOrNull()?.let { coinReward = it } },
                        label = { Text("Monedas 🪙") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val assigneeId = if (assignToOther) "" else selectedMember?.id.orEmpty()
                        val assigneeName = if (assignToOther) otherAssigneeName.ifBlank { "Otra persona" } else selectedMember?.name.orEmpty()
                        onConfirm(
                            title.trim(),
                            selectedCategory,
                            assigneeId,
                            assigneeName,
                            assignToOther,
                            coinReward,
                            "2026-05-10",
                            dueTime.ifBlank { "18:00" },
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HogarGoColors.brandOrangeDeep),
            ) {
                Text("Guardar Tarea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
