package com.neuralnet.maisfinancas.ui.screens.depesas.detalhes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuralnet.maisfinancas.R
import com.neuralnet.maisfinancas.ui.components.despesa.DetalhesDespesaItem
import com.neuralnet.maisfinancas.ui.components.despesa.InserirRegistroBottomSheet
import com.neuralnet.maisfinancas.ui.components.despesa.RegistroDespesaItem
import com.neuralnet.maisfinancas.ui.navigation.MaisFinancasTopAppBar
import com.neuralnet.maisfinancas.ui.screens.ConnectionState
import com.neuralnet.maisfinancas.ui.screens.LoadingScreen
import com.neuralnet.maisfinancas.ui.screens.ServidorIndisponivel
import com.neuralnet.maisfinancas.ui.theme.MaisFinancasTheme
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesDespesaScreen(
    viewModel: DetalhesDespesaViewModel,
    onNavigateUp: () -> Unit,
    calendarState: DatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val registroUiState = viewModel.registroUiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val connectionState = viewModel.connectionState.collectAsStateWithLifecycle()
    val connectionMessage = connectionState.value.message?.let { stringResource(id = it) }

    when (connectionState.value) {
        is ConnectionState.Loading -> LoadingScreen()
        is ConnectionState.ServerUnavailable -> {
            ServidorIndisponivel {
                viewModel.adicionarRegistro(calendarState.selectedDateMillis)
            }
        }

        else -> {
            DetalhesDespesaScreen(
                uiState = uiState.value,
                onNavigateUp = onNavigateUp,
                onCheckChange = viewModel::setDefinirLembrete,
                registroState = registroUiState.value,
                onRegistroChange = viewModel::updateRegistroState,
                calendarState = calendarState,
                onSaveClick = {
                    if (viewModel.isRegistroValid()) {
                        viewModel.adicionarRegistro(calendarState.selectedDateMillis)
                        isSheetOpen = false
                        calendarState.selectedDateMillis = Instant.now().toEpochMilli()
                    }
                },
                sheetState = sheetState,
                connectionMessage = connectionMessage,
                isSheetOpen = isSheetOpen,
                onSheetStateToggle = { isSheetOpen = !isSheetOpen },
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesDespesaScreen(
    uiState: DetalhesDespesaUiState,
    onNavigateUp: () -> Unit,
    onCheckChange: (Boolean) -> Unit,
    registroState: RegistroUiState,
    onRegistroChange: (RegistroUiState) -> Unit,
    calendarState: DatePickerState = rememberDatePickerState(),
    onSaveClick: () -> Unit,
    sheetState: SheetState,
    isSheetOpen: Boolean,
    onSheetStateToggle: () -> Unit,
    connectionMessage: String? = null,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            MaisFinancasTopAppBar(
                title = stringResource(id = R.string.detalhes_despesa),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            item {
                LaunchedEffect(key1 = connectionMessage) {
                    launch {

                        if (connectionMessage != null) {
                            snackbarHostState.showSnackbar(message = connectionMessage)
                        }
                    }
                }
            }

            item {
                DetalhesDespesaItem(
                    nome = uiState.nome,
                    categoria = uiState.categoria,
                    definirLembrete = uiState.definirLembrete,
                    recorrencia = uiState.recorrencia,
                    onCheckChange = onCheckChange,
                )
            }

            item {

                Row(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = stringResource(R.string.registros),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSheetStateToggle) {
                        Text(
                            text = stringResource(R.string.adicionar),
                        )
                    }
                }

                if (isSheetOpen) {
                    InserirRegistroBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = onSheetStateToggle,
                        valor = registroState.valor,
                        valorErrorField = registroState.valorErrorField,
                        onValueChange = {
                            onRegistroChange(registroState.copy(valor = it, valorErrorField = null))
                        },
                        calendarState = calendarState,
                        onSaveClick = onSaveClick,
                    )
                }
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.data),
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    Text(
                        text = stringResource(id = R.string.valor),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            items(items = uiState.registros) { registro ->
                RegistroDespesaItem(registro)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DetalhesDespesaScreenPreview() {
    MaisFinancasTheme {
        DetalhesDespesaScreen(
            uiState = DetalhesDespesaUiState(),
            onNavigateUp = {},
            onCheckChange = {},
            registroState = RegistroUiState(),
            onRegistroChange = {},
            onSaveClick = {},
            sheetState = rememberModalBottomSheetState(),
            isSheetOpen = false,
            onSheetStateToggle = {},
        )
    }
}
