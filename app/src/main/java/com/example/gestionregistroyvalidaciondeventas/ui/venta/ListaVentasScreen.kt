package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVentasScreen(
    navController: NavController,
    viewModel: VentaViewModel
) {
    val ventas by viewModel.ventasList.collectAsState(initial = emptyList())
    val busqueda by viewModel.busqueda.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val context = LocalContext.current

    val ventasFiltradas = ventas.filter { venta ->
        val coincideEstado = filtroEstado == "Todas" || venta.estado == filtroEstado
        val coincideTexto = busqueda.isBlank() ||
                venta.codigoAutorizacion.contains(busqueda, ignoreCase = true) ||
                venta.cliente.contains(busqueda, ignoreCase = true) ||
                venta.tipoServicio.contains(busqueda, ignoreCase = true)
        coincideEstado && coincideTexto
    }

    val totalAcumulado = ventas.sumOf { it.monto.toDoubleOrNull() ?: 0.0 }.toInt()
    val totalBorradores = ventas.count { it.estado == "Borrador" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Ventas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.limpiarFormulario()
                    navController.navigate("ventaForm")
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar Venta")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Ventas: ${ventas.size}", style = MaterialTheme.typography.labelLarge)
                        Text("Borradores: $totalBorradores", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Monto Acumulado", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "$$totalAcumulado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por código, cliente o servicio") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todas", "Borrador", "Recibida").forEach { opcion ->
                    FilterChip(
                        selected = filtroEstado == opcion,
                        onClick = { viewModel.onFiltroEstadoChange(opcion) },
                        label = { Text(opcion) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (ventasFiltradas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No hay ventas registradas para este filtro. Pulsa (+) para crear una nueva venta.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ventasFiltradas) { venta ->
                        VentaItemCard(
                            venta = venta,
                            onClick = {
                                if (venta.estado == "Borrador") {
                                    viewModel.cargarVenta(venta.id)
                                    navController.navigate("ventaForm")
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Venta en estado '${venta.estado}': bloqueada para modificación",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onDelete = { viewModel.eliminarVenta(venta) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VentaItemCard(
    venta: VentaEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Código: ${venta.codigoAutorizacion}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = {},
                    label = { Text(venta.estado) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (venta.estado == "Borrador")
                            Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                        labelColor = if (venta.estado == "Borrador")
                            Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Convenio/Servicio: ${venta.tipoServicio}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Cliente: ${venta.cliente} | Fecha: ${venta.fechaOperacion}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Monto: $${venta.monto}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (venta.respaldoUri != null) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (venta.respaldoUri != null) "Con respaldo fotográfico" else "Sin respaldo adjunto",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (venta.estado == "Borrador") {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar borrador",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}