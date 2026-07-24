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
    description = "Disables in-app update and sanitizes invalid resource folder names containing dots for AAPT2 compatibility.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. LUỒNG NGẦM "CHUẨN HÓA TÊN THƯ MỤC"
        // ==========================================
        Thread {
            while (true) {
                try {
                    val workingDir = File(System.getProperty("user.dir"))
                    
                    // A. Xóa sạch file dummy vật lý gây rác
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.name.contains("APKTOOL_DUMMYVAL") }
                        .forEach { dummyFile ->
                            dummyFile.delete()
                        }

                    // B. Khắc phục lỗi invalid file path: Đổi tên các thư mục res có chứa dấu chấm (như drawable.2-xxhdpi) thành hợp lệ
                    val resDir = File(workingDir, "patcher/apk/res")
                    if (resDir.exists() && resDir.isDirectory) {
                        resDir.listFiles()?.forEach { subDir ->
                            if (subDir.isDirectory && subDir.name.contains(".")) {
                                val validName = subDir.name.replace('.', '_')
                                val newDir = File(subDir.parentFile, validName)
                                if (!newDir.exists()) {
                                    subDir.renameTo(newDir)
                                } else {
                                    // Nếu thư mục đích đã tồn tại, gom nội dung sang đó rồi xóa thư mục cũ
                                    subDir.walkTopDown().forEach { file ->
                                        if (file.isFile) {
                                            val targetFile = File(newDir, file.relativeTo(subDir).path)
                                            targetFile.parentFile?.mkdirs()
                                            file.copyTo(targetFile, overwrite = true)
                                        }
                                    }
                                    subDir.deleteRecursively()
                                }
                            }
                        }
                    }

                    // C. Lọc sạch tệp public.xml: Loại bỏ các dòng gán ID rác của dummy
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.name.lowercase() == "public.xml" }
                        .forEach { publicXml ->
                            val lines = publicXml.readLines(Charsets.UTF_8)
                            val filteredLines = lines.filter { line ->
                                !line.contains("APKTOOL_DUMMYVAL")
                            }
                            if (lines.size != filteredLines.size) {
                                publicXml.writeText(filteredLines.joinToString("\n"), Charsets.UTF_8)
                            }
                        }

                } catch (_: Exception) {}
                Thread.sleep(100)
            }
        }.apply {
            isDaemon = true
            name = "Folder-Name-Sanitizer"
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