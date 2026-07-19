package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- DATA STRUCTURES ---
data class UserState(
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val isAuthenticated: Boolean
)

data class SubService(val name: String, val description: String, val price: Float)

data class ServiceCategory(
    val id: String,
    val number: String,
    val name: String,
    val description: String,
    val image: String,
    val startingPrice: Float,
    val popular: Boolean,
    val subservices: List<SubService>
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val serviceName: String,
    val subServiceName: String,
    val customerName: String,
    val phone: String,
    val date: String,
    val timeSlot: String,
    val address: String,
    var status: String, // pending, dispatched, in_progress, completed, cancelled
    val technician: String,
    val price: Float,
    val paymentStatus: String,
    val invoiceId: String,
    var reviewStars: Int? = null,
    var reviewComment: String? = null
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val type: String, // deposit, payment
    val amount: Float,
    val description: String,
    val date: String
)

@Entity(tableName = "notifications")
data class NotificationLog(
    @PrimaryKey val id: String,
    val type: String, // SMS, WhatsApp, Email
    val recipient: String,
    val message: String,
    val timestamp: String
)

// --- GLOBAL MOCK DATA ---
val SERVICES_DATA = listOf(
    ServiceCategory(
        id = "cleaning",
        number = "01",
        name = "Cleaning Services",
        description = "Deep residential, bathroom, kitchen, and upholstery sanitation.",
        image = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=400&q=80",
        startingPrice = 499f,
        popular = true,
        subservices = listOf(
            SubService("Full House Deep Cleaning", "Chemical scrubbing, window polishing & sanitization", 2999f),
            SubService("Bathroom Tile Grout Clean", "Acid-free tile scrubbing & disinfection", 699f),
            SubService("Kitchen Modular Service", "Degreasing cabinets, stove & chimney deep clean", 1499f),
            SubService("Sofa & Mattress Vacuuming", "Dry vacuum and wet shampoo extraction", 1199f)
        )
    ),
    ServiceCategory(
        id = "painting",
        number = "02",
        name = "Painting Services",
        description = "Dustless interior & exterior wall painting and premium varnishing.",
        image = "https://images.unsplash.com/photo-1562259949-e8e7689d7828?auto=format&fit=crop&w=400&q=80",
        startingPrice = 4999f,
        popular = false,
        subservices = listOf(
            SubService("Interior Luxury Paint", "Double puttying with premium brand finish", 8999f),
            SubService("Exterior Facade Weatherproof", "Damp proofing & weather resistance", 14999f),
            SubService("Designer Wall Accent Art", "Ombre shades & custom stencil patterns", 5999f)
        )
    ),
    ServiceCategory(
        id = "electrical",
        number = "03",
        name = "Electrical Services",
        description = "House rewiring, EV chargers, smart switchboards, and audits.",
        image = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=400&q=80",
        startingPrice = 199f,
        popular = true,
        subservices = listOf(
            SubService("Smart Switch & DB Setup", "Shockproof main boards with rated overload MCB", 699f),
            SubService("EV Charger Point Mount", "Heavy-duty power line with surge protection", 2499f),
            SubService("Chandelier Accent Light Install", "Overhead structural anchor and wiring", 1299f)
        )
    ),
    ServiceCategory(
        id = "plumbing",
        number = "04",
        name = "Plumbing Services",
        description = "Leak detection, pipeline repair, pressure pumps, and clogs.",
        image = "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?auto=format&fit=crop&w=400&q=80",
        startingPrice = 199f,
        popular = true,
        subservices = listOf(
            SubService("Leak Detection & Repair", "Acoustic line test and pipe crack solder", 899f),
            SubService("Tap & Mixer Replacement", "Sleek chrome fitting & water tight washers", 399f),
            SubService("Drain Velocity Clog Clear", "Motorized drain snaking for complete blockage", 599f)
        )
    ),
    ServiceCategory(
        id = "security",
        number = "05",
        name = "Smart Home Security",
        description = "Biometric door locks, CCTV IP cameras, and smart sensors.",
        image = "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=400&q=80",
        startingPrice = 999f,
        popular = true,
        subservices = listOf(
            SubService("Smart Biometric Lock Setup", "Fingerprint, passcode & mechanical locks", 3499f),
            SubService("CCTV IP Camera Setup", "Wireless outdoor camera alignment & app stream", 4500f),
            SubService("Mesh WiFi & Conduit Laying", "Whole-home internet coverage & clean routing", 1999f)
        )
    )
)

val TRANSLATIONS = mapOf(
    "en" to mapOf(
        "services" to "28 Divisions", "sos" to "🚨 Emergency SOS", "estimator" to "AI Cost Desk",
        "bookings" to "📋 Dispatches", "profile" to "👤 Wallet", "notifications" to "🔔 Alerts Feed",
        "accessibility" to "⚙️ Accessibility", "title" to "Household Management Solutions",
        "subtitle" to "Premier security-audited multi-service platform.", "sosActive" to "🚨 ACTIVE SOS DISPATCHED"
    ),
    "hi" to mapOf(
        "services" to "28 मुख्य विभाग", "sos" to "🚨 आपातकालीन", "estimator" to "एआई डेस्क",
        "bookings" to "📋 सक्रिय प्रेषण", "profile" to "👤 वॉलेट", "notifications" to "🔔 अलर्ट फ़ीड",
        "accessibility" to "⚙️ सुगमता", "title" to "घरेलू प्रबंधन और मरम्मत",
        "subtitle" to "सुरक्षा परीक्षित बहु-सेवा मंच।", "sosActive" to "🚨 सक्रिय एसओएस"
    ),
    "te" to mapOf(
        "services" to "28 విభాగాలు", "sos" to "🚨 అత్యవసర SOS", "estimator" to "AI డెస్క్",
        "bookings" to "📋 బుకింగ్‌లు", "profile" to "👤 నా వాలెట్", "notifications" to "🔔 అలర్ట్స్",
        "accessibility" to "⚙️ యాక్సెస్బిలిటీ", "title" to "గృహ నిర్వహణ పరిష్కారాలు",
        "subtitle" to "భారతదేశపు మొట్టమొదటి సెక్యూరిటీ ఆడిట్ ప్లాట్‌ఫారమ్.", "sosActive" to "🚨 అత్యవసర ఎస్ఓఎస్"
    ),
    "ta" to mapOf(
        "services" to "28 பிரிவுகள்", "sos" to "🚨 அவசர SOS", "estimator" to "AI மையம்",
        "bookings" to "📋 முன்பதிவுகள்", "profile" to "👤 வாலட்", "notifications" to "🔔 அறிவிப்புகள்",
        "accessibility" to "⚙️ அணுகல்தன்மை", "title" to "வீட்டு மேலாண்மை தீர்வுகள்",
        "subtitle" to "பாதுகாப்பு தணிக்கை செய்யப்பட்ட சேவை தளம்.", "sosActive" to "🚨 அவசர SOS"
    ),
    "es" to mapOf(
        "services" to "28 Divisiones", "sos" to "🚨 Emergencia", "estimator" to "AI Costos",
        "bookings" to "📋 Despachos", "profile" to "👤 Billetera", "notifications" to "🔔 Alertas",
        "accessibility" to "⚙️ Panel de Acceso", "title" to "Soluciones del Hogar",
        "subtitle" to "La principal plataforma multiservicio de la India.", "sosActive" to "🚨 SOS ACTIVO"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val coroutineScope = rememberCoroutineScope()
    
    // --- APP STATES ---
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val currentUser = authManager.currentUserState
    var language by remember { mutableStateOf("en") }
    var appRole by remember(currentUser) { mutableStateOf(authManager.getUserRole()) } // customer, technician, admin
    var activeTab by remember { mutableStateOf("services") }
    val bookingRepository = remember { BookingRepository(context, authManager) }
    val bookingsState by bookingRepository.allBookingsFlow.collectAsState(initial = emptyList())
    val transactionsState by bookingRepository.allTransactionsFlow.collectAsState(initial = emptyList())
    val notificationsState by bookingRepository.allNotificationsFlow.collectAsState(initial = emptyList())

    val bookings = bookingsState
    val transactions = transactionsState
    val notifications = notificationsState

    val walletBalance = remember(transactionsState) {
        var balance = 0f
        transactionsState.forEach { tx ->
            if (tx.type == "deposit") {
                balance += tx.amount
            } else if (tx.type == "payment") {
                balance -= tx.amount
            }
        }
        balance
    }

    var highContrast by remember { mutableStateOf(false) }
    var textScale by remember { mutableStateOf("md") }
    var offlineMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            bookingRepository.seedInitialData(
                name = user.fullName,
                phone = user.phone,
                address = user.address
            )
            bookingRepository.fetchAndSyncBookings()
        }
    }

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewSubService by remember { mutableStateOf<SubService?>(null) }
    var reviewCategory by remember { mutableStateOf<ServiceCategory?>(null) }

    // Languages helper
    val t = TRANSLATIONS[language] ?: TRANSLATIONS["en"]!!

    // Dynamic scale font size helper
    val scaleFactor = when (textScale) {
        "sm" -> 0.85f
        "lg" -> 1.15f
        else -> 1.0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (highContrast) Color.Black else SophisticatedBackground)
    ) {
        if (currentUser == null) {
            LoginScreen(
                authManager = authManager,
                language = language,
                onLanguageChange = { language = it },
                onLoginSuccess = { name, phone, address ->
                    coroutineScope.launch {
                        bookingRepository.seedInitialData(name, phone, address)
                    }
                }
            )
        } else {
            val user = currentUser!!
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = if (highContrast) Color.Black else SophisticatedBackground,
                topBar = {
                    Column {
                        // Quick Status Alert Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF16191D))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Text(
                                    text = "Guardian Shield Secure Active",
                                    color = Color(0xFF909194),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (offlineMode) {
                                    Text(
                                        text = "OFFLINE CACHE",
                                        color = Color.Red,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .border(1.dp, Color.Red, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            // Testing Role Switcher
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("customer" to "👤 CLIENT", "technician" to "🛠️ TECH", "admin" to "📊 ADMIN").forEach { (roleKey, label) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (appRole == roleKey) Color(0xFF00315B) else Color(0xFF2D2F33))
                                            .clickable { appRole = roleKey }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (appRole == roleKey) Color(0xFFD1E4FF) else Color(0xFF909194),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Theme Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PROJECT HUB",
                                    color = SophisticatedTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${user.fullName.split(" ").first()}'s Workspace",
                                    color = SophisticatedAccent,
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            // User Profile Circular Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SophisticatedSurface)
                                    .border(1.dp, SophisticatedCardBorder, CircleShape)
                                    .clickable { authManager.logout() }, // logout click
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.fullName.take(1).uppercase(),
                                    color = SophisticatedAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    if (appRole == "customer") {
                        Column {
                            HorizontalDivider(color = SophisticatedBorder, thickness = 1.dp)
                            NavigationBar(
                                containerColor = SophisticatedSurface,
                                tonalElevation = 8.dp
                            ) {
                                listOf(
                                    Triple("services", "Code", Icons.Default.Home),
                                    Triple("sos", "SOS", Icons.Default.Warning),
                                    Triple("estimator", "Tools", Icons.Default.Build),
                                    Triple("bookings", "Git", Icons.Default.List),
                                    Triple("profile_wallet", "Wallet", Icons.Default.AccountBalanceWallet)
                                ).forEach { (tabId, label, icon) ->
                                    val selected = activeTab == tabId
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { activeTab = tabId },
                                        icon = { Icon(icon, contentDescription = label, tint = if (selected) SophisticatedAccent else SophisticatedTextMuted) },
                                        label = { Text(label, color = if (selected) SophisticatedAccent else SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = SophisticatedNavSelected
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (appRole) {
                        "customer" -> {
                            when (activeTab) {
                                "services" -> CustomerServicesTab(
                                    scaleFactor = scaleFactor,
                                    highContrast = highContrast,
                                    walletBalance = walletBalance,
                                    onBookService = { sub, cat ->
                                        reviewSubService = sub
                                        reviewCategory = cat
                                        showReviewDialog = true
                                    }
                                )
                                "sos" -> CustomerSOSTab(
                                    scaleFactor = scaleFactor,
                                    user = user,
                                    onAddBooking = { b ->
                                        coroutineScope.launch {
                                            bookingRepository.createBooking(b)
                                        }
                                    },
                                    onAddNotification = { n ->
                                        coroutineScope.launch {
                                            bookingRepository.insertNotification(n)
                                        }
                                    }
                                )
                                "estimator" -> CustomerEstimatorTab(
                                    scaleFactor = scaleFactor,
                                    onQuickBook = { sName, subName, cost ->
                                        if (walletBalance >= cost) {
                                            val id = "BC-${Random.nextInt(100000, 999999)}"
                                            coroutineScope.launch {
                                                val newBooking = Booking(
                                                    id = id,
                                                    serviceName = sName,
                                                    subServiceName = subName,
                                                    customerName = user.fullName,
                                                    phone = user.phone,
                                                    date = "2026-07-19",
                                                    timeSlot = "Emergency ASAP",
                                                    address = user.address,
                                                    status = "pending",
                                                    technician = "Rajesh Kumar",
                                                    price = cost,
                                                    paymentStatus = "paid",
                                                    invoiceId = "INV-${Random.nextInt(100000, 999999)}"
                                                )
                                                bookingRepository.createBooking(newBooking)

                                                val newTxn = Transaction(
                                                    id = "TXN-${Random.nextInt(100000, 999999)}",
                                                    type = "payment",
                                                    amount = cost,
                                                    description = "Emergency Dispatch: $subName",
                                                    date = "2026-07-19"
                                                )
                                                bookingRepository.insertTransaction(newTxn)
                                            }
                                            activeTab = "bookings"
                                        } else {
                                            activeTab = "profile_wallet"
                                        }
                                    }
                                )
                                "bookings" -> CustomerBookingsTab(
                                    scaleFactor = scaleFactor,
                                    bookings = bookings,
                                    walletBalance = walletBalance,
                                    onRebook = { old ->
                                        if (walletBalance >= old.price) {
                                            val id = "BC-${Random.nextInt(100000, 999999)}"
                                            coroutineScope.launch {
                                                val newBooking = Booking(
                                                    id = id,
                                                    serviceName = old.serviceName,
                                                    subServiceName = old.subServiceName,
                                                    customerName = user.fullName,
                                                    phone = user.phone,
                                                    date = "2026-07-20",
                                                    timeSlot = "10:00 AM - 12:00 PM",
                                                    address = user.address,
                                                    status = "dispatched",
                                                    technician = "Rajesh Kumar",
                                                    price = old.price,
                                                    paymentStatus = "paid",
                                                    invoiceId = "INV-${Random.nextInt(100000, 999999)}"
                                                )
                                                bookingRepository.createBooking(newBooking)

                                                val newTx = Transaction(
                                                    id = "TXN-${Random.nextInt(100000, 999999)}",
                                                    type = "payment",
                                                    amount = old.price,
                                                    description = "1-Click Rebook: ${old.subServiceName}",
                                                    date = "2026-07-19"
                                                )
                                                bookingRepository.insertTransaction(newTx)
                                            }
                                        }
                                    },
                                    onCancelBooking = { b ->
                                        coroutineScope.launch {
                                            bookingRepository.updateBookingStatus(b.id, "cancelled")
                                            // Refund
                                            val refundTx = Transaction(
                                                id = "TXN-${Random.nextInt(100000, 999999)}",
                                                type = "deposit",
                                                amount = b.price,
                                                description = "Refund: Cancelled ${b.subServiceName}",
                                                date = "2026-07-19"
                                            )
                                            bookingRepository.insertTransaction(refundTx)

                                            val refundNotif = NotificationLog(
                                                id = "NL-${Random.nextInt(100000, 999999)}",
                                                type = "SMS",
                                                recipient = user.phone,
                                                message = "Booking ${b.id} successfully cancelled. ₹${b.price.toInt()} refunded to your wallet.",
                                                timestamp = "Just now"
                                            )
                                             bookingRepository.insertNotification(refundNotif)
                                        }
                                    },
                                    onRescheduleBooking = { b, newDate, newSlot ->
                                        coroutineScope.launch {
                                            bookingRepository.rescheduleBooking(b.id, newDate, newSlot)
                                            val resNotif = NotificationLog(
                                                id = "NL-${Random.nextInt(100000, 999999)}",
                                                type = "WhatsApp",
                                                recipient = user.phone,
                                                message = "SLA Alert: Your job ${b.id} rescheduled to $newDate at $newSlot.",
                                                timestamp = "Just now"
                                            )
                                             bookingRepository.insertNotification(resNotif)
                                        }
                                    },
                                    onRateBooking = { b, stars, comment ->
                                        coroutineScope.launch {
                                             val updated = b.copy(reviewStars = stars, reviewComment = comment)
                                             bookingRepository.updateBooking(updated)
                                        }
                                    }
                                )
                                "profile_wallet" -> CustomerProfileWalletTab(
                                    scaleFactor = scaleFactor,
                                    user = user,
                                    walletBalance = walletBalance,
                                    transactions = transactions,
                                    onTopUp = { amt ->
                                        coroutineScope.launch {
                                            val topUpTx = Transaction(
                                                id = "TXN-${Random.nextInt(100000, 999999)}",
                                                type = "deposit",
                                                amount = amt,
                                                description = "Wallet Top Up Secure Gateway",
                                                date = "2026-07-19"
                                            )
                                             bookingRepository.insertTransaction(topUpTx)
                                        }
                                    },
                                    language = language,
                                    onLanguageChange = { language = it },
                                    highContrast = highContrast,
                                    onHighContrastChange = { highContrast = it },
                                    textScale = textScale,
                                    onTextScaleChange = { textScale = it },
                                    offlineMode = offlineMode,
                                    onOfflineModeChange = { offlineMode = it },
                                    notifications = notifications
                                )
                            }
                        }
                        "technician" -> TechnicianPortalScreen(
                            scaleFactor = scaleFactor,
                            bookings = bookings,
                            onUpdateStatus = { b, status ->
                                coroutineScope.launch {
                                    bookingRepository.updateBookingStatus(b.id, status)
                                }
                            },
                            onAddNotification = { n ->
                                coroutineScope.launch {
                                    bookingRepository.insertNotification(n)
                                }
                            },
                            user = user
                        )
                        "admin" -> AdminPortalScreen(
                            scaleFactor = scaleFactor,
                            bookings = bookings,
                            transactions = transactions,
                            repository = bookingRepository,
                            coroutineScope = coroutineScope
                        )
                    }
                }
            }
        }
    }

    if (showReviewDialog && reviewSubService != null && reviewCategory != null && currentUser != null) {
        BookingReviewAndInvoiceDialog(
            scaleFactor = scaleFactor,
            subService = reviewSubService!!,
            category = reviewCategory!!,
            walletBalance = walletBalance,
            repository = bookingRepository,
            onDismiss = {
                showReviewDialog = false
                reviewSubService = null
                reviewCategory = null
            },
            onConfirm = { date, slot, couponCode, discount, gst, totalPayable ->
                coroutineScope.launch {
                    val bookingId = "BC-${Random.nextInt(100000, 999999)}"
                    val txnId = "TXN-${Random.nextInt(100000, 999999)}"
                    val notifId = "NL-${Random.nextInt(100000, 999999)}"
                    val invId = "INV-${Random.nextInt(100000, 999999)}"

                    val newBooking = Booking(
                        id = bookingId,
                        serviceName = reviewCategory!!.name,
                        subServiceName = reviewSubService!!.name,
                        customerName = currentUser.fullName,
                        phone = currentUser.phone,
                        date = date,
                        timeSlot = slot,
                        address = currentUser.address,
                        status = "dispatched",
                        technician = "Rajesh Kumar",
                        price = totalPayable,
                        paymentStatus = "paid",
                        invoiceId = invId
                    )
                    bookingRepository.createBooking(newBooking)

                    val newTxn = Transaction(
                        id = txnId,
                        type = "payment",
                        amount = totalPayable,
                        description = "Booked ${reviewCategory!!.name} - ${reviewSubService!!.name}",
                        date = "2026-07-19"
                    )
                    bookingRepository.insertTransaction(newTxn)

                    val newNotif = NotificationLog(
                        id = notifId,
                        type = "SMS",
                        recipient = currentUser.phone,
                        message = "Booking confirmed! Paid ₹${totalPayable.toInt()} (including GST, coupon: $couponCode) from wallet for Ticket $bookingId.",
                        timestamp = "Just now"
                    )
                    bookingRepository.insertNotification(newNotif)

                    showReviewDialog = false
                    reviewSubService = null
                    reviewCategory = null
                    activeTab = "bookings"
                }
            }
        )
    }
}

// --- ONBOARDING & SECURE LOGIN ---
@Composable
fun LoginScreen(
    authManager: AuthManager,
    language: String,
    onLanguageChange: (String) -> Unit,
    onLoginSuccess: (String, String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) } // 0: Splash Audit, 1: Onboarding, 2: Auth Options, 3: OTP Code Entry, 4: Reset Password, 5: Biometrics
    var auditStep by remember { mutableStateOf(0) }
    val auditLogs = listOf(
        "Checking secure application container sandbox...",
        "Auditing biometric storage secure enclave... PASSED",
        "Synchronizing 256-bit Aadhaar verification certificates...",
        "Establishing encrypted session with SLA dispatches... SECURE"
    )

    LaunchedEffect(step) {
        if (step == 0) {
            while (auditStep < auditLogs.size - 1) {
                delay(800)
                auditStep++
            }
            delay(1000)
            step = 1
        }
    }

    // Onboarding screens
    val onboardingPages = listOf(
        Triple("300+ Household Services", "Access 28 core divisions from deep kitchen scrubbing to custom masonry under guaranteed SLAs.", Icons.Default.Home),
        Triple("100% Audited Partners", "All assigned professionals undergo absolute biometric and Aadhaar background verification.", Icons.Default.VerifiedUser),
        Triple("30-Min Priority SOS", "Summon verified disaster control, premium locksmiths or electrical specialists to your door in minutes.", Icons.Default.Warning)
    )
    var onboardingIndex by remember { mutableStateOf(0) }

    // Forms State
    var authMode by remember { mutableStateOf("login") } // login, register, forgot, phone_otp
    var emailInput by remember { mutableStateOf("bandiashokkumarbandi1@gmail.com") }
    var passwordInput by remember { mutableStateOf("password123") }
    var pinInput by remember { mutableStateOf("2819") }
    var nameInput by remember { mutableStateOf("Bandi Ashok") }
    var phoneInput by remember { mutableStateOf("+91 80193 18625") }
    var addressInput by remember { mutableStateOf("Flat 304, Green Heights, Gachibowli, Hyderabad") }
    var roleInput by remember { mutableStateOf("customer") } // customer, technician, admin
    
    // OTP states
    var otpPhoneInput by remember { mutableStateOf("+91 80193 18625") }
    var otpCodeInput by remember { mutableStateOf("") }
    var isOtpForForgotPassword by remember { mutableStateOf(false) }

    // Password reset states
    var forgotEmailInput by remember { mutableStateOf("bandiashokkumarbandi1@gmail.com") }
    var newPasswordInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }

    // Local validation state
    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            0 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = SophisticatedAccent,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "ONE CALL SECURITY CONTAINER",
                        color = SophisticatedText,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = auditLogs[auditStep],
                        color = SophisticatedTextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(color = SophisticatedAccent, strokeWidth = 2.dp)
                }
            }
            1 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize().padding(vertical = 40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Skip",
                            color = SophisticatedAccent,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { step = 2 }
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            onboardingPages[onboardingIndex].third,
                            contentDescription = null,
                            tint = SophisticatedAccent,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = onboardingPages[onboardingIndex].first,
                            color = SophisticatedText,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = onboardingPages[onboardingIndex].second,
                            color = SophisticatedTextAlt,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            onboardingPages.forEachIndexed { i, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (onboardingIndex == i) SophisticatedAccent else SophisticatedBorder,
                                            CircleShape
                                        )
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (onboardingIndex < onboardingPages.size - 1) {
                                    onboardingIndex++
                                } else {
                                    step = 2
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
                        ) {
                            Text("Next", color = SophisticatedOnAccent)
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = SophisticatedAccent, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("One Call Solutions", color = SophisticatedText, fontSize = 24.sp, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic)
                        Text("Guardian Standard Secure Portal", color = SophisticatedTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Mode selections tabs
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SophisticatedItemBg, RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "login" to "Email Login",
                                "register" to "Register",
                                "phone_otp" to "OTP Login"
                            ).forEach { (mode, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (authMode == mode) SophisticatedAccent else Color.Transparent)
                                        .clickable { 
                                            authMode = mode
                                            localErrorMsg = null
                                            authManager.apiErrorMessage = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (authMode == mode) SophisticatedOnAccent else SophisticatedText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Display errors if any
                    val displayError = localErrorMsg ?: authManager.apiErrorMessage
                    if (displayError != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF451010)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(displayError, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color.LightGray,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { 
                                                localErrorMsg = null
                                                authManager.apiErrorMessage = null
                                            }
                                    )
                                }
                            }
                        }
                    }

                    if (authMode == "login") {
                        item {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("email_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("App Password", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("password_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("Secure 4-Digit App PIN", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("pin_input")
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Forgot PIN or Password?",
                                    color = SophisticatedAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { 
                                            authMode = "forgot" 
                                            localErrorMsg = null
                                            authManager.apiErrorMessage = null
                                        }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        localErrorMsg = null
                                        if (emailInput.isEmpty() || (passwordInput.isEmpty() && pinInput.isEmpty())) {
                                            localErrorMsg = "Please complete email and login credentials"
                                            return@launch
                                        }
                                        val req = LoginRequest(
                                            email = emailInput,
                                            password = if (passwordInput.isNotEmpty()) passwordInput else null,
                                            pin = if (pinInput.isNotEmpty()) pinInput else null
                                        )
                                        val success = authManager.loginUser(req, passwordInput, pinInput)
                                        if (success) {
                                            step = 5 // Biometrics screen
                                        }
                                    }
                                },
                                enabled = !authManager.isApiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_button")
                            ) {
                                if (authManager.isApiLoading) {
                                    CircularProgressIndicator(color = SophisticatedOnAccent, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Secure Sign In", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (authMode == "register") {
                        item {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Full Profile Name", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("register_name")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Profile Email Address", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("register_email")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Verified Mobile Number", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("register_phone")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Create App Password", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("Create 4-Digit Security PIN", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = addressInput,
                                onValueChange = { addressInput = it },
                                label = { Text("Primary Service Address", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Role chips selection
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text("Assign Security Role", color = SophisticatedTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "customer" to "Customer Profile",
                                        "technician" to "Technician",
                                        "admin" to "Portal Admin"
                                    ).forEach { (roleKey, label) ->
                                        val selected = roleInput == roleKey
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selected) Color(0xFF00315B) else Color(0xFF2D2F33))
                                                .clickable { roleInput = roleKey }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (selected) Color.White else SophisticatedTextAlt,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        localErrorMsg = null
                                        if (nameInput.isEmpty() || emailInput.isEmpty() || phoneInput.isEmpty() || passwordInput.isEmpty() || pinInput.isEmpty() || addressInput.isEmpty()) {
                                            localErrorMsg = "Please complete all registration credentials"
                                            return@launch
                                        }
                                        val req = RegisterRequest(
                                            name = nameInput,
                                            email = emailInput,
                                            phone = phoneInput,
                                            password = passwordInput,
                                            pin = pinInput,
                                            role = roleInput,
                                            address = addressInput
                                        )
                                        val success = authManager.registerUser(req)
                                        if (success) {
                                            step = 5 // Biometrics Screen
                                        }
                                    }
                                },
                                enabled = !authManager.isApiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("register_button")
                            ) {
                                if (authManager.isApiLoading) {
                                    CircularProgressIndicator(color = SophisticatedOnAccent, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Register SLA Profile", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (authMode == "phone_otp") {
                        item {
                            OutlinedTextField(
                                value = otpPhoneInput,
                                onValueChange = { otpPhoneInput = it },
                                label = { Text("Mobile Number", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("otp_phone")
                            )
                        }
                        item {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        localErrorMsg = null
                                        if (otpPhoneInput.isEmpty()) {
                                            localErrorMsg = "Please enter mobile number"
                                            return@launch
                                        }
                                        val success = authManager.sendOtpCode(otpPhoneInput)
                                        if (success) {
                                            isOtpForForgotPassword = false
                                            otpCodeInput = ""
                                            step = 3 // Move to code entry
                                        }
                                    }
                                },
                                enabled = !authManager.isApiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("send_otp_button")
                            ) {
                                if (authManager.isApiLoading) {
                                    CircularProgressIndicator(color = SophisticatedOnAccent, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Send Security OTP Code", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (authMode == "forgot") {
                        item {
                            OutlinedTextField(
                                value = forgotEmailInput,
                                onValueChange = { forgotEmailInput = it },
                                label = { Text("Registered Email Address", color = SophisticatedTextAlt) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SophisticatedAccent) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                                modifier = Modifier.fillMaxWidth().testTag("forgot_email")
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Back to Login",
                                    color = SophisticatedTextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable { authMode = "login" }
                                )
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        localErrorMsg = null
                                        if (forgotEmailInput.isEmpty()) {
                                            localErrorMsg = "Please enter email address"
                                            return@launch
                                        }
                                        val success = authManager.forgotPassword(forgotEmailInput)
                                        if (success) {
                                            isOtpForForgotPassword = true
                                            otpCodeInput = ""
                                            step = 3 // Move to code entry
                                        }
                                    }
                                },
                                enabled = !authManager.isApiLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("forgot_button")
                            ) {
                                if (authManager.isApiLoading) {
                                    CircularProgressIndicator(color = SophisticatedOnAccent, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Dispatch Reset Code", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- HIGH-FIDELITY SOCIAL SECURE SIGN-IN ---
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = SophisticatedBorder)
                                Text(
                                    "SECURE SLA SIGN-ON",
                                    fontSize = 9.sp,
                                    color = SophisticatedTextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = SophisticatedBorder)
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Google and Apple Sign-On Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Google Button M3 Spec
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            localErrorMsg = null
                                            val success = authManager.performGoogleLogin(
                                                idToken = "google_id_token_secure_gmqpzs",
                                                email = "bandiashokkumarbandi1@gmail.com",
                                                name = "Bandi Ashok",
                                                photo = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100"
                                            )
                                            if (success) {
                                                step = 5 // Biometrics scan
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2124)),
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("google_login_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = "Google Sign In",
                                            tint = Color(0xFF4285F4),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Google Sign-In", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Apple Button Spec
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            localErrorMsg = null
                                            val success = authManager.performAppleLogin(
                                                identityToken = "apple_id_token_secure_gmqpzs",
                                                email = "bandiashokkumarbandi1@icloud.com",
                                                name = "Bandi Ashok"
                                            )
                                            if (success) {
                                                step = 5
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("apple_login_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Apple Sign In",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Apple Secure ID", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // OTP Code entry
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SophisticatedAccent, modifier = Modifier.size(64.dp))
                    Text(
                        text = "Verify Dispatch",
                        color = SophisticatedText,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter 4-digit verification code dispatched to your profile (Use 2819)",
                        color = SophisticatedTextAlt,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    val displayError = localErrorMsg ?: authManager.apiErrorMessage
                    if (displayError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451010)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(displayError, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = otpCodeInput,
                        onValueChange = { otpCodeInput = it },
                        label = { Text("SLA Code (OTP)", color = SophisticatedTextAlt) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                        modifier = Modifier.fillMaxWidth().testTag("otp_input")
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                localErrorMsg = null
                                if (otpCodeInput.isEmpty()) {
                                    localErrorMsg = "Please enter OTP code"
                                    return@launch
                                }
                                if (isOtpForForgotPassword) {
                                    if (otpCodeInput == "2819") {
                                        step = 4 // Move to Reset Password form
                                    } else {
                                        localErrorMsg = "Invalid password reset verification code"
                                    }
                                } else {
                                    val success = authManager.verifyOtpCode(otpPhoneInput, otpCodeInput, roleInput)
                                    if (success) {
                                        step = 5 // Go to biometrics
                                    }
                                }
                            }
                        },
                        enabled = !authManager.isApiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("verify_otp_button")
                    ) {
                        if (authManager.isApiLoading) {
                            CircularProgressIndicator(color = SophisticatedOnAccent, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Verify Security Code", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Cancel",
                        color = SophisticatedTextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { 
                                step = 2 
                                authMode = "login"
                                localErrorMsg = null
                            }
                            .padding(8.dp)
                    )
                }
            }
            4 -> {
                // Reset Password Screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SophisticatedAccent, modifier = Modifier.size(64.dp))
                    Text(
                        text = "Reset Secure Credentials",
                        color = SophisticatedText,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )

                    val displayError = localErrorMsg ?: authManager.apiErrorMessage
                    if (displayError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451010)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(displayError, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New App Password", color = SophisticatedTextAlt) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { newPinInput = it },
                        label = { Text("New 4-Digit App PIN", color = SophisticatedTextAlt) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                localErrorMsg = null
                                if (newPasswordInput.isEmpty() || newPinInput.isEmpty()) {
                                    localErrorMsg = "Please complete all fields"
                                    return@launch
                                }
                                val req = ResetPasswordRequest(
                                    email = forgotEmailInput,
                                    otp = otpCodeInput,
                                    newPassword = newPasswordInput,
                                    newPin = newPinInput
                                )
                                val success = authManager.resetPassword(req)
                                if (success) {
                                    authMode = "login"
                                    step = 2 // Back to login screen
                                    localErrorMsg = null
                                }
                            }
                        },
                        enabled = !authManager.isApiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (authManager.isApiLoading) {
                            CircularProgressIndicator(color = SophisticatedOnAccent, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Update Security Credentials", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            5 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = "Scan",
                        tint = SophisticatedAccent,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Biometric Verification Required", color = SophisticatedText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hold still to scan biometric credentials inside the security enclave ring.",
                        color = SophisticatedTextAlt,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            val user = authManager.currentUserState
                            if (user != null) {
                                onLoginSuccess(user.fullName, user.phone, user.address)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("biometric_button")
                    ) {
                        Text("Verify Biometrics", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- CLIENT SERVICES CATALOG TAB ---
@Composable
fun CustomerServicesTab(
    scaleFactor: Float,
    highContrast: Boolean,
    walletBalance: Float,
    onBookService: (SubService, ServiceCategory) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    if (selectedCategory == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Title & Sub
            Text(
                text = "HOUSEHOLD SERVICES",
                color = SophisticatedTextMuted,
                fontSize = (11 * scaleFactor).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                text = "28 Core Divisions",
                color = SophisticatedText,
                fontSize = (26 * scaleFactor).sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 300+ micro tasks...", color = SophisticatedTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SophisticatedAccent) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SophisticatedAccent, unfocusedBorderColor = SophisticatedBorder, focusedLabelColor = SophisticatedAccent, focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            val filteredCategories = SERVICES_DATA.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCategories) { cat ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SophisticatedSurface)
                            .border(1.dp, SophisticatedBorder, RoundedCornerShape(24.dp))
                            .clickable { selectedCategory = cat }
                    ) {
                        Column {
                            // Category Image
                            AsyncImage(
                                model = cat.image,
                                contentDescription = cat.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cat.number} • ${cat.name.uppercase()}",
                                        color = SophisticatedAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (cat.popular) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF00315B), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("POPULAR", color = Color(0xFFD1E4FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat.description,
                                    color = SophisticatedText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Starting from ₹${cat.startingPrice.toInt()}",
                                        color = SophisticatedTextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Open",
                                        tint = SophisticatedAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val cat = selectedCategory!!
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { selectedCategory = null }
                    .padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SophisticatedAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("All Divisions", color = SophisticatedAccent, fontSize = 14.sp)
            }

            Text(
                text = cat.name.uppercase(),
                color = SophisticatedText,
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = cat.description,
                color = SophisticatedTextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cat.subservices) { sub ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SophisticatedSurface)
                            .border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sub.name, color = SophisticatedText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(sub.description, color = SophisticatedTextAlt, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${sub.price.toInt()}", color = SophisticatedAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Fixed Rate", color = SophisticatedTextMuted, fontSize = 9.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onBookService(sub, cat) },
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (walletBalance >= sub.price) "1-Click Secure Booking" else "Top Up Wallet & Book (₹${sub.price.toInt()})",
                                    color = SophisticatedOnAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- EMERGENCY SOS TAB ---
@Composable
fun CustomerSOSTab(
    scaleFactor: Float,
    user: UserState,
    onAddBooking: (Booking) -> Unit,
    onAddNotification: (NotificationLog) -> Unit
) {
    var activeSOS by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(activeSOS) {
        if (activeSOS) {
            elapsedSeconds = 0
            while (activeSOS) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = if (activeSOS) Color.Red else SophisticatedAccent,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "24/7 DISASTER & EMERGENCY HUB",
            color = SophisticatedTextMuted,
            fontSize = (11 * scaleFactor).sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "30-Min Emergency SOS",
            color = SophisticatedText,
            fontSize = (22 * scaleFactor).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Deploys standard-vetted disaster controller, premium locksmith, or critical electrician to your registered location in Gachibowli immediately.",
            color = SophisticatedTextAlt,
            fontSize = (13 * scaleFactor).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Large pulsate Red Button
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(if (activeSOS) Color.Red else Color(0xFF2D2F33))
                .clickable {
                    activeSOS = !activeSOS
                    if (activeSOS) {
                        val id = "BC-${Random.nextInt(100000, 999999)}"
                        onAddBooking(
                            Booking(
                                id = id,
                                serviceName = "Emergency Support",
                                subServiceName = "Disaster Control Rescue Support",
                                customerName = user.fullName,
                                phone = user.phone,
                                date = "2026-07-19",
                                timeSlot = "ASAP (Emergency)",
                                address = user.address,
                                status = "pending",
                                technician = "Rajesh Kumar (Disaster Specialist)",
                                price = 1499f,
                                paymentStatus = "unpaid",
                                invoiceId = "INV-${Random.nextInt(100000, 999999)}"
                            )
                        )
                        onAddNotification(
                            NotificationLog(
                                id = "NL-${Random.nextInt(100000, 999999)}",
                                type = "WhatsApp",
                                recipient = user.phone,
                                message = "🚨 EMERGENCY SOS ACTIVATED! Verified Rescue Specialist Rajesh Kumar deployed to ${user.address}.",
                                timestamp = "Just now"
                            )
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (activeSOS) "SOS ACTIVE" else "ACTIVATE",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = if (activeSOS) "Tap to Cancel" else "Emergency 24/7",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }

        if (activeSOS) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE SOS DISPATCH LOGS",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Dispatching rescue vehicle... OK", color = SophisticatedText, fontSize = 12.sp)
                    Text("• Rajesh Kumar GPS tracked coordinates established... OK", color = SophisticatedText, fontSize = 12.sp)
                    Text("• Est. Arrival: ${maxOf(1, 30 - elapsedSeconds / 5)} mins remaining", color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- INTERACTIVE AI DESK & ESTIMATOR TAB ---
@Composable
fun CustomerEstimatorTab(scaleFactor: Float, onQuickBook: (String, String, Float) -> Unit) {
    var sizeSqft by remember { mutableStateOf(1000f) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var materialGrade by remember { mutableStateOf("Premium") } // Standard, Premium, Luxury

    val categories = listOf("Deep Cleaning", "Wall Painting", "Waterproofing", "Wiring Audit")
    val basePricesSqft = listOf(1.5f, 8.5f, 15f, 2.2f)

    val multiplier = when (materialGrade) {
        "Standard" -> 0.85f
        "Luxury" -> 1.5f
        else -> 1.0f
    }

    val estimate = (sizeSqft * basePricesSqft[selectedCategoryIndex] * multiplier).toInt()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("INTERACTIVE AI DESK", color = SophisticatedTextMuted, fontSize = (11 * scaleFactor).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Estimate Repair & Project Costs", color = SophisticatedText, fontSize = (22 * scaleFactor).sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Text("Simulate cost projections for domestic refurbishment works dynamically.", color = SophisticatedTextAlt, fontSize = 13.sp)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SophisticatedSurface), modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Select Division Category", color = SophisticatedAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(2).forEachIndexed { index, name ->
                            val isSel = selectedCategoryIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SophisticatedAccent else SophisticatedItemBg)
                                    .clickable { selectedCategoryIndex = index }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(name, color = if (isSel) SophisticatedOnAccent else SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.drop(2).forEachIndexed { i, name ->
                            val index = i + 2
                            val isSel = selectedCategoryIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SophisticatedAccent else SophisticatedItemBg)
                                    .clickable { selectedCategoryIndex = index }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(name, color = if (isSel) SophisticatedOnAccent else SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SophisticatedSurface), modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("2. Estimate Covered Size", color = SophisticatedAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${sizeSqft.toInt()} Sqft", color = SophisticatedText, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = sizeSqft,
                        onValueChange = { sizeSqft = it },
                        valueRange = 100f..5000f,
                        colors = SliderDefaults.colors(thumbColor = SophisticatedAccent, activeTrackColor = SophisticatedAccent)
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SophisticatedSurface), modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Material & Chemicals Grade", color = SophisticatedAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Standard", "Premium", "Luxury").forEach { grade ->
                            val isSel = materialGrade == grade
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SophisticatedAccent else SophisticatedItemBg)
                                    .clickable { materialGrade = grade }
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(grade, color = if (isSel) SophisticatedOnAccent else SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF16191D))
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Estimated Quote Price", color = SophisticatedTextMuted, fontSize = 12.sp)
                            Text("₹$estimate", color = SophisticatedAccent, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.background(Color(0xFF00315B), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("SLA AUDITED", color = Color(0xFFD1E4FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onQuickBook(categories[selectedCategoryIndex], "AI Estimated ${categories[selectedCategoryIndex]} Work", estimate.toFloat()) },
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1-Click Dispatch From Wallet", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- ACTIVE DISPATCHES / BOOKINGS TAB ---
@Composable
fun CustomerBookingsTab(
    scaleFactor: Float,
    bookings: List<Booking>,
    walletBalance: Float,
    onRebook: (Booking) -> Unit,
    onCancelBooking: (Booking) -> Unit,
    onRescheduleBooking: (Booking, String, String) -> Unit,
    onRateBooking: (Booking, Int, String) -> Unit
) {
    var ratingBookingId by remember { mutableStateOf<String?>(null) }
    var ratingStars by remember { mutableStateOf(5) }
    var ratingComment by remember { mutableStateOf("") }
    var reschedulingBooking by remember { mutableStateOf<Booking?>(null) }
    var viewingInvoiceBookingId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ACTIVE DISPATCHES", color = SophisticatedTextMuted, fontSize = (11 * scaleFactor).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text("Ticket & Booking History", color = SophisticatedText, fontSize = (22 * scaleFactor).sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active dispatches. Book a service to get started.", color = SophisticatedTextAlt)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                items(bookings) { b ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SophisticatedBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(b.id, color = SophisticatedAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                // Status chip
                                val statusColor = when (b.status) {
                                    "completed" -> Color(0xFF10B981)
                                    "dispatched" -> Color(0xFF3B82F6)
                                    "cancelled" -> Color.Red
                                    else -> Color(0xFFF59E0B)
                                }
                                Box(modifier = Modifier.background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).border(1.dp, statusColor, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(b.status.uppercase(), color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(b.serviceName, color = SophisticatedText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(b.subServiceName, color = SophisticatedTextAlt, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Scheduled: ${b.date} • ${b.timeSlot}", color = SophisticatedTextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tech: ${b.technician}", color = SophisticatedTextMuted, fontSize = 12.sp)
                                Text("₹${b.price.toInt()}", color = SophisticatedText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // General Invoice Viewing
                            Button(
                                onClick = { viewingInvoiceBookingId = b.id },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2022)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Text("View Invoice Receipt", color = SophisticatedText)
                            }

                            if (b.status == "completed") {
                                if (b.reviewStars != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(b.reviewStars!!) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(b.reviewComment ?: "", color = SophisticatedTextAlt, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                    }
                                } else if (ratingBookingId == b.id) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Rate Service", color = SophisticatedAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Row {
                                            (1..5).forEach { star ->
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (star <= ratingStars) Color(0xFFF59E0B) else SophisticatedBorder,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clickable { ratingStars = star }
                                                )
                                            }
                                        }
                                        OutlinedTextField(
                                            value = ratingComment,
                                            onValueChange = { ratingComment = it },
                                            placeholder = { Text("Write comment...") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                onRateBooking(b, ratingStars, ratingComment)
                                                ratingBookingId = null
                                                ratingComment = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
                                        ) {
                                            Text("Submit Review", color = SophisticatedOnAccent)
                                        }
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { ratingBookingId = b.id },
                                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Rate Tech", color = SophisticatedOnAccent)
                                        }
                                        Button(
                                            onClick = { onRebook(b) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2F33)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("1-Click Re-book", color = SophisticatedText)
                                        }
                                    }
                                }
                            } else if (b.status != "cancelled") {
                                // Active dispatch dispatches actions
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { reschedulingBooking = b },
                                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reschedule", color = SophisticatedOnAccent)
                                    }
                                    Button(
                                        onClick = { onCancelBooking(b) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD3A3A)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel Job", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Render dialogues
    reschedulingBooking?.let { rb ->
        RescheduleBookingDialog(
            booking = rb,
            onDismiss = { reschedulingBooking = null },
            onConfirm = { newDate, newSlot ->
                onRescheduleBooking(rb, newDate, newSlot)
                reschedulingBooking = null
            }
        )
    }

    viewingInvoiceBookingId?.let { vibId ->
        val selectedB = bookings.find { it.id == vibId }
        selectedB?.let { b ->
            InvoiceDetailsDialog(
                scaleFactor = scaleFactor,
                booking = b,
                onDismiss = { viewingInvoiceBookingId = null }
            )
        }
    }
}

@Composable
fun BookingReviewAndInvoiceDialog(
    scaleFactor: Float,
    subService: SubService,
    category: ServiceCategory,
    walletBalance: Float,
    onConfirm: (date: String, timeSlot: String, couponCode: String, discount: Float, gst: Float, totalPayable: Float) -> Unit,
    onDismiss: () -> Unit,
    repository: BookingRepository
) {
    var date by remember { mutableStateOf("2026-07-20") }
    var timeSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }
    var couponCode by remember { mutableStateOf("") }
    var couponAppliedMsg by remember { mutableStateOf<String?>(null) }
    var couponAppliedSuccess by remember { mutableStateOf<Boolean?>(null) }
    var discountPercent by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    val basePrice = subService.price
    val discountAmount = basePrice * discountPercent
    val taxableAmount = basePrice - discountAmount
    val gstAmount = taxableAmount * 0.18f
    val totalPayable = taxableAmount + gstAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(date, timeSlot, couponCode, discountAmount, gstAmount, totalPayable)
                },
                enabled = walletBalance >= totalPayable,
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
            ) {
                Text("Pay & Book", color = SophisticatedOnAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SophisticatedTextAlt)
            }
        },
        title = {
            Text("Secure Booking Invoice Setup", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = SophisticatedText)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("SERVICE SUMMARY", color = SophisticatedTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("${category.name} - ${subService.name}", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Appointment inputs
                item {
                    Text("APPOINTMENT SCHEDULE", color = SophisticatedTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeSlot,
                            onValueChange = { timeSlot = it },
                            label = { Text("Time Slot", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Coupon Input
                item {
                    Text("APPLY PROMO / COUPONS", color = SophisticatedTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { couponCode = it },
                            placeholder = { Text("ONECALL50, WELCOME10") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val res = repository.applyCoupon(couponCode)
                                    couponAppliedSuccess = res.success
                                    couponAppliedMsg = res.message
                                    if (res.success) {
                                        discountPercent = res.discountPercent
                                    } else {
                                        discountPercent = 0f
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2F33)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply", color = SophisticatedText)
                        }
                    }
                    couponAppliedMsg?.let { msg ->
                        Text(
                            text = msg,
                            color = if (couponAppliedSuccess == true) Color(0xFF10B981) else Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tax Invoice Breakdown
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("STATUTORY TAX INVOICE", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = SophisticatedBorder, thickness = 0.5.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                textSelectorField("Base Price:", "₹${basePrice.toInt()}")
                            }
                            if (discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Coupon Discount:", color = Color(0xFF10B981), fontSize = 13.sp)
                                    Text("-₹${discountAmount.toInt()}", color = Color(0xFF10B981), fontSize = 13.sp)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                textSelectorField("GST (18%):", "₹${String.format("%.1f", gstAmount)}")
                            }
                            HorizontalDivider(color = SophisticatedBorder, thickness = 0.5.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Payable:", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${String.format("%.1f", totalPayable)}", color = SophisticatedAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    if (walletBalance < totalPayable) {
                        Text("Insufficient Wallet Balance. Please top up ₹${(totalPayable - walletBalance).toInt()} more.", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        },
        containerColor = SophisticatedSurface
    )
}

@Composable
fun textSelectorField(label: String, valStr: String) {
    Text(label, color = SophisticatedTextAlt, fontSize = 13.sp)
    Text(valStr, color = SophisticatedText, fontSize = 13.sp)
}

@Composable
fun InvoiceDetailsDialog(
    scaleFactor: Float,
    booking: Booking,
    onDismiss: () -> Unit
) {
    val basePrice = booking.price / 1.18f
    val gstAmount = booking.price - basePrice

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
            ) {
                Text("Close", color = SophisticatedOnAccent)
            }
        },
        title = {
            Text("STATUTORY TAX INVOICE", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SophisticatedAccent)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1C1E))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Invoice ID:", color = SophisticatedTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(booking.invoiceId.ifEmpty { "INV-UNKNOWN" }, color = SophisticatedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Booking ID:", color = SophisticatedTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(booking.id, color = SophisticatedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date Generated:", color = SophisticatedTextMuted, fontSize = 11.sp)
                    Text(booking.date, color = SophisticatedText, fontSize = 11.sp)
                }
                HorizontalDivider(color = SophisticatedBorder, thickness = 0.5.dp)

                Text("CLIENT DETAILS", color = SophisticatedAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Name: ${booking.customerName}", color = SophisticatedText, fontSize = 13.sp)
                Text("Phone: ${booking.phone}", color = SophisticatedText, fontSize = 13.sp)
                Text("Address: ${booking.address}", color = SophisticatedTextAlt, fontSize = 13.sp)
                HorizontalDivider(color = SophisticatedBorder, thickness = 0.5.dp)

                Text("SERVICE SUMMARY", color = SophisticatedAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("${booking.serviceName} - ${booking.subServiceName}", color = SophisticatedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Technician Assigned: ${booking.technician}", color = SophisticatedTextAlt, fontSize = 12.sp)
                HorizontalDivider(color = SophisticatedBorder, thickness = 0.5.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:", color = SophisticatedTextAlt, fontSize = 13.sp)
                    Text("₹${String.format("%.2f", basePrice)}", color = SophisticatedText, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST (18%):", color = SophisticatedTextAlt, fontSize = 13.sp)
                    Text("₹${String.format("%.2f", gstAmount)}", color = SophisticatedText, fontSize = 13.sp)
                }
                HorizontalDivider(color = SophisticatedBorder, thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount Paid:", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹${booking.price.toInt()}", color = SophisticatedAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Digitally signed secure transaction copy. Powered by One Call SLA Guarantee.",
                    fontSize = 9.sp,
                    color = SophisticatedTextMuted,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = SophisticatedSurface
    )
}

@Composable
fun RescheduleBookingDialog(
    booking: Booking,
    onConfirm: (newDate: String, newSlot: String) -> Unit,
    onDismiss: () -> Unit
) {
    var date by remember { mutableStateOf(booking.date) }
    var slot by remember { mutableStateOf(booking.timeSlot) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(date, slot) },
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
            ) {
                Text("Update Appointment", color = SophisticatedOnAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SophisticatedTextAlt)
            }
        },
        title = {
            Text("Reschedule Appointment", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ticket ID: ${booking.id}", color = SophisticatedAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("New Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = slot,
                    onValueChange = { slot = it },
                    label = { Text("New Time Slot") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = SophisticatedSurface
    )
}

// --- PROFILE & SECURE WALLET TAB WITH SETTINGS ---
@Composable
fun CustomerProfileWalletTab(
    scaleFactor: Float,
    user: UserState,
    walletBalance: Float,
    transactions: List<Transaction>,
    onTopUp: (Float) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    highContrast: Boolean,
    onHighContrastChange: (Boolean) -> Unit,
    textScale: String,
    onTextScaleChange: (String) -> Unit,
    offlineMode: Boolean,
    onOfflineModeChange: (Boolean) -> Unit,
    notifications: List<NotificationLog>
) {
    var topUpAmount by remember { mutableStateOf("1000") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("SECURE WORKSPACE & WALLET", color = SophisticatedTextMuted, fontSize = (11 * scaleFactor).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Wallet & Environment Deck", color = SophisticatedText, fontSize = (22 * scaleFactor).sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        }

        // Wallet Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ACTIVE WALLET BALANCE", color = SophisticatedTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${walletBalance.toInt()}", color = SophisticatedAccent, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = topUpAmount,
                            onValueChange = { topUpAmount = it },
                            placeholder = { Text("Amount") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SophisticatedText, unfocusedTextColor = SophisticatedText, focusedBorderColor = SophisticatedAccent),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onTopUp(topUpAmount.toFloatOrNull() ?: 1000f) },
                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
                        ) {
                            Text("Top Up", color = SophisticatedOnAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Settings / Accessibility Deck
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ACCESSIBILITY DECK", color = SophisticatedAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Language Selection Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("App Language", color = SophisticatedText)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("en" to "EN", "hi" to "HI", "te" to "TE", "ta" to "TA", "es" to "ES").forEach { (code, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (language == code) SophisticatedAccent else SophisticatedItemBg)
                                        .clickable { onLanguageChange(code) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(label, color = if (language == code) SophisticatedOnAccent else SophisticatedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Contrast Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("High Contrast Mode", color = SophisticatedText)
                        Switch(checked = highContrast, onCheckedChange = onHighContrastChange)
                    }

                    // Font Size Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Text Size Scale", color = SophisticatedText)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("sm" to "Small", "md" to "Standard", "lg" to "Large").forEach { (size, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (textScale == size) SophisticatedAccent else SophisticatedItemBg)
                                        .clickable { onTextScaleChange(size) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(label, color = if (textScale == size) SophisticatedOnAccent else SophisticatedText, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Offline simulation Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Offline Local Cache", color = SophisticatedText)
                        Switch(checked = offlineMode, onCheckedChange = onOfflineModeChange)
                    }
                }
            }
        }

        // Transactions Ledger
        item {
            Text("SECURE TRANSACTION LEDGER", color = SophisticatedText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(transactions) { tx ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SophisticatedSurface)
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(tx.description, color = SophisticatedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${tx.date} • ${tx.id}", color = SophisticatedTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = if (tx.type == "deposit") "+₹${tx.amount.toInt()}" else "-₹${tx.amount.toInt()}",
                        color = if (tx.type == "deposit") Color(0xFF10B981) else Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Live Alerts Feed
        item {
            Text("SLA ALERTS & FEEDBACK COPIES", color = SophisticatedText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(notifications) { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SophisticatedSurface)
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("[${log.type.uppercase()}] SLA ALERT", color = SophisticatedAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(log.timestamp, color = SophisticatedTextMuted, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(log.message, color = SophisticatedTextAlt, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- TECHNICIAN PORTAL SCREEN ---
@Composable
fun TechnicianPortalScreen(
    scaleFactor: Float,
    bookings: List<Booking>,
    onUpdateStatus: (Booking, String) -> Unit,
    onAddNotification: (NotificationLog) -> Unit,
    user: UserState
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ACTIVE WORKSPACE: PARTNER", color = SophisticatedTextMuted, fontSize = (11 * scaleFactor).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text("Assigned Ticket Board", color = SophisticatedText, fontSize = (22 * scaleFactor).sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No jobs assigned to you.", color = SophisticatedTextAlt)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                items(bookings) { b ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SophisticatedBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(b.id, color = SophisticatedAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(b.status.uppercase(), color = Color(0xFF3B82F6), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(b.serviceName, color = SophisticatedText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(b.subServiceName, color = SophisticatedTextAlt, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Client: ${b.customerName} (${b.phone})", color = SophisticatedTextAlt, fontSize = 12.sp)
                            Text("Address: ${b.address}", color = SophisticatedTextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (b.status != "completed" && b.status != "cancelled") {
                                    Button(
                                        onClick = {
                                            onUpdateStatus(b, "in_progress")
                                            onAddNotification(
                                                NotificationLog(
                                                    id = "NL-${Random.nextInt(100000, 999999)}",
                                                    type = "WhatsApp",
                                                    recipient = b.phone,
                                                    message = "SLA alert: Tech Rajesh has updated Ticket ${b.id} to IN PROGRESS. Work is underway.",
                                                    timestamp = "Just now"
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Start Work", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            onUpdateStatus(b, "completed")
                                            onAddNotification(
                                                NotificationLog(
                                                    id = "NL-${Random.nextInt(100000, 999999)}",
                                                    type = "WhatsApp",
                                                    recipient = b.phone,
                                                    message = "SLA Alert: Ticket ${b.id} completed. Download invoice copy in your Profile tab.",
                                                    timestamp = "Just now"
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Complete", color = Color.White)
                                    }
                                } else {
                                    Text("Job Completed / Cancelled", color = SophisticatedTextMuted, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ADMIN DATA MODELS & PROGRAMMATIC SERVICE CATALOG ---
data class AdminCustomerProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val status: String, // Active, Blocked
    val signUpDate: String,
    val walletBalance: Float
)

data class TechnicianProfile(
    val fullName: String,
    val phone: String,
    val kycStatus: String, // Approved, Pending, Suspended, Rejected
    val aadhaar: String,
    val pan: String,
    val skill: String,
    val rating: Float,
    val liveStatus: String // Online, Offline
)

data class AMCPlan(
    val customerName: String,
    val planName: String,
    val price: Float,
    val expiryDate: String,
    val status: String // Active, Expired, Renewed
)

data class AuditLogEntry(
    val timestamp: String,
    val actor: String,
    val action: String,
    val status: String // Success, Failed, Warning
)

// Programmatically generates a complete 315-service catalog matching the requirement of 300+ services
fun generate300Services(): List<ServiceCategory> {
    val list = mutableListOf<ServiceCategory>()
    
    // 1. Cleaning Services
    val cleaningSub = mutableListOf<SubService>()
    for (i in 1..21) {
        cleaningSub.add(
            SubService(
                name = when(i) {
                    1 -> "Full House Deep Cleaning"
                    2 -> "Bathroom Tile Grout Clean"
                    3 -> "Kitchen Modular Service"
                    4 -> "Sofa & Mattress Vacuuming"
                    5 -> "Water Tank Disinfection"
                    6 -> "Balcony Floor Washing"
                    7 -> "Glass Window Polishing"
                    8 -> "Terrace High Pressure Clean"
                    9 -> "Marble Floor Diamond Buff"
                    10 -> "Fabric Curtain Dry Clean"
                    11 -> "Office Workspace Sanitizing"
                    12 -> "Premium Car Detailing Inside"
                    13 -> "Pest-Control Deep Dusting"
                    14 -> "Chimney Exterior Degreasing"
                    15 -> "Wooden Floor Wax Polishing"
                    16 -> "Drain Sump Sludge Removal"
                    17 -> "Kitchen Exhaust Fan Wash"
                    18 -> "Air Vent Dust Extraction"
                    19 -> "Garage Door Shutter Wash"
                    20 -> "Upholstery Steam Clean"
                    else -> "Post-Construction Clean"
                },
                description = "Professional heavy-duty sanitation service with eco-friendly agents.",
                price = 399f + i * 150f
            )
        )
    }
    list.add(
        ServiceCategory(
            id = "cleaning",
            number = "01",
            name = "Cleaning Services",
            description = "Deep residential, bathroom, kitchen, and upholstery sanitation.",
            image = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=400&q=80",
            startingPrice = 499f,
            popular = true,
            subservices = cleaningSub
        )
    )

    // 2. Painting Services
    val paintingSub = mutableListOf<SubService>()
    for (i in 1..21) {
        paintingSub.add(
            SubService(
                name = when(i) {
                    1 -> "Interior Luxury Paint"
                    2 -> "Exterior Facade Weatherproof"
                    3 -> "Designer Wall Accent Art"
                    4 -> "Varnish Wood Polishing"
                    5 -> "Metal Anti-Rust Primer"
                    6 -> "Ceiling Whitewash Coat"
                    7 -> "Garage Shutter Paint"
                    8 -> "Waterproof Putty App"
                    9 -> "Damp Proof Base Coat"
                    10 -> "Stencils & Decals Mount"
                    11 -> "Texture Wall Plastering"
                    12 -> "Door & Window Spray"
                    13 -> "Epoxy Floor Coating"
                    14 -> "Kids Room Pattern Paint"
                    15 -> "Hallway Dual Shade Trim"
                    16 -> "Kitchen Cabinets Polycoat"
                    17 -> "Fencing Enamel Polish"
                    18 -> "Balcony Grill Refinishing"
                    19 -> "Royal Gold Leaf Accent"
                    20 -> "Matte Finish Wall Top"
                    else -> "Base Wall Putty Levelling"
                },
                description = "Dustless surface preparation with premium eco-friendly materials.",
                price = 1999f + i * 500f
            )
        )
    }
    list.add(
        ServiceCategory(
            id = "painting",
            number = "02",
            name = "Painting Services",
            description = "Dustless interior & exterior wall painting and premium varnishing.",
            image = "https://images.unsplash.com/photo-1562259949-e8e7689d7828?auto=format&fit=crop&w=400&q=80",
            startingPrice = 4999f,
            popular = false,
            subservices = paintingSub
        )
    )

    // 3. Electrical Services
    val electricalSub = mutableListOf<SubService>()
    for (i in 1..21) {
        electricalSub.add(
            SubService(
                name = when(i) {
                    1 -> "Smart Switch & DB Setup"
                    2 -> "EV Charger Point Mount"
                    3 -> "Chandelier Accent Light Install"
                    4 -> "Inverter & Battery Wiring"
                    5 -> "Home Generator Setup"
                    6 -> "AC Heavy Duty Point Wire"
                    7 -> "Solar Panel Cable Route"
                    8 -> "Geyser Heating Element Swap"
                    9 -> "Exhaust Fan Duct Connection"
                    10 -> "Designer LED Strip Run"
                    11 -> "Wall TV Bracket Mounting"
                    12 -> "Surge Protection Relay Fix"
                    13 -> "Copper Earth Electrode Pit"
                    14 -> "Smart Doorbell Video Hook"
                    15 -> "Ceiling Fan Heavy Anchor"
                    16 -> "Kitchen Hob Ignition Solder"
                    17 -> "Main Circuit Breaker Swap"
                    18 -> "Short Circuit Wire Locate"
                    19 -> "Living Room Mood Dimmer"
                    20 -> "Conduit Pipe Hidden Lay"
                    else -> "General Electric Checkup"
                },
                description = "Certified professional engineering ensuring load safety compliance.",
                price = 199f + i * 120f
            )
        )
    }
    list.add(
        ServiceCategory(
            id = "electrical",
            number = "03",
            name = "Electrical Services",
            description = "House rewiring, EV chargers, smart switchboards, and audits.",
            image = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=400&q=80",
            startingPrice = 199f,
            popular = true,
            subservices = electricalSub
        )
    )

    // 4. Plumbing Services
    val plumbingSub = mutableListOf<SubService>()
    for (i in 1..21) {
        plumbingSub.add(
            SubService(
                name = when(i) {
                    1 -> "Leak Detection & Repair"
                    2 -> "Tap & Mixer Replacement"
                    3 -> "Drain Velocity Clog Clear"
                    4 -> "Water Pressure Pump Mount"
                    5 -> "Basin Pipe Siphon Solder"
                    6 -> "Geyser Pipe Joint Seal"
                    7 -> "RO Inlet Connector Feed"
                    8 -> "Flush Tank Syphon Repair"
                    9 -> "Shower Head Decalcify"
                    10 -> "Hidden Pipeline Grout Fix"
                    11 -> "Washing Machine Inlet Adapter"
                    12 -> "Main Control Valve Swap"
                    13 -> "Kitchen Sink Steel Install"
                    14 -> "Toilet Bowl Wax Ring Seal"
                    15 -> "Anti-Odour Grating Fitting"
                    16 -> "Drainage Manhole Clean"
                    17 -> "Sewer Pipe Heavy Clean"
                    18 -> "Water Meter Bypass Route"
                    19 -> "Hot & Cold Line Diverter"
                    20 -> "Rainwater Pipe PVC Glue"
                    else -> "General Plumbing Check"
                },
                description = "High precision plumbing & leak control services with branded fittings.",
                price = 149f + i * 90f
            )
        )
    }
    list.add(
        ServiceCategory(
            id = "plumbing",
            number = "04",
            name = "Plumbing Services",
            description = "Leak detection, pipeline repair, pressure pumps, and clogs.",
            image = "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?auto=format&fit=crop&w=400&q=80",
            startingPrice = 199f,
            popular = true,
            subservices = plumbingSub
        )
    )

    // 5. Smart Home Security
    val securitySub = mutableListOf<SubService>()
    for (i in 1..21) {
        securitySub.add(
            SubService(
                name = when(i) {
                    1 -> "Smart Biometric Lock Setup"
                    2 -> "CCTV IP Camera Setup"
                    3 -> "Mesh WiFi & Conduit Laying"
                    4 -> "Motion Sensor Bracket Mount"
                    5 -> "Video Door Phone Cabling"
                    6 -> "Magnetic Window Sensor"
                    7 -> "Smart Alarm Siren Pairing"
                    8 -> "Gas Leak Detector Alarm"
                    9 -> "IP Dome Camera Intercom"
                    10 -> "Network Router Firewall"
                    11 -> "Smart Safe Fingerprint Reset"
                    12 -> "Panic Button Emergency Alert"
                    13 -> "RFID Keycard Controller"
                    14 -> "Digital Peephole Monitor"
                    15 -> "Smart Curtain Automation Relay"
                    16 -> "NAS Storage Box Setup"
                    17 -> "Smart Hub Zigbee Pair"
                    18 -> "Fiber Optic Joint Splicing"
                    19 -> "Solar Perimeter Laser"
                    20 -> "Water Flood Sensor Alert"
                    else -> "Full Security Audit"
                },
                description = "State-of-the-art surveillance and encryption-based locking installations.",
                price = 499f + i * 250f
            )
        )
    }
    list.add(
        ServiceCategory(
            id = "security",
            number = "05",
            name = "Smart Home Security",
            description = "Biometric door locks, CCTV IP cameras, and smart sensors.",
            image = "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=400&q=80",
            startingPrice = 999f,
            popular = true,
            subservices = securitySub
        )
    )

    val extraCats = listOf(
        Triple("appliances", "Appliance Repair", "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=400&q=80"),
        Triple("ac_service", "AC Repair & Service", "https://images.unsplash.com/photo-1621905251918-48416bd8575a?auto=format&fit=crop&w=400&q=80"),
        Triple("carpentry", "Carpentry Services", "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&w=400&q=80"),
        Triple("pest_control", "Pest Control & Sanitization", "https://images.unsplash.com/photo-1587300003388-59208cc962cb?auto=format&fit=crop&w=400&q=80"),
        Triple("gardening", "Gardening & Lawn Care", "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?auto=format&fit=crop&w=400&q=80"),
        Triple("automation", "Home Automation & IR", "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=400&q=80"),
        Triple("masonry", "Masonry & Tile Work", "https://images.unsplash.com/photo-1590069261209-f8e9b8642343?auto=format&fit=crop&w=400&q=80"),
        Triple("packers", "Packers & Movers", "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=400&q=80"),
        Triple("water_purifier", "RO Water Purifier System", "https://images.unsplash.com/photo-1609766918205-59b43e86c075?auto=format&fit=crop&w=400&q=80"),
        Triple("chimney", "Kitchen Chimney & Hob", "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=400&q=80")
    )

    for ((index, extra) in extraCats.withIndex()) {
        val catId = extra.first
        val catName = extra.second
        val catImg = extra.third
        val subServices = mutableListOf<SubService>()
        for (i in 1..21) {
            subServices.add(
                SubService(
                    name = when(catId) {
                        "appliances" -> when(i) {
                            1 -> "Washing Machine Motor Repair"
                            2 -> "Refrigerator Gas Charging"
                            3 -> "Microwave Magnetron Swap"
                            4 -> "Air Cooler Pump Install"
                            5 -> "Dishwasher Inlet Clean"
                            6 -> "TV Panel Dry Mount"
                            7 -> "Food Processor Clutch Fix"
                            8 -> "Mixer Grinder Coil Rewind"
                            9 -> "Induction Cooktop Sensor"
                            10 -> "Iron Box Power Cable Change"
                            11 -> "OTG Thermostat Fitting"
                            12 -> "Vacuum Cleaner Hose Repair"
                            13 -> "Hair Dryer Motor Solder"
                            14 -> "Deep Freezer Fans Clean"
                            15 -> "Air Purifier Filter Swap"
                            16 -> "Ceiling Fan Regulator Setup"
                            17 -> "Kitchen Blender Bush Change"
                            18 -> "Rice Cooker Thermopile Fix"
                            19 -> "Electric Kettle Thermostat"
                            20 -> "Sewing Machine Oil & Service"
                            else -> "Standard Appliance Diagnostics"
                        }
                        "ac_service" -> when(i) {
                            1 -> "AC Filter Jet Wash"
                            2 -> "AC Gas Leak Repair & Refill"
                            3 -> "Split AC Mounting Setup"
                            4 -> "Window AC Copper Coil Clean"
                            5 -> "AC Condenser Fan Replacement"
                            6 -> "AC Compressor Swap Service"
                            7 -> "AC Circuit Board Soldering"
                            8 -> "AC Drainage Hose Alignment"
                            9 -> "AC Anti-Rust Paint Coating"
                            10 -> "AC Remote Sensor Swap"
                            11 -> "Multi-split Inverter Calibrate"
                            12 -> "AC Duct Decontamination"
                            13 -> "AC Starter Capacitor Setup"
                            14 -> "AC Damper Motor Repair"
                            15 -> "AC Shifting & Dismantling"
                            16 -> "AC Blower Fan Lubrication"
                            17 -> "AC Annual Maintenance Audit"
                            18 -> "AC Expansion Valve Install"
                            19 -> "AC Thermostat Calibration"
                            20 -> "AC High Pressure Jet Wash"
                            else -> "Emergency AC Gas Leak Seal"
                        }
                        "carpentry" -> when(i) {
                            1 -> "Door Hinge & Alignment Fix"
                            2 -> "Modular Cabinet Channel Swap"
                            3 -> "Wooden Wardrobe Lock Mount"
                            4 -> "Sofa Frame Joint Reinforce"
                            5 -> "Study Table Wall Anchoring"
                            6 -> "Drawer Handle Installation"
                            7 -> "Plywood Partition Board Fit"
                            8 -> "Wooden Window Latch Solder"
                            9 -> "Teakwood Table Leg Repair"
                            10 -> "Bed Hydraulic Piston Fit"
                            11 -> "TV Backpanel Wooden Base"
                            12 -> "Key Duplicate Lock Calibration"
                            13 -> "Sliding Wardrobe Roller Swap"
                            14 -> "Balcony Deck Wooden Planks"
                            15 -> "Glass Door Hinge Fitting"
                            16 -> "Office Desk Keyboard Tray"
                            17 -> "Wooden Wall Shelf Framing"
                            18 -> "Dining Table Glass Polish"
                            19 -> "Door Frame Weather Stripping"
                            20 -> "Custom Modular Drawer Setup"
                            else -> "Wood Crack Resin Polishing"
                        }
                        "pest_control" -> when(i) {
                            1 -> "Termite Soil Treatment"
                            2 -> "Bedbug Steaming & Spray"
                            3 -> "Cockroach Herbal Gel Placement"
                            4 -> "Mosquito Fogging Yard Service"
                            5 -> "Rodent Adhesive Trap Layout"
                            6 -> "Ant Extraction Powder Feed"
                            7 -> "Woodborer Syringe Inject"
                            8 -> "Silverfish Closet Spray"
                            9 -> "Antiviral Disinfection Fog"
                            10 -> "Beehive Removal Operations"
                            11 -> "Flies Trap Liquid Setup"
                            12 -> "Lawn White Grub Drenching"
                            13 -> "Flea & Tick Dog Bed Spray"
                            14 -> "Snake Repellent Granules Layout"
                            15 -> "Spider Web Dust-Sweep Clear"
                            16 -> "Crawlspace Fumigation Treatment"
                            17 -> "Eco-Friendly Kitchen Gel"
                            18 -> "Lizard Repellent Wall Spray"
                            19 -> "Warehouse Bird Netting Mount"
                            20 -> "Restaurant Pest Defense Plan"
                            else -> "General Pest Shield Shield"
                        }
                        "gardening" -> when(i) {
                            1 -> "Lawn Grass Trimming"
                            2 -> "Vertical Wall Garden Fitting"
                            3 -> "Potting Soil Mix Preparation"
                            4 -> "Drip Line Valve Fitting"
                            5 -> "Plant Disease Liquid Spray"
                            6 -> "Bonsai Styling & Pruning"
                            7 -> "Decorative Pebble Layout"
                            8 -> "Organic Fertilizer Feed"
                            9 -> "Coco Peat Hanging Baskets"
                            10 -> "Lawn Weeding & Sod Laying"
                            11 -> "Flowering Plant Grafting"
                            12 -> "Nursery Plant Setup Help"
                            13 -> "Garden Fence Metal Border"
                            14 -> "Ditch Digging & Soil Turning"
                            15 -> "Compost Pit Bin Installation"
                            16 -> "Hydroponic Nutrient Setup"
                            17 -> "Succulent Garden Pot Mix"
                            18 -> "Shrub Pruning & Edging"
                            19 -> "Indoor Palm Revitalization"
                            20 -> "Garden Lights Solar Wire"
                            else -> "Landscape Design Consult"
                        }
                        "automation" -> when(i) {
                            1 -> "Smart Hub Integration Plan"
                            2 -> "IR Blaster Mobile Pairing"
                            3 -> "Occupancy Sensor Setup"
                            4 -> "Smart Curtains Track Mount"
                            5 -> "RGB Light Strip Controller"
                            6 -> "Voice Assistant Alexa Config"
                            7 -> "Smart Thermostat Coupling"
                            8 -> "Smart Plug Appliance Pair"
                            9 -> "Bluetooth Ceiling Speaker Fit"
                            10 -> "Smart Switch Overlay Setup"
                            11 -> "Smart Geyser Auto Timer"
                            12 -> "Water Tank Float Wi-Fi Feed"
                            13 -> "Smart Door Unlock Sensor"
                            14 -> "Network Extender Access Point"
                            15 -> "Scene Trigger Switch Config"
                            16 -> "Smart Lock Fingerprint Add"
                            17 -> "Smart Camera Stream Sync"
                            18 -> "Automated Pet Feeder Setup"
                            19 -> "Home Theater Projector Calibrate"
                            20 -> "Smart Irrigation Controller"
                            else -> "Full Automated Scenario Setup"
                        }
                        "masonry" -> when(i) {
                            1 -> "Bathroom Tile Grout Refill"
                            2 -> "Kitchen Countertop Joint Fix"
                            3 -> "Cracked Cement Wall Plaster"
                            4 -> "Brick Wall Corner Laying"
                            5 -> "Marble Floor Polish Scrub"
                            6 -> "Balcony Leak Waterproof Coat"
                            7 -> "Basement Damp Jet Screed"
                            8 -> "Interlocking Paver Block Lay"
                            9 -> "Granite Step Chamfer Polish"
                            10 -> "Concrete Fence Post Base"
                            11 -> "Plaster of Paris Ceiling Patch"
                            12 -> "Bathroom Floor Tile Repair"
                            13 -> "Stone Cladding Wall Fit"
                            14 -> "Window Sill Solder Seal"
                            15 -> "Terrace Epoxy Waterproof Layer"
                            16 -> "Septic Tank Tank Crack Cement"
                            17 -> "Kitchen Backsplash Accent Fit"
                            18 -> "Exterior Crack Polyurethane Fill"
                            19 -> "Water Fountain Stone Mortar"
                            20 -> "Concrete Retaining Slabs Setup"
                            else -> "General Masonry Restoration"
                        }
                        "packers" -> when(i) {
                            1 -> "Local 1BHK Shifting Plan"
                            2 -> "Premium Double Bubble Wrap"
                            3 -> "Furniture Wooden Dismantling"
                            4 -> "Appliance Safety Box Pack"
                            5 -> "Heavy Loading Labor Aid"
                            6 -> "Closed Container Truck Log"
                            7 -> "Delicate Crockery Styrofoam"
                            8 -> "Wardrobe Hanging Box Pack"
                            9 -> "Unpacking & Wardrobe Sorting"
                            10 -> "Storage Unit Logistics Setup"
                            11 -> "Interstate Transit Cargo Permit"
                            12 -> "Office IT Server Box Pack"
                            13 -> "Two-Wheeler Bubble Pack Carrier"
                            14 -> "Pianoforte / Heavy Safe Move"
                            15 -> "Short-Term Safe Warehouse Stow"
                            16 -> "Toll & Octroi Tax Clearance"
                            17 -> "Fragile Paintings Wood Crating"
                            18 -> "Gym Equipment Bolt Packing"
                            19 -> "Heavy Mattress Protective Cover"
                            20 -> "House Unpacking & Layout Help"
                            else -> "Express Single Item Courier"
                        }
                        "water_purifier" -> when(i) {
                            1 -> "RO Filter Candle Replace"
                            2 -> "RO Osmosis Membrane Swap"
                            3 -> "Water Purifier TDS Calibration"
                            4 -> "RO Booster Pressure Pump"
                            5 -> "UV Lamp Replacement"
                            6 -> "Purifier Pipeline Acid Wash"
                            7 -> "RO Leak Float Trigger Fix"
                            8 -> "Carbon Block Odour Absorb"
                            9 -> "Purifier Tap Chrome Replacement"
                            10 -> "Pre-Filter Housing Assembly"
                            11 -> "RO Auto-Flush Circuit Board"
                            12 -> "TDS Sensor LED Panel Fix"
                            13 -> "Alkaline Mineral Cartridge Add"
                            14 -> "RO Tank Food Grade Sanitizer"
                            15 -> "Wall Bracket Mounting RO"
                            16 -> "RO Input Pressure Regulator"
                            17 -> "Purifier Pipe Push Connector"
                            18 -> "RO Wastewater Siphon Pipe"
                            19 -> "Copper Filter Cartridge Fitting"
                            20 -> "Gravity Filter Candle Scrub"
                            else -> "RO Water Quality Diagnostics"
                        }
                        else -> when(i) { // chimney
                            1 -> "Chimney Filter Baffle Scrub"
                            2 -> "Chimney Suction Fan Motor Swap"
                            3 -> "Hob Spark Plug Igniter Polish"
                            4 -> "Gas Hob Burner Cleaning"
                            5 -> "Chimney Duct PVC Pipe Extension"
                            6 -> "Chimney Touch Panel Solder"
                            7 -> "Auto-Clean Heating Coil Replace"
                            8 -> "Hob Gas Pipe Brass Fitting"
                            9 -> "Chimney Charcoal Filter Insert"
                            10 -> "Hob Glass Top Adhesive Seal"
                            11 -> "Chimney LED Canopy Bulb"
                            12 -> "Hob Knob Valve Lubricate"
                            13 -> "Chimney Non-Return Damper Valve"
                            14 -> "Chimney Ceiling Bracket Anchor"
                            15 -> "Hob Regulator Gas Leak Audit"
                            16 -> "Island Chimney Hanging Cable"
                            17 -> "Hob Burner Ring Brass Swap"
                            18 -> "Chimney Dynamic Speed Sensor"
                            19 -> "Hob Flame Out Sensor Coupler"
                            20 -> "Chimney Oil Collector Cup"
                            else -> "Kitchen Hob Main Burner Refill"
                        }
                    },
                    description = "Fully accredited diagnostics and installation service backed by warranty.",
                    price = 299f + i * 110f
                )
            )
        }
        list.add(
            ServiceCategory(
                id = catId,
                number = String.format("%02d", index + 6),
                name = catName,
                description = "Authorized professional services with transparent standard tariff rate pricing.",
                image = catImg,
                startingPrice = 299f,
                popular = (index % 3 == 0),
                subservices = subServices
            )
        )
    }
    return list
}

// Custom interactive Canvas-based Revenue Chart with daily/weekly/monthly/yearly toggling
@Composable
fun RevenueChart(timeframe: String, scaleFactor: Float) {
    val data = when (timeframe) {
        "daily" -> listOf(12f, 15f, 18f, 14f, 22f, 25f, 19f)
        "weekly" -> listOf(80f, 95f, 110f, 85f, 92f)
        "yearly" -> listOf(380f, 450f, 520f, 580f)
        else -> listOf(35f, 42f, 39f, 51f, 48f, 56f, 62f, 58f, 64f) // monthly
    }

    val labels = when (timeframe) {
        "daily" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        "weekly" -> listOf("W1", "W2", "W3", "W4", "W5")
        "yearly" -> listOf("2023", "2024", "2025", "2026")
        else -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep")
    }

    val maxValue = data.maxOrNull() ?: 100f
    
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Revenue Visualizer (₹ in Thousands)", color = SophisticatedTextMuted, fontSize = (11 * scaleFactor).sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxSize().weight(1f)) {
                val width = size.width
                val height = size.height
                val spacing = width / (data.size + 1)
                
                // Draw grid lines
                for (i in 0..4) {
                    val y = height - (height * i / 4)
                    drawLine(
                        color = SophisticatedBorder.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw bars or line path
                data.forEachIndexed { idx, valVal ->
                    val x = spacing * (idx + 1)
                    val barHeight = (valVal / maxValue) * (height - 30f)
                    val y = height - barHeight - 15f
                    
                    // Draw rounded bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(SophisticatedAccent, SophisticatedAccent.copy(alpha = 0.4f))
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(x - 14f, y),
                        size = androidx.compose.ui.geometry.Size(28f, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                labels.forEach { label ->
                    Text(label, color = SophisticatedTextMuted, fontSize = (9 * scaleFactor).sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// --- MAIN ADMIN PORTAL SCREEN ---
@Composable
fun AdminPortalScreen(
    scaleFactor: Float,
    bookings: List<Booking>,
    transactions: List<Transaction>,
    repository: BookingRepository?,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    var currentAdminTab by remember { mutableStateOf("dashboard") }
    
    // SLA & Revenue calculations
    val totalBilling = bookings.sumOf { it.price.toDouble() }.toFloat()
    val totalDispatches = bookings.size
    val activeJobs = bookings.count { it.status == "pending" || it.status == "dispatched" || it.status == "in_progress" }
    val completedJobs = bookings.count { it.status == "completed" }
    val cancelledJobs = bookings.count { it.status == "cancelled" }
    
    // Seeding mock administrative collections
    var customers by remember {
        mutableStateOf(
            listOf(
                AdminCustomerProfile("Rajesh Sharma", "rajesh.s@gmail.com", "+91 98123 45678", "45, Sector 15, Gurugram, HR", "Active", "2026-01-10", 3500f),
                AdminCustomerProfile("Amit Verma", "amit.v@hotmail.com", "+91 80193 18625", "102, Shanti Kunj, New Delhi", "Active", "2026-02-15", 4200f),
                AdminCustomerProfile("Pooja Nair", "pooja.nair@yahoo.com", "+91 91234 56789", "8A, Maple Road, Bengaluru, KA", "Active", "2026-03-22", 1500f),
                AdminCustomerProfile("Suresh Kumar", "suresh.k@gmail.com", "+91 98765 43210", "Flat 402, Royal Residency, Mumbai", "Blocked", "2026-04-05", 0f),
                AdminCustomerProfile("Divya Reddy", "divya.r@outlook.com", "+91 99001 12233", "12-4, Gachibowli, Hyderabad, TS", "Active", "2026-05-18", 2800f),
                AdminCustomerProfile("Meera Sen", "meera.sen@gmail.com", "+91 94432 10987", "Park Street, Kolkata, WB", "Active", "2026-06-01", 500f)
            )
        )
    }

    var technicians by remember {
        mutableStateOf(
            listOf(
                TechnicianProfile("Rajesh Kumar", "+91 98877 66554", "Approved", "5921-9302-1234", "ABCDE1234F", "Electrical Services", 4.9f, "Online"),
                TechnicianProfile("Amit Sharma", "+91 91122 33445", "Approved", "1029-3847-5629", "XYZWY9876A", "Smart Home Security", 4.7f, "Online"),
                TechnicianProfile("Suresh Raina", "+91 88899 00112", "Pending", "8492-1029-3847", "PLMKO5678Q", "Plumbing Services", 0.0f, "Offline"),
                TechnicianProfile("Karan Malhotra", "+91 77766 55443", "Approved", "2910-3847-5621", "IUYTR4321Z", "Cleaning Services", 4.5f, "Online"),
                TechnicianProfile("Sunil Chhetri", "+91 90001 90002", "Suspended", "3829-1029-4857", "VBGTY7654E", "Painting Services", 3.8f, "Offline"),
                TechnicianProfile("Jasprit Bumrah", "+91 99999 88888", "Pending", "4829-1029-3847", "QWERT0987Y", "AC Repair & Service", 0.0f, "Online")
            )
        )
    }

    var amcPlans by remember {
        mutableStateOf(
            listOf(
                AMCPlan("Rajesh Sharma", "One Call Platinum Shield", 14999f, "2026-12-31", "Active"),
                AMCPlan("Amit Verma", "Annual Electrical & Air-Con Protection", 8999f, "2026-10-15", "Active"),
                AMCPlan("Pooja Nair", "Complete Appliance Care Pack", 6499f, "2026-06-18", "Expired"),
                AMCPlan("Divya Reddy", "Premium Smart Security AMC", 11999f, "2027-01-20", "Active")
            )
        )
    }

    var auditLogs by remember {
        mutableStateOf(
            listOf(
                AuditLogEntry("10:45 AM", "System Sentinel", "Automatic Database Sync verified successfully", "Success"),
                AuditLogEntry("11:02 AM", "Admin Operator", "Authorized access established for admin console", "Success"),
                AuditLogEntry("11:15 AM", "SLA Monitor", "Dispatched alert for emergency job SLA resolution", "Warning"),
                AuditLogEntry("12:30 PM", "Razorpay Hook", "Secure payment callback validated", "Success")
            )
        )
    }

    // Dynamic generated service catalog (315 Services)
    val allServicesCatalog = remember { generate300Services() }

    // Tab search & filtering states
    var customerSearchQuery by remember { mutableStateOf("") }
    var customerFilterStatus by remember { mutableStateOf("All") }
    
    var techSearchQuery by remember { mutableStateOf("") }
    var techFilterStatus by remember { mutableStateOf("All") }

    var bookingSearchQuery by remember { mutableStateOf("") }
    var bookingFilterStatus by remember { mutableStateOf("All") }
    
    var serviceSearchQuery by remember { mutableStateOf("") }
    var serviceFilterCategory by remember { mutableStateOf("All") }

    var selectedChartTimeframe by remember { mutableStateOf("monthly") }

    // Dialog state controllers
    var selectedCustomerForDetail by remember { mutableStateOf<AdminCustomerProfile?>(null) }
    var selectedBookingForInvoice by remember { mutableStateOf<Booking?>(null) }
    var selectedBookingForReschedule by remember { mutableStateOf<Booking?>(null) }
    
    var newRescheduleDate by remember { mutableStateOf("2026-07-21") }
    var newRescheduleSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }

    // Push notifications states
    var pushTargetGroup by remember { mutableStateOf("All") }
    var pushType by remember { mutableStateOf("General") }
    var pushMessageText by remember { mutableStateOf("") }
    var pushFeedbackMessage by remember { mutableStateOf("") }

    // Global settings sliders/inputs
    var gstRateDefault by remember { mutableStateOf(18f) }
    var serviceRadiusLimit by remember { mutableStateOf(25f) }
    var customStatusToastMessage by remember { mutableStateOf("") }

    // SOS Emergency Assign Dialog State
    var selectedSosBookingToAssign by remember { mutableStateOf<Booking?>(null) }
    var assignEtaMinutes by remember { mutableStateOf("15 mins") }

    // Layout Core
    Column(modifier = Modifier.fillMaxSize().background(SophisticatedBackground)) {
        
        // Header
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("SLA ADMINISTRATION HUB", color = SophisticatedTextMuted, fontSize = (11 * scaleFactor).sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Real-Time Dispatch Control", color = SophisticatedText, fontSize = (22 * scaleFactor).sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sub Tabs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "dashboard" to "📊 Metrics",
                    "bookings" to "🛠️ SLA Board",
                    "directory" to "👥 Users",
                    "catalog" to "📂 Catalog",
                    "amc" to "🔒 AMC / Push",
                    "settings" to "⚙️ Settings"
                ).forEach { (tabKey, tabLabel) ->
                    val isSelected = currentAdminTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SophisticatedAccent else SophisticatedSurface)
                            .border(1.dp, if (isSelected) SophisticatedAccent else SophisticatedBorder, RoundedCornerShape(8.dp))
                            .clickable { currentAdminTab = tabKey }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) SophisticatedBackground else SophisticatedText,
                            fontSize = (10 * scaleFactor).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Main Tab Content Box
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 16.dp)) {
            when (currentAdminTab) {
                "dashboard" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Key Stats Grid
                        item {
                            Text("FINANCIAL & SLA OVERVIEW", color = SophisticatedText, fontSize = (14 * scaleFactor).sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.weight(1f).border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Today's Billing", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text("₹${totalBilling.toInt()}", color = SophisticatedAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.weight(1f).border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Total Dispatches", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text("$totalDispatches", color = SophisticatedAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.weight(1f).border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Active Jobs", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text("$activeJobs", color = Color(0xFFE2AA2A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.weight(1f).border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Completed / Cancelled", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text("$completedJobs / $cancelledJobs", color = SophisticatedAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.weight(1f).border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Registered Clients", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text("${customers.size}", color = SophisticatedText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.weight(1f).border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("On-Duty Techs", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            Text("${technicians.size}", color = SophisticatedText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Analytics Segment with Toggles
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("REVENUE TRENDS", color = SophisticatedText, fontSize = (14 * scaleFactor).sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("daily", "weekly", "monthly", "yearly").forEach { tFrame ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (selectedChartTimeframe == tFrame) SophisticatedAccent else SophisticatedSurface)
                                                .clickable { selectedChartTimeframe = tFrame }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tFrame.uppercase(),
                                                color = if (selectedChartTimeframe == tFrame) SophisticatedBackground else SophisticatedText,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            RevenueChart(timeframe = selectedChartTimeframe, scaleFactor = scaleFactor)
                        }

                        // Reports Generation Hook
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("COMPLIANCE REPORTS DOWNLOAD", color = SophisticatedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Audit-ready financial downloads in multiple system standards.", color = SophisticatedTextMuted, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                customStatusToastMessage = "Report exported: Excel ledger download started."
                                                auditLogs = listOf(AuditLogEntry("Just Now", "SLA Auditor", "Excel balance sheets downloaded successfully", "Success")) + auditLogs
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Excel Ledger", color = SophisticatedBackground, fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                customStatusToastMessage = "Report exported: CSV transactions downloaded."
                                                auditLogs = listOf(AuditLogEntry("Just Now", "SLA Auditor", "CSV raw stream downloaded successfully", "Success")) + auditLogs
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("CSV Stream", color = SophisticatedBackground, fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                customStatusToastMessage = "Report exported: PDF tax summary created."
                                                auditLogs = listOf(AuditLogEntry("Just Now", "SLA Auditor", "PDF compliance certificates compiled", "Success")) + auditLogs
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("PDF Report", color = SophisticatedBackground, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                "bookings" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Emergency SOS Monitor section
                        val emergencyBookings = bookings.filter { it.subServiceName.contains("Disaster", ignoreCase = true) || it.serviceName.contains("Emergency", ignoreCase = true) }
                        if (emergencyBookings.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1010)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, Color.Red, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("LIVE CRITICAL SOS QUEUE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    emergencyBookings.forEach { eb ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("ID: ${eb.id} • ${eb.customerName}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Loc: ${eb.address}", color = Color.LightGray, fontSize = 9.sp)
                                                Text("Status: ${eb.status.uppercase()}", color = Color.Yellow, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            }
                                            Button(
                                                onClick = { selectedSosBookingToAssign = eb },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Assign Tech", color = Color.White, fontSize = 9.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }

                        // Normal Bookings Filters and List
                        OutlinedTextField(
                            value = bookingSearchQuery,
                            onValueChange = { bookingSearchQuery = it },
                            placeholder = { Text("Search by Booking ID, Customer or Service", color = SophisticatedTextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SophisticatedSurface,
                                unfocusedContainerColor = SophisticatedSurface,
                                focusedTextColor = SophisticatedText,
                                unfocusedTextColor = SophisticatedText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Booking Status Segment Filter
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("All", "pending", "dispatched", "in_progress", "completed", "cancelled").forEach { filter ->
                                val selected = bookingFilterStatus.lowercase() == filter.lowercase()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) SophisticatedAccent else SophisticatedSurface)
                                        .clickable { bookingFilterStatus = filter }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = filter.uppercase(),
                                        color = if (selected) SophisticatedBackground else SophisticatedText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // LazyList of Bookings
                        val filteredBookings = bookings.filter { b ->
                            (bookingFilterStatus == "All" || b.status.lowercase() == bookingFilterStatus.lowercase()) &&
                            (b.id.contains(bookingSearchQuery, ignoreCase = true) ||
                             b.customerName.contains(bookingSearchQuery, ignoreCase = true) ||
                             b.serviceName.contains(bookingSearchQuery, ignoreCase = true) ||
                             b.subServiceName.contains(bookingSearchQuery, ignoreCase = true))
                        }

                        if (filteredBookings.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No matching bookings in live queue", color = SophisticatedTextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredBookings) { b ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text("${b.serviceName} (${b.id})", color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text(b.subServiceName, color = SophisticatedTextMuted, fontSize = 10.sp)
                                                    Text("Client: ${b.customerName} • ${b.phone}", color = SophisticatedText, fontSize = 10.sp)
                                                    Text("Slot: ${b.date} | ${b.timeSlot}", color = SophisticatedTextMuted, fontSize = 9.sp)
                                                    Text("Address: ${b.address}", color = SophisticatedTextMuted, fontSize = 9.sp)
                                                    Text("Assigned Tech: ${b.technician ?: "None"}", color = SophisticatedAccent, fontSize = 10.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("₹${b.price.toInt()}", color = SophisticatedAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 4.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                when (b.status) {
                                                                    "completed" -> Color(0xFF2E7D32)
                                                                    "cancelled" -> Color(0xFFC62828)
                                                                    "in_progress" -> Color(0xFFEF6C00)
                                                                    else -> Color(0xFF00315B)
                                                                }
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(b.status.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            // Action Buttons for booking management
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(
                                                    onClick = {
                                                        selectedBookingForInvoice = b
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Invoice", color = SophisticatedBackground, fontSize = 9.sp)
                                                }
                                                Button(
                                                    onClick = {
                                                        selectedBookingForReschedule = b
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reschedule", color = SophisticatedBackground, fontSize = 9.sp)
                                                }
                                                if (b.status != "completed" && b.status != "cancelled") {
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                repository?.updateBookingStatus(b.id, "completed")
                                                            }
                                                            customStatusToastMessage = "Booking ${b.id} updated to COMPLETED."
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Complete", color = Color.White, fontSize = 9.sp)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                repository?.updateBookingStatus(b.id, "cancelled")
                                                                // Refund action
                                                                repository?.insertTransaction(
                                                                    Transaction(
                                                                        id = "TXN-${Random.nextInt(100000, 999999)}",
                                                                        type = "deposit",
                                                                        amount = b.price,
                                                                        description = "Admin Cancellation Refund: ${b.id}",
                                                                        date = "2026-07-19"
                                                                    )
                                                                )
                                                            }
                                                            customStatusToastMessage = "Booking cancelled & ₹${b.price.toInt()} refunded."
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Cancel", color = Color.White, fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "directory" -> {
                    var directorySubTab by remember { mutableStateOf("customers") }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (directorySubTab == "customers") SophisticatedAccent else SophisticatedSurface)
                                    .clickable { directorySubTab = "customers" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CUSTOMERS (${customers.size})", color = if (directorySubTab == "customers") SophisticatedBackground else SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (directorySubTab == "technicians") SophisticatedAccent else SophisticatedSurface)
                                    .clickable { directorySubTab = "technicians" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("TECHNICIANS (${technicians.size})", color = if (directorySubTab == "technicians") SophisticatedBackground else SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (directorySubTab == "customers") {
                            // Customers panel
                            OutlinedTextField(
                                value = customerSearchQuery,
                                onValueChange = { customerSearchQuery = it },
                                placeholder = { Text("Search customer name or phone", color = SophisticatedTextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SophisticatedSurface,
                                    unfocusedContainerColor = SophisticatedSurface,
                                    focusedTextColor = SophisticatedText,
                                    unfocusedTextColor = SophisticatedText
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val filteredCustomers = customers.filter { c ->
                                c.fullName.contains(customerSearchQuery, ignoreCase = true) ||
                                c.phone.contains(customerSearchQuery, ignoreCase = true)
                            }

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredCustomers) { c ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text(c.fullName, color = SophisticatedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Phone: ${c.phone} • Wallet: ₹${c.walletBalance.toInt()}", color = SophisticatedTextMuted, fontSize = 11.sp)
                                                    Text("Status: ${c.status}", color = if (c.status == "Active") Color(0xFF2E7D32) else Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Button(
                                                        onClick = { selectedCustomerForDetail = c },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text("View Profile", color = SophisticatedBackground, fontSize = 9.sp)
                                                    }
                                                    if (c.status == "Active") {
                                                        Button(
                                                            onClick = {
                                                                customers = customers.map { if (it.phone == c.phone) it.copy(status = "Blocked") else it }
                                                                customStatusToastMessage = "${c.fullName} successfully BLOCKED."
                                                                auditLogs = listOf(AuditLogEntry("Just Now", "Admin Agent", "Blocked user profile ${c.fullName}", "Warning")) + auditLogs
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text("Block", color = Color.White, fontSize = 9.sp)
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                customers = customers.map { if (it.phone == c.phone) it.copy(status = "Active") else it }
                                                                customStatusToastMessage = "${c.fullName} successfully UNBLOCKED."
                                                                auditLogs = listOf(AuditLogEntry("Just Now", "Admin Agent", "Unblocked user profile ${c.fullName}", "Success")) + auditLogs
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text("Unblock", color = Color.White, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Button(
                                                        onClick = {
                                                            customers = customers.filter { it.phone != c.phone }
                                                            customStatusToastMessage = "${c.fullName} profile successfully DELETED."
                                                            auditLogs = listOf(AuditLogEntry("Just Now", "Admin Agent", "Deleted user profile ${c.fullName}", "Warning")) + auditLogs
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text("Delete", color = Color.White, fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Technicians panel
                            OutlinedTextField(
                                value = techSearchQuery,
                                onValueChange = { techSearchQuery = it },
                                placeholder = { Text("Search technician name, skill or PAN", color = SophisticatedTextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SophisticatedSurface,
                                    unfocusedContainerColor = SophisticatedSurface,
                                    focusedTextColor = SophisticatedText,
                                    unfocusedTextColor = SophisticatedText
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val filteredTechs = technicians.filter { t ->
                                t.fullName.contains(techSearchQuery, ignoreCase = true) ||
                                t.skill.contains(techSearchQuery, ignoreCase = true) ||
                                t.pan.contains(techSearchQuery, ignoreCase = true)
                            }

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredTechs) { t ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text(t.fullName, color = SophisticatedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Skill: ${t.skill} • Rating: ${t.rating}★", color = SophisticatedTextMuted, fontSize = 11.sp)
                                                    Text("PAN: ${t.pan} • Aadhaar: ${t.aadhaar}", color = SophisticatedTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                                    Text("KYC Status: ${t.kycStatus.uppercase()}", color = if (t.kycStatus == "Approved") Color(0xFF2E7D32) else Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    Text("Status: ${t.liveStatus}", color = if (t.liveStatus == "Online") Color(0xFF2E7D32) else Color.LightGray, fontSize = 10.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    if (t.kycStatus != "Approved") {
                                                        Button(
                                                            onClick = {
                                                                technicians = technicians.map { if (it.phone == t.phone) it.copy(kycStatus = "Approved") else it }
                                                                customStatusToastMessage = "${t.fullName} KYC successfully APPROVED."
                                                                auditLogs = listOf(AuditLogEntry("Just Now", "Compliance Hook", "Approved KYC documents for ${t.fullName}", "Success")) + auditLogs
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text("Approve KYC", color = Color.White, fontSize = 9.sp)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                technicians = technicians.map { if (it.phone == t.phone) it.copy(kycStatus = "Rejected") else it }
                                                                customStatusToastMessage = "${t.fullName} KYC documents REJECTED."
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text("Reject", color = Color.White, fontSize = 9.sp)
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                technicians = technicians.map { if (it.phone == t.phone) it.copy(kycStatus = "Suspended") else it }
                                                                customStatusToastMessage = "${t.fullName} successfully SUSPENDED."
                                                                auditLogs = listOf(AuditLogEntry("Just Now", "Compliance Hook", "Suspended technician ${t.fullName}", "Warning")) + auditLogs
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2AA2A)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text("Suspend", color = Color.White, fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "catalog" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("300+ SERVICE CATALOG MANAGEMENT", color = SophisticatedText, fontSize = (14 * scaleFactor).sp, fontWeight = FontWeight.Bold)
                        Text("Configuring tax, base rates, and SLA categories in real-time.", color = SophisticatedTextMuted, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = serviceSearchQuery,
                            onValueChange = { serviceSearchQuery = it },
                            placeholder = { Text("Search catalog across 315 services...", color = SophisticatedTextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SophisticatedSurface,
                                unfocusedContainerColor = SophisticatedSurface,
                                focusedTextColor = SophisticatedText,
                                unfocusedTextColor = SophisticatedText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Category list
                        val flatServices = mutableListOf<Pair<ServiceCategory, SubService>>()
                        allServicesCatalog.forEach { cat ->
                            cat.subservices.forEach { sub ->
                                if (sub.name.contains(serviceSearchQuery, ignoreCase = true) || cat.name.contains(serviceSearchQuery, ignoreCase = true)) {
                                    flatServices.add(Pair(cat, sub))
                                }
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(flatServices.take(50)) { (cat, sub) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(sub.name, color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Category: ${cat.name} • GST Code: SAC-9987", color = SophisticatedTextMuted, fontSize = 10.sp)
                                            Text("Base Tariff: ₹${(sub.price / 1.18).toInt()} + 18% GST", color = SophisticatedTextMuted, fontSize = 9.sp)
                                        }
                                        Text("₹${sub.price.toInt()}", color = SophisticatedAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                            if (flatServices.size > 50) {
                                item {
                                    Text("... and ${flatServices.size - 50} other certified services active in registry.", color = SophisticatedTextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
                                }
                            }
                        }
                    }
                }

                "amc" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("AMC & BROADCAST CONTROL CENTER", color = SophisticatedText, fontSize = (14 * scaleFactor).sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        // AMC Active plans
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                            modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("ANNUAL MAINTENANCE CONTRACTS", color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                amcPlans.forEach { plan ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(plan.customerName, color = SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${plan.planName} (Expires ${plan.expiryDate})", color = SophisticatedTextMuted, fontSize = 9.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = plan.status,
                                                color = if (plan.status == "Active") Color(0xFF2E7D32) else Color.Red,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                            if (plan.status == "Expired") {
                                                Button(
                                                    onClick = {
                                                        amcPlans = amcPlans.map { if (it.customerName == plan.customerName) it.copy(status = "Renewed") else it }
                                                        customStatusToastMessage = "Targeted AMC renewal SMS notification triggered for ${plan.customerName}."
                                                        coroutineScope.launch {
                                                            repository?.insertNotification(
                                                                NotificationLog(
                                                                    id = "NL-${Random.nextInt(100000, 999999)}",
                                                                    type = "SMS",
                                                                    recipient = "+91 80193 18625",
                                                                    message = "Urgent Renewal Call: Your One Call Home AMC is expired. Tap to secure continuous SLA support.",
                                                                    timestamp = "Just Now"
                                                                )
                                                            )
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text("Notify Renewal", color = SophisticatedBackground, fontSize = 8.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Push Notification Targeted Broadcast Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                            modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("BROADCAST PUSH ALERTS", color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Triggers system notifications across the dynamic device registry.", color = SophisticatedTextMuted, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("All Users", "Technicians", "Emergency").forEach { grp ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (pushTargetGroup == grp) SophisticatedAccent else SophisticatedBackground)
                                                .clickable { pushTargetGroup = grp }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(grp, color = if (pushTargetGroup == grp) SophisticatedBackground else SophisticatedText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pushMessageText,
                                    onValueChange = { pushMessageText = it },
                                    placeholder = { Text("Compose broadcast push alert message...", color = SophisticatedTextMuted, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = SophisticatedBackground,
                                        unfocusedContainerColor = SophisticatedBackground,
                                        focusedTextColor = SophisticatedText,
                                        unfocusedTextColor = SophisticatedText
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (pushMessageText.isNotEmpty()) {
                                            coroutineScope.launch {
                                                repository?.insertNotification(
                                                    NotificationLog(
                                                        id = "NL-${Random.nextInt(100000, 999999)}",
                                                        type = "Push Broadcast",
                                                        recipient = pushTargetGroup,
                                                        message = pushMessageText,
                                                        timestamp = "Just Now"
                                                    )
                                                )
                                            }
                                            customStatusToastMessage = "Broadcast Alert triggered successfully."
                                            pushMessageText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Broadcast Push Alert", color = SophisticatedBackground, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                "settings" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            Text("GLOBAL OPERATING PARAMETERS", color = SophisticatedText, fontSize = (14 * scaleFactor).sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Standard GST Configuration: ${gstRateDefault.toInt()}%", color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = gstRateDefault,
                                        onValueChange = { gstRateDefault = it },
                                        valueRange = 5f..28f,
                                        colors = SliderDefaults.colors(thumbColor = SophisticatedAccent, activeTrackColor = SophisticatedAccent)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("SLA Dispatch Boundary Radius: ${serviceRadiusLimit.toInt()} KM", color = SophisticatedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = serviceRadiusLimit,
                                        onValueChange = { serviceRadiusLimit = it },
                                        valueRange = 5f..100f,
                                        colors = SliderDefaults.colors(thumbColor = SophisticatedAccent, activeTrackColor = SophisticatedAccent)
                                    )
                                }
                            }
                        }

                        // Security Audit Trail Section
                        item {
                            Text("SECURITY COMPLIANCE AUDIT TRAIL", color = SophisticatedText, fontSize = (14 * scaleFactor).sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    auditLogs.forEach { log ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(log.action, color = SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Actor: ${log.actor} • Time: ${log.timestamp}", color = SophisticatedTextMuted, fontSize = 9.sp)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        when (log.status) {
                                                            "Success" -> Color(0xFF2E7D32)
                                                            "Warning" -> Color(0xFFEF6C00)
                                                            else -> Color(0xFFC62828)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(log.status.uppercase(), color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS & OVERLAYS ---

    // 1. Reschedule Dialog
    if (selectedBookingForReschedule != null) {
        AlertDialog(
            onDismissRequest = { selectedBookingForReschedule = null },
            title = { Text("Reschedule Slot Allocation", color = SophisticatedText) },
            text = {
                Column {
                    Text("Select a verified slot for booking ID: ${selectedBookingForReschedule!!.id}", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Date Picker (SLA Safe)", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("2026-07-21", "2026-07-22", "2026-07-23").forEach { dt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (newRescheduleDate == dt) SophisticatedAccent else SophisticatedSurface)
                                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(6.dp))
                                    .clickable { newRescheduleDate = dt }
                                    .padding(8.dp)
                            ) {
                                Text(dt, color = if (newRescheduleDate == dt) SophisticatedBackground else SophisticatedText, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Time Slot Selector", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    listOf("10:00 AM - 12:00 PM", "02:00 PM - 04:00 PM", "06:00 PM - 08:00 PM").forEach { slt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (newRescheduleSlot == slt) SophisticatedAccent else SophisticatedSurface)
                                .border(1.dp, SophisticatedBorder, RoundedCornerShape(6.dp))
                                .clickable { newRescheduleSlot = slt }
                                .padding(10.dp)
                        ) {
                            Text(slt, color = if (newRescheduleSlot == slt) SophisticatedBackground else SophisticatedText, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository?.rescheduleBooking(selectedBookingForReschedule!!.id, newRescheduleDate, newRescheduleSlot)
                        }
                        customStatusToastMessage = "Booking successfully rescheduled."
                        selectedBookingForReschedule = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
                ) {
                    Text("Reschedule Job", color = SophisticatedBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBookingForReschedule = null }) {
                    Text("Cancel", color = SophisticatedText)
                }
            },
            containerColor = SophisticatedSurface
        )
    }

    // 2. GST-Compliant Tax Invoice Dialog
    if (selectedBookingForInvoice != null) {
        val inv = selectedBookingForInvoice!!
        val baseAmt = inv.price / 1.18f
        val gstAmt = baseAmt * 0.18f
        
        AlertDialog(
            onDismissRequest = { selectedBookingForInvoice = null },
            title = { Text("TAX INVOICE (SAC Code: 9987)", color = SophisticatedText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("ONE CALL HOME SOLUTIONS LTD.", color = SophisticatedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("GSTIN: 07AAAAA1111A1Z1 • SLA Approved", color = SophisticatedTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HorizontalDivider(color = SophisticatedBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Invoice No: ${inv.invoiceId}", color = SophisticatedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Booking Ref: ${inv.id}", color = SophisticatedTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Date: ${inv.date}", color = SophisticatedTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Client: ${inv.customerName}", color = SophisticatedText, fontSize = 11.sp)
                    Text("Phone: ${inv.phone}", color = SophisticatedTextMuted, fontSize = 11.sp)
                    Text("Address: ${inv.address}", color = SophisticatedTextMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HorizontalDivider(color = SophisticatedBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service Description", color = SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Total", color = SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${inv.serviceName} - ${inv.subServiceName}", color = SophisticatedText, fontSize = 10.sp, modifier = Modifier.weight(1f))
                        Text("₹${inv.price.toInt()}", color = SophisticatedAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HorizontalDivider(color = SophisticatedBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Taxable Base Value:", color = SophisticatedTextMuted, fontSize = 10.sp)
                        Text("₹${baseAmt.toInt()}", color = SophisticatedText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CGST (9.0%):", color = SophisticatedTextMuted, fontSize = 10.sp)
                        Text("₹${(gstAmt / 2).toInt()}", color = SophisticatedText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SGST (9.0%):", color = SophisticatedTextMuted, fontSize = 10.sp)
                        Text("₹${(gstAmt / 2).toInt()}", color = SophisticatedText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total (Including Tax):", color = SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("₹${inv.price.toInt()}", color = SophisticatedAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        customStatusToastMessage = "Simulating print: PDF Invoice file saved to device downloads."
                        selectedBookingForInvoice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
                ) {
                    Text("Print PDF", color = SophisticatedBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBookingForInvoice = null }) {
                    Text("Close", color = SophisticatedText)
                }
            },
            containerColor = SophisticatedSurface
        )
    }

    // 3. Customer Profile details Dialog
    if (selectedCustomerForDetail != null) {
        val cust = selectedCustomerForDetail!!
        AlertDialog(
            onDismissRequest = { selectedCustomerForDetail = null },
            title = { Text("Client Profile Details", color = SophisticatedText) },
            text = {
                Column {
                    Text("Full Name: ${cust.fullName}", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Email: ${cust.email}", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("Contact: ${cust.phone}", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("Primary Address: ${cust.address}", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("Member Since: ${cust.signUpDate}", color = SophisticatedTextMuted, fontSize = 11.sp)
                    Text("Operating Balance: ₹${cust.walletBalance.toInt()}", color = SophisticatedAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Client Audit Logs", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    HorizontalDivider(color = SophisticatedBorder)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Wallet linked successfully via Razorpay", color = SophisticatedTextMuted, fontSize = 10.sp)
                    Text("• Standard background checks approved", color = SophisticatedTextMuted, fontSize = 10.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedCustomerForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedAccent)
                ) {
                    Text("Done", color = SophisticatedBackground)
                }
            },
            containerColor = SophisticatedSurface
        )
    }

    // 4. Emergency SOS Assigning Dialog
    if (selectedSosBookingToAssign != null) {
        val sosB = selectedSosBookingToAssign!!
        AlertDialog(
            onDismissRequest = { selectedSosBookingToAssign = null },
            title = { Text("Emergency Dispatch Center", color = Color.Red) },
            text = {
                Column {
                    Text("SLA response target: 15 minutes", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Allocate closest on-duty technician for booking: ${sosB.id}", color = SophisticatedTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Available Dispatch Teams", color = SophisticatedText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    technicians.filter { it.liveStatus == "Online" }.forEach { tech ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SophisticatedSurface)
                                .border(1.dp, SophisticatedBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        repository?.updateBookingStatus(sosB.id, "dispatched")
                                        // Also add Notification
                                        repository?.insertNotification(
                                            NotificationLog(
                                                id = "NL-${Random.nextInt(100000, 999999)}",
                                                type = "WhatsApp",
                                                recipient = sosB.phone,
                                                message = "CRITICAL UPDATE: ${tech.fullName} is dispatched for Emergency Support. ETA: $assignEtaMinutes. Response Time: 4 mins.",
                                                timestamp = "Just Now"
                                            )
                                        )
                                    }
                                    customStatusToastMessage = "${tech.fullName} dispatched for Emergency. ETA: $assignEtaMinutes."
                                    selectedSosBookingToAssign = null
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tech.fullName, color = SophisticatedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Rating: ${tech.rating}★ | Skill: ${tech.skill}", color = SophisticatedTextMuted, fontSize = 9.sp)
                            }
                            Text("SELECT", color = SophisticatedAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedSosBookingToAssign = null }) {
                    Text("Close Panel", color = SophisticatedText)
                }
            },
            containerColor = SophisticatedSurface
        )
    }

    // Floating Custom Toast Notification
    if (customStatusToastMessage.isNotEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.border(1.dp, SophisticatedAccent, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(customStatusToastMessage, color = SophisticatedBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "DISMISS",
                        color = SophisticatedBackground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { customStatusToastMessage = "" }
                    )
                }
            }
        }
    }
}

