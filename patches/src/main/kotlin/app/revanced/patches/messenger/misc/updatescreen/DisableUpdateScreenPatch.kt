package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable

internal val inAppUpdaterTimestampFingerprint = fingerprint {
    strings("appupdater_timestamp")
}

internal val inAppUpdateListenerFingerprint = fingerprint {
    strings("AppUpdateInfo Listener Failed")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables Messenger in-app update checks and listener triggers.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Vô hiệu hóa hàm kiểm tra/xử lý mốc thời gian cập nhật
        val timestampMethod = inAppUpdaterTimestampFingerprint.method.toMutable()
        when (timestampMethod.returnType) {
            "V" -> timestampMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                timestampMethod.replaceInstruction(0, "const/4 v0, 0x0")
                timestampMethod.replaceInstruction(1, "return v0")
            }
        }

        // Vô hiệu hóa bộ lắng nghe thông tin cập nhật (InAppUpdate listener)
        val listenerMethod = inAppUpdateListenerFingerprint.method.toMutable()
        when (listenerMethod.returnType) {
            "V" -> listenerMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                listenerMethod.replaceInstruction(0, "const/4 v0, 0x0")
                listenerMethod.replaceInstruction(1, "return v0")
            }
        }
    }
}