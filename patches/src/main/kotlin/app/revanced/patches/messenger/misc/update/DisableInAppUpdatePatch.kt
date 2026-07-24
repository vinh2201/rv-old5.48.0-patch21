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
    description = "Disables the in-app update check and actively sanitizes ALL Meta resource traps (v2, v3, v4...).",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. KÍCH HOẠT LUỒNG NGẦM "SÁT THỦ REGEX"
        // Quét và tiêu diệt mọi thư mục có đuôi .2, .3, .4, .5...
        // ==========================================
        Thread {
            while (true) {
                try {
                    val workingDir = File(System.getProperty("user.dir"))
                    workingDir.walkTopDown()
                        // Regex bắt các thư mục có dấu chấm và số liền sau. Vd: drawable.3, color.4-night
                        .filter { it.isDirectory && it.name.matches(Regex("^[a-z-]+\\.\\d+.*$")) }
                        .forEach { trapDir ->
                            // Tách tên thư mục bằng Regex. 
                            // Nhóm 1: Tên gốc (drawable). Nhóm 2: Hậu tố (-mdpi) nếu có.
                            val match = Regex("^([a-z-]+)\\.\\d+(.*)$").find(trapDir.name)
                            if (match != null) {
                                val baseName = match.groupValues[1]
                                val qualifier = match.groupValues[2]
                                
                                val targetDir = File(trapDir.parentFile, baseName + qualifier)
                                
                                // Tạo thư mục chuẩn nếu chưa tồn tại
                                if (!targetDir.exists()) {
                                    targetDir.mkdirs()
                                }
                                
                                // Bế toàn bộ file sang nhà mới
                                trapDir.listFiles()?.forEach { file ->
                                    val dest = File(targetDir, file.name)
                                    if (!dest.exists()) {
                                        file.copyTo(dest)
                                    }
                                }
                                // Tiêu hủy cái bẫy
                                trapDir.deleteRecursively()
                            }
                        }
                } catch (_: Exception) {
                    // Tránh crash luồng nếu bị tranh chấp I/O
                }
                Thread.sleep(100) // Tốc độ quét 100ms/lần
            }
        }.apply {
            isDaemon = true
            name = "Resource-Sanitizer-Daemon-Pro"
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