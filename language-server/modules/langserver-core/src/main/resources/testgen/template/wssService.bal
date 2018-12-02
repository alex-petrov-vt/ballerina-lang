@test:Config
function ${testServiceFunctionName} () {
    http:WebSocketClient wsEndpoint = new(
        ${serviceUriStrName},
        config = {
            callbackService: ${callbackServiceName},
            secureSocket: {
                trustStore: {
                    path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                }
            },
            readyOnConnect: false
    });
    //Send a message
    _ = wsEndpoint->pushText("hey");
}

service ${callbackServiceName} = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient ${callbackServiceName}Ep, string text) {
        //Test received message
        test:assertEquals(text, "hey", msg = "Received message should be equal to the expected message");
    }
};
