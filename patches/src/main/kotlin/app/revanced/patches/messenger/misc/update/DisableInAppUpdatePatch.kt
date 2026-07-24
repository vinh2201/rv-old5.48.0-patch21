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
        val updaterClass = classes.find { it.type == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
        
        updaterClass?.methods?.forEach { method ->
            if (method.name != "<init>" && method.name != "<clinit>") {
                try {
                    when (method.returnType) {
                        // Trả về void: Chèn lệnh return-void ngay đầu hàm
                        "V" -> {
                            method.addInstructions(0, "return-void")
                        }
                        // Trả về boolean: Ép trả về giá trị false (0x0)
                        "Z" -> {
                            method.addInstructions(0, "const/4 v0, 0x0")
                            method.addInstructions(1, "return v0")
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}