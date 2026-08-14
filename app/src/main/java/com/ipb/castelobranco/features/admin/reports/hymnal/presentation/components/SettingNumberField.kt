package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField

/**
 * One collection parameter, with its accepted range in view and its effect spelled out.
 *
 * The explanation matters as much as the number: some of these change only what is collected from
 * now on, and one of them changes how the history already stored is read.
 */
@Composable
fun SettingNumberField(
    field: SettingField,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(field.label) },
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = { Text(error ?: field.rangeText) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = field.explanation,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingNumberFieldPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        SettingNumberField(
            field = SettingField.WINDOW_GRACE_MINUTES,
            value = "30",
            error = null,
            onValueChange = {},
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingNumberField(
            field = SettingField.MIN_SECONDS_TO_COUNT,
            value = "0",
            error = "Informe um número entre 1 e 3600.",
            onValueChange = {},
        )
    }
}
