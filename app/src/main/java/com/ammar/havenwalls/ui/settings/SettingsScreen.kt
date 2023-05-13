package com.ammar.havenwalls.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ammar.havenwalls.R
import com.ammar.havenwalls.data.preferences.AppPreferences
import com.ammar.havenwalls.data.preferences.ObjectDetectionPreferences
import com.ammar.havenwalls.extensions.produceState
import com.ammar.havenwalls.model.ObjectDetectionModel
import com.ammar.havenwalls.ui.common.TopBar
import com.ammar.havenwalls.ui.common.bottombar.LocalBottomBarController
import com.ammar.havenwalls.ui.common.mainsearch.LocalMainSearchBarController
import com.ammar.havenwalls.ui.common.mainsearch.MainSearchBarState
import com.ammar.havenwalls.ui.destinations.WallhavenApiKeyDialogDestination
import com.ammar.havenwalls.ui.theme.HavenWallsTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState = lifecycle.produceState(
        viewModel = viewModel,
        initialValue = SettingsUiState(),
    )
    val searchBarController = LocalMainSearchBarController.current
    val bottomBarController = LocalBottomBarController.current

    LaunchedEffect(Unit) {
        searchBarController.update { MainSearchBarState(visible = false) }
        bottomBarController.update { it.copy(visible = false) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(
            navController = navController,
            title = {
                Text(
                    text = stringResource(R.string.settings),
                    maxLines = 1,
                )
            }
        )
        SettingsScreenContent(
            appPreferences = uiState.appPreferences,
            model = uiState.selectedModel,
            onBlurSketchyCheckChange = viewModel::setBlurSketchy,
            onBlurNsfwCheckChange = viewModel::setBlurNsfw,
            onWallhavenApiKeyItemClick = { navController.navigate(WallhavenApiKeyDialogDestination) },
            onObjectDetectionPrefsChange = viewModel::updateSubjectDetectionPrefs,
            onObjectDetectionDelegateClick = { viewModel.showObjectDetectionDelegateOptions(true) },
            onObjectDetectionModelClick = { viewModel.showObjectDetectionModelOptions(true) }
        )
    }

    if (uiState.showObjectDetectionModelOptions) {
        ObjectDetectionModelOptionsDialog(
            models = uiState.objectDetectionModels,
            selectedModelId = uiState.appPreferences.objectDetectionPreferences.modelId,
            onOptionEditClick = {
                viewModel.showEditModelDialog(model = it, show = true)
            },
            onAddClick = { viewModel.showEditModelDialog() },
            onSaveClick = viewModel::setSelectedModel,
            onDismissRequest = { viewModel.showObjectDetectionModelOptions(false) },
        )
    }

    if (uiState.showEditModelDialog) {
        ObjectDetectionModelEditDialog(
            model = uiState.editModel,
            downloadStatus = uiState.modelDownloadStatus,
            checkNameExists = viewModel::checkModelNameExists,
            onSaveClick = viewModel::saveModel,
            onDeleteClick = {
                uiState.editModel?.run { viewModel.deleteModel(this) }
                viewModel.showEditModelDialog(model = null, show = false)
            },
            onDismissRequest = {
                viewModel.showEditModelDialog(
                    model = null,
                    show = false,
                )
            }
        )
    }

    if (uiState.deleteModel != null) {
        ObjectDetectionModelDeleteConfirmDialog(
            model = uiState.deleteModel,
            onConfirmClick = { viewModel.deleteModel(uiState.deleteModel, true) },
            onDismissRequest = { viewModel.deleteModel(null) },
        )
    }

    if (uiState.showObjectDetectionDelegateOptions) {
        ObjectDetectionDelegateOptionsDialog(
            selectedDelegate = uiState.appPreferences.objectDetectionPreferences.delegate,
            onSaveClick = {
                viewModel.updateSubjectDetectionPrefs(
                    uiState.appPreferences.objectDetectionPreferences.copy(delegate = it)
                )
                viewModel.showObjectDetectionDelegateOptions(false)
            },
            onDismissRequest = { viewModel.showObjectDetectionDelegateOptions(false) }
        )
    }
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences = AppPreferences(),
    model: ObjectDetectionModel = ObjectDetectionModel.DEFAULT,
    onBlurSketchyCheckChange: (checked: Boolean) -> Unit = {},
    onBlurNsfwCheckChange: (checked: Boolean) -> Unit = {},
    onWallhavenApiKeyItemClick: () -> Unit = {},
    onObjectDetectionPrefsChange: (objectDetectionPrefs: ObjectDetectionPreferences) -> Unit = {},
    onObjectDetectionDelegateClick: () -> Unit = {},
    onObjectDetectionModelClick: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        accountSection(onWallhavenApiKeyItemClick = onWallhavenApiKeyItemClick)
        dividerItem()
        generalSection(
            blurSketchy = appPreferences.blurSketchy,
            blurNsfw = appPreferences.blurNsfw,
            onBlurSketchyCheckChange = onBlurSketchyCheckChange,
            onBlurNsfwCheckChange = onBlurNsfwCheckChange,
        )
        dividerItem()
        objectDetectionSection(
            enabled = appPreferences.objectDetectionPreferences.enabled,
            delegate = appPreferences.objectDetectionPreferences.delegate,
            model = model,
            onEnabledChange = {
                onObjectDetectionPrefsChange(
                    appPreferences.objectDetectionPreferences.copy(enabled = it)
                )
            },
            onDelegateClick = onObjectDetectionDelegateClick,
            onModelClick = onObjectDetectionModelClick,
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSettingsScreenContent() {
    HavenWallsTheme {
        Surface {
            SettingsScreenContent()
        }
    }
}
