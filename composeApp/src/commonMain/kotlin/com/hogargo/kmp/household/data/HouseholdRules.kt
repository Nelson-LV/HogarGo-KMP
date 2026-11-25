package com.hogargo.kmp.household.data

import com.hogargo.kmp.l10n.L10n
import kotlin.random.Random

/** Codigo de invitacion del hogar: se muestra como "HGR-789" y se guarda como "HGR789". */
object HouseholdCode {
    const val LENGTH = 6

    // Sin caracteres confusos: sin I, O, 0, 1.
    private const val LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val DIGITS = "23456789"

    /** Quita todo lo que no sea letra o numero ASCII y pasa a mayusculas. */
    fun normalize(text: String): String =
        text.filter { it.code < 128 && it.isLetterOrDigit() }.uppercase()

    fun isValid(normalized: String): Boolean = normalized.length == LENGTH

    /** 3 letras + 3 numeros. */
    fun random(random: Random = Random.Default): String = buildString {
        repeat(3) { append(LETTERS[random.nextInt(LETTERS.length)]) }
        repeat(3) { append(DIGITS[random.nextInt(DIGITS.length)]) }
    }

    fun format(code: String): String {
        val normalized = normalize(code)
        if (normalized.length != LENGTH) return normalized
        return "${normalized.take(3)}-${normalized.takeLast(3)}"
    }
}

/** Usuario de recuperacion: segundo identificador, distinto del nombre. Solo se guarda su hash. */
object RecoveryUser {
    const val MIN_LENGTH = 4

    /** trim + minusculas. */
    fun normalize(text: String): String = text.trim().lowercase()

    /** SHA-256(normalize(texto)) en hexadecimal minuscula (64 caracteres). */
    fun hash(text: String): String = Sha256.hex(normalize(text))

    /** Devuelve null si es valido. */
    fun validate(text: String, name: String): HogarError? {
        val normalized = normalize(text)
        if (normalized.length < MIN_LENGTH) return HogarError.RECOVERY_USER_INVALID
        if (normalized == memberKey(name)) return HogarError.RECOVERY_USER_SAME_AS_NAME
        return null
    }
}

/** `name` en minusculas: identifica al integrante dentro del hogar. */
fun memberKey(name: String): String = name.trim().lowercase()

enum class NoticeType {
    RENAMED,
    JOIN_REQUEST,
    JOINED,
    LEFT,
    MEMBER_REMOVED,
    ADMIN,

    /** No es notificacion: registro para quien fue eliminado. */
    REMOVED,
}

/** Catalogo de errores del repositorio. */
enum class HogarError {
    EMPTY_NAME,
    EMPTY_HOUSEHOLD_NAME,
    INVALID_CODE,
    CODE_NOT_FOUND,
    CODE_TAKEN,
    NAME_TAKEN,
    MEMBER_NOT_FOUND,
    PENDING_APPROVAL,
    RECOVERY_USER_INVALID,
    RECOVERY_USER_SAME_AS_NAME,
    NAME_SAME_AS_RECOVERY_USER,
    RECOVERY_CURRENT_WRONG,
    RECOVERY_MISMATCH,
    RECOVERY_SAME_AS_CURRENT,

    /** El usuario de recuperacion ya lo usa (o es el nombre de) otra persona del hogar. */
    RECOVERY_USER_TAKEN,
    REMOVED_FROM_HOUSEHOLD,

    /** Quien actua no existe, no es admin, no esta aprobado o es de otro hogar. */
    NOT_ALLOWED,
    UNKNOWN;

    /** Campo bajo el que se muestra el error en los formularios. */
    enum class Field { NAME, HOUSEHOLD, CODE, RECOVERY, GENERAL }

    val field: Field
        get() = when (this) {
            INVALID_CODE, CODE_NOT_FOUND, CODE_TAKEN -> Field.CODE
            RECOVERY_USER_INVALID, RECOVERY_USER_SAME_AS_NAME, RECOVERY_CURRENT_WRONG,
            RECOVERY_MISMATCH, RECOVERY_SAME_AS_CURRENT, RECOVERY_USER_TAKEN -> Field.RECOVERY
            EMPTY_HOUSEHOLD_NAME -> Field.HOUSEHOLD
            NOT_ALLOWED, UNKNOWN -> Field.GENERAL
            else -> Field.NAME
        }

    val message: String
        get() = when (this) {
            EMPTY_NAME -> L10n.tr("Escribe tu nombre", "Enter your name")
            EMPTY_HOUSEHOLD_NAME -> L10n.tr("Escribe el nombre del hogar", "Enter the home name")
            INVALID_CODE -> L10n.tr("El código debe tener 6 caracteres", "The code must have 6 characters")
            CODE_NOT_FOUND -> L10n.tr("No existe un hogar con ese código", "There's no home with that code")
            CODE_TAKEN -> L10n.tr("Ese código ya está en uso. Generamos otro.", "That code is already in use. We generated another one.")
            NAME_TAKEN -> L10n.tr("Ya hay alguien con ese nombre en este hogar", "Someone in this home already has that name")
            MEMBER_NOT_FOUND -> L10n.tr(
                "No encontramos a nadie con ese nombre o usuario en este hogar",
                "We couldn't find anyone with that name or username in this home",
            )
            PENDING_APPROVAL -> L10n.tr(
                "Tu solicitud aún no ha sido aceptada. Pídele al administrador que te apruebe.",
                "Your request hasn't been accepted yet. Ask the admin to approve you.",
            )
            RECOVERY_USER_INVALID -> L10n.tr("Debe tener al menos 4 caracteres", "Must be at least 4 characters")
            RECOVERY_USER_SAME_AS_NAME -> L10n.tr("Debe ser distinto de tu nombre", "Must be different from your name")
            NAME_SAME_AS_RECOVERY_USER -> L10n.tr(
                "Debe ser distinto de tu usuario de recuperación",
                "Must be different from your recovery username",
            )
            RECOVERY_CURRENT_WRONG -> L10n.tr("El usuario actual no coincide", "The current username doesn't match")
            RECOVERY_MISMATCH -> L10n.tr("Los dos usuarios no coinciden", "The two usernames don't match")
            RECOVERY_SAME_AS_CURRENT -> L10n.tr("Debe ser distinto del usuario actual", "Must be different from the current username")
            RECOVERY_USER_TAKEN -> L10n.tr(
                "Ese usuario ya lo usa otra persona de este hogar. Elige otro.",
                "Someone in this home already uses that username. Pick another one.",
            )
            REMOVED_FROM_HOUSEHOLD -> L10n.tr(
                "El administrador te eliminó de este hogar. Si quieres volver, pide ingreso de nuevo desde «Unirme a un hogar».",
                "The admin removed you from this home. If you want to come back, request to join again from “Join a Home”.",
            )
            NOT_ALLOWED -> L10n.tr("Solo el administrador puede hacer esto", "Only the admin can do this")
            UNKNOWN -> L10n.tr("Algo salió mal. Inténtalo de nuevo.", "Something went wrong. Please try again.")
        }

    /** Mensaje solo si corresponde al campo indicado. */
    fun messageFor(field: Field): String? = if (this.field == field) message else null
}

fun HogarError?.messageFor(field: HogarError.Field): String? = this?.messageFor(field)

class HogarException(val error: HogarError) : Exception(error.name)
