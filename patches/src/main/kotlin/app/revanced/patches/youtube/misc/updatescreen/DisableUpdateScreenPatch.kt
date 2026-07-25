package app.revanced.patches.youtube.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.fingerprint.legacyFingerprint
import app.revanced.patcher.fingerprint.mutableClassOrThrow
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
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
        // Dùng contains để quét linh hoạt, khắc phục triệt để việc đổi cấu trúc package ở bản fork lạ
        val targetClass = classes.firstOrNull { clazz ->
            clazz.methods.any { method ->
                method.returnType == "Ljava/lang/String;" &&
                method.implementation?.instructions?.any { instr ->
                    if (instr is ReferenceInstruction) {
                        val ref = instr.reference
                        ref is StringReference && ref.string.contains("AppBlockingCheckResult{intent=")
                    } else {
                        false
                    }
                } == true
            }
        } ?: throw IllegalStateException("Không tìm thấy class để Disable Update Screen")

        // Tìm đúng Constructor và chèn mã vô hiệu hóa màn hình cập nhật
        targetClass.methods.first { method ->
            MethodUtil.isConstructor(method) &&
            method.parameterTypes.map { it.toString() } == listOf("Landroid/content/Intent;", "Z")
        }.addInstructions(
            1,
            "const/4 p1, 0x0"
        )
    }
}