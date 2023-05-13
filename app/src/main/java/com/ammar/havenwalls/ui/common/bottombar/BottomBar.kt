package com.ammar.havenwalls.ui.common.bottombar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ammar.havenwalls.ui.destinations.TypedDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    currentDestination: TypedDestination<*>? = null,
    onItemClick: (destination: DirectionDestinationSpec) -> Unit = {},
) {
    val bottomBarController = LocalBottomBarController.current
    val state by bottomBarController.state

    AnimatedVisibility(
        modifier = modifier,
        visible = state.visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        content = {
            NavigationBar {
                BottomBarDestination.values().forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination.direction,
                        onClick = { onItemClick(destination.direction) },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = stringResource(destination.label)
                            )
                        },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            }
        }
    )
}
