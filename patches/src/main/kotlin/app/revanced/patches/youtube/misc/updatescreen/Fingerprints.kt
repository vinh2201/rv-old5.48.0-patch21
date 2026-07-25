package app.revanced.patches.youtube.misc.updatescreen

import app.revanced.util.fingerprint.legacyFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import app.revanced.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference


internal val appBlockingCheckResultToStringFingerprint = legacyFingerprint(
    name = "appBlockingCheckResultToStringFingerprint",
    returnType = "Ljava/lang/String;",
    strings = listOf("AppBlockingCheckResult{intent=")
)
