package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x
import com.android.tools.smali.dexlib2.mutable.MutableMethod

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disable in-app update notification in Facebook Messenger.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        for (dexFile in dexFiles) {
            for (classDef in dexFile.classes) {
                if (classDef.type == "Lcom/facebook/messenger/app/update/InAppUpdater;") {
                    val targetMethod = classDef.methods.find { it.name == "<init>" } as? MutableMethod
                    val implementation = targetMethod?.implementation

                    if (implementation != null) {
                        val instructions = implementation.instructions
                        // Index 0 giữ lại lệnh super.<init>() để tránh lỗi VerifyError
                        // Chèn return-void vào Index 1 để ngắt toàn bộ chuỗi khởi tạo updater phía sau
                        if (instructions.size > 1) {
                            instructions.add(1, BuilderInstruction10x(Opcode.RETURN_VOID))
                        }
                    }
                }
            }
        }
    }
}