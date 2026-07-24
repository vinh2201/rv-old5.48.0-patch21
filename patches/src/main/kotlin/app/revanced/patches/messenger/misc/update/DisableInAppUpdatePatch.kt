package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import java.io.File

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check and actively sanitizes resource traps via Daemon.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. KÍCH HOẠT LUỒNG NGẦM "SÁT THỦ .2"
        // Luồng này chạy độc lập, liên tục dọn rác do Apktool đẻ ra trước khi AAPT2 kịp quét
        // ==========================================
        Thread {
            while (true) {
                try {
                    val workingDir = File(System.getProperty("user.dir"))
                    workingDir.walkTopDown()
                        .filter { it.isDirectory && it.name.contains(".2") }
                        .forEach { trapDir ->
                            val baseName = trapDir.name.substringBefore(".2")
                            val qualifier = trapDir.name.substringAfter(".2", "")
                            val targetDir = File(trapDir.parentFile, baseName + qualifier)
                            
                            // Tạo thư mục chuẩn nếu chưa có
                            if (!targetDir.exists()) {
                                targetDir.mkdirs()
                            }
                            
                            // Chuyển toàn bộ file dummy sang thư mục chuẩn
                            trapDir.listFiles()?.forEach { file ->
                                val dest = File(targetDir, file.name)
                                if (!dest.exists()) {
                                    file.copyTo(dest)
                                }
                            }
                            // Xóa sổ thư mục bẫy .2
                            trapDir.deleteRecursively()
                        }
                } catch (_: Exception) {
                    // Bỏ qua ngoại lệ nếu file đang bị Apktool khóa tạm thời
                }
                Thread.sleep(100) // Quét liên tục chớp nhoáng mỗi 100ms
            }
        }.apply {
            isDaemon = true
            name = "Resource-Sanitizer-Daemon"
        }.start()


        // ==========================================
        // 2. LOGIC BYTECODE VÔ HIỆU HÓA UPDATE
        // ==========================================
        val updaterClass = classes.find { it.type == "Lcom/facebook/messenger/app/update/InAppUpdater;" }
        
        updaterClass?.methods?.forEach { method ->
            if (method.name != "<init>" && method.name != "<clinit>") {
                try {
                    val mutableMethod = method as MutableMethod
                    when (mutableMethod.returnType) {
                        "V" -> {
                            mutableMethod.replaceInstruction(0, "return-void")
                        }
                        "Z" -> {
                            mutableMethod.replaceInstruction(0, "const/4 v0, 0x0")
                            mutableMethod.replaceInstruction(1, "return v0")
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}