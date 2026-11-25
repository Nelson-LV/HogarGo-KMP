package com.hogargo.kmp.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.db.Household
import com.hogargo.kmp.db.Member
import com.hogargo.kmp.household.data.HogarError
import com.hogargo.kmp.household.data.HogarException
import com.hogargo.kmp.household.data.HouseholdRepository
import com.hogargo.kmp.household.data.SessionStore
import com.hogargo.kmp.household.notifications.HouseholdNotifier
import com.hogargo.kmp.household.notifications.noticeText
import com.hogargo.kmp.l10n.L10n
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la sesion del dispositivo. */
sealed interface SessionPhase {
    /** Restaurando la sesion (pantalla vacia para no parpadear). */
    data object Loading : SessionPhase
    data object Welcome : SessionPhase
    data class SignedIn(val memberId: String, val householdId: String) : SessionPhase
}

/**
 * Estado global de hogares: quien tiene la sesion abierta, en que hogar,
 * y las acciones de acceso, perfil y administrador. Las reglas viven en el repositorio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdViewModel(
    val repository: HouseholdRepository,
    private val session: SessionStore,
    private val notifier: HouseholdNotifier,
) : ViewModel() {

    private val _phase = MutableStateFlow<SessionPhase>(SessionPhase.Loading)
    val phase: StateFlow<SessionPhase> = _phase.asStateFlow()

    /** Mensajes breves tipo "toast" (se muestran en un Snackbar). */
    private val _toasts = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toasts: SharedFlow<String> = _toasts

    private val signedIn get() = _phase.value as? SessionPhase.SignedIn

    val household: StateFlow<Household?> = _phase
        .flatMapLatest { if (it is SessionPhase.SignedIn) repository.observeHousehold(it.householdId) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Integrantes del hogar activo, incluidas las solicitudes pendientes. */
    val members: StateFlow<List<Member>> = _phase
        .flatMapLatest { if (it is SessionPhase.SignedIn) repository.observeMembers(it.householdId) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Integrante vivo en la base (el rol de admin se lee de aqui, no de la sesion). */
    val me: StateFlow<Member?> = combine(_phase, members) { phase, list ->
        (phase as? SessionPhase.SignedIn)?.let { s -> list.firstOrNull { it.id == s.memberId } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var sessionJob: Job? = null

    init {
        restore()
    }

    // region Sesion

    private fun restore() {
        val saved = session.load()
        val member = saved?.let { repository.member(it.memberId) }
        if (saved == null || member == null || member.householdId != saved.householdId ||
            !member.isApproved || repository.household(saved.householdId) == null
        ) {
            session.clear()
            _phase.value = SessionPhase.Welcome
            return
        }
        start(member)
    }

    private fun start(member: Member) {
        session.save(SessionStore.Session(member.id, member.householdId))
        _phase.value = SessionPhase.SignedIn(member.id, member.householdId)
        notifier.requestPermission()
        watchSession(member.id, member.householdId)
    }

    /** Cierra la sesion del dispositivo. Sigues siendo integrante y los datos se conservan. */
    fun logout() {
        sessionJob?.cancel()
        sessionJob = null
        session.clear()
        _phase.value = SessionPhase.Welcome
    }

    /**
     * Salir del hogar: primero se cierra la sesion para que la UI no reaccione
     * a que "mi fila desaparecio".
     */
    fun leaveHousehold(): HogarError? {
        val memberId = signedIn?.memberId ?: return null
        logout()
        return attempt { repository.leaveHousehold(memberId) }
    }

    /**
     * Mientras haya sesion: entrega los avisos pendientes como notificaciones y
     * cierra la sesion si me eliminaron con la app abierta.
     */
    private fun watchSession(memberId: String, householdId: String) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            launch {
                repository.observeMember(memberId).collect { member ->
                    if (member == null &&
                        (repository.members(householdId).isNotEmpty() || repository.household(householdId) == null)
                    ) {
                        logout()
                    }
                }
            }
            launch {
                repository.observePendingNotices(memberId).collect { notices ->
                    for (notice in notices) {
                        // Solo quien reclama el aviso lo muestra: asi nunca se duplica.
                        if (repository.claim(notice.id)) {
                            noticeText(notice)?.let { (title, body) -> notifier.show(notice.id, title, body) }
                        }
                    }
                }
            }
        }
    }

    // endregion

    // region Acceso (bienvenida)

    fun suggestCode(): String = repository.suggestCode()

    fun createHousehold(name: String, householdName: String, recoveryUser: String, code: String): HogarError? =
        attempt { start(repository.createHousehold(name, householdName, recoveryUser, code)) }

    fun joinHousehold(name: String, recoveryUser: String, code: String): HogarError? =
        attempt { repository.joinHousehold(name, recoveryUser, code) }

    fun login(nameOrUser: String, code: String): HogarError? =
        attempt { start(repository.login(nameOrUser, code)) }

    fun recoverHomes(recoveryUser: String): List<HouseholdRepository.RecoveredHome> =
        repository.recoverHomes(recoveryUser)

    fun enter(home: HouseholdRepository.RecoveredHome): Boolean =
        login(home.memberName, home.code) == null

    // endregion

    // region Perfil y administrador

    fun changeMyName(newName: String): HogarError? = withMember { me ->
        repository.changeMyName(me, newName)
        showToast(L10n.nameUpdated)
    }

    fun changeRecoveryUser(current: String, new: String, repeatNew: String): HogarError? = withMember { me ->
        repository.changeRecoveryUser(me, current, new, repeatNew)
        showToast(L10n.usernameSaved)
    }

    fun renameHousehold(newName: String): HogarError? {
        val s = signedIn ?: return HogarError.NOT_ALLOWED
        return attempt {
            repository.renameHousehold(s.memberId, s.householdId, newName)
            showToast(L10n.homeRenamed)
        }
    }

    fun removeMember(memberId: String): HogarError? = withMember { repository.removeMember(it, memberId) }

    fun acceptRequest(memberId: String): HogarError? = withMember { repository.acceptRequest(it, memberId) }

    fun rejectRequest(memberId: String): HogarError? = withMember { repository.rejectRequest(it, memberId) }

    // endregion

    fun showToast(message: String) {
        _toasts.tryEmit(message)
    }

    private inline fun withMember(block: (memberId: String) -> Unit): HogarError? {
        val memberId = signedIn?.memberId ?: return HogarError.NOT_ALLOWED
        return attempt { block(memberId) }
    }

    private inline fun attempt(block: () -> Unit): HogarError? = try {
        block()
        null
    } catch (e: HogarException) {
        e.error
    } catch (_: Exception) {
        HogarError.UNKNOWN
    }
}
