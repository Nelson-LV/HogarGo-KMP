package com.hogargo.kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.household.ui.HogarCard
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.household.ui.HogarPrimaryButton
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.pet.ui.ZoriAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = L10n.tr("Acerca de HogarGo", "About HogarGo"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = HogarGoColors.primaryText, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = HogarGoColors.background,
                ),
            )
        },
        containerColor = HogarGoColors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Hero Mascot & App Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(HogarGoColors.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    ZoriAvatar(equippedIds = emptySet(), size = 88.dp)
                }

                Text(
                    text = "HogarGo",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = HogarGoColors.primaryText,
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HogarGoColors.brandOrangeContainer,
                ) {
                    Text(
                        text = "v1.0.0 • Kotlin Multiplatform",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HogarGoColors.accentText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }

                Text(
                    text = L10n.tr(
                        "Organiza tu hogar en equipo, con Zori.",
                        "Run your home as a team, with Zori.",
                    ),
                    fontSize = 14.sp,
                    color = HogarGoColors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }

            // 2. Tarjeta Universidad y Asignatura
            HogarCard(background = HogarGoColors.surfaceContainer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(HogarGoColors.brandOrangeContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🎓", fontSize = 22.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Universidad del Cauca",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                        Text(
                            text = L10n.tr(
                                "Ingeniería de Sistemas • Electiva de Desarrollo de Aplicaciones Móviles",
                                "Systems Engineering • Mobile App Development Elective",
                            ),
                            fontSize = 12.sp,
                            color = HogarGoColors.secondaryText,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }

            // 3. Tarjeta de Créditos del Equipo
            HogarCard(background = HogarGoColors.surfaceContainer) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("👥", fontSize = 18.sp)
                        Text(
                            text = L10n.tr("Equipo de Desarrollo", "Development Team"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                    }

                    // Integrante 1: Jhonatan
                    MemberCreditCard(
                        initials = "JP",
                        name = "Jhonatan Palacios Gómez",
                        role = L10n.tr(
                            "Módulo de Tareas, Racha Diaria, Recordatorios y Consejo del Día (Advice Slip API)",
                            "Tasks Module, Daily Streak, Notifications & Daily Advice (Advice Slip API)",
                        ),
                    )

                    // Integrante 2: Nelson
                    MemberCreditCard(
                        initials = "NL",
                        name = "Nelson Rodrigo López Vidales",
                        role = L10n.tr(
                            "Módulo de Finanzas, Mascota Zori y Calendario",
                            "Finances, Zori Pet & Calendar Modules",
                        ),
                    )

                    // Integrante 3: Dana
                    MemberCreditCard(
                        initials = "DR",
                        name = "Dana Isabella Romero Núñez",
                        role = L10n.tr(
                            "Módulo de Hogares, Integrantes y Sesión",
                            "Household, Members & Session Modules",
                        ),
                    )
                }
            }

            // 4. Tarjeta de Tecnologías
            HogarCard(background = HogarGoColors.surfaceContainer) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("🛠️", fontSize = 18.sp)
                        Text(
                            text = L10n.tr("Stack Tecnológico", "Tech Stack"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TechBadge("Compose Multiplatform", Modifier.weight(1f))
                        TechBadge("Kotlin KMP", Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TechBadge("SQLDelight", Modifier.weight(1f))
                        TechBadge("Ktor 3 HTTP", Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TechBadge("Android & iOS", Modifier.weight(1f))
                        TechBadge("Coroutines & Flows", Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            HogarPrimaryButton(
                title = L10n.tr("Volver", "Back"),
                onClick = onBack,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MemberCreditCard(
    initials: String,
    name: String,
    role: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HogarGoColors.surface)
            .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HogarGoColors.brandOrangeContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HogarGoColors.primaryText,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HogarGoColors.primaryText,
                )
                Text(
                    text = role,
                    fontSize = 12.sp,
                    color = HogarGoColors.secondaryText,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun TechBadge(name: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = HogarGoColors.surface,
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = HogarGoColors.primaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
