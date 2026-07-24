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
    description = "Disables in-app update and synchronously sanitizes resource directories and public.xml before compilation.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. XỬ LÝ TRỰC TIẾP TRƯỚC KHI AAPT2 BIÊN DỊCH
        // ==========================================
        try {
            val workingDir = File(System.getProperty("user.dir"))

            // A. Xóa sạch file dummy vật lý gây rác
            workingDir.walkTopDown()
                .filter { it.isFile && it.name.contains("APKTOOL_DUMMYVAL") }
                .forEach { it.delete() }

            // B. Quét và đổi tên toàn bộ thư mục res chứa dấu chấm (vd: drawable.2-xxhdpi -> drawable_2_xxhdpi)
            val resDir = File(workingDir, "patcher/apk/res")
            if (resDir.exists() && resDir.isDirectory) {
                resDir.listFiles()?.forEach { subDir ->
                    if (subDir.isDirectory && subDir.name.contains(".")) {
                        val validName = subDir.name.replace('.', '_')
                        val newDir = File(subDir.parentFile, validName)
                        if (!newDir.exists()) {
                            subDir.renameTo(newDir)
                        } else {
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

            // C. Làm sạch tệp public.xml ngay lập tức
            workingDir.walkTopDown()
                .filter { it.isFile && it.name.lowercase() == "public.xml" }
                .forEach { publicXml ->
                    val lines = publicXml.readLines(Charsets.UTF_8)
                    val filteredLines = lines.filter { !it.contains("APKTOOL_DUMMYVAL") }
                    if (lines.size != filteredLines.size) {
                        publicXml.writeText(filteredLines.joinToString("\n"), Charsets.UTF_8)
                    }
                }
        } catch (_: Exception) {}

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