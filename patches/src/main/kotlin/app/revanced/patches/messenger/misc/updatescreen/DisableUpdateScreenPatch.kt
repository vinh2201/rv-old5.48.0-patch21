package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.fingerprint

// 1. Chộp lấy các hàm check version và cờ chặn cốt lõi
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
    description = "Forces upgrade check flags to return false and kills upgrade blockers instantly.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // --- MŨI 1: Ép toàn bộ các hàm kiểm tra update trả về false (0) ---
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

        // --- MŨI 2: Đấm chết tươi Activity MsgrRUPBlockActivity nếu nó cố lú đầu lên ---
        val targetActivityClass = "Lcom/facebook/rtc/activities/upgradepolicy/msgr/MsgrRUPBlockActivity;"
        val activityClass = classes.firstOrNull { it.type == targetActivityClass }
        
        if (activityClass != null) {
            val onCreateMethod = activityClass.methods.firstOrNull { it.name == "onCreate" }?.toMutable()
            onCreateMethod?.addInstructions(
                0,
                """
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
                """
            )
        }
    }
}