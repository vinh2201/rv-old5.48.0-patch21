package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import org.w3c.dom.Element

// Định nghĩa fingerprint quét chuỗi nhận diện độc quyền của tiến trình check update
internal val inAppUpdateStringFingerprint = fingerprint {
    strings("InAppUpdater#checkUpdateAvailability")
}

@Suppress("unused")
val disableInAppUpdatePatch = patch(
    name = "Disable in-app update",
    description = "Disables Messenger in-app updates comprehensively via safe manifest DOM modification and precise bytecode fingerprinting.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // 1. Tầng Manifest: Sử dụng DOM thuần túy (chuẩn an toàn Morphe) để vô hiệu hóa component, tránh hoàn toàn rác APKTOOL_DUMMYVAL
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application")
                .item(0) as? Element ?: return@use

            val children = application.childNodes
            for (i in 0 until children.length) {
                val child = children.item(i) as? Element ?: continue
                val name = child.getAttribute("android:name") ?: continue

                if (name == "com.facebook.messenger.app.update.InAppUpdater" || name.endsWith(".app.update.InAppUpdater")) {
                    val tagName = child.tagName
                    if (tagName == "receiver" || tagName == "service" || tagName == "activity" || tagName == "provider") {
                        child.setAttribute("android:enabled", "false")
                    }
                }
            }
        }

        // 2. Tầng Bytecode: Định vị chính xác phương thức qua Fingerprint để vô hiệu hóa logic ngầm bên trong
        val targetMethod = inAppUpdateStringFingerprint.method.toMutable()
        
        when (targetMethod.returnType) {
            "V" -> {
                targetMethod.replaceInstruction(0, "return-void")
            }
            "Z" -> {
                targetMethod.replaceInstruction(0, "const/4 v0, 0x0")
                targetMethod.replaceInstruction(1, "return v0")
            }
        }
    }
}