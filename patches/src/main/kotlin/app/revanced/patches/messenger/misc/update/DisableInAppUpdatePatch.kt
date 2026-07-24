package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable

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
            // Bỏ qua các hàm khởi tạo để giữ nguyên vẹn vòng đời đối tượng
            if (method.name != "<init>" && method.name != "<clinit>") {
                try {
                    // Ép kiểu tường minh sang MutableMethod để mở khóa quyền chỉnh sửa
                    val mutableMethod = method as MutableMethod
                    
                    when (mutableMethod.returnType) {
                        // Nếu hàm trả về void: Thay thế lệnh đầu tiên thành return-void
                        "V" -> {
                            mutableMethod.replaceInstruction(0, "return-void")
                        }
                        // Nếu hàm trả về boolean: Thay thế 2 lệnh đầu để ép trả về false (0x0)
                        "Z" -> {
                            mutableMethod.replaceInstruction(0, "const/4 v0, 0x0")
                            mutableMethod.replaceInstruction(1, "return v0")
                        }
                    }
                } catch (_: Exception) {
                    // Bỏ qua an toàn nếu hàm rỗng hoặc quá ngắn
                }
            }
        }
    }
}