package app.revanced.patches.messenger.misc.update

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

internal val inAppUpdaterConstructorFingerprint = fingerprint {
    returns("V")
    // Bổ sung opcodes để ngăn Patcher quét toàn bộ APK gây lỗi ngầm
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_STATIC
    )
    custom { method, _ ->
        method.name == "<init>" &&
        method.definingClass == "Lcom/facebook/messenger/app/update/InAppUpdater;"
    }
}

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    // Dùng tiếng Anh để tránh lỗi decode UTF-8 trên Windows CMD
    description = "Disables the in-app update check mechanism.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        inAppUpdaterConstructorFingerprint.methodOrNull?.replaceInstruction(1, "return-void")
    }
}