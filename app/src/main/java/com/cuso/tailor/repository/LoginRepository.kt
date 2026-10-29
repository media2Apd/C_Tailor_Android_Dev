// com/cuso/mobile/repository/LoginRepository.kt

package com.cuso.tailor.repository

import androidx.room.withTransaction
import com.cuso.tailor.database.AppDatabase
import com.cuso.tailor.database.entities.*
import com.cuso.tailor.model.login_forgotPassword_resetPassword.GoogleLoginData
import com.cuso.tailor.model.login_forgotPassword_resetPassword.LoginData
import com.cuso.tailor.model.login_forgotPassword_resetPassword.Organization
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun getUser() = db.userDao().getUser()
    suspend fun getOrganization() = db.organizationDao().getOrganization()
    suspend fun getTokens() = db.tokensDao().getTokens()

    // ---------------------------------------------------------
    // Login
    // ---------------------------------------------------------
    suspend fun saveLoginData(loginData: LoginData) {
        db.withTransaction {
            clearAll()
            val user = loginData.user
            val org = user.organizationId
            val tokens = loginData.tokens

            // 1. Save User
            // orEmpty() protects against explicit "null" from the server (e.g. profilePicture: null)
            db.userDao().insertUser(
                UserEntity(
                    id = user.id.orEmpty(),
                    userId = user.userId.orEmpty(),
                    firstName = user.firstName.orEmpty(),
                    lastName = user.lastName.orEmpty(),
                    email = user.email.orEmpty(),
                    profilePicture = user.profilePicture.orEmpty(),
                    role = user.role.orEmpty(),
                    memberId = user.memberId.orEmpty(),
                    organizationId = org._id.orEmpty(),
                    companySize = ""
                )
            )

            // 2-5. Organization, lists, subscription, settings (shared helper)
            saveOrgTables(org)

            // 6. Save Tokens
            db.tokensDao().insertTokens(
                TokensEntity(
                    userId = user.id.orEmpty(),
                    accessToken = tokens.accessToken.orEmpty(),
                    refreshToken = tokens.refreshToken.orEmpty(),
                    csrfToken = tokens.csrfToken.orEmpty(),
                    sessionLoginToken = tokens.sessionLoginToken.orEmpty(),
                    orgToken = tokens.orgToken.orEmpty()
                )
            )
        }
    }



    // ---------------------------------------------------------
    // Shared: saves organization + lists + subscription + settings.
    // MUST be called inside db.withTransaction { }
    // Every value goes through orEmpty() / safe defaults because the server
    // may omit fields (Gson then leaves them null even for non-null Kotlin types).
    // ---------------------------------------------------------
    private suspend fun saveOrgTables(org: Organization) {
        val orgId = org._id.orEmpty()

        // Organization
        db.organizationDao().insertOrganization(
            OrganizationEntity(
                orgId = orgId,
                businessId = org.businessId.orEmpty(),
                name = org.name.orEmpty(),
                industry = org.industry.orEmpty(),          // <-- the field that crashed
                orgType = org.orgType.orEmpty(),
                organizationPicture = org.organizationPicture,
                organizationPictureId = org.organizationPictureId,
                email = org.email.orEmpty(),
                mobile = org.mobile.orEmpty(),
                orgSetupComplete = org.orgSetupComplete,
                totalMembers = org.totalMembers,
                activeMembers = org.activeMembers,
                isTaxId = org.isTaxId,
                status = org.status.orEmpty(),
                createdAt = org.createdAt.orEmpty(),
                updatedAt = org.updatedAt.orEmpty(),
                slug = org.slug.orEmpty(),
                v = org.__v,
                defaultBranch = org.defaultBranch.orEmpty(),
                ownerId = org.ownerId.orEmpty(),
                ownerMemberId = org.ownerMemberId.orEmpty(),
                businessType = org.businessType.orEmpty(),
                taxId = org.taxId.orEmpty(),
                isInternalOrganization = org.isInternalOrganization
            )
        )

        // Lists (orEmpty() on List also handles a null list)
        db.organizationDao().insertDomains(
            org.domains.orEmpty().map { OrgDomainEntity(orgId = orgId, domain = it) }
        )
        db.organizationDao().insertSegments(
            org.segments.orEmpty().map { OrgSegmentEntity(orgId = orgId, segment = it) }
        )
        db.organizationDao().insertBranches(
            org.branches.orEmpty().map { OrgBranchEntity(orgId = orgId, branch = it) }
        )

        // Subscription (login API sends none, so this is usually all empty defaults)
        val sub = org.subscription
        db.subscriptionDao().insertSubscription(
            SubscriptionEntity(
                orgId = orgId,
                startDate = sub?.startDate.orEmpty(),
                endDate = sub?.endDate.orEmpty(),
                status = sub?.status.orEmpty(),
                memberLimit = sub?.memberLimit ?: 0
            )
        )
        db.subscriptionDao().insertFeatures(
            sub?.featuresEnabled.orEmpty().map { FeatureEnabledEntity(orgId = orgId, feature = it) }
        )

        // Settings
        val settings = org.settings
        db.settingsDao().insertSettings(
            SettingsEntity(
                orgId = orgId,
                country = settings?.country.orEmpty(),
                state = settings?.state.orEmpty(),
                portalName = settings?.portalName.orEmpty(),
                termsAccepted = settings?.termsAccepted ?: false,
                marketingEmails = settings?.marketingEmails ?: false,
                companySize = settings?.companySize.orEmpty(),
                timezone = settings?.timezone.orEmpty(),
                currency = settings?.currency.orEmpty(),
                language = settings?.language.orEmpty(),
                address = settings?.address.orEmpty(),
                city = settings?.city.orEmpty(),
                pincode = settings?.pincode.orEmpty()
            )
        )
        db.settingsDao().insertWorkingDays(
            settings?.workingDays.orEmpty().map { WorkingDayEntity(orgId = orgId, day = it) }
        )
    }

    suspend fun updateProfilePicture(userId: String, newUrl: String?) {
        val currentUser = db.userDao().getUser() ?: return
        if (currentUser.id != userId) return

        val updatedUser = currentUser.copy(profilePicture = newUrl)
        db.userDao().updateUser(updatedUser)
    }

    fun getUserFlow() = db.userDao().getUserFlow()

    suspend fun clearAll() {
        db.userDao().clearUser()
        db.organizationDao().clearOrganization()
        db.organizationDao().clearDomains()
        db.organizationDao().clearSegments()
        db.organizationDao().clearBranches()
        db.subscriptionDao().clearSubscription()
        db.subscriptionDao().clearFeatures()
        db.settingsDao().clearSettings()
        db.settingsDao().clearWorkingDays()
        db.tokensDao().clearTokens()
    }

    @Suppress("unused")
    suspend fun saveGoogleLoginData(loginData: GoogleLoginData) {
        db.withTransaction {
            clearAll()
            val user = loginData.user
            val org = user.organizationId
            val tokens = loginData.tokens

            // 1. Save User
            db.userDao().insertUser(
                UserEntity(
                    id = user.id,
                    userId = user.userId,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    profilePicture = user.profilePicture,
                    role = user.role,
                    memberId = user.memberId,
                    organizationId = org.id,
                    companySize = ""
                )
            )

            // 2. Save Organization
            db.organizationDao().insertOrganization(
                OrganizationEntity(
                    orgId = org.id,
                    businessId = org.businessId,
                    name = org.name,
                    industry = org.industry,
                    orgType = org.orgType,
                    organizationPicture = org.organizationPicture,
                    organizationPictureId = org.organizationPictureId,
                    email = org.email,
                    mobile = org.mobile,
                    orgSetupComplete = org.orgSetupComplete,
                    totalMembers = org.totalMembers,
                    activeMembers = org.activeMembers,
                    isTaxId = org.isTaxId,
                    status = org.status,
                    createdAt = org.createdAt,
                    updatedAt = org.updatedAt,
                    slug = org.slug,
                    v = org.version,
                    defaultBranch = org.defaultBranch,
                    ownerId = org.ownerId,
                    ownerMemberId = org.ownerMemberId,
                    businessType = org.businessType,
                    taxId = org.taxId,
                    isInternalOrganization = org.isInternalOrganization
                )
            )

            // 3. Save Org Lists
            db.organizationDao().insertDomains(
                org.domains.map { OrgDomainEntity(orgId = org.id, domain = it) }
            )
            db.organizationDao().insertSegments(
                org.segments.map { OrgSegmentEntity(orgId = org.id, segment = it) }
            )
            //   Google branches are List<String>
            db.organizationDao().insertBranches(
                org.branches.map { branchId ->
                    OrgBranchEntity(orgId = org.id, branch = branchId)
                }
            )

            // 4. Save Subscription
            val sub = org.subscription
            db.subscriptionDao().insertSubscription(
                SubscriptionEntity(
                    orgId = org.id,
                    startDate = sub.startDate,
                    endDate = sub.endDate,
                    status = sub.status,
                    memberLimit = sub.memberLimit
                )
            )
            db.subscriptionDao().insertFeatures(
                sub.featuresEnabled.map {
                    FeatureEnabledEntity(orgId = org.id, feature = it)
                }
            )

            // 5. Save Settings
            val settings = org.settings
            db.settingsDao().insertSettings(
                SettingsEntity(
                    orgId = org.id,
                    country = settings.country,
                    state = settings.state,
                    portalName = settings.portalName,
                    termsAccepted = settings.termsAccepted,
                    marketingEmails = settings.marketingEmails,
                    timezone = settings.timezone,
                    currency = settings.currency,
                    language = settings.language,
                    address = settings.address,
                    city = settings.city,
                    pincode = settings.pincode
                )
            )
            db.settingsDao().insertWorkingDays(
                settings.workingDays.map {
                    WorkingDayEntity(orgId = org.id, day = it)
                }
            )

            // 6. Save Tokens
            db.tokensDao().insertTokens(
                TokensEntity(
                    userId = user.id,
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    csrfToken = tokens.csrfToken,
                    sessionLoginToken = tokens.sessionLoginToken,
                    orgToken = ""
                )
            )
        }
    }

    suspend fun saveOrganizationData(org: Organization) {
        db.withTransaction {
            // Clear only org-related tables
            db.organizationDao().clearOrganization()
            db.organizationDao().clearDomains()
            db.organizationDao().clearSegments()
            db.organizationDao().clearBranches()
            db.subscriptionDao().clearSubscription()
            db.subscriptionDao().clearFeatures()
            db.settingsDao().clearSettings()
            db.settingsDao().clearWorkingDays()

            // 1. Save Organization
            db.organizationDao().insertOrganization(
                OrganizationEntity(
                    orgId = org._id,
                    businessId = org.businessId,
                    name = org.name,
                    industry = org.industry ?: "",
                    orgType = org.orgType,
                    organizationPicture = org.organizationPicture,
                    organizationPictureId = org.organizationPictureId,
                    email = org.email,
                    mobile = org.mobile,
                    orgSetupComplete = org.orgSetupComplete,
                    totalMembers = org.totalMembers,
                    activeMembers = org.activeMembers,
                    isTaxId = org.isTaxId,
                    status = org.status,
                    createdAt = org.createdAt,
                    updatedAt = org.updatedAt,
                    slug = org.slug,
                    v = org.__v,
                    defaultBranch = org.defaultBranch,
                    ownerId = org.ownerId,
                    ownerMemberId = org.ownerMemberId,
                    businessType = org.businessType,
                    taxId = org.taxId,
                    isInternalOrganization = org.isInternalOrganization
                )
            )

            // 2. Save Org Lists
            db.organizationDao().insertDomains(
                org.domains.map { OrgDomainEntity(orgId = org._id, domain = it) }
            )
            db.organizationDao().insertSegments(
                org.segments.map { OrgSegmentEntity(orgId = org._id, segment = it) }
            )
            //   branches is List<String> from Organization API response
            db.organizationDao().insertBranches(
                org.branches.map { branchId ->
                    OrgBranchEntity(orgId = org._id, branch = branchId)
                }
            )

            // 3. Save Subscription
            val sub = org.subscription
            db.subscriptionDao().insertSubscription(
                SubscriptionEntity(
                    orgId = org._id,
                    startDate = sub.startDate,
                    endDate = sub.endDate,
                    status = sub.status,
                    memberLimit = sub.memberLimit
                )
            )
            db.subscriptionDao().insertFeatures(
                sub.featuresEnabled.map { FeatureEnabledEntity(orgId = org._id, feature = it) }
            )

            // 4. Save Settings
            val settings = org.settings
            db.settingsDao().insertSettings(
                SettingsEntity(
                    orgId = org._id,
                    country = settings.country,
                    state = settings.state,
                    portalName = settings.portalName,
                    termsAccepted = settings.termsAccepted,
                    marketingEmails = settings.marketingEmails,
                    timezone = settings.timezone,
                    currency = settings.currency,
                    language = settings.language,
                    address = settings.address,
                    city = settings.city,
                    pincode = settings.pincode
                )
            )
            db.settingsDao().insertWorkingDays(
                settings.workingDays.map { WorkingDayEntity(orgId = org._id, day = it) }
            )
        }
    }
    //    // ---------------------------------------------------------
//    // Called after /my-organization API (clears only org tables)
//    // ---------------------------------------------------------
//    suspend fun saveOrganizationData(org: Organization) {
//        db.withTransaction {
//            db.organizationDao().clearOrganization()
//            db.organizationDao().clearDomains()
//            db.organizationDao().clearSegments()
//            db.organizationDao().clearBranches()
//            db.subscriptionDao().clearSubscription()
//            db.subscriptionDao().clearFeatures()
//            db.settingsDao().clearSettings()
//            db.settingsDao().clearWorkingDays()
//
//            saveOrgTables(org)
//        }
//    }
}