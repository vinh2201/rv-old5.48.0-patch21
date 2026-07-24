package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import org.w3c.dom.Element

// Định nghĩa fingerprint quét chuỗi nhận diện độc quyền của tiến trình check update
internal val inAppUpdateStringFingerprint = fingerprint {
    strings("InAppUpdater#checkUpdateAvailability")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables Messenger in-app update checks precisely via targeted bytecode fingerprinting.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Định vị chính xác phương thức qua Fingerprint để vô hiệu hóa logic ngầm bên trong
        val targetMethod = inAppUpdateStringFingerprint.method.toMutable()

        when (targetMethod.returnType) {
            "V" -> {
                targetMethod.replaceInstruction(0, "return-void")
            }
            "Z" -> {
                targetMethod.replaceInstruction(0, "const/4 v0, 0x0")
                targetMethod.replaceInstruction(1, "return v0")
            }
        }
    }
}