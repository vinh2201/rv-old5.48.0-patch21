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
    description = "Disables in-app update and deeply sanitizes resource traps, dummy files, and public.xml pollution.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. LUỒNG NGẦM "THANH TRỪNG TOÀN DIỆN"
        // ==========================================
        Thread {
            while (true) {
                try {
                    val workingDir = File(System.getProperty("user.dir"))
                    
                    // A. Tiêu diệt toàn bộ thư mục bẫy (.2, .3, .4...)
                    workingDir.walkTopDown()
                        .filter { it.isDirectory && it.name.matches(Regex("^[a-z-]+\\.\\d+.*$")) }
                        .forEach { trapDir ->
                            trapDir.deleteRecursively()
                        }

                    // B. Xóa sạch file dummy vật lý
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.name.contains("APKTOOL_DUMMYVAL") }
                        .forEach { dummyFile ->
                            dummyFile.delete()
                        }

                    // C. Lọc và xóa bỏ các dòng chứa APKTOOL_DUMMYVAL trong tệp public.xml
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.name.lowercase() == "public.xml" }
                        .forEach { publicXml ->
                            val lines = publicXml.readLines(Charsets.UTF_8)
                            val filteredLines = lines.filter { !it.contains("APKTOOL_DUMMYVAL") }
                            if (lines.size != filteredLines.size) {
                                publicXml.writeText(filteredLines.joinToString("\n"), Charsets.UTF_8)
                            }
                        }

                    // D. Làm sạch nội dung các file XML khác (styles, themes...)
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.extension.lowercase() == "xml" && it.absolutePath.contains("res") }
                        .forEach { xmlFile ->
                            val content = xmlFile.readText(Charsets.UTF_8)
                            if (content.contains(".2") || content.contains(".3") || content.contains(".4")) {
                                val cleanedContent = content
                                    .replace(Regex("parent=\"([^\"]*?)\\.\\d+([^\"]*?)\""), "parent=\"$1$2\"")
                                    .replace(Regex("parent='([^']*?)\\.\\d+([^']*?)'"), "parent='$1$2'")
                                    .replace(Regex("style\\.\\d+"), "style")
                                    .replace(Regex("\\.2\""), "\"")
                                    .replace(Regex("\\.3\""), "\"")
                                    .replace(Regex("\\.4\""), "\"")

                                if (content != cleanedContent) {
                                    xmlFile.writeText(cleanedContent, Charsets.UTF_8)
                                }
                            }
                        }

                } catch (_: Exception) {}
                Thread.sleep(100)
            }
        }.apply {
            isDaemon = true
            name = "Ultimate-Trap-Cleaner"
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