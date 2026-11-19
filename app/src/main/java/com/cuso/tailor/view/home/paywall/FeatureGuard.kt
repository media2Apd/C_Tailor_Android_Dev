@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.paywall

import androidx.compose.runtime.*
import com.cuso.tailor.model.permission.PaywallModuleConfig
import com.cuso.tailor.viewmodel.PermissionViewModel

enum class PermissionAction {
    VIEW, CREATE, EDIT, DELETE, EXPORT, PRINT, SHARE, APPROVE
}

/**
 * Holds action-level permissions for the currently active screen.
 */
data class ScreenActionPermissions(
    val module: String = "",
    val feature: String = "",
    val canView: Boolean = true,
    val canCreate: Boolean = true,
    val canEdit: Boolean = true,
    val canDelete: Boolean = true,
    val canExport: Boolean = true,
    val canPrint: Boolean = true,
    val canShare: Boolean = true,
    val canApprove: Boolean = true
) {
    fun isAllowed(action: PermissionAction): Boolean = when (action) {
        PermissionAction.VIEW -> canView
        PermissionAction.CREATE -> canCreate
        PermissionAction.EDIT -> canEdit
        PermissionAction.DELETE -> canDelete
        PermissionAction.EXPORT -> canExport
        PermissionAction.PRINT -> canPrint
        PermissionAction.SHARE -> canShare
        PermissionAction.APPROVE -> canApprove
    }
}

val LocalScreenPermissions = staticCompositionLocalOf { ScreenActionPermissions() }

/**
 * Checks whether an action menu item should be enabled.
 * VIEW actions stay enabled so clicking them triggers AccessRestrictedDialog when view == false.
 * Non-VIEW actions (Create, Edit, Delete, etc.) are disabled when permission is missing.
 */
fun isActionLabelPermitted(label: String, perms: ScreenActionPermissions): Boolean {
    val text = label.trim().lowercase()
    return when {
        text.contains("delete") || text.contains("remove") -> perms.canDelete
        text.contains("edit") || text.contains("update") -> perms.canEdit
        text.contains("create") || text.contains("add") || text.contains("new") || text.startsWith("+") -> perms.canCreate
        text.contains("export") -> perms.canExport
        text.contains("print") -> perms.canPrint
        text.contains("share") -> perms.canShare
        text.contains("approve") -> perms.canApprove
        else -> true
    }
}

/**
 * Checks whether a FAB button should be enabled based on its label or explicit action.
 */
fun isFabPermitted(
    label: String,
    explicitAction: PermissionAction?,
    perms: ScreenActionPermissions
): Boolean {
    if (explicitAction != null) {
        return if (explicitAction == PermissionAction.VIEW) true else perms.isAllowed(explicitAction)
    }
    return isActionLabelPermitted(label, perms)
}

/**
 * Jetpack Compose equivalent of React FeatureGuard.
 * Shows AccessRestrictedDialog for VIEW denials, prevents API hits, and provides action permissions to child UI.
 */
@Composable
fun FeatureGuard(
    module: String,
    feature: String,
    action: PermissionAction = PermissionAction.VIEW,
    permissionViewModel: PermissionViewModel,
    upgradeKey: String = module,
    onUpgradeRequired: ((PaywallModuleConfig) -> Unit)? = null,
    onGoBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val myPermissions by permissionViewModel.myPermissions.collectAsState()
    val modulePermissions by permissionViewModel.modulePermissions.collectAsState()

    val hasActionPermission = remember(myPermissions, module, feature, action) {
        permissionViewModel.canUserAction(module, feature, action)
    }

    val isPlanSubscribed = remember(modulePermissions, module, feature) {
        permissionViewModel.isPlanFeatureSubscribed(module, feature)
    }

    val screenPermissions = remember(myPermissions, modulePermissions, module, feature) {
        ScreenActionPermissions(
            module = module,
            feature = feature,
            canView = permissionViewModel.canAction(module, feature, PermissionAction.VIEW),
            canCreate = permissionViewModel.canAction(module, feature, PermissionAction.CREATE),
            canEdit = permissionViewModel.canAction(module, feature, PermissionAction.EDIT),
            canDelete = permissionViewModel.canAction(module, feature, PermissionAction.DELETE),
            canExport = permissionViewModel.canAction(module, feature, PermissionAction.EXPORT),
            canPrint = permissionViewModel.canAction(module, feature, PermissionAction.PRINT),
            canShare = permissionViewModel.canAction(module, feature, PermissionAction.SHARE),
            canApprove = permissionViewModel.canAction(module, feature, PermissionAction.APPROVE)
        )
    }

    when {
        // 1. Highest priority: Plan does not include this feature -> show upgrade paywall first
        !isPlanSubscribed -> {
            if (onUpgradeRequired != null) {
                LaunchedEffect(module, feature, upgradeKey) {
                    onUpgradeRequired(permissionViewModel.getPaywallConfigForModule(upgradeKey))
                }
            } else {
                AccessRestrictedDialog(
                    requiredPermission = "${module.replaceFirstChar { it.uppercase() }} - $feature",
                    message = "This feature is not included in your current subscription plan.",
                    onGoBack = onGoBack,
                    onDismiss = onGoBack,
                    onRequestAccess = onGoBack
                )
            }
        }

        // 2. Plan is subscribed: now check role permission and show AccessRestrictedDialog only for VIEW denials
        !hasActionPermission -> {
            if (action == PermissionAction.VIEW) {
                AccessRestrictedDialog(
                    requiredPermission = "${module.replaceFirstChar { it.uppercase() }} - $feature",
                    message = "You don't have permission to view this section. Please contact your administrator.",
                    onGoBack = onGoBack,
                    onDismiss = onGoBack,
                    onRequestAccess = onGoBack
                )
            } else {
                // Non-VIEW actions should be disabled at UI level; pop back without showing dialog
                LaunchedEffect(module, feature, action) {
                    onGoBack()
                }
            }
        }

        else -> {
            CompositionLocalProvider(LocalScreenPermissions provides screenPermissions) {
                content()
            }
        }
    }
}