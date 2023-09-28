package com.neuralnet.maisfinancas.ui.screens.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralnet.maisfinancas.data.repository.DespesaRepository
import com.neuralnet.maisfinancas.data.repository.GestorRepository
import com.neuralnet.maisfinancas.data.room.model.GestorEntity
import com.neuralnet.maisfinancas.model.Despesa
import com.neuralnet.maisfinancas.ui.screens.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    gestorRepository: GestorRepository,
    despesaRepository: DespesaRepository,
) : ViewModel() {

    private val gestorId: UUID? = savedStateHandle["gestor_id"] ?:
        UUID.fromString("00a7b810-9dad-11d1-80b4-00c04fd430c8")
    private val gestor: Flow<GestorEntity?> = gestorRepository.getGestor(gestorId)

    private val hasLoggedIn: Flow<Boolean> = gestor.map { it != null }

    val authState: StateFlow<AuthState> = hasLoggedIn.map { hasLoggedIn ->
        if (hasLoggedIn) {
            AuthState.LoggedIn
        } else {
            AuthState.NotLoggedIn
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AuthState.Loading
    )

    private val depesas: Flow<List<Despesa>> = despesaRepository.getDespesas(gestorId)

    val uiState: StateFlow<HomeUiState> = depesas.map { despesas ->
        val ultimaSemana = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }

        val despesasSemana = despesas.filter { it.data.after(ultimaSemana) }

        HomeUiState(
            gastoMensal = 0.0,
            saldoMensal = 0.0,
            despesasSemanais = despesasSemana.sumOf { it.valor.toDouble() },
            rendaSemanal = 0.0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HomeUiState()
    )

}