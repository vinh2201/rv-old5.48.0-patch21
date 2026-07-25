package app.revanced.patches.youtube.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.fingerprint.legacyFingerprint
import app.revanced.util.fingerprint.mutableClassOrThrow
import com.android.tools.smali.dexlib2.util.MethodUtil


internal val appBlockingCheckResultToStringFingerprint = legacyFingerprint(
    name = "appBlockingCheckResultToStringFingerprint",
    returnType = "Ljava/lang/String;",
    strings = listOf("AppBlockingCheckResult{intent=")
)

val disableUpdateScreen = bytecodePatch(
    name = "Disable update screen",
    description = "Disable the force update screen (\"Switch to YouTube.com\" or \"Update your app\")",
    use = true,
) {
    compatibleWith("com.google.android.youtube")

    execute {
        appBlockingCheckResultToStringFingerprint.mutableClassOrThrow().methods.first { method ->
            MethodUtil.isConstructor(method) &&
                    method.parameters == listOf("Landroid/content/Intent;", "Z")
        }.addInstructions(
            1,
            "const/4 p1, 0x0"
        )
    }
}