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
    description = "Disables in-app update and exterminates all .2/.3/.4 resource traps and XML parent pollution.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. LUỒNG NGẦM "DIỆT TẬN GỐC ĐA VŨ TRỤ .2 .3 .4"
        // ==========================================
        Thread {
            while (true) {
                try {
                    val workingDir = File(System.getProperty("user.dir"))
                    
                    // A. Gộp và xóa sạch các thư mục rác (anim.2, drawable.3, values.2...)
                    workingDir.walkTopDown()
                        .filter { it.isDirectory && it.name.matches(Regex("^[a-z-]+\\.\\d+.*$")) }
                        .forEach { trapDir ->
                            val match = Regex("^([a-z-]+)\\.\\d+(.*)$").find(trapDir.name)
                            if (match != null) {
                                val baseName = match.groupValues[1]
                                val qualifier = match.groupValues[2]
                                val targetDir = File(trapDir.parentFile, baseName + qualifier)
                                
                                if (!targetDir.exists()) {
                                    targetDir.mkdirs()
                                }
                                trapDir.listFiles()?.forEach { file ->
                                    val dest = File(targetDir, file.name)
                                    if (!dest.exists()) {
                                        file.copyTo(dest)
                                    }
                                }
                                trapDir.deleteRecursively()
                            }
                        }

                    // B. Phẫu thuật chuyên sâu bên trong mọi tệp XML (Xử lý tận gốc style.2, parent="..." có dính số)
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.extension.lowercase() == "xml" && it.absolutePath.contains("res") }
                        .forEach { xmlFile ->
                            val content = xmlFile.readText(Charsets.UTF_8)
                            if (content.contains(".2") || content.contains(".3") || content.contains(".4")) {
                                val cleanedContent = content
                                    // Xử lý dứt điểm các parent bị dính đuôi .2, .3, .4 (Ví dụ: parent="Theme.Messenger.2" -> parent="Theme.Messenger")
                                    .replace(Regex("parent=\"([^\"]*?)\\.\\d+([^\"]*?)\""), "parent=\"$1$2\"")
                                    .replace(Regex("parent='([^']*?)\\.\\d+([^']*?)'"), "parent='$1$2'")
                                    // Xử lý tổng quát các thẻ style hoặc tên bị dính số
                                    .replace(Regex("style\\.\\d+"), "style")
                                    .replace(Regex("\\.2\""), "\"")
                                    .replace(Regex("\\.3\""), "\"")
                                    .replace(Regex("\\.4\""), "\"")

                                if (content != cleanedContent) {
                                    xmlFile.writeText(cleanedContent, Charsets.UTF_8)
                                }
                            }
                        }

                } catch (_: Exception) {
                    // Tránh crash tiến trình khi file đang bị khóa tạm thời
                }
                Thread.sleep(100)
            }
        }.apply {
            isDaemon = true
            name = "Meta-Traps-Exterminator"
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