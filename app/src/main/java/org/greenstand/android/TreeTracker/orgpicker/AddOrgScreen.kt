package org.greenstand.android.TreeTracker.orgpicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.greenstand.android.TreeTracker.R
import org.greenstand.android.TreeTracker.models.NavRoute
import org.greenstand.android.TreeTracker.root.LocalNavHostController
import org.greenstand.android.TreeTracker.view.ActionBar
import org.greenstand.android.TreeTracker.view.ArrowButton
import org.greenstand.android.TreeTracker.view.BorderedTextField

@Composable
fun AddOrgScreen(
    userId: Long,
    destinationWallet: String,
    viewModel: AddOrgViewModel = viewModel(factory = AddOrgViewModelFactory(userId, destinationWallet))
) {
    val navController = LocalNavHostController.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.observeAsState(AddOrgState())

    Scaffold(
        bottomBar = {
            ActionBar(
                leftAction = {
                    ArrowButton(isLeft = true) {
                        navController.popBackStack()
                    }
                },
                rightAction = {
                    ArrowButton(
                        isLeft = false,
                    ) {
                        scope.launch {
                            viewModel.startSession()
                            navController.navigate(NavRoute.TreeCapture.create(state.userImagePath))
                        }
                    }
                }
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp)
        ) {
            BorderedTextField(
                value = state.orgName,
                padding = PaddingValues(4.dp),
                onValueChange = { updatedName -> viewModel.updateOrgName(updatedName) },
                placeholder = { Text(text = stringResource(id = R.string.organization), color = Color.White) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Go,
                    autoCorrect = false,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        scope.launch {
                            viewModel.startSession()
                            navController.navigate(NavRoute.TreeCapture.create(state.userImagePath))
                        }
                    }
                )
            )
        }
    }
}