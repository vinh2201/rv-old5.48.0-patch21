package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable

internal val versionUpgradeRequiredFingerprint = fingerprint {
    strings("Setting versionUpgradeRequired = ")
}

internal val armadilloUpgradeBlockerFingerprint = fingerprint {
    strings("armadillo_app_upgrade_screen_blocker")
}

internal val linkUpgradeVersionFingerprint = fingerprint {
    strings("link_upgrade_version")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Forces update checks and upgrade blockers to return false immediately.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        val targets = listOf(
            versionUpgradeRequiredFingerprint,
            armadilloUpgradeBlockerFingerprint,
            linkUpgradeVersionFingerprint
        )

        for (target in targets) {
            val method = target.method.toMutable()
            when (method.returnType) {
                "V" -> method.addInstructions(0, "return-void")
                "Z", "I" -> {
                    method.addInstructions(
                        0,
                        """
                        const/4 v0, 0x0
                        return v0
                        """
                    )
                }
            }
        }
    }
}