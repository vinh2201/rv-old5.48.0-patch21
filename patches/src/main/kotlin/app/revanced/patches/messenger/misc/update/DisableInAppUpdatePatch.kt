package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

// Using old fingerprint structures to find constructors
internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    custom { method, _ ->
        method.name == "<init>" &&
        method.definingClass == "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disable in-app update notification in Facebook Messenger app.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // 0 to call super.<init>() force system use.
        // 1 to add return-void to break the entire check update string.
        inAppUpdaterConstructorFingerprint.methodOrNull?.replaceInstruction(1, "return-void")
    }
}