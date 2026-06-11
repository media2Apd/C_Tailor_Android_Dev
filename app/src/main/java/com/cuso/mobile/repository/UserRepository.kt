//import com.cuso.mobile.database.dao.UserDao
//import com.cuso.mobile.model.PasswordResponse
//import com.cuso.mobile.database.entity.OrganizationEntity
//import com.cuso.mobile.database.entity.TokensEntity
//import com.cuso.mobile.database.entity.UserEntity
//
//suspend fun savePasswordResponse(response: PasswordResponse) {
//
//    val data = response.data
//
//    val user = data.user
//    val org = user.organizationId
//    val tokens = data.tokens
//
//    val userEntity = UserEntity(
//        id = user.id,
//        userId = user.userId,
//        firstName = user.firstName,
//        lastName = user.lastName,
//        email = user.email,
//        profilePicture = user.profilePicture,
//        role = user.role,
//        memberId = user.memberId,
//        organizationId = org._id
//    )
//
////    val id: String,
////    val userId: String,
////    val firstName: String,
////    val lastName: String,
////    val email: String,
////    val profilePicture: String?,
////    val role: String,
////    val memberId: String
//
//    val orgEntity = OrganizationEntity(
//        _id = org._id,
//        businessId = org.businessId,
//        name = org.name,
//        industry = org.industry,
//        orgType = org.orgType,
//        email = org.email,
//        mobile = org.mobile,
//        status = org.status,
//        createdAt = org.createdAt,
//        updatedAt = org.updatedAt,
//        slug = org.slug,
//        defaultBranch = org.defaultBranch
//    )
//
//    val tokenEntity = TokensEntity(
//        accessToken = tokens.accessToken,
//        refreshToken = tokens.refreshToken,
//        csrfToken = tokens.csrfToken,
//        sessionLoginToken = tokens.sessionLoginToken,
//        orgToken = tokens.orgToken
//    )
//
//    UserDao.insertUser(userEntity)
//    UserDao.insertOrganization(orgEntity)
//    UserDao.insertTokens(tokenEntity)
//}