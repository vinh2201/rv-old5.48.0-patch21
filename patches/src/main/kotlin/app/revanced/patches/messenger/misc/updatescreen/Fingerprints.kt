package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.fingerprint

internal val findUpdateStringFingerprint = fingerprint {
    strings("rtc_upgrade_policy_deprecated_version")
}
