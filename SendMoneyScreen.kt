package ch.juliusbaer.daybreak.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.juliusbaer.daybreak.data.MockData
import ch.juliusbaer.daybreak.data.Recipient
import ch.juliusbaer.daybreak.ui.components.Avatar
import ch.juliusbaer.daybreak.ui.components.Caption
import ch.juliusbaer.daybreak.ui.components.JbCard
import ch.juliusbaer.daybreak.ui.components.PrimaryButton
import ch.juliusbaer.daybreak.ui.components.SectionLabel
import ch.juliusbaer.daybreak.ui.theme.Hairline
import ch.juliusbaer.daybreak.ui.theme.Ink
import ch.juliusbaer.daybreak.ui.theme.InkSecondary
import ch.juliusbaer.daybreak.ui.theme.Up
import ch.juliusbaer.daybreak.ui.theme.UpSoft
import ch.juliusbaer.daybreak.ui.theme.WealthLarge

private enum class SendStep { Recipient, Amount, Review, Done }

@Composable
fun SendMoneyScreen(onBack: () -> Unit) {
    var step by remember { mutableStateOf(SendStep.Recipient) }
    var recipient by remember { mutableStateOf<Recipient?>(null) }
    var amount by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        when (step) {
            SendStep.Recipient -> RecipientStep(onPick = { recipient = it; step = SendStep.Amount })
            SendStep.Amount -> AmountStep(
                recipient = recipient!!,
                amount = amount,
                onKey = { amount = applyKey(amount, it) },
                onContinue = { if (amount.isNotEmpty()) step = SendStep.Review }
            )
            SendStep.Review -> ReviewStep(
                recipient = recipient!!,
                amount = amount,
                onConfirm = { step = SendStep.Done }
            )
            SendStep.Done -> DoneStep(recipient = recipient!!, amount = amount, onDone = onBack)
        }
    }
}

private fun applyKey(current: String, key: String): String = when (key) {
    "<" -> current.dropLast(1)
    "." -> if (current.contains(".") || current.isEmpty()) current else "$current."
    else -> {
        val next = if (current == "0") key else current + key
        if (next.length <= 10) next else current
    }
}

@Composable
private fun RecipientStep(onPick: (Recipient) -> Unit) {
    Column {
        SectionLabel("Send to")
        Spacer(Modifier.height(12.dp))
        JbCard {
            Column {
                MockData.recipients.forEachIndexed { i, r ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(r) }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(r.initials, size = 40.dp)
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Caption(r.detail, modifier = Modifier.padding(top = 1.dp))
                        }
                    }
                    if (i != MockData.recipients.lastIndex) HairlineDivider()
                }
            }
        }
    }
}

@Composable
private fun AmountStep(recipient: Recipient, amount: String, onKey: (String) -> Unit, onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Caption("To ${recipient.name}")
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            Text("CHF ${amount.ifEmpty { "0" }}", style = WealthLarge, fontSize = 36.sp)
        }
        Spacer(Modifier.weight(1f))
        Keypad(onKey = onKey)
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Continue", onClick = onContinue, enabled = amount.isNotEmpty())
    }
}

@Composable
private fun Keypad(onKey: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "<")
    )
    Column {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onKey(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "<") {
                            Icon(
                                Icons.Outlined.Backspace,
                                contentDescription = "Delete",
                                tint = InkSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(key, color = Ink, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(recipient: Recipient, amount: String, onConfirm: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SectionLabel("Review")
        Spacer(Modifier.height(12.dp))
        JbCard {
            Column {
                ReviewRow("To", recipient.name); HairlineDivider()
                ReviewRow("Account", recipient.detail); HairlineDivider()
                ReviewRow("Amount", "CHF $amount"); HairlineDivider()
                ReviewRow("Fee", "Free"); HairlineDivider()
                ReviewRow("Arrives", "Instantly")
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Confirm transfer", onClick = onConfirm)
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = InkSecondary, fontSize = 14.sp)
        Text(value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HairlineDivider() {
    Box(Modifier.fillMaxWidth().height(0.7.dp).background(Hairline))
}

@Composable
private fun DoneStep(recipient: Recipient, amount: String, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(72.dp).clip(CircleShape).background(UpSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = Up, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("CHF $amount sent", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Caption("to ${recipient.name}", modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.weight(1f))
        PrimaryButton("Done", onClick = onDone)
    }
}
