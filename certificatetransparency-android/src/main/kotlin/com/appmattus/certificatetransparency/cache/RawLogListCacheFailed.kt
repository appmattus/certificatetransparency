package com.appmattus.certificatetransparency.cache

import com.appmattus.certificatetransparency.loglist.RawLogListResult

internal object RawLogListCacheFailedJsonTooBig : RawLogListResult.Failure() {
    override fun toString() = "Cache contains too large log-list.json file"
}

internal object RawLogListCacheFailedSigTooBig : RawLogListResult.Failure() {
    override fun toString() = "Cache contains too large log-list.sig file"
}
