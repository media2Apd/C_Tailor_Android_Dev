@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.paywall

import androidx.compose.runtime.*
import com.cuso.tailor.viewmodel.PermissionViewModel

enum class PermissionAction {
    VIEW, CREATE, EDIT, DELETE, EXPORT, PRINT, SHARE, APPROVE
}

/**
 * Jetpack Compose equivalent of React FeatureGuard.
 * Wraps page content and restricts unauthorized users.
 */
@Composable
fun FeatureGuard(
    module: String,
    feature: String,
    action: PermissionAction = PermissionAction.VIEW,
    permissionViewModel: PermissionViewModel,
    onGoBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val myPermissions by permissionViewModel.myPermissions.collectAsState()
    val isPlanSubscribed = permissionViewModel.isPlanFeatureSubscribed(module, feature)

    val hasActionPermission = remember(myPermissions, module, feature, action) {
        val perm = myPermissions ?: return@remember true
        if (perm.isOwner) return@remember true
        when (action) {
            PermissionAction.VIEW -> perm.canView(module, feature)
            PermissionAction.CREATE -> perm.canCreate(module, feature)
            PermissionAction.EDIT -> perm.canEdit(module, feature)
            PermissionAction.DELETE -> perm.canDelete(module, feature)
            PermissionAction.EXPORT -> perm.canExport(module, feature)
            PermissionAction.PRINT -> perm.canPrint(module, feature)
            PermissionAction.SHARE -> perm.canShare(module, feature)
            PermissionAction.APPROVE -> perm.canApprove(module, feature)
        }
    }

    if (!isPlanSubscribed || !hasActionPermission) {
        AccessRestrictedDialog(
            requiredPermission = "$module.$feature ($action)",
            message = "You don't have permission to perform this action. Please contact your administrator.",
            onGoBack = onGoBack,
            onDismiss = onGoBack,
            onRequestAccess = { /* Send request API call if needed */ }
        )
    } else {
        content()
    }
}