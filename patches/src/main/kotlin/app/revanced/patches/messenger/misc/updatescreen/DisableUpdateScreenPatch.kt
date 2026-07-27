package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.fingerprint

// 1. Chộp lấy class quản lý cập nhật InAppUpdater cốt lõi
internal val inAppUpdaterFingerprint = fingerprint {
    strings("InAppUpdater.checkUpdateAvailability", "inAppUpdater", "InAppUpdater", "InAppUpdate", "com.facebook.messenger.app.update.InAppUpdater", "com.facebook.messenger.app.update.InAppUpdater.Companion", "com.facebook.messenger.app.update.InAppUpdater#checkUpdateAvailability", "rtc_upgrade_policy_deprecated_version", "MsgrRUPBlockFragment")
}

// 2. Chộp lấy rào cản RTC (kẻ bóp chết cuộc gọi)
internal val rtcUpgradePolicyFingerprint = fingerprint {
    strings("rtc_upgrade_policy_deprecated_version")
}

// 3. Giữ lại chốt chặn cũ như một lớp bảo vệ phụ
internal val versionUpgradeRequiredFingerprint = fingerprint {
    strings("Setting versionUpgradeRequired = ")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Forces all version upgrade checks and RTC update policies to return false.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // --- TẤN CÔNG 1: Vô hiệu hoá logic checkUpdateAvailability ---
        val updaterMethod = inAppUpdaterFingerprint.method?.toMutable()
        if (updaterMethod != null) {
            // Ép hàm trả về false (0) hoặc return void ngay lập tức tuỳ vào kiểu trả về
            val returnInstruction = if (updaterMethod.returnType == "Z") {
                """
                const/4 v0, 0x0
                return v0
                """
            } else {
                "return-void"
            }
            updaterMethod.addInstructions(0, returnInstruction)
        }

        // --- TẤN CÔNG 2: Tiệt đường ngắt cuộc gọi của RTC Policy ---
        val rtcMethod = rtcUpgradePolicyFingerprint.method?.toMutable()
        if (rtcMethod != null) {
            // Ghi đè phương thức kiểm tra rtc_upgrade_policy_deprecated_version
            // Buộc logic trả về false, giả lập rằng version này không hề bị deprecated
            if (rtcMethod.returnType == "Z") {
                rtcMethod.addInstructions(
                    0,
                    """
                    const/4 v0, 0x0
                    return v0
                    """
                )
            }
        }

        // --- TẤN CÔNG 3: Chặn cờ versionUpgradeRequired (Giữ nguyên của bạn) ---
        val versionMethod = versionUpgradeRequiredFingerprint.method?.toMutable()
        if (versionMethod != null && versionMethod.returnType == "Z") {
            versionMethod.addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
                """
            )
        }
    }
}