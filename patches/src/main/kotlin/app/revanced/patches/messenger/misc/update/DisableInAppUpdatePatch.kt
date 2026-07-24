package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check mechanism safely by neutralizing update worker methods.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Tìm đúng class InAppUpdater
        val updaterClass = classes.find { it.type == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
        
        updaterClass?.methods?.forEach { method ->
            // Bỏ qua các hàm khởi tạo để giữ nguyên vòng đời đối tượng
            if (method.name != "<init>" && method.name != "<clinit>") {
                try {
                    when (method.returnType) {
                        // Nếu trả về void: Thay thế lệnh đầu tiên thành return-void
                        "V" -> {
                            method.replaceInstruction(0, "return-void")
                        }
                        // Nếu trả về boolean: Thay thế 2 lệnh đầu tiên để ép trả về false (0x0)
                        "Z" -> {
                            method.replaceInstruction(0, "const/4 v0, 0x0")
                            method.replaceInstruction(1, "return v0")
                        }
                    }
                } catch (_: Exception) {
                    // Bỏ qua an toàn nếu method quá ngắn không đủ 2 dòng lệnh
                }
            }
        }
    }
}