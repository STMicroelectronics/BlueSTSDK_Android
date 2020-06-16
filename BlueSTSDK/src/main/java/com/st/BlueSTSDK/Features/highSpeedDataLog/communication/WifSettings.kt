package com.st.BlueSTSDK.Features.highSpeedDataLog.communication

data class WifSettings(
        val enable:Boolean,
        val ssid:CharSequence?,
        val password:CharSequence?
)