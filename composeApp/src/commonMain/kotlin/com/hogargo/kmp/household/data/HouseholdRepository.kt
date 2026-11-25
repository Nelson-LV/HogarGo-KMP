package com.hogargo.kmp.household.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.db.Household
import com.hogargo.kmp.db.HouseholdNotice
import com.hogargo.kmp.db.Member
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Toda la logica de hogares e integrantes. Las reglas se validan aqui siempre,
 * aunque la pantalla ya las haya validado. Cada operacion que cambia varias filas
 * va en una sola transaccion (si algo falla se deshace todo).
 */
class HouseholdRepository(
    private val db: HogarGoDatabase,
    private val now: () -> Long = ::nowMillis,
) {
    private val q = db.householdQueries

    // region Consultas

    fun household(id: String): Household? = q.householdById(id).executeAsOneOrNull()

    fun householdByCode(code: String): Household? = q.householdByCode(code).executeAsOneOrNull()

    fun member(id: String): Member? = q.memberById(id).executeAsOneOrNull()

    fun members(householdId: String): List<Member> = q.membersByHousehold(householdId).executeAsList()

    fun approvedMembers(householdId: String): List<Member> = members(householdId).filter { it.isApproved }

    fun observeHousehold(id: String): Flow<Household?> =
        q.householdById(id).asFlow().mapToOneOrNull(Dispatchers.Default)

    fun observeMember(id: String): Flow<Member?> =
        q.memberById(id).asFlow().mapToOneOrNull(Dispatchers.Default)

    /** Integrantes del hogar (incluidas las solicitudes pendientes), del mas antiguo al mas nuevo. */
    fun observeMembers(householdId: String): Flow<List<Member>> =
        q.membersByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observePendingNotices(memberId: String): Flow<List<HouseholdNotice>> =
        q.pendingNotices(memberId).asFlow().mapToList(Dispatchers.Default)

    /** Codigo aleatorio que todavia no usa ningun hogar. */
    fun suggestCode(): String {
        var code = HouseholdCode.random()
        while (householdByCode(code) != null) code = HouseholdCode.random()
        return code
    }

    // endregion

    // region 3.1 Crear hogar

    fun createHousehold(name: String, householdName: String, recoveryUser: String, code: String): Member {
        val cleanName = name.trim()
        val cleanHousehold = householdName.trim()
        val cleanCode = HouseholdCode.normalize(code)
        if (cleanName.isEmpty()) fail(HogarError.EMPTY_NAME)
        if (cleanHousehold.isEmpty()) fail(HogarError.EMPTY_HOUSEHOLD_NAME)
        RecoveryUser.validate(recoveryUser, cleanName)?.let(::fail)
        if (!HouseholdCode.isValid(cleanCode)) fail(HogarError.INVALID_CODE)

        return transaction {
            if (householdByCode(cleanCode) != null) fail(HogarError.CODE_TAKEN)
            val householdId = newId()
            q.insertHousehold(householdId, cleanHousehold, cleanCode, now())
            val memberId = newId()
            q.insertMember(
                id = memberId,
                householdId = householdId,
                name = cleanName,
                nameKey = memberKey(cleanName),
                isAdmin = true,
                isApproved = true,
                recoveryKey = RecoveryUser.hash(recoveryUser),
                createdAt = now(),
            )
            member(memberId)!!
        }
    }

    // endregion

    // region 3.2 Unirse (queda pendiente de aprobacion)

    fun joinHousehold(name: String, recoveryUser: String, code: String) {
        val cleanName = name.trim()
        val cleanCode = HouseholdCode.normalize(code)
        if (cleanName.isEmpty()) fail(HogarError.EMPTY_NAME)
        RecoveryUser.validate(recoveryUser, cleanName)?.let(::fail)
        if (!HouseholdCode.isValid(cleanCode)) fail(HogarError.INVALID_CODE)
        val household = householdByCode(cleanCode) ?: fail(HogarError.CODE_NOT_FOUND)

        val nameKey = memberKey(cleanName)
        val existing = members(household.id)
        if (existing.any { it.nameKey == nameKey }) fail(HogarError.NAME_TAKEN)
        // Evita que el nombre o el usuario de esta persona se confundan con los de otra al iniciar sesion.
        if (nameClashesWithRecovery(cleanName, existing, excluding = null)) fail(HogarError.NAME_TAKEN)
        if (recoveryClashes(recoveryUser, existing, excluding = null)) fail(HogarError.RECOVERY_USER_TAKEN)

        transaction {
            q.insertMember(
                id = newId(),
                householdId = household.id,
                name = cleanName,
                nameKey = nameKey,
                isAdmin = false,
                isApproved = false,
                recoveryKey = RecoveryUser.hash(recoveryUser),
                createdAt = now(),
            )
            // Si alguna vez lo eliminaron, ya no debe ver ese aviso.
            q.deleteRemovedNotices(household.id, nameKey)
            for (admin in existing) {
                if (admin.isAdmin && admin.isApproved) {
                    notify(admin, household, NoticeType.JOIN_REQUEST, cleanName, household.name)
                }
            }
        }
    }

    // endregion

    // region 3.3 Iniciar sesion

    fun login(nameOrUser: String, code: String): Member {
        val cleanCode = HouseholdCode.normalize(code)
        if (!HouseholdCode.isValid(cleanCode)) fail(HogarError.INVALID_CODE)
        val household = householdByCode(cleanCode) ?: fail(HogarError.CODE_NOT_FOUND)

        val all = members(household.id)
        val nameKey = memberKey(nameOrUser)
        val recoveryKey = RecoveryUser.hash(nameOrUser)
        // El nombre manda. El usuario de recuperacion solo sirve si identifica a UNA persona:
        // si dos comparten el mismo, no se adivina quien es (asi nadie entra a la cuenta de otra).
        val byRecovery = all.filter { it.recoveryKey.isNotEmpty() && it.recoveryKey == recoveryKey }
        val found = all.firstOrNull { it.nameKey == nameKey } ?: byRecovery.singleOrNull()

        if (found == null) {
            if (q.removedNotices(household.id, nameKey).executeAsList().isNotEmpty()) {
                fail(HogarError.REMOVED_FROM_HOUSEHOLD)
            }
            fail(HogarError.MEMBER_NOT_FOUND)
        }
        if (!found.isApproved) fail(HogarError.PENDING_APPROVAL)
        return found
    }

    // endregion

    // region 3.4 Recuperar codigos

    data class RecoveredHome(
        val memberId: String,
        val householdName: String,
        val memberName: String,
        val code: String,
        val isAdmin: Boolean,
    )

    fun recoverHomes(recoveryUser: String): List<RecoveredHome> {
        if (RecoveryUser.normalize(recoveryUser).isEmpty()) return emptyList()
        return q.approvedMembersByRecoveryKey(RecoveryUser.hash(recoveryUser)).executeAsList().mapNotNull { member ->
            val household = household(member.householdId) ?: return@mapNotNull null
            RecoveredHome(member.id, household.name, member.name, household.code, member.isAdmin)
        }
    }

    // endregion

    // region 5. Acciones de administrador

    fun renameHousehold(actorId: String, householdId: String, newName: String) {
        val (_, household) = requireAdmin(actorId, householdId)
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) fail(HogarError.EMPTY_HOUSEHOLD_NAME)
        if (cleanName == household.name) return

        transaction {
            q.renameHousehold(cleanName, householdId)
            for (member in approvedMembers(householdId)) {
                if (!member.isAdmin) notify(member, household, NoticeType.RENAMED, household.name, cleanName)
            }
        }
    }

    fun removeMember(actorId: String, memberId: String) {
        val target = member(memberId) ?: fail(HogarError.MEMBER_NOT_FOUND)
        val (admin, household) = requireAdmin(actorId, target.householdId)
        if (target.id == admin.id || target.isAdmin) fail(HogarError.NOT_ALLOWED)

        transaction {
            if (target.isApproved) {
                q.insertNotice(
                    id = newId(),
                    householdId = household.id,
                    recipientMemberId = target.id,
                    recipientNameKey = target.nameKey,
                    type = NoticeType.REMOVED.name,
                    arg1 = household.name,
                    arg2 = "",
                    createdAt = now(),
                )
                for (member in approvedMembers(household.id)) {
                    if (!member.isAdmin && member.id != target.id) {
                        notify(member, household, NoticeType.MEMBER_REMOVED, target.name)
                    }
                }
            }
            q.deleteMember(target.id)
        }
    }

    fun acceptRequest(actorId: String, memberId: String) {
        val target = member(memberId) ?: fail(HogarError.MEMBER_NOT_FOUND)
        val (_, household) = requireAdmin(actorId, target.householdId)
        if (target.isApproved) return

        transaction {
            for (member in approvedMembers(household.id)) {
                if (!member.isAdmin) notify(member, household, NoticeType.JOINED, target.name)
            }
            q.approveMember(target.id)
        }
    }

    fun rejectRequest(actorId: String, memberId: String) {
        val target = member(memberId) ?: fail(HogarError.MEMBER_NOT_FOUND)
        requireAdmin(actorId, target.householdId)
        if (target.isApproved) return
        transaction { q.deleteMember(target.id) }
    }

    // endregion

    // region 6. Salir del hogar

    enum class LeaveOutcome { LEFT, HOUSEHOLD_DELETED }

    /** La sesion del dispositivo se debe cerrar antes de llamar a esto. */
    fun leaveHousehold(memberId: String): LeaveOutcome {
        val me = member(memberId) ?: fail(HogarError.MEMBER_NOT_FOUND)
        val household = household(me.householdId) ?: fail(HogarError.CODE_NOT_FOUND)

        return transaction {
            val others = approvedMembers(household.id).filter { it.id != me.id }
            if (me.isAdmin) {
                val successor = others.minByOrNull { it.createdAt }
                if (successor == null) {
                    deleteEverything(household.id)
                    return@transaction LeaveOutcome.HOUSEHOLD_DELETED
                }
                q.makeAdmin(successor.id)
                notify(successor, household, NoticeType.ADMIN, household.name)
            }
            q.deleteMember(me.id)
            for (member in others) notify(member, household, NoticeType.LEFT, me.name)
            LeaveOutcome.LEFT
        }
    }

    // endregion

    // region 7. Mi nombre y mi usuario de recuperacion

    fun changeMyName(memberId: String, newName: String) {
        val me = member(memberId) ?: fail(HogarError.MEMBER_NOT_FOUND)
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) fail(HogarError.EMPTY_NAME)
        if (cleanName == me.name) return

        val newKey = memberKey(cleanName)
        val all = members(me.householdId)
        if (all.any { it.id != me.id && it.nameKey == newKey }) fail(HogarError.NAME_TAKEN)
        if (me.recoveryKey.isNotEmpty() && RecoveryUser.hash(cleanName) == me.recoveryKey) {
            fail(HogarError.NAME_SAME_AS_RECOVERY_USER)
        }
        if (nameClashesWithRecovery(cleanName, all, excluding = me.id)) fail(HogarError.NAME_TAKEN)

        transaction { q.updateMemberName(cleanName, newKey, me.id) }
    }

    fun changeRecoveryUser(memberId: String, current: String, new: String, repeatNew: String) {
        val me = member(memberId) ?: fail(HogarError.MEMBER_NOT_FOUND)
        val hasCurrent = me.recoveryKey.isNotEmpty()
        if (hasCurrent && RecoveryUser.hash(current) != me.recoveryKey) fail(HogarError.RECOVERY_CURRENT_WRONG)
        RecoveryUser.validate(new, me.name)?.let(::fail)
        if (RecoveryUser.normalize(new) != RecoveryUser.normalize(repeatNew)) fail(HogarError.RECOVERY_MISMATCH)
        val newKey = RecoveryUser.hash(new)
        if (hasCurrent && newKey == me.recoveryKey) fail(HogarError.RECOVERY_SAME_AS_CURRENT)
        if (recoveryClashes(new, members(me.householdId), excluding = me.id)) fail(HogarError.RECOVERY_USER_TAKEN)

        transaction { q.updateRecoveryKey(newKey, me.id) }
    }

    // endregion

    // region 8. Entrega de avisos

    /**
     * "Claim" atomico: marca el aviso como entregado solo si nadie lo hizo antes.
     * Devuelve true si esta llamada lo reclamo (y por lo tanto debe mostrarlo).
     */
    fun claim(noticeId: String): Boolean = try {
        db.transactionWithResult {
            val notice = q.noticeById(noticeId).executeAsOneOrNull()
            if (notice == null || notice.delivered) {
                false
            } else {
                q.markNoticeDelivered(noticeId)
                true
            }
        }
    } catch (_: Exception) {
        false
    }

    // endregion

    // region Ayudantes

    /**
     * Comprueba en la capa de datos que quien actua existe, es admin, esta aprobado
     * y pertenece al hogar. Esconder botones no basta.
     */
    private fun requireAdmin(actorId: String, householdId: String): Pair<Member, Household> {
        val actor = member(actorId)
        val household = household(householdId)
        if (actor == null || !actor.isAdmin || !actor.isApproved ||
            actor.householdId != householdId || household == null
        ) {
            fail(HogarError.NOT_ALLOWED)
        }
        return actor to household
    }

    private fun notify(recipient: Member, household: Household, type: NoticeType, arg1: String = "", arg2: String = "") {
        q.insertNotice(
            id = newId(),
            householdId = household.id,
            recipientMemberId = recipient.id,
            recipientNameKey = recipient.nameKey,
            type = type.name,
            arg1 = arg1,
            arg2 = arg2,
            createdAt = now(),
        )
    }

    /** ¿Ese nombre es el usuario de recuperacion de otra persona del hogar? */
    private fun nameClashesWithRecovery(name: String, members: List<Member>, excluding: String?): Boolean {
        val key = RecoveryUser.hash(name)
        return members.any { it.id != excluding && it.recoveryKey.isNotEmpty() && it.recoveryKey == key }
    }

    /** ¿Ese usuario de recuperacion ya lo usa otra persona, o es el nombre de otra persona? */
    private fun recoveryClashes(recoveryUser: String, members: List<Member>, excluding: String?): Boolean {
        val key = RecoveryUser.hash(recoveryUser)
        val normalized = RecoveryUser.normalize(recoveryUser)
        return members.any { it.id != excluding && (it.recoveryKey == key || it.nameKey == normalized) }
    }

    /**
     * Borra el hogar con sus integrantes y avisos. Los modulos que agreguen datos
     * por hogar (tareas, finanzas, mascota, calendario) deben borrarlos tambien aqui.
     */
    private fun deleteEverything(householdId: String) {
        q.deleteNoticesByHousehold(householdId)
        q.deleteMembersByHousehold(householdId)
        q.deleteHousehold(householdId)
    }

    private fun <T> transaction(work: () -> T): T = try {
        db.transactionWithResult { work() }
    } catch (e: HogarException) {
        throw e
    } catch (_: Exception) {
        throw HogarException(HogarError.UNKNOWN)
    }

    private fun fail(error: HogarError): Nothing = throw HogarException(error)

    // endregion
}

@OptIn(ExperimentalUuidApi::class)
private fun newId(): String = Uuid.random().toString()

@OptIn(ExperimentalTime::class)
private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
