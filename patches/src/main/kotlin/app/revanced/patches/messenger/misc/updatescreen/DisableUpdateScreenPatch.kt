package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable

internal val inAppUpdateStringFingerprint = fingerprint {
    strings("InAppUpdater.checkUpdateAvailability")
}

internal val appUpdateServiceStringFingerprint = fingerprint {
    strings("AppUpdateService")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables Messenger in-app update checks and Google Play update service hooks.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Vô hiệu hóa hàm kiểm tra cập nhật nội bộ của Messenger
        val updateMethod = inAppUpdateStringFingerprint.method.toMutable()
        when (updateMethod.returnType) {
            "V" -> updateMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                updateMethod.replaceInstruction(0, "const/4 v0, 0x0")
                updateMethod.replaceInstruction(1, "return v0")
            }
        }

        // Vô hiệu hóa dịch vụ yêu cầu cập nhật qua Google Play Core
        val serviceMethod = appUpdateServiceStringFingerprint.method.toMutable()
        when (serviceMethod.returnType) {
            "V" -> serviceMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                serviceMethod.replaceInstruction(0, "const/4 v0, 0x0")
                serviceMethod.replaceInstruction(1, "return v0")
            }
        }
    }
}