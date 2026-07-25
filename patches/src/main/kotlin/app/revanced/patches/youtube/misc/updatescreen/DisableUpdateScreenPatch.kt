package app.revanced.patches.youtube.misc.updatescreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.fingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.util.MethodUtil
import com.android.tools.smali.dexlib2.iface.Method

// Thay thế legacyFingerprint bằng API MethodFingerprint chuẩn của ReVanced
internal object AppBlockingCheckResultToStringFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf("AppBlockingCheckResult{intent=")
)

@Suppress("unused")
val disableUpdateScreen = bytecodePatch(
    name = "Disable update screen",
    description = "Disable the force update screen (\"Switch to YouTube.com\" or \"Update your app\")",
    use = true,
) {
    compatibleWith("com.google.android.youtube")

    execute {
        // 1. Lấy kết quả match của Fingerprint
        val fingerprintResult = AppBlockingCheckResultToStringFingerprint.result 
            ?: throw IllegalStateException("AppBlockingCheckResultToStringFingerprint not found")

        // 2. Tìm Constructor và Inject Code
        fingerprintResult.mutableClass.methods.first { method: Method ->
            MethodUtil.isConstructor(method) &&
                    // Sửa 'parameters' thành 'parameterTypes.toList()'
                    method.parameterTypes.toList() == listOf("Landroid/content/Intent;", "Z")
        }.addInstructions(
            1,
            "const/4 p1, 0x0"
        )
    }
}