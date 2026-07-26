package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable

internal val inAppUpdateStringFingerprint = fingerprint {
    strings("InAppUpdater")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables Messenger in-app update checks.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        val targetMethod = inAppUpdateStringFingerprint.method.toMutable()
        
        when (targetMethod.returnType) {
            "V" -> targetMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                targetMethod.replaceInstruction(0, "const/4 v0, 0x0")
                targetMethod.replaceInstruction(1, "return v0")
            }
        }
    }
}