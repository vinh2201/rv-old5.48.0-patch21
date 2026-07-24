package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import java.io.File

internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    custom { method, _ ->
        method.name == "<init>" &&
        method.definingClass ==
            "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disable in-app update."
) {
    compatibleWith("com.facebook.orca")

    execute {
        val ctor = inAppUpdaterConstructorFingerprint.methodOrNull
            ?: return@execute

        // Không sửa gì.
        // Chỉ verify fingerprint trước.
    }
}