package com.example.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.infrastructure.PinSecurityManager
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.WarmGold

@Composable
fun PinLockScreen(
    biometricEnabled: Boolean,
    onVerifyPin: (String) -> Boolean,
    onBiometricUnlocked: () -> Unit
) {
    val strings = LocalAppStrings.current
    val numerals = LocalNumeralStyle.current
    val context = LocalContext.current
    var entered by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val biometricReady = remember(context, biometricEnabled) {
        biometricEnabled && PinSecurityManager.isBiometricAvailable(context)
    }

    val primaryGlow = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle ambient header glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = primaryGlow.copy(alpha = 0.08f),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.5f, size.height * 0.06f)
                )
                drawCircle(
                    color = WarmGold.copy(alpha = 0.06f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.92f, size.height * 0.22f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.jomakhoroch_ic),
                            contentDescription = strings.appName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .testTag("pin_lock_brand_logo")
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = strings.securityPin,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = strings.enterPin,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(28.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { idx ->
                            val filled = idx < entered.length
                            val dotSize by animateDpAsState(
                                targetValue = if (filled) 20.dp else 15.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "pin_dot_size"
                            )
                            val dotColor by animateColorAsState(
                                targetValue = when {
                                    errorMsg != null -> MaterialTheme.colorScheme.error
                                    filled -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                label = "pin_dot_color"
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (errorMsg != null) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.primary.copy(alpha = if (filled) 1f else 0.45f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = errorMsg ?: " ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BIO", "0", "DEL")
                    )
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                when (key) {
                                    "BIO" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .clickable(enabled = biometricReady) {
                                                    PinSecurityManager.authenticateBiometric(
                                                        context = context,
                                                        title = strings.appName,
                                                        subtitle = strings.unlockWithBiometrics,
                                                        negativeLabel = strings.cancel,
                                                        onSuccess = onBiometricUnlocked,
                                                        onError = { errorMsg = it }
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (biometricReady) {
                                                Icon(
                                                    imageVector = Icons.Filled.Fingerprint,
                                                    contentDescription = strings.unlockWithBiometrics,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(34.dp)
                                                )
                                            }
                                        }
                                    }
                                    "DEL" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (entered.isNotEmpty()) {
                                                        entered = entered.dropLast(1)
                                                        errorMsg = null
                                                    }
                                                }
                                                .testTag("pin_backspace"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    else -> {
                                        Surface(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (entered.length < 4) {
                                                        val next = entered + key
                                                        entered = next
                                                        errorMsg = null
                                                        if (next.length == 4) {
                                                            if (!onVerifyPin(next)) {
                                                                errorMsg = strings.wrongPin
                                                                entered = ""
                                                            }
                                                        }
                                                    }
                                                }
                                                .testTag("pin_key_$key"),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface,
                                            tonalElevation = 2.dp,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = CurrencyFormatter.toLocalizedDigits(key, numerals),
                                                    style = MaterialTheme.typography.headlineSmall.copy(
                                                        fontFamily = PlusJakartaSansFont,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (biometricReady) {
                        TextButton(onClick = {
                            PinSecurityManager.authenticateBiometric(
                                context = context,
                                title = strings.appName,
                                subtitle = strings.unlockWithBiometrics,
                                negativeLabel = strings.cancel,
                                onSuccess = onBiometricUnlocked,
                                onError = { errorMsg = it }
                            )
                        }) {
                            Text(strings.unlockWithBiometrics)
                        }
                    }
                }
            }
        }
    }
}
