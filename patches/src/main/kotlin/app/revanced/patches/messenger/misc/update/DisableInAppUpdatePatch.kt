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
    description = "Disables the in-app update check mechanism and sanitizes resource traps.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Lệnh siêu mạnh: Quét toàn bộ thư mục, dọn sạch mọi thư mục bẫy *.2 (anim.2, color.2, drawable.2, ...)
        try {
            File(".").walkTopDown().forEach { file ->
                if (file.isDirectory) {
                    val name = file.name
                    if (name.endsWith(".2") || name.contains("APKTOOL_DUMMYVAL")) {
                        file.deleteRecursively()
                    }
                }
            }
        } catch (_: Exception) {}

        // Vô hiệu hóa InAppUpdater một cách an toàn tuyệt đối
        try {
            val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull
            if (targetMethod != null) {
                targetMethod.replaceInstruction(1, "return-void")
            }
        } catch (_: Exception) {}
    }
}