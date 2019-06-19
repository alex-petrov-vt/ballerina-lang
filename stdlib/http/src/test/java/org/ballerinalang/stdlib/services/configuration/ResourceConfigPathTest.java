/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.configuration;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test case for http:resourceConfig path field.
 *
 * @since 0.995.0
 */
public class ResourceConfigPathTest {

    @Test
    public void testResourceConfigPathAnnotationsNegativeCases() {
        CompileResult compileResult = BCompileUtil
                .compile("test-src/services/configuration/resource-config-path-field.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag.length, 11);
        assertResponse(diag[0], "Illegal closing brace detected in resource path config", 12);
        assertResponse(diag[1], "Illegal closing brace detected in resource path config", 19);
        assertResponse(diag[2], "Incomplete path param expression", 26);
        assertResponse(diag[3], "Incomplete path param expression", 34);
        assertResponse(diag[4], "Invalid param expression in resource path config", 42);
        assertResponse(diag[5], "Invalid param expression in resource path config", 50);
        assertResponse(diag[6], "Illegal expression in resource path config", 58);
        assertResponse(diag[7], "Illegal closing brace detected in resource path config", 66);
        assertResponse(diag[8], "Illegal closing brace detected in resource path config", 74);
        assertResponse(diag[9], "Illegal open brace character in resource path config", 82);
        assertResponse(diag[10], "Illegal expression in resource path config", 90);
    }

    @Test
    public void testPathParamAndSignatureParamMatch() {
        CompileResult compileResult = BCompileUtil
                .compile("test-src/services/configuration/resource-arg--pathparam-match.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag.length, 1);
        assertResponse(diag[0], "Illegal closing brace detected in resource path config", 12);

    }

    private void assertResponse(Diagnostic diag, String msg, int line) {
        Assert.assertEquals(diag.getMessage(), msg);
        Assert.assertEquals(diag.getPosition().getEndLine(), line);
    }
}
