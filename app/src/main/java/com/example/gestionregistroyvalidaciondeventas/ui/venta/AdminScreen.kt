package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.utils.PdfGenerator
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
    var mostrarPosts by remember { mutableStateOf(false) }
    val posts by viewModel.postList.collectAsState()
    var qrPendiente by remember { mutableStateOf<Pair<Bitmap, String>?>(null) }

    fun guardarQrEnGaleria(bitmap: Bitmap, nombre: String) {
        val guardado = QrGenerator.saveBitmap(context, bitmap, nombre)
        Toast.makeText(
            context,
            if (guardado) "QR guardado en la galería (Imágenes/GestionVentas)" else "No se pudo guardar el QR",
            Toast.LENGTH_LONG
        ).show()
    }

    val permisoAlmacenamientoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        qrPendiente?.let { (bitmap, nombre) ->
            if (concedido) {
                guardarQrEnGaleria(bitmap, nombre)
            } else {
                Toast.makeText(context, "Se necesita permiso de almacenamiento para guardar el QR", Toast.LENGTH_LONG).show()
            }
        }
        qrPendiente = null
    }

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
                Text("Sincronizar Ventas con microservicio ms-ventas")
            }

            OutlinedButton(
                onClick = {
                    viewModel.fetchPosts()
                    mostrarPosts = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Consultar API externa (JSONPlaceholder)")
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

                                if (venta.estado == VentaViewModel.ESTADO_EN_REVISION ||
                                    venta.estado == VentaViewModel.ESTADO_RECIBIDA
                                ) {
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

                                if (venta.estado == VentaViewModel.ESTADO_EN_REVISION) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = { navController.navigate("qrScanner/${venta.id}") },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Validar QR de la venta")
                                    }
                                }

                                if (venta.estado == VentaViewModel.ESTADO_RECIBIDA) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            val file = PdfGenerator.generarPdfVenta(context, venta)
                                            Toast.makeText(context, "Comprobante PDF generado en:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Descargar Comprobante PDF")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarPosts) {
        AlertDialog(
            onDismissRequest = { mostrarPosts = false },
            title = { Text("Listado de Posts (API externa)") },
            text = {
                if (posts.isEmpty()) {
                    Text("Cargando publicaciones...")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(posts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Título: ${post.title}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = post.body,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPosts = false }) { Text("Cerrar") }
            }
        )
    }

    selectedVentaForQr?.let { venta ->
        val qrContent = viewModel.contenidoQr(venta)
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
                        val nombre = "QR_${venta.codigoAutorizacion}"
                        val requierePermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                                PackageManager.PERMISSION_GRANTED
                        if (requierePermiso) {
                            qrPendiente = qrBitmap to nombre
                            permisoAlmacenamientoLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            guardarQrEnGaleria(qrBitmap, nombre)
                        }
                        selectedVentaForQr = null
                    }
                ) {
                    Text("Descargar QR a la galería")
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
