package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check mechanism safely by neutralizing update worker methods.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Định vị chính xác class InAppUpdater trong tầng DEX
        val updaterClass = classes.find { it.type == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
        
        updaterClass?.methods?.forEach { method ->
            // Bỏ qua constructor và static block để giữ nguyên vẹn khởi tạo biến thể hiện
            if (method.name != "<init>" && method.name != "<clinit>") {
                try {
                    when (method.returnType) {
                        // Nếu phương thức trả về kiểu void (thực thi ngầm): Chèn lệnh return-void ngay dòng đầu
                        "V" -> {
                            method.addInstructions(0, listOf("return-void"))
                        }
                        // Nếu phương thức trả về kiểu boolean (kiểm tra trạng thái): Ép trả về false (0x0)
                        "Z" -> {
                            method.addInstructions(0, listOf("const/4 v0, 0x0", "return v0"))
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}