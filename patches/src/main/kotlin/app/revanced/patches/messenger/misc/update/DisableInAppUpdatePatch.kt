package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

// Dùng Fingerprint quét thẳng đến chuỗi định danh InAppUpdater để lấy trọn vẹn method
internal val inAppUpdateFingerprint = fingerprint {
    returns("V")
    strings("InAppUpdater.checkUpdateAvailability")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check mechanism.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Thay vì đụng vào resource hay constructor, ta vô hiệu hóa ngay lệnh đầu tiên của hàm chứa chuỗi update
        inAppUpdateFingerprint.methodOrNull?.replaceInstruction(0, "return-void")
    }
}