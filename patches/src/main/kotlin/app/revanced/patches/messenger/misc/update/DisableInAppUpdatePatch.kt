package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import java.io.File

internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    custom { method, classDef ->
        method.name == "<init>" &&
        classDef.type == "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check and sanitizes Meta resource traps for AAPT2 compatibility.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // 1. Thu hồi và chuẩn hóa các thư mục bẫy .2 để AAPT2 biên dịch an toàn
        try {
            val baseDir = File(".")
            val trapDirs = baseDir.walkTopDown()
                .filter { it.isDirectory && it.name.contains(".2") }
                .toList()

            for (trapDir in trapDirs) {
                val parentDir = trapDir.parentFile ?: continue
                // Tạo tên chuẩn bằng cách loại bỏ ".2" (Ví dụ: drawable.2-xxhdpi -> drawable-xxhdpi)
                val cleanName = trapDir.name.replace(".2", "")
                val targetDir = File(parentDir, cleanName)

                if (!targetDir.exists()) {
                    // Nếu thư mục đích chưa có, đổi tên trực tiếp thư mục bẫy thành tên chuẩn
                    trapDir.renameTo(targetDir)
                } else {
                    // Nếu thư mục chuẩn đã tồn tại, tiến hành gộp (merge) toàn bộ file bên trong sang thư mục chuẩn
                    trapDir.walkTopDown().filter { it.isFile }.forEach { file ->
                        val relativePath = file.toRelativeString(trapDir)
                        val destFile = File(targetDir, relativePath)
                        if (!destFile.exists()) {
                            file.copyTo(destFile, overwrite = true)
                        }
                    }
                    // Dọn sạch thư mục bẫy sau khi đã chuyển dữ liệu xong
                    trapDir.deleteRecursively()
                }
            }
        } catch (_: Exception) {
            // Đảm bảo không làm gián đoạn luồng chính nếu có ngoại lệ I/O
        }

        // 2. Vô hiệu hóa InAppUpdater ở tầng Bytecode
        try {
            val targetMethod = inAppUpdaterConstructorFingerprint.methodOrNull
            if (targetMethod != null) {
                targetMethod.replaceInstruction(1, "return-void")
            }
        } catch (_: Exception) {}
    }
}