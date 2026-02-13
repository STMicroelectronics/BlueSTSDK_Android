package com.st.blue_sdk.features.extended.odometry

import com.st.blue_sdk.features.FeatureCommand

class OdometryResetCommand(feature: Odometry, val payload : ByteArray) :
    FeatureCommand(feature = feature, commandId = Odometry.COMMAND_ODOMETER_RESET)

