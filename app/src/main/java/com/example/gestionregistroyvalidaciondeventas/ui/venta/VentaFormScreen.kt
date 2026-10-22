package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaFormScreen(
    navController: NavController,
    viewModel: VentaViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    var menuExpandido by remember { mutableStateOf(false) }
    val conveniosDisponibles = listOf(
        "Convenio Salud Preferente",
        "Seguro Automotriz Full",
        "Plan Telecomunicaciones Pyme",
        "Convenio Dental Corporativo",
        "Asistencia Hogar 24/7"
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val rutaArchivo = guardarBitmapEnArchivos(context, bitmap)
            viewModel.onRespaldoUriChange(rutaArchivo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (estado.id == 0) "Registrar Nueva Venta" else "Modificar Venta #${estado.id}")
                },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = { menuExpandido = !menuExpandido }
            ) {
                OutlinedTextField(
                    value = estado.tipoServicio,
                    onValueChange = viewModel::onTipoServicioChange,
                    label = { Text("Tipo de convenio o servicio *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                    },
                    isError = estado.errores.tipoServicio != null,
                    supportingText = {
                        estado.errores.tipoServicio?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    conveniosDisponibles.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                viewModel.onTipoServicioChange(opcion)
                                menuExpandido = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = estado.fechaOperacion,
                onValueChange = viewModel::onFechaOperacionChange,
                label = { Text("Fecha de operación (DD/MM/AAAA) *") },
                trailingIcon = {
                    IconButton(onClick = {
                        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        viewModel.onFechaOperacionChange(hoy)
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Usar fecha de hoy")
                    }
                },
                isError = estado.errores.fechaOperacion != null,
                supportingText = {
                    estado.errores.fechaOperacion?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.cliente,
                onValueChange = viewModel::onClienteChange,
                label = { Text("Nombre de cliente de mentira *") },
                isError = estado.errores.cliente != null,
                supportingText = {
                    estado.errores.cliente?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.monto,
                onValueChange = viewModel::onMontoChange,
                label = { Text("Monto de mentira ($) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = estado.errores.monto != null,
                supportingText = {
                    estado.errores.monto?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.codigoAutorizacion,
                onValueChange = viewModel::onCodigoAutorizacionChange,
                label = { Text("Código de autorización (Ej: AUT-101) *") },
                isError = estado.errores.codigoAutorizacion != null,
                supportingText = {
                    estado.errores.codigoAutorizacion?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.observaciones,
                onValueChange = viewModel::onObservacionesChange,
                label = { Text("Observaciones (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (estado.respaldoUri != null) "Respaldo capturado" else "Sin respaldo fotográfico",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = { cameraLauncher.launch() }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capturar Foto")
                        }
                    }
                    estado.respaldoUri?.let { ruta ->
                        val bitmap = remember(ruta) { BitmapFactory.decodeFile(ruta) }
                        if (bitmap != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Vista previa de respaldo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (viewModel.validarFormulario()) {
                            viewModel.guardar(estadoDestino = "Borrador") {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar Borrador")
                }

                Button(
                    onClick = {
                        if (viewModel.validarFormulario()) {
                            viewModel.guardar(estadoDestino = "Recibida") {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enviar (Recibida)")
                }
            }
        }
    }
}

private fun guardarBitmapEnArchivos(context: Context, bitmap: Bitmap): String {
    val archivo = File(context.filesDir, "respaldo_${System.currentTimeMillis()}.jpg")
    FileOutputStream(archivo).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    }
    return archivo.absolutePath
}