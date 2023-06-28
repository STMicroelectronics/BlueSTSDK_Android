/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models


/** State of the node  */
enum class NodeState {

    /** we open a connection with the node  */
    Connecting,

    /** we are connected with the node but services are yet to be discovered*/
    Connected,

    /** Services has been discovered but features are not yet processed*/
    ServicesDiscovered,

    /** connected with the node and services are discovered. User can use the board */
    Ready,

    /** we are closing the node connection  */
    Disconnecting,

    /** connection with the board closed  */
    Disconnected
}