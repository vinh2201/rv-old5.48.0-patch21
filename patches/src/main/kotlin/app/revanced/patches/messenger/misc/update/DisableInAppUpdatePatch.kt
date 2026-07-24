package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables Messenger in-app update checks dynamically across matching updater classes via pure bytecode modification.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Quét toàn bộ các lớp có chứa từ khóa InAppUpdater hoặc UpdateChecker để đảm bảo không bị sót
        val targetClasses = classes.filter { 
            it.type.contains("InAppUpdater", ignoreCase = true) || 
            it.type.contains("UpdateChecker", ignoreCase = true) 
        }

        if (targetClasses.isEmpty()) {
            throw IllegalStateException("Target update classes not found.")
        }

        targetClasses.forEach { classDef ->
            classDef.methods.forEach { method ->
                if (method.name != "<init>" && method.name != "<clinit>") {
                    try {
                        val mutableMethod = method as MutableMethod
                        when (mutableMethod.returnType) {
                            "V" -> {
                                mutableMethod.replaceInstruction(0, "return-void")
                            }
                            "Z" -> {
                                mutableMethod.replaceInstruction(0, "const/4 v0, 0x0")
                                mutableMethod.replaceInstruction(1, "return v0")
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }
}