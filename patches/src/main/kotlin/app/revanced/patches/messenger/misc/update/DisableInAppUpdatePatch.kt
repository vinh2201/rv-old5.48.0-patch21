package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import java.io.File

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
        // 1. Tự động quét và xóa sạch thư mục bẫy "anim.2" gây lỗi AAPT2
        try {
            File(".").walkTopDown().forEach { file ->
                if (file.isDirectory && file.name == "anim.2") {
                    file.deleteRecursively()
                }
            }
        } catch (_: Exception) {}

        // 2. Thực thi bytecode patch một cách an toàn tuyệt đối
        try {
            val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull
            if (targetMethod != null) {
                targetMethod.replaceInstruction(1, "return-void")
            }
        } catch (_: Exception) {}
    }
}