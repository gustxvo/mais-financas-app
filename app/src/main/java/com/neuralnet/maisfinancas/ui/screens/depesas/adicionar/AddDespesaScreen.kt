package com.neuralnet.maisfinancas.ui.screens.depesas.adicionar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuralnet.maisfinancas.R
import com.neuralnet.maisfinancas.model.despesa.Frequencia
import com.neuralnet.maisfinancas.model.despesa.Recorrencia
import com.neuralnet.maisfinancas.ui.components.core.AppDropdown
import com.neuralnet.maisfinancas.ui.components.core.NumberTextField
import com.neuralnet.maisfinancas.ui.components.despesa.ValorDescricaoTextField
import com.neuralnet.maisfinancas.ui.components.despesa.RecorrenciaDespesa
import com.neuralnet.maisfinancas.ui.components.despesa.getDate
import com.neuralnet.maisfinancas.ui.navigation.MaisFinancasTopAppBar
import com.neuralnet.maisfinancas.ui.navigation.graphs.DespesasDestinations
import com.neuralnet.maisfinancas.ui.theme.MaisFinancasTheme
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDespesaScreen(
    viewModel: AddDespesaViewModel,
    calendarState: DatePickerState = rememberDatePickerState(),
    onNavigateUp: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val categoriasState = viewModel.categorias.collectAsStateWithLifecycle()

    val categorias by remember {
        derivedStateOf {
            categoriasState.value.map { it.nome }
        }
    }

    AddDespesaScreen(
        uiState = uiState.value,
        onUiStateChanged = viewModel::updateUiState,
        categorias = categorias,
        calendarState = calendarState,
        onNavigateUp = onNavigateUp,
        onSaveClick = onSaveClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDespesaScreen(
    uiState: AddDespesaUiState,
    onUiStateChanged: (AddDespesaUiState) -> Unit,
    categorias: List<String>,
    calendarState: DatePickerState,
    onNavigateUp: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MaisFinancasTopAppBar(
                title = stringResource(id = DespesasDestinations.AddDespesa.title),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(top = 8.dp)
                .verticalScroll(
                    state = rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {

            ValorDescricaoTextField(
                value = uiState.nome,
                onValueChange = {
                    onUiStateChanged(uiState.copy(nome = it, nomeErrorField = null))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                errorMessage = uiState.nomeErrorField,
            )

            NumberTextField(
                valor = uiState.valor,
                onValueChange = {
                    onUiStateChanged(uiState.copy(valor = it, valorErrorField = null))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                errorMessage = uiState.valorErrorField,
            )

            var expanded by remember { mutableStateOf(false) }
            AppDropdown(
                label = R.string.categoria,
                options = categorias,
                selectedOptionText = uiState.categoria,
                onSelectedOptionText = {
                    onUiStateChanged(uiState.copy(categoria = it, categoriaErrorField = null))
                },
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                errorMessage = uiState.categoriaErrorField,
            )

            RecorrenciaDespesa(
                frequencia = uiState.frequencia,
                onRecorrenciaChanged = { onUiStateChanged(uiState.copy(frequencia = it)) },
                modifier = Modifier.padding(top = 0.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.definir_lembrete),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    enabled = uiState.frequencia != Frequencia.NENHUMA,
                    checked = uiState.definirLembrete && uiState.frequencia != Frequencia.NENHUMA,
                    onCheckedChange = { onUiStateChanged(uiState.copy(definirLembrete = it)) }
                )
            }

            AnimatedVisibility(visible = uiState.definirLembrete) {
                if (uiState.frequencia != Frequencia.NENHUMA) {
                    val dataProximoLembrete =
                        remember(calendarState.selectedDateMillis, uiState.frequencia) {
                            derivedStateOf {
                                definirProximoLembrete(
                                    selectedDateMillis = calendarState.selectedDateMillis
                                        ?: Instant.now().toEpochMilli(),
                                    recorrencia = Recorrencia(
                                        frequencia = uiState.frequencia,
                                        quantidade = uiState.quantidadeRecorrencia
                                    )
                                )
                            }
                        }
                    Text(
                        text = stringResource(
                            R.string.prximo_lembrete,
                            dataProximoLembrete.value.timeInMillis.getDate()
                        ),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            DatePicker(
                state = calendarState,
                showModeToggle = false,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
private fun AddDespesaScreenPreview() {
    MaisFinancasTheme {
        AddDespesaScreen(
            uiState = AddDespesaUiState(
                frequencia = Frequencia.ANUAL,
                definirLembrete = true
            ),
            onUiStateChanged = {},
            categorias = listOf("Essenciais", "Entretenimento", "Saúde"),
            calendarState = rememberDatePickerState(),
            onNavigateUp = {},
            onSaveClick = {}
        )
    }
}
