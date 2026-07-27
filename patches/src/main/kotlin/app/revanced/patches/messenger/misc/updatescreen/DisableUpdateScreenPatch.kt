package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Forces the version upgrade check to return false dynamically.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        val method = findUpdateStringFingerprint.method
 
        // 1. Tìm index của chuỗi rtc_upgrade_policy_deprecated_version
        val stringIndex = findUpdateStringFingerprint.stringMatches!!.first().index

        // 2. Dò ngược để tìm lệnh new-instance
        val typeRefIndex = method.indexOfFirstInstructionReversedOrThrow(stringIndex) { this.opcode == Opcode.NEW_INSTANCE }

        // 3. Lấy tham chiếu của class mục tiêu
        val targetClass = method.getInstruction<ReferenceInstruction>(typeRefIndex).reference as TypeReference

        // 4. Mở rộng vùng nhận diện (Fingerprint) thay vì fix cứng returns("I")
        val targetUpdateMethod = fingerprint {
            custom { m, classDef ->
                // Kiểm tra đúng class, hàm không có tham số và thuộc 1 trong 3 kiểu trả về
                classDef.type == targetClass.type && 
                m.parameters.isEmpty() && 
                (m.returnType == "I" || m.returnType == "Z" || m.returnType == "V")
            }
        }.method

        // 5. Cấu trúc điều kiện để patch giá trị trả về tương ứng với cấu trúc của app
        when (targetUpdateMethod.returnType) {
            "V" -> targetUpdateMethod.returnEarly()         // Void: Không trả về gì cả
            "Z", "I" -> targetUpdateMethod.returnEarly(1)   // Boolean hoặc Int: Trả về 1 (True)
            else -> throw PatchException("Kiểu trả về không được hỗ trợ: ${targetUpdateMethod.returnType}")
        }
    }
}