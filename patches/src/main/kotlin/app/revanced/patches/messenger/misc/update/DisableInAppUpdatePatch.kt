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
    description = "Disables in-app update and safely neutralizes resource traps without deleting physical resource directories.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        // ==========================================
        // 1. LUỒNG NGẦM "DỌN RÁC AN TOÀN"
        // ==========================================
        Thread {
            while (true) {
                try {
                    val workingDir = File(System.getProperty("user.dir"))
                    
                    // A. CHỈ xóa các file dummy vật lý chứa chữ APKTOOL_DUMMYVAL, TUYỆT ĐỐI KHÔNG xóa thư mục phân mảnh (.2, .3...) để tránh lỗi not found
                    workingDir.walkTopDown()
                        .filter { it.isFile && it.name.contains("APKTOOL_DUMMYVAL") }
                        .forEach { dummyFile ->
                            dummyFile.delete()
                        }

                    // B. Lọc sạch tệp public.xml: Chỉ loại bỏ các dòng gán ID rác của dummy
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
            name = "Safe-Resource-Sanitizer"
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