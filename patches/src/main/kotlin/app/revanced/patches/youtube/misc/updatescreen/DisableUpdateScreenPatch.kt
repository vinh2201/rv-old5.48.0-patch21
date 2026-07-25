package app.revanced.patches.youtube.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.util.MethodUtil
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val disableUpdateScreen = bytecodePatch(
    name = "Disable update screen",
    description = "Disable the force update screen (\"Switch to YouTube.com\" or \"Update your app\")",
    use = true,
) {
    compatibleWith("com.google.android.youtube")

    execute {
        // 1. Tự động tìm Class chứa chuỗi "AppBlockingCheckResult{intent=" (thay thế hoàn toàn cho Fingerprint cũ)
        val targetClass = classes.firstOrNull { clazz ->
            clazz.methods.any { method ->
                method.returnType == "Ljava/lang/String;" &&
                method.implementation?.instructions?.any { instr ->
                    if (instr is ReferenceInstruction) {
                        val ref = instr.reference
                        ref is StringReference && ref.string == "AppBlockingCheckResult{intent="
                    } else {
                        false
                    }
                } == true
            }
        } ?: throw IllegalStateException("Không tìm thấy class để Disable Update Screen")

        // 2. Tìm đúng hàm Constructor và thêm mã smali để vô hiệu hóa
        targetClass.methods.first { method: Method ->
            MethodUtil.isConstructor(method) &&
            method.parameterTypes.map { it.toString() }.toList() == listOf("Landroid/content/Intent;", "Z")
        }.addInstructions(
            1,
            "const/4 p1, 0x0"
        )
    }
}