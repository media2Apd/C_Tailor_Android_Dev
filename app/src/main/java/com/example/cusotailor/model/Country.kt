package com.example.cusotailor.model

data class Country(
    val name: String,
    val code: String,
    val iso: String,
    val flag: String
)

//Countries
val countries = listOf(
Country("Afghanistan", "+93", "AF", "🇦🇫"),
Country("Albania", "+355", "AL", "🇦🇱"),
Country("Algeria", "+213", "DZ", "🇩🇿"),
Country("Andorra", "+376", "AD", "🇦🇩"),
Country("Angola", "+244", "AO", "🇦🇴"),
Country("Argentina", "+54", "AR", "🇦🇷"),
Country("Armenia", "+374", "AM", "🇦🇲"),
Country("Australia", "+61", "AU", "🇦🇺"),
Country("Austria", "+43", "AT", "🇦🇹"),
Country("Azerbaijan", "+994", "AZ", "🇦🇿"),

Country("Bahrain", "+973", "BH", "🇧🇭"),
Country("Bangladesh", "+880", "BD", "🇧🇩"),
Country("Belgium", "+32", "BE", "🇧🇪"),
Country("Brazil", "+55", "BR", "🇧🇷"),
Country("Canada", "+1", "CA", "🇨🇦"),
Country("China", "+86", "CN", "🇨🇳"),
Country("Denmark", "+45", "DK", "🇩🇰"),
Country("Egypt", "+20", "EG", "🇪🇬"),
Country("France", "+33", "FR", "🇫🇷"),
Country("Germany", "+49", "DE", "🇩🇪"),

Country("India", "+91", "IN", "🇮🇳"),
Country("Indonesia", "+62", "ID", "🇮🇩"),
Country("Iran", "+98", "IR", "🇮🇷"),
Country("Iraq", "+964", "IQ", "🇮🇶"),
Country("Ireland", "+353", "IE", "🇮🇪"),
Country("Italy", "+39", "IT", "🇮🇹"),
Country("Japan", "+81", "JP", "🇯🇵"),
Country("Kenya", "+254", "KE", "🇰🇪"),
Country("Malaysia", "+60", "MY", "🇲🇾"),
Country("Mexico", "+52", "MX", "🇲🇽"),

Country("Netherlands", "+31", "NL", "🇳🇱"),
Country("New Zealand", "+64", "NZ", "🇳🇿"),
Country("Nigeria", "+234", "NG", "🇳🇬"),
Country("Norway", "+47", "NO", "🇳🇴"),
Country("Pakistan", "+92", "PK", "🇵🇰"),
Country("Philippines", "+63", "PH", "🇵🇭"),
Country("Poland", "+48", "PL", "🇵🇱"),
Country("Portugal", "+351", "PT", "🇵🇹"),
Country("Qatar", "+974", "QA", "🇶🇦"),
Country("Russia", "+7", "RU", "🇷🇺"),

Country("Saudi Arabia", "+966", "SA", "🇸🇦"),
Country("Singapore", "+65", "SG", "🇸🇬"),
Country("South Africa", "+27", "ZA", "🇿🇦"),
Country("South Korea", "+82", "KR", "🇰🇷"),
Country("Spain", "+34", "ES", "🇪🇸"),
Country("Sri Lanka", "+94", "LK", "🇱🇰"),
Country("Sweden", "+46", "SE", "🇸🇪"),
Country("Switzerland", "+41", "CH", "🇨🇭"),
Country("Thailand", "+66", "TH", "🇹🇭"),
Country("Turkey", "+90", "TR", "🇹🇷"),

Country("UAE", "+971", "AE", "🇦🇪"),
Country("United Kingdom", "+44", "GB", "🇬🇧"),
Country("United States", "+1", "US", "🇺🇸"),
Country("Vietnam", "+84", "VN", "🇻🇳")

)
