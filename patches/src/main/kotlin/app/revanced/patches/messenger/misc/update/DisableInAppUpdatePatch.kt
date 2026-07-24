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
    description = "Disables the in-app update check mechanism and dynamically sanitizes resource traps.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // 1. Quét động đa nền tảng (PC & Mobile) và tiêu diệt thư mục/file bẫy
        try {
            val baseDir = File(".")
            
            // Thu thập danh sách các thư mục/file rác cần xóa (tìm các folder chứa ".2" và file DUMMY)
            // Dùng walkBottomUp() để quét từ dưới lên, đảm bảo an toàn khi xóa thư mục chứa file.
            val trashes = baseDir.walkBottomUp().filter { file ->
                val name = file.name
                (file.isDirectory && name.contains(".2")) || 
                (file.isFile && name.contains("APKTOOL_DUMMYVAL"))
            }.toList()

            // Tiến hành xóa sổ hoàn toàn
            trashes.forEach { it.deleteRecursively() }
            
        } catch (e: Exception) {
            println("Cleanup error: ${e.message}")
        }

        // 2. Vô hiệu hóa InAppUpdater một cách an toàn
        try {
            val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull
            if (targetMethod != null) {
                targetMethod.replaceInstruction(1, "return-void")
            }
        } catch (_: Exception) {}
    }
}