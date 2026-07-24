package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch

// Khai báo Fingerprint để tìm hàm khởi tạo (<init>) của class InAppUpdater
internal val BytecodePatchContext.inAppUpdaterConstructor by gettingFirstMethodDeclaratively {
    definingClass("Lcom/facebook/messenger/app/update/InAppUpdater;")
    name("<init>")
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disabe in-app update notification in Facebook Messenger.",
) {
    compatibleWith("com.facebook.orca")

    apply {
        // Index 0: Giữ nguyên lệnh gọi super.<init>() để tuân thủ quy tắc bytecode của Android
        // Index 1: Chèn lệnh return-void để ngắt toàn bộ chuỗi khởi tạo updater phía sau
        inAppUpdaterConstructor.replaceInstruction(1, "return-void")
    }
}