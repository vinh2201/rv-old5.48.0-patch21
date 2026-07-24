package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    custom { method, classDef ->
        method.name == "<init>" &&
        classDef.type == "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check mechanism.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Gọi trực tiếp extension hệt như cách patch ẩn quảng cáo hoạt động
        inAppUpdaterConstructorFingerprint.methodOrNull?.replaceInstruction(1, "return-void")
    }
}