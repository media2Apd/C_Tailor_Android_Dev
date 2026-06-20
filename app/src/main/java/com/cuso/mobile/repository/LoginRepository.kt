package com.cuso.mobile.repository

import androidx.room.withTransaction
import com.cuso.mobile.database.AppDatabase
import com.cuso.mobile.database.entities.*
import com.cuso.mobile.model.GoogleLoginData
import com.cuso.mobile.model.LoginData
import com.cuso.mobile.model.Organization
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun getUser() = db.userDao().getUser()
    suspend fun getOrganization() = db.organizationDao().getOrganization()
    suspend fun getTokens() = db.tokensDao().getTokens()

    suspend fun saveLoginData(loginData: LoginData) {

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
                    organizationId = org._id,
                    companySize = ""
                )
            )

            // 2. Save Organization
            // NOTE: all org metadata fields below are guaranteed non-null by the
            // Organization model/API contract, so no fallback defaults are needed.
            db.organizationDao().insertOrganization(
                OrganizationEntity(
                    orgId = org._id,
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
                    v = org.__v,
                    defaultBranch = org.defaultBranch,
                    ownerId = org.ownerId,
                    ownerMemberId = org.ownerMemberId,
                    businessType = org.businessType,
                    taxId = org.taxId,
                    isInternalOrganization = org.isInternalOrganization
                )
            )

            // 3. Save Org Lists
            db.organizationDao().insertDomains(org.domains.map {
                OrgDomainEntity(
                    orgId = org._id,
                    domain = it
                )
            })
            db.organizationDao().insertSegments(org.segments.map {
                OrgSegmentEntity(
                    orgId = org._id,
                    segment = it
                )
            })
            db.organizationDao().insertBranches(org.branches.map {
                OrgBranchEntity(
                    orgId = org._id,
                    branch = it
                )
            })

            // 4. Save Subscription
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
                sub.featuresEnabled.map {
                    FeatureEnabledEntity(
                        orgId = org._id,
                        feature = it
                    )
                }
            )

            // 5. Save Settings
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

            // 6. Save Tokens
            db.tokensDao().insertTokens(
                TokensEntity(
                    userId = user.id,
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    csrfToken = tokens.csrfToken,
                    sessionLoginToken = tokens.sessionLoginToken,
                    orgToken = tokens.orgToken
                )
            )
        }
    }

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
                organizationId = org.id,   // ← org.id not org._id
                companySize = ""
            )
        )

        // 2. Save Organization
        db.organizationDao().insertOrganization(
            OrganizationEntity(
                orgId = org.id,           // ← org.id not org._id
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
                v = org.version,          // ← org.version not org.__v
                defaultBranch = org.defaultBranch,
                ownerId = org.ownerId,
                ownerMemberId = org.ownerMemberId,
                businessType = org.businessType,
                taxId = org.taxId,
                isInternalOrganization = org.isInternalOrganization
            )
        )

        // 3. Save Org Lists
        db.organizationDao().insertDomains(org.domains.map {
            OrgDomainEntity(orgId = org.id, domain = it)
        })
        db.organizationDao().insertSegments(org.segments.map {
            OrgSegmentEntity(orgId = org.id, segment = it)
        })
        db.organizationDao().insertBranches(org.branches.map {
            OrgBranchEntity(orgId = org.id, branch = it)
        })

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

        // 6. Save Tokens — orgToken doesn't exist for Google login
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

    suspend fun saveOrganizationData(org: Organization) {
        db.withTransaction {
            // Clear only org-related tables, not user/tokens
            db.organizationDao().clearOrganization()
            db.organizationDao().clearDomains()
            db.organizationDao().clearSegments()
            db.organizationDao().clearBranches()
            db.subscriptionDao().clearSubscription()
            db.subscriptionDao().clearFeatures()
            db.settingsDao().clearSettings()
            db.settingsDao().clearWorkingDays()

            db.organizationDao().insertOrganization(
                OrganizationEntity(
                    orgId = org._id,
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
                    v = org.__v,
                    defaultBranch = org.defaultBranch,
                    ownerId = org.ownerId,
                    ownerMemberId = org.ownerMemberId,
                    businessType = org.businessType,
                    taxId = org.taxId,
                    isInternalOrganization = org.isInternalOrganization
                )
            )

            db.organizationDao().insertDomains(org.domains.map { OrgDomainEntity(orgId = org._id, domain = it) })
            db.organizationDao().insertSegments(org.segments.map { OrgSegmentEntity(orgId = org._id, segment = it) })
            db.organizationDao().insertBranches(org.branches.map { OrgBranchEntity(orgId = org._id, branch = it) })

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
}