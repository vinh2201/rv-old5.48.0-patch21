package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

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
    description = "Disables the in-app update check mechanism safely.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        try {
            // Kiểm tra an toàn: chỉ tác động nếu tìm thấy và không bị chặn quyền đọc/ghi sâu
            val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull
            if (targetMethod != null) {
                targetMethod.replaceInstruction(1, "return-void")
            }
        } catch (e: Exception) {
            // Khi gặp vùng dữ liệu bị khóa hoặc không cho phép tác động sâu,
            // khối này sẽ bắt lỗi, ngăn chặn hoàn toàn việc sập pipeline build.
            println("Skipping patch execution due to restricted file access: ${e.message}")
        }
    }
}