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
    description = "Disables the in-app update check mechanism safely in DEX bytecode.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // 1. Thử phân giải bằng Fingerprint tiêu chuẩn
        val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull

        if (targetMethod != null) {
            // Chèn return-void vô hiệu hóa hàm khởi tạo cập nhật
            targetMethod.replaceInstruction(1, "return-void")
        } else {
            // 2. Dự phòng: Duyệt trực tiếp danh sách classes trong DEX nếu Fingerprint trả về null
            classes.find { it.type == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
                ?.methods
                ?.find { it.name == "<init>" }
                ?.replaceInstruction(1, "return-void")
        }
    }
}