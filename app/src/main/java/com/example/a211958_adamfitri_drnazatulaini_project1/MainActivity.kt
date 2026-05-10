package com.example.a211958_adamfitri_drnazatulaini_project1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.a211958_adamfitri_drnazatulaini_project1.ui.theme.AppTheme

enum class AduKafeScreen(val title: String) {
    Dashboard("Halaman Utama"),
    ReportForm("Borang Aduan Harga"),
    TicketDetails("Maklumat Tiket"),
    SemakAduan("Semakan Aduan"),
    Profile("Profil Pengguna"),
    Settings("Tetapan Profil"),

    ReportDetail("Butiran Penuh Aduan")// <--- NEW!
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
                AduKafeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AduKafeAppBar(
    currentScreen: AduKafeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(currentScreen.title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        }
    )
}

private fun shareAduan(context: Context, subject: String, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(Intent.createChooser(intent, "Kongsi Aduan Harga"))
}

@Composable
fun AduKafeApp(
    viewModel: AduKafeViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val currentAduan = viewModel.currentAduan
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = AduKafeScreen.valueOf(
        backStackEntry?.destination?.route ?: AduKafeScreen.Dashboard.name
    )

    fun cancelAndGoHome() {
        viewModel.resetAduan()
        navController.popBackStack(AduKafeScreen.Dashboard.name, inclusive = false)
    }

    Scaffold(
        topBar = {
            AduKafeAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AduKafeScreen.Dashboard.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AduKafeScreen.Dashboard.name) {
                HomeDashboardScreen(
                    userName = viewModel.userName,
                    userMatric = viewModel.userMatric,
                    aduanList = viewModel.aduanList,
                    onNewReportClicked = { navController.navigate(AduKafeScreen.ReportForm.name) },
                    onSemakClicked = { navController.navigate(AduKafeScreen.SemakAduan.name) },
                    onProfileClicked = { navController.navigate(AduKafeScreen.Profile.name) },
                    onSettingsClicked = { navController.navigate(AduKafeScreen.Settings.name) },
                    onAduanClicked = { record ->
                        viewModel.selectAduan(record)
                        navController.navigate(AduKafeScreen.ReportDetail.name)
                    } // <--- ERROR 1 FIXED: Added missing brace
                )
            }

            composable(route = AduKafeScreen.ReportForm.name) {
                ReportFormScreen(
                    currentCafe = viewModel.currentAduan.cafeName,
                    currentCollege = viewModel.currentAduan.collegeLocation,
                    currentFoodName = viewModel.currentAduan.foodName,
                    currentNormalPrice = viewModel.currentAduan.normalPrice,
                    currentPrice = viewModel.currentAduan.newPrice,
                    currentDescription = viewModel.currentAduan.description,
                    onCafeChanged = { viewModel.updateCafeName(it) },
                    onCollegeChanged = { viewModel.updateCollegeLocation(it) },
                    onFoodNameChanged = { viewModel.updateFoodName(it) },
                    onNormalPriceChanged = { viewModel.updateNormalPrice(it) },
                    onPriceChanged = { viewModel.updateNewPrice(it) },
                    onDescriptionChanged = { viewModel.updateDescription(it) },
                    onSubmitClicked = {
                        viewModel.submitAduan()
                        navController.navigate(AduKafeScreen.TicketDetails.name)
                    },
                    onCancelClicked = {
                        cancelAndGoHome()
                    }
                )
            }

            composable(route = AduKafeScreen.TicketDetails.name) {
                val context = LocalContext.current
                val shareSubject = "Aduan Harga Kafe: ${currentAduan.cafeName}"
                val shareSummary =
                    "Saya telah melaporkan kenaikan harga makanan di ${currentAduan.cafeName} (${currentAduan.collegeLocation}). Harga baru dilaporkan: RM ${currentAduan.newPrice}."

                TicketDetailScreen(
                    aduanRecord = currentAduan,
                    onBackToHomeClicked = { cancelAndGoHome() },
                    onShareButtonClicked = { shareAduan(context, shareSubject, shareSummary) }
                )
            }

            composable(route = AduKafeScreen.SemakAduan.name) {
                // ERROR 2 FIXED: Added the missing onAduanClicked parameter here!
                SemakAduanScreen(
                    aduanList = viewModel.aduanList,
                    onAduanClicked = { record ->
                        viewModel.selectAduan(record)
                        navController.navigate(AduKafeScreen.ReportDetail.name)
                    }
                )
            }

            composable(route = AduKafeScreen.Profile.name) {
                ProfileScreen(
                    userName = viewModel.userName,
                    userMatric = viewModel.userMatric,
                    totalReports = viewModel.aduanList.size
                )
            }

            composable(route = AduKafeScreen.Settings.name) {
                SettingsScreen(
                    currentName = viewModel.userName,
                    currentMatric = viewModel.userMatric,
                    onNameChanged = { viewModel.updateUserName(it) },
                    onMatricChanged = { viewModel.updateUserMatric(it) },
                    onSaveClicked = {
                        navController.popBackStack()
                    }
                )
            }

            // ERROR 3 FIXED: Moved this block INSIDE the NavHost!
            composable(route = AduKafeScreen.ReportDetail.name) {
                val aduan = viewModel.selectedAduan
                if (aduan != null) {
                    ReportDetailScreen(
                        aduan = aduan,
                        onBackClicked = { navController.popBackStack() }
                    )
                }
            }
        } // <--- NavHost closes here now
    } // <--- Scaffold closes here
} // <--- AduKafeApp closes here

// ==========================================
// THE SCREENS
// ==========================================

@Composable
fun HomeDashboardScreen(
    userName: String,
    userMatric: String,
    aduanList: List<AduanRecord>,
    onNewReportClicked: () -> Unit,
    onSemakClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAduanClicked: (AduanRecord) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            Row(modifier = Modifier.padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(50.dp)) {
                    Image(painter = painterResource(id = R.drawable.gambar), contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Selamat Datang", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // NOW USES THE VARIABLE INSTEAD OF HARDCODED TEXT
                    Text(text = userName, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                    Text(text = userMatric, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.baris_dua), style = MaterialTheme.typography.displayMedium)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.baris_tiga), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (aduanList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Tiada aduan direkodkan.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(aduanList) { record -> // <--- REMOVED THE EXTRA '}' HERE
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onAduanClicked(record) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = record.cafeName, style = MaterialTheme.typography.labelMedium)
                                    Text(text = record.collegeLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "RM ${record.newPrice}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selesai", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                            Text(text = stringResource(R.string.baris_lima_A), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onSemakClicked() }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Semak")
                            Text(text = stringResource(R.string.baris_lima_B), style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onProfileClicked() } ) {
                            Icon(Icons.Default.Person, contentDescription = "Profil")
                            Text(text = stringResource(R.string.baris_lima_C), style = MaterialTheme.typography.labelSmall)
                        }
                        // ADDED CLICKABLE HERE
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onSettingsClicked() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Tetapan")
                            Text(text = stringResource(R.string.baris_lima_D), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                FloatingActionButton(onClick = onNewReportClicked, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-20).dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    currentCafe: String,
    currentCollege: String,
    currentFoodName: String,         // NEW
    currentNormalPrice: String,      // NEW
    currentPrice: String,
    currentDescription: String,      // NEW
    onCafeChanged: (String) -> Unit,
    onCollegeChanged: (String) -> Unit,
    onFoodNameChanged: (String) -> Unit,       // NEW
    onNormalPriceChanged: (String) -> Unit,    // NEW
    onPriceChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,    // NEW
    onSubmitClicked: () -> Unit,
    onCancelClicked: () -> Unit
) {
    val collegeList = listOf(
        "Kolej Aminuddin Baki",
        "Kolej Burhanuddin Helmi",
        "Kolej Dato' Onn",
        "Kolej Pendeta Za'ba",
        "Lain-lain / Fakulti"
    )

    var expanded by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        // Changed to LazyColumn so the screen is scrollable if the keyboard pops up!
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Borang Maklumat Harga Kafe", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = currentCafe,
                            onValueChange = onCafeChanged,
                            label = { Text("Nama Kafe / Gerai") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = currentCollege,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Lokasi Kolej Kediaman") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                collegeList.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            onCollegeChanged(selectionOption)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // NEW: Food Name
                        OutlinedTextField(
                            value = currentFoodName,
                            onValueChange = onFoodNameChanged,
                            label = { Text("Nama Makanan / Minuman") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Row for Side-by-Side Prices
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // NEW: Normal Price
                            OutlinedTextField(
                                value = currentNormalPrice,
                                onValueChange = onNormalPriceChanged,
                                label = { Text("Harga Asal (RM)") },
                                modifier = Modifier.weight(1f)
                            )
                            // Existing: New Price
                            OutlinedTextField(
                                value = currentPrice,
                                onValueChange = onPriceChanged,
                                label = { Text("Harga Baru (RM)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // NEW: Description
                        OutlinedTextField(
                            value = currentDescription,
                            onValueChange = onDescriptionChanged,
                            label = { Text("Maklumat Tambahan (Pilihan)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3 // Makes the text box taller!
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onCancelClicked, modifier = Modifier.weight(1f)) { Text("Batal") }
                            Button(onClick = onSubmitClicked, modifier = Modifier.weight(1f)) { Text("Hantar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketDetailScreen(
    aduanRecord: AduanRecord,
    onBackToHomeClicked: () -> Unit,
    onShareButtonClicked: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tiket Aduan Berjaya Dimuat Naik!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Kafe: ${aduanRecord.cafeName}", style = MaterialTheme.typography.bodyLarge)
                    Text("Kolej: ${aduanRecord.collegeLocation}", style = MaterialTheme.typography.bodyLarge)
                    Text("Harga Baru Dilaporkan: RM ${aduanRecord.newPrice}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(onClick = onShareButtonClicked, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = "Kongsi", modifier = Modifier.padding(end = 8.dp))
                Text("Kongsi Aduan")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBackToHomeClicked, modifier = Modifier.fillMaxWidth()) {
                Text("Kembali ke Halaman Utama")
            }
        }
    }
}

@Composable
fun SemakAduanScreen(aduanList: List<AduanRecord>,
                     onAduanClicked: (AduanRecord) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            Text("Status Aduan Kafe Anda", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (aduanList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tiada aduan direkodkan.")
                }
            } else {
                LazyColumn {
                    items(aduanList) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onAduanClicked(record) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = record.cafeName, style = MaterialTheme.typography.titleMedium)
                                    Text(text = record.collegeLocation, style = MaterialTheme.typography.bodySmall)
                                    Text(text = "RM ${record.newPrice}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Surface(
                                    color = if (record.status == "Sedang Diproses") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = record.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userName: String,       // NEW
    userMatric: String,     // NEW
    totalReports: Int
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(120.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gambar),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // NOW USES VARIABLES
            Text(text = userName, style = MaterialTheme.typography.displayMedium)
            Text(text = userMatric, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Jumlah Aduan Dihantar", style = MaterialTheme.typography.titleMedium)
                    Text("$totalReports", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    currentName: String,
    currentMatric: String,
    onNameChanged: (String) -> Unit,
    onMatricChanged: (String) -> Unit,
    onSaveClicked: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kemaskini Profil", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = currentName,
                        onValueChange = onNameChanged,
                        label = { Text("Nama Penuh") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = currentMatric,
                        onValueChange = onMatricChanged,
                        label = { Text("Nombor Matrik") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onSaveClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDetailScreen(
    aduan: AduanRecord,
    onBackClicked: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            Text("Maklumat Penuh Aduan", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kafe: ${aduan.cafeName}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Lokasi: ${aduan.collegeLocation}", style = MaterialTheme.typography.bodyLarge)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Makanan/Minuman: ${aduan.foodName}", style = MaterialTheme.typography.titleMedium)
                    Text("Harga Asal: RM ${aduan.normalPrice}", style = MaterialTheme.typography.bodyMedium)
                    Text("Harga Baru: RM ${aduan.newPrice}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Maklumat Tambahan:", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = if (aduan.description.isNotBlank()) aduan.description else "Tiada maklumat tambahan disediakan.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Status: ${aduan.status}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBackClicked, modifier = Modifier.fillMaxWidth()) {
                Text("Kembali")
            }
        }
    }
}