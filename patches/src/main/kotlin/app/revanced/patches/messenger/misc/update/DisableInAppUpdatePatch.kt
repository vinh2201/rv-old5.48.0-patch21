package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

// Khóa cứng chính xác vào class InAppUpdater thực sự, không quét chuỗi linh tinh nữa
internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    custom { method, classDef ->
        method.name == "<init>" &&
        classDef.type == "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check mechanism.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Lấy an toàn method, nếu không tìm thấy sẽ thoát êm, không gây crash patcher
        val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull ?: return@execute
        
        // Kiểm tra an toàn trước khi thay thế instruction ở index 1
        if (targetMethod.instructions.size > 1) {
            targetMethod.replaceInstruction(1, "return-void")
        }
    }
}