package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

// Sử dụng đúng cấu trúc fingerprint của phiên bản cũ để tìm hàm khởi tạo
internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    custom { method, _ ->
        method.name == "<init>" &&
        method.definingClass == "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Vô hiệu hóa hoàn toàn cơ chế kiểm tra và hiển thị thông báo cập nhật bên trong ứng dụng.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Chỉ mục 0 là lệnh gọi super.<init>() bắt buộc của hệ thống.
        // Chỉ mục 1 là nơi ta chèn return-void để ngắt toàn bộ chuỗi check update.
        inAppUpdaterConstructorFingerprint.methodOrNull?.replaceInstruction(1, "return-void")
    }
}