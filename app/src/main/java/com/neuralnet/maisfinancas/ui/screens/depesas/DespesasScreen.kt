package com.neuralnet.maisfinancas.ui.screens.depesas

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuralnet.maisfinancas.model.Despesa
import com.neuralnet.maisfinancas.ui.components.ItemDespesa
import com.neuralnet.maisfinancas.ui.components.toReal
import com.neuralnet.maisfinancas.ui.navigation.MaisFinancasTopAppBar
import com.neuralnet.maisfinancas.ui.navigation.graphs.HomeDestinations
import com.neuralnet.maisfinancas.ui.theme.MaisFinancasTheme

@Composable
fun DespesasScreen(viewModel: DespesaViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    DespesasScreen(uiState = uiState.value)
}

@Composable
fun DespesasScreen(
    uiState: DespesasUiState,
) {
    Scaffold(
        topBar = {
            MaisFinancasTopAppBar(
                title = HomeDestinations.DespesasGraph.title,
                canNavigateBack = false,
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {

            for (grupos in uiState.despesas.groupBy { it.categoria }) {
                item(grupos.key) {
                    Row {
                        Text(
                            text = grupos.key,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )
                        Text(
                            text = "(${valorPorCategoria(grupos)})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                items(grupos.value, key = { it.nome }) { despesa ->
                    ItemDespesa(
                        despesa = despesa,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DespesasScreenPreview() {
    MaisFinancasTheme {
        DespesasScreen(
            uiState = DespesasUiState(
                listOf(
                    Despesa("Água", "Essenciais", 100.0, "Mensal", false, 1693577802000),
                    Despesa("Energia", "Essenciais", 123.0, "Mensal", true, 1693577802000),
                    Despesa("Almoço", "Alimentação", 30.0, "Diária", true, 1693064202000),
                    Despesa("Cinema", "Entretenimento", 70.0, "Nenhuma", false, 1693564202000),
                    Despesa("Jantar Restaurante", "Alimentação", 40.0, "Nenhuma", false, 1693064202000),
                )
            )
        )
    }
}

private fun valorPorCategoria(despesas: Map.Entry<String, List<Despesa>>): String {
    return despesas.value
        .filter { despesa -> despesa.categoria == despesas.key }
        .sumOf { it.valor }
        .toReal()
}
