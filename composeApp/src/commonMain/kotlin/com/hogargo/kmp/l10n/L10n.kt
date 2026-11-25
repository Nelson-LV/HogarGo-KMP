package com.hogargo.kmp.l10n

/** Idioma preferido del dispositivo (Android: Locale, iOS: NSLocale). */
internal expect fun deviceLanguageIsEnglish(): Boolean

/**
 * Textos en espanol e ingles, igual que en HogarGo iOS: `tr(es, en)`.
 * En ingles el proyecto usa "home", nunca "household".
 */
object L10n {
    val isEnglish: Boolean by lazy { deviceLanguageIsEnglish() }

    fun tr(es: String, en: String): String = if (isEnglish) en else es

    // Comunes
    val cancel get() = tr("Cancelar", "Cancel")
    val close get() = tr("Cerrar", "Close")
    val back get() = tr("Volver", "Back")
    val save get() = tr("Guardar", "Save")
    val copy get() = tr("Copiar", "Copy")
    val copied get() = tr("Código copiado", "Code copied")

    // Bienvenida
    val welcomeSubtitle get() = tr("Organiza tu hogar en equipo, con Zori.", "Run your home as a team, with Zori.")
    val createHome get() = tr("Crear un hogar", "Create a Home")
    val joinHome get() = tr("Unirme a un hogar", "Join a Home")
    val signIn get() = tr("Iniciar Sesión", "Sign In")

    // Campos
    val yourName get() = tr("Tu nombre", "Your name")
    val yourNamePlaceholder get() = tr("Ej. Ana", "E.g. Ana")
    val homeName get() = tr("Nombre del hogar", "Home name")
    val homeNamePlaceholder get() = tr("Ej. Casa Pérez", "E.g. Perez House")
    val homeCode get() = tr("Código del hogar", "Home code")
    val regenerateCode get() = tr("Generar otro", "Generate another")
    val createAction get() = tr("Crear hogar", "Create home")
    val joinAction get() = tr("Enviar solicitud", "Send request")

    val recoveryLabel get() = tr("Usuario de recuperación", "Recovery username")
    val recoveryPlaceholder get() = tr("Ej. familia_perez24", "E.g. perez_family24")
    val recoveryHelp
        get() = tr(
            "Es distinto de tu nombre. Con él podrás ver el código de tus hogares si lo olvidas. Guárdalo bien y no lo compartas.",
            "It's different from your name. With it you can see the code of your homes if you forget it. Keep it safe and don't share it.",
        )
    val recoveryChangeHelp
        get() = tr(
            "Por seguridad escribe tu usuario actual y luego el nuevo dos veces.",
            "For security, type your current username and then the new one twice.",
        )
    val recoveryCurrent get() = tr("Usuario actual", "Current username")
    val recoveryNew get() = tr("Usuario nuevo", "New username")
    val recoveryRepeat get() = tr("Repite el usuario nuevo", "Repeat the new username")

    // Login y recuperacion
    val loginLabel get() = tr("Tu nombre o usuario", "Your name or username")
    val loginPlaceholder get() = tr("Nombre en el hogar o usuario de recuperación", "Name in the home or recovery username")
    val forgotCode get() = tr("¿Olvidaste el código?", "Forgot the code?")
    val recoverTitle get() = tr("Recuperar código", "Recover code")
    val showCodes get() = tr("Ver códigos", "Show codes")
    val enter get() = tr("Entrar", "Enter")
    val recoverDescription
        get() = tr(
            "Escribe tu usuario de recuperación para ver los códigos de los hogares en los que sigues siendo integrante.",
            "Enter your recovery username to see the codes of the homes you're still a member of.",
        )
    val recoverEmpty
        get() = tr(
            "No encontramos hogares con ese usuario. Revisa que esté bien escrito.",
            "We couldn't find homes with that username. Check that it's spelled correctly.",
        )
    fun registeredAs(name: String) = tr("Te registraste como: $name", "You signed up as: $name")

    val requestSentTitle get() = tr("¡Solicitud enviada!", "Request sent!")
    val requestSentBody
        get() = tr(
            "El administrador del hogar debe aceptarte antes de que puedas entrar. Cuando lo haga, usa Iniciar Sesión con tu nombre y el código.",
            "The home admin must accept you before you can get in. Once they do, use Sign In with your name and the code.",
        )

    // Perfil
    val profile get() = tr("Perfil", "Profile")
    val changeMyName get() = tr("Cambiar mi nombre", "Change my name")
    fun currentName(name: String) = tr("Nombre actual: $name", "Current name: $name")
    val newName get() = tr("Tu nuevo nombre", "Your new name")
    val nameOnlyHere
        get() = tr(
            "Solo cambia en este hogar. En tus otros hogares conservas el nombre que tengas allí.",
            "It only changes in this home. In your other homes you keep the name you have there.",
        )
    val renameHome get() = tr("Cambiar nombre del hogar", "Rename home")
    fun pendingRequests(n: Int) = tr("Solicitudes pendientes ($n)", "Pending requests ($n)")
    val accept get() = tr("Aceptar", "Accept")
    val reject get() = tr("Rechazar", "Reject")
    val remove get() = tr("Eliminar", "Remove")
    val members get() = tr("Integrantes", "Members")
    val adminBadge get() = tr("Admin", "Admin")
    val you get() = tr("Tú", "You")
    val edit get() = tr("Editar", "Edit")
    val recoveryConfigured get() = tr("Configurado · toca para cambiarlo", "Set up · tap to change it")
    val recoveryNotConfigured get() = tr("Sin configurar · toca para crearlo", "Not set up · tap to create it")
    val leaveHome get() = tr("Salir del hogar", "Leave home")
    fun leaveHomeTitle(home: String) = tr("¿Salir de $home?", "Leave $home?")
    val leave get() = tr("Salir", "Leave")
    val logout get() = tr("Cerrar sesión", "Sign out")
    val nameUpdated get() = tr("Nombre actualizado", "Name updated")
    val usernameSaved get() = tr("Usuario guardado", "Username saved")
    val homeRenamed get() = tr("Nombre del hogar actualizado", "Home renamed")

    fun removeTitle(name: String) = tr("¿Eliminar a $name?", "Remove $name?")
    val removeBody
        get() = tr(
            "Saldrá del hogar y sus tareas quedarán sin asignar. Podrá volver a pedir ingreso con el código.",
            "They'll leave the home and their tasks will be unassigned. They can request to join again with the code.",
        )
    val leaveMemberBody
        get() = tr(
            "Dejarás de ser integrante y el código de este hogar ya no aparecerá en tu recuperación. Para volver tendrás que pedir ingreso de nuevo y que el admin te acepte.",
            "You'll stop being a member and this home's code will no longer appear in your recovery. To come back you'll have to request to join again and be accepted by the admin.",
        )
    val leaveAdminBody
        get() = tr(
            "Eres el admin: al salir, el rol pasará al integrante más antiguo del hogar. Para volver tendrás que pedir ingreso de nuevo.",
            "You're the admin: when you leave, the role will pass to the oldest member of the home. To come back you'll have to request to join again.",
        )
    val leaveLastBody
        get() = tr(
            "Eres la única persona de este hogar. Si sales, el hogar y todos sus datos se eliminarán para siempre.",
            "You're the only person in this home. If you leave, the home and all its data will be deleted forever.",
        )

    // Notificaciones
    val channelName get() = tr("Novedades del hogar", "Home updates")
}
