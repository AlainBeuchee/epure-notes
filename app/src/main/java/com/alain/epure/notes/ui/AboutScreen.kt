package com.alain.epure.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.alain.epure.notes.BuildConfig
import com.alain.epure.notes.R
import com.alain.epure.notes.ui.theme.LocalNoteColors

private const val REPOSITORY_URL = "https://github.com/AlainBeuchee/epure-notes"

// « À propos » (SPEC F-15) : version, licence, confidentialité, avertissement de perte (F-18.2).
@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalNoteColors.current.background)
            .systemBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.titleLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalNoteColors.current.textPrimary
            )
            Text(
                text = stringResource(R.string.about_license),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalNoteColors.current.textSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = REPOSITORY_URL,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalNoteColors.current.textPrimary,
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .clickable { uriHandler.openUri(REPOSITORY_URL) }
                    .padding(vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.about_privacy_title),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalNoteColors.current.textPrimary,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = stringResource(R.string.about_privacy),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalNoteColors.current.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(R.string.about_open_with),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalNoteColors.current.textSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.backup_warning),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = LocalNoteColors.current.textSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.about_restore),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = LocalNoteColors.current.textSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        HorizontalDivider(color = LocalNoteColors.current.textSecondary, thickness = 1.dp)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = LocalNoteColors.current.textPrimary)
            ) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    }
}
