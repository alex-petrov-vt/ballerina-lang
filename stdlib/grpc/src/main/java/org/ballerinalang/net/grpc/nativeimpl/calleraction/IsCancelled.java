/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.calleraction;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.StreamObserver;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;

/**
 * Extern function to check whether caller has terminated the connection in between.
 *
 * @since 1.0.0
 */
public class IsCancelled {

    public static boolean externIsCancelled(ObjectValue endpointClient) {
        StreamObserver responseObserver = MessageUtils.getResponseObserver(endpointClient);

        if (responseObserver instanceof ServerCallHandler.ServerCallStreamObserver) {
            ServerCallHandler.ServerCallStreamObserver serverCallStreamObserver = (ServerCallHandler
                    .ServerCallStreamObserver) responseObserver;
            return serverCallStreamObserver.isCancelled();
        } else {
            return Boolean.FALSE;
        }
    }
}
