package com.cuso.tailor.model.hr
//
//data class MonthlyAttendanceResponse(
//    val success: Boolean,
//    val message: String? = null,
//    val data: List<MonthlyAttendanceItem> = emptyList()
//)
//
//data class MonthlyAttendanceItem(
//    val _id: String,
//    val organizationId: String? = null,
//    val organizationMemberId: MemberReference? = null,
//    val date: String,                      // e.g. "2026-09-19T18:30:00.000Z"
//    val shiftId: ShiftReference? = null,
//    val workMode: String? = null,
//    val totalWorkingMinutes: Int = 0,
//    val totalBreakMinutes: Int = 0,
//    val overtimeMinutes: Int = 0,
//    val missedCheckout: Boolean = false,
//    val isHalfDay: Boolean = false,
//    val isLate: Boolean = false,
//    val lateMinutes: Int = 0,
//    val isEarlyOut: Boolean = false,
//    val earlyOutMinutes: Int = 0,
//    val firstIn: String? = null,           // e.g. "2026-09-20T04:00:00.000Z"
//    val lastOut: String? = null,           // e.g. "2026-09-20T14:30:00.000Z"
//    val status: String? = null,            // "present", "absent", "late", "leave"
//    val manualEntry: Boolean = false,
//    val isApproved: Boolean = false,
//    val approvalStatus: String? = null,
//    val adminNote: String? = null
//)
//
//data class MemberReference(
//    val _id: String,
//    val departmentId: String? = null,
//    val firstName: String? = null,
//    val lastName: String? = null,
//    val memberId: String? = null
//)
//
//data class ShiftReference(
//    val _id: String,
//    val name: String? = null,
//    val shiftId: String? = null,
//    val startTime: String? = null,
//    val endTime: String? = null
//)
//
//data class PunchItem(
//    val type: String? = null,    // "in" or "out"
//    val time: String? = null     // "2026-09-01T09:15:00.000Z"
//)





data class MonthlyAttendanceResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<MonthlyAttendanceItem> = emptyList()
)

data class MonthlyAttendanceItem(
    val _id: String,
    val organizationId: String? = null,
    val organizationMemberId: MemberReference? = null,
    val date: String,                      // e.g. "2026-09-19T18:30:00.000Z"
    val shiftId: ShiftReference? = null,
    val punches: List<PunchItem> = emptyList(), // <--- INTHA LINE MATTUM ADD PANNUNGA!
    val workMode: String? = null,
    val totalWorkingMinutes: Int = 0,
    val totalBreakMinutes: Int = 0,
    val overtimeMinutes: Int = 0,
    val missedCheckout: Boolean = false,
    val isHalfDay: Boolean = false,
    val isLate: Boolean = false,
    val lateMinutes: Int = 0,
    val isEarlyOut: Boolean = false,
    val earlyOutMinutes: Int = 0,
    val firstIn: String? = null,           // e.g. "2026-09-20T04:00:00.000Z"
    val lastOut: String? = null,           // e.g. "2026-09-20T14:30:00.000Z"
    val status: String? = null,            // "present", "absent", "late", "leave"
    val manualEntry: Boolean = false,
    val isApproved: Boolean = false,
    val approvalStatus: String? = null,
    val adminNote: String? = null
)

data class MemberReference(
    val _id: String,
    val departmentId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val memberId: String? = null
)

data class ShiftReference(
    val _id: String,
    val name: String? = null,
    val shiftId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)

data class PunchItem(
    val type: String? = null,    // "in" or "out"
    val time: String? = null     // "2026-09-01T09:15:00.000Z"
)