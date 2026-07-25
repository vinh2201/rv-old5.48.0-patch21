package app.revanced.patches.youtube.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.util.MethodUtil
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
        // 1. Tìm class và ép kiểu nó sang MutableClass (Class có thể chỉnh sửa)
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
        } as? MutableClass ?: throw IllegalStateException("Không tìm thấy class để Disable Update Screen")

        // 2. Tìm Method và ép kiểu sang MutableMethod để dùng được hàm addInstructions
        val methodToPatch = targetClass.methods.first { method ->
            MethodUtil.isConstructor(method) &&
            method.parameterTypes.map { it.toString() }.toList() == listOf("Landroid/content/Intent;", "Z")
        } as MutableMethod

        // 3. Tiến hành chèn mã smali vô hiệu hóa
        methodToPatch.addInstructions(
            1,
            "const/4 p1, 0x0"
        )
    }
}