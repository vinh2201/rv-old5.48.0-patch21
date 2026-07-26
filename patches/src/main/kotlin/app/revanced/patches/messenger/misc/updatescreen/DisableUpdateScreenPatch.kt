package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val disableInAppUpdatePatch = resourcePatch(
    name = "Disable in-app update",
    description = "Blocks the update screen during calls by disabling the MsgrRUPBlockActivity in AndroidManifest.",
) {
    compatibleWith("com.facebook.orca")

    finalize {
        document("AndroidManifest.xml").use { document ->
            // Lấy danh sách toàn bộ các thẻ <activity> trong Manifest
            val activities = document.getElementsByTagName("activity")
            
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as Element
                val activityName = activity.getAttribute("android:name")
                
                // Chỉ điểm đúng thủ phạm chặn cuộc gọi
                if (activityName == "com.facebook.rtc.activities.upgradepolicy.msgr.MsgrRUPBlockActivity") {
                    // Chốt hạ: Vô hiệu hóa hoàn toàn Activity này
                    activity.setAttribute("android:enabled", "false")
                    
                    // Ép thêm phát nữa: Rút luôn quyền gọi từ bên ngoài (nếu có)
                    if (activity.hasAttribute("android:exported")) {
                        activity.setAttribute("android:exported", "false")
                    }
                    
                    break // Tóm được rồi thì ngắt vòng lặp cho nhẹ build
                }
            }
        }
    }
}