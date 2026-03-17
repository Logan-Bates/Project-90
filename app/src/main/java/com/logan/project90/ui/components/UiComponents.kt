package com.logan.project90.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val ScreenPadding = 24.dp
private val ScreenSpacing = 20.dp
private val SectionSpacing = 12.dp
private val CardPadding = 16.dp

enum class MessageTone {
    ERROR,
    WARNING,
    INFO
}

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollModifier = if (scrollable) {
        Modifier.verticalScroll(rememberScrollState())
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(scrollModifier)
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ScreenSpacing),
        content = content
    )
}

@Composable
fun ScreenIntro(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    if (onClick == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = colors
        ) {
            Column(
                modifier = Modifier.padding(CardPadding),
                verticalArrangement = Arrangement.spacedBy(SectionSpacing),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            colors = colors
        ) {
            Column(
                modifier = Modifier.padding(CardPadding),
                verticalArrangement = Arrangement.spacedBy(SectionSpacing),
                content = content
            )
        }
    }
}

@Composable
fun ScreenSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun InlineMessage(
    text: String,
    tone: MessageTone,
    modifier: Modifier = Modifier
) {
    val color = when (tone) {
        MessageTone.ERROR -> MaterialTheme.colorScheme.error
        MessageTone.WARNING -> MaterialTheme.colorScheme.tertiary
        MessageTone.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun SupportText(
    text: String,
    modifier: Modifier = Modifier,
    tone: MessageTone = MessageTone.INFO
) {
    InlineMessage(text = text, tone = tone, modifier = modifier)
}

@Composable
fun LabeledValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
