package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disabe in-app update notification in Facebook Messenger.",
) {
    compatibleWith("com.facebook.orca")

    apply {
        // Tìm trực tiếp class InAppUpdater dựa trên đường dẫn không bị obfuscate
        val targetClass = classes.find { it.name == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
        
        // Tìm hàm khởi tạo (<init>) bên trong class đó
        val targetMethod = targetClass?.methods?.find { it.name == "<init>" }

        // Index 0 giữ lại super.<init>(), Index 1 chèn return-void để ngắt toàn bộ chuỗi check update
        targetMethod?.replaceInstruction(1, "return-void")
    }
}