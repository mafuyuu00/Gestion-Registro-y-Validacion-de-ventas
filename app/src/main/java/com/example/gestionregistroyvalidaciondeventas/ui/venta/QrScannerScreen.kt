package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gestionregistroyvalidaciondeventas.utils.CameraPermissionHelper
import com.example.gestionregistroyvalidaciondeventas.utils.QrScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavController,
    viewModel: QrViewModel = viewModel(),
    ventaViewModel: VentaViewModel? = null
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(CameraPermissionHelper.hasCameraPermission(context))
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara para escanear QR", Toast.LENGTH_LONG).show()
        }
    }

    val qrResult by viewModel.qrResult.observeAsState()
    var isScanning by remember { mutableStateOf(true) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val qr = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE && it.rawValue != null }
                        if (qr != null) {
                            viewModel.onQrDetected(qr.rawValue!!)
                            isScanning = false
                            Toast.makeText(context, "¡QR leído desde la galería!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No se encontró ningún código QR en la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al procesar la imagen de la galería", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lector QR / Validación") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!hasCameraPermission) {
                Text(
                    "Permiso de cámara requerido",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Para escanear códigos QR y validar ventas, necesitamos acceso a la cámara.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Conceder permiso de cámara")
                }
            } else if (qrResult == null && isScanning) {
                Text(
                    "Escanea un código QR",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    QrScanner(
                        onQrCodeScanned = { qrContent ->
                            viewModel.onQrDetected(qrContent)
                            isScanning = false
                            Toast.makeText(context, "¡Código QR detectado!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        modifier = Modifier
                            .size(250.dp)
                            .align(Alignment.Center),
                        color = Color.Transparent,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    ) {}
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Enfoca el código QR dentro del marco central",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Elegir QR desde la Galería")
                }
            } else if (qrResult != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        "QR Detectado / Venta Validada:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = qrResult!!.content,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val scannedText = qrResult!!.content
                            if (ventaViewModel != null) {
                                ventaViewModel.onCodigoAutorizacionChange(scannedText)
                                Toast.makeText(context, "Código aplicado al formulario de venta", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Usar como Código de Autorización")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.clearResult()
                            isScanning = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Escanear otro código QR")
                    }
                }
            }
        }
    }
}
