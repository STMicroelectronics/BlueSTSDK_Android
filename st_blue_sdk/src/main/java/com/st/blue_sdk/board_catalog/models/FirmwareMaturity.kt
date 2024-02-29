package com.st.blue_sdk.board_catalog.models

enum class FirmwareMaturity {
    DRAFT,   //Entry added to catalog
    BETA,    // Release Candidate
    RELEASE, // Release
    DEMO,    // Internal code
    CUSTOM,  // fwId == 0xFF
    SPECIAL  // Default
}