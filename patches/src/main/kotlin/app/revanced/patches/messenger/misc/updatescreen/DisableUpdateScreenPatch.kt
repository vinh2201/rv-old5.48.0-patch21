package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.fingerprint

// Chộp lấy chính xác method chứa cờ kiểm tra version thông qua chuỗi log
internal val versionUpgradeRequiredFingerprint = fingerprint {
    strings("Setting versionUpgradeRequired = ")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Forces the version upgrade check to return false, preventing the update wall from appearing without touching RTC or call features.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // --- CHỈ TẬP TRUNG TẤN CÔNG GỐC RỄ: Ép hàm check version trả về false (0) ---
        val versionMethod = versionUpgradeRequiredFingerprint.method?.toMutable()
            ?: throw IllegalStateException("Không tìm thấy phương thức kiểm tra phiên bản (versionUpgradeRequired)!")

        if (versionMethod.returnType == "Z") {
            versionMethod.addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
                """
            )
        } else {
            throw IllegalStateException("Kiểu trả về của phương thức kiểm tra phiên bản không phải là boolean (Z)!")
        }
    }
}