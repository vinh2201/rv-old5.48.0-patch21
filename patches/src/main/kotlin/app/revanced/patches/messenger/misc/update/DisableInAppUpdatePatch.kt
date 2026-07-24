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
    description = "Disables Messenger in-app update checks precisely via pure bytecode modification.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        val updaterClass = classes.find { it.type == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
            ?: throw IllegalStateException("InAppUpdater class not found.")

        updaterClass.methods.forEach { method ->
            if (method.name != "<init>" && method.name != "<clinit>") {
                try {
                    val mutableMethod = method.toMutable()
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