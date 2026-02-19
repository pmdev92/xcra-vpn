import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.enums.AboutItem

@Composable
fun AboutScreen(
    path: String,
    onClick: (AboutItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {

        // Backup
        PreferenceItem(
            icon = Icons.Default.FilePresent,
            title = stringResource(R.string.title_configuration_backup),
            summary = stringResource(R.string.summary_configuration_backup, path),
            onClick = {
                onClick(AboutItem.Backup)
            }
        )

        // Share
        PreferenceItem(
            icon = Icons.Default.Share,
            title = stringResource(R.string.title_configuration_share),
            onClick = {
                onClick(AboutItem.Share)
            }
        )

        // Restore
        PreferenceItem(
            icon = Icons.Default.SettingsBackupRestore,
            title = stringResource(R.string.title_configuration_restore),
            onClick = {
                onClick(AboutItem.Restore)
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp))

        // Source Code
        PreferenceItem(
            icon = Icons.Default.Code,
            title = stringResource(R.string.title_source_code),
            onClick = {
                onClick(AboutItem.Source)
            }
        )

        // OSS Licenses
        PreferenceItem(
            icon = Icons.AutoMirrored.Filled.Article,
            title = stringResource(R.string.title_oss_license),
            onClick = {
                onClick(AboutItem.License)
            }
        )

        // Feedback
        PreferenceItem(
            icon = Icons.Default.Feedback,
            title = stringResource(R.string.title_pref_feedback),
            onClick = {
                onClick(AboutItem.Feedback)
            }
        )

        // Telegram Channel
        PreferenceItem(
            icon = Icons.AutoMirrored.Filled.Chat,
            title = stringResource(R.string.title_tg_channel),
            onClick = {
                onClick(AboutItem.Telegram)
            }
        )

        // Privacy Policy
        PreferenceItem(
            icon = Icons.Default.PrivacyTip,
            title = stringResource(R.string.title_privacy_policy),
            onClick = {
                onClick(AboutItem.PrivacyPolicy)
            }
        )

        Spacer(modifier = Modifier.weight(1F))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Version: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
            Text(
                "App ID: ${BuildConfig.APPLICATION_ID}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    summary: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!summary.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(summary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}