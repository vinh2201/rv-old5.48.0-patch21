package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
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
    description = "Disables Messenger in-app update checks, call upgrade screens, and Armadillo upgrade blockers.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // 1. Chặn cờ yêu cầu nâng cấp phiên bản chung (versionUpgradeRequired)
        val versionUpgradeMethod = versionUpgradeRequiredFingerprint.method.toMutable()
        when (versionUpgradeMethod.returnType) {
            "V" -> versionUpgradeMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                versionUpgradeMethod.replaceInstruction(0, "const/4 v0, 0x0")
                versionUpgradeMethod.replaceInstruction(1, "return v0")
            }
        }

        // 2. Chặn màn hình khóa/chặn nâng cấp Armadillo (E2EE chats)
        val armadilloBlockerMethod = armadilloUpgradeBlockerFingerprint.method.toMutable()
        when (armadilloBlockerMethod.returnType) {
            "V" -> armadilloBlockerMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                armadilloBlockerMethod.replaceInstruction(0, "const/4 v0, 0x0")
                armadilloBlockerMethod.replaceInstruction(1, "return v0")
            }
        }

        // 3. Chặn kiểm tra phiên bản nâng cấp trong giao diện gọi thoại/videocall (Lobby)
        val linkUpgradeMethod = linkUpgradeVersionFingerprint.method.toMutable()
        when (linkUpgradeMethod.returnType) {
            "V" -> linkUpgradeMethod.replaceInstruction(0, "return-void")
            "Z" -> {
                linkUpgradeMethod.replaceInstruction(0, "const/4 v0, 0x0")
                linkUpgradeMethod.replaceInstruction(1, "return v0")
            }
        }
    }
}