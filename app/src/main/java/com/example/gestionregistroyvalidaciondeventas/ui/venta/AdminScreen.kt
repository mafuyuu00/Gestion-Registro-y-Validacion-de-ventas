package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.utils.QrGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: VentaViewModel
) {
    val ventas by viewModel.ventasList.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var selectedVentaForQr by remember { mutableStateOf<VentaEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador - Revisión de Ventas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Total de ventas en el sistema: ${ventas.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    viewModel.sincronizarConApi { resultado ->
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sincronizar Ventas con API REST (Retrofit)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (ventas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No hay ventas registradas actualmente.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ventas) { venta ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(venta.estado) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Servicio: ${venta.tipoServicio}", style = MaterialTheme.typography.bodyMedium)
                                Text("Cliente: ${venta.cliente} | Fecha: ${venta.fechaOperacion}", style = MaterialTheme.typography.bodySmall)
                                Text("Monto: \$${venta.monto}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)

                                if (venta.estado == "Recibida") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { selectedVentaForQr = venta },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ver y Descargar QR de Venta")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedVentaForQr?.let { venta ->
        val qrContent = "VENTA:${venta.codigoAutorizacion}|CLIENTE:${venta.cliente}|MONTO:${venta.monto}|FECHA:${venta.fechaOperacion}|SERVICIO:${venta.tipoServicio}"
        val qrBitmap = remember(qrContent) { QrGenerator.generateQrBitmap(qrContent) }

        AlertDialog(
            onDismissRequest = { selectedVentaForQr = null },
            title = { Text("QR de Venta: ${venta.codigoAutorizacion}") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR de la venta",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cliente: ${venta.cliente}\nMonto: \$${venta.monto}\nEstado: ${venta.estado}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = QrGenerator.saveBitmap(context, qrBitmap, "QR_${venta.codigoAutorizacion}")
                        Toast.makeText(context, "QR descargado en:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
                        selectedVentaForQr = null
                    }
                ) {
                    Text("Descargar QR (Guardar)")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedVentaForQr = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
