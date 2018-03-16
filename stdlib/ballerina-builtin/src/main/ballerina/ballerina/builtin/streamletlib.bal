// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package ballerina.builtin;

@Description {value:"Stops the streamlet runtime"}
@Param {value:"s: The streamlet runtime which needs to be stop"}
public native function <streamlet s> stop ();

@Description {value:"Creates the streamlet runtime"}
@Param {value:"streamletType: The streamlet runtime type which needs to be created"}
@Param {value:"inStreamRefs: References of the input streams in the streamlet"}
@Param {value:"inTableRefs: References of the input tables in the streamlet"}
@Param {value:"outStreamRefs: References of the output streams in the streamlet"}
@Param {value:"outTableRefs: References of the output tables in the streamlet"}
public native function startStreamlet (streamlet streamletRef, any inStreamRefs, any inTableRefs, any outStreamRefs,
        any outTableRefs) (streamlet);
