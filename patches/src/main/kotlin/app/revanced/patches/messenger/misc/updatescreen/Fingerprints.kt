package app.revanced.patches.messenger.misc.updatescreen

import app.revanced.patcher.fingerprint

internal val findInAppUpdaterFingerprint = fingerprint {
    strings("InAppUpdater#checkUpdateAvailability")
}
