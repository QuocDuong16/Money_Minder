package com.niand.moneyminder.screen

import androidx.compose.ui.graphics.Color
import com.niand.moneyminder.model.Transactions
import io.realm.kotlin.types.RealmInstant
import kotlin.math.abs

val uri = "nguyenquocduong-380-77nswlqf3q-as.a.run.app"

enum class TransactionType(val value: String) {
    INCOME("INCOME"),
    SPEND("SPEND");

    companion object {
        // Hàm tìm giá trị từ String
        fun fromValue(value: String): TransactionType? {
            return values().find { it.value == value }
        }
    }
}

val spendCategories = listOf("Quần áo", "Thực phẩm", "Giao thông", "Mua sắm", "Cá nhân", "Giáo dục")
val incomeCategories = listOf("Lương", "Thu nhập khác", "Tiền thưởng", "Bán vật dụng")

val spendColorList: List<Color> = listOf(
    Color(0xFFE57373), // Đỏ sậm nhẹ
    Color(0xFFEF5350),
    Color(0xFFFF8A80),
    Color(0xFFFF5252),
    Color(0xFFFF1744),
    Color(0xFFD32F2F)
)

val incomeColorList: List<Color> = listOf(
    Color(0xFF64B5F6), // Xanh dương sậm nhẹ
    Color(0xFF42A5F5),
    Color(0xFF2196F3),
    Color(0xFF1E88E5)
)

val adviceList = listOf(
    "Hãy kiểm soát ngân sách của bạn một cách thông minh.",
    "Đặt một mục tiêu tiết kiệm và duy trì nó.",
    "Lập kế hoạch cho mọi chi tiêu của bạn.",
    "Tìm kiếm các ưu đãi và giảm giá khi mua sắm.",
    "Hạn chế việc chi tiêu không cần thiết.",
    "Kiểm tra và cập nhật ngân sách hàng tháng.",
    "Đặt kỳ vọng hợp lý về thu nhập và chi tiêu.",
    "Thực hiện đánh giá định kỳ về tình hình tài chính của bạn.",
    "Tạo quỹ dự phòng cho các chi tiêu không dự kiến.",
    "Kiểm tra các chương trình khuyến mãi và ưu đãi của thẻ tín dụng.",
    "Tìm hiểu về các cách tiết kiệm chi phí hàng ngày.",
    "Không mua sắm dựa trên tâm trạng.",
    "Sử dụng ưu đãi từ các chương trình thành viên.",
    "Hãy xem xét và cắt giảm những chi tiêu không cần thiết.",
    "Hãy giữ cho mục tiêu tiết kiệm của bạn có thể đo được.",
    "Thực hiện kế hoạch tài chính hợp lý cho tương lai.",
    "Thực hiện bảng cân đối chi tiêu hàng tháng.",
    "Kiểm tra và so sánh giá trước khi mua sắm.",
    "Hãy xem xét lại các khoản chi trực tuyến của bạn đều đặn.",
    "Giữ cho hồ sơ tín dụng của bạn trong tình trạng tốt.",
    "Kiểm tra và cập nhật thông tin tài khoản ngân hàng của bạn."
)

fun getTotalAmount(transactionList: List<Transactions>) : Long {
    var total: Long = 0
    for (t in transactionList) {
        when (t.type) {
            TransactionType.SPEND.value -> total -= t.amount
            TransactionType.INCOME.value -> total += t.amount
        }
    }
    return total
}

fun Long.toRealmInstant(): RealmInstant {
    val seconds = this / 1000L
    val nanoseconds = (this % 1000L) * 1_000_000L
    return RealmInstant.from(seconds, nanoseconds.toInt())
}

fun RealmInstant.toLong(): Long {
    return epochSeconds * 1000 + nanosecondsOfSecond / 1_000_000
}

fun Long.toPositiveFloat(): Float {
    return abs(this.toFloat())
}

fun Long.toPositiveLong(): Long {
    return abs(this)
}