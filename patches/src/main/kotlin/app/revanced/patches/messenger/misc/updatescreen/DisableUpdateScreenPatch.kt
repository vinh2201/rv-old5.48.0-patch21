package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Không cần dùng chuỗi dài ngoằng, chúng ta chĩa thẳng vào tên class của Activity
@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Forces the upgrade blocker activity to finish immediately.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // Tên class chính xác của cái UI chặn cuộc gọi
        val targetActivityClass = "Lcom/facebook/rtc/activities/upgradepolicy/msgr/MsgrRUPBlockActivity;"
        
        // Tìm class này trong bộ mã Smali
        val activityClass = classes.firstOrNull { it.type == targetActivityClass }
        
        if (activityClass != null) {
            // Tìm hàm onCreate của Activity này
            val onCreateMethod = activityClass.methods.firstOrNull { it.name == "onCreate" }?.toMutable()
            
            if (onCreateMethod != null) {
                // Chèn lệnh gọi hàm finish() ngay dòng đầu tiên của onCreate
                // v0 = p0 (biến this của Activity), gọi this.finish() sau đó return void
                onCreateMethod.addInstructions(
                    0,
                    """
                    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                    return-void
                    """
                )
            }
        }
    }
}