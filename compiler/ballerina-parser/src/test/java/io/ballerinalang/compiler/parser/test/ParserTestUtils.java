/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerinalang.compiler.internal.parser.BallerinaParser;
import io.ballerinalang.compiler.internal.parser.ParserRuleContext;
import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import org.testng.Assert;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Convenient methods for testing the parser.
 * 
 * @since 1.2.0
 */
public class ParserTestUtils {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");
    private static final String KIND_FIELD = "kind";
    private static final String CHILDREN_FIELD = "children";
    private static final String VALUE_FIELD = "value";
    private static final String IS_MISSING_FIELD = "isMissing";

    /**
     * Test parsing a valid source.
     * 
     * @param sourceFilePath Path to the ballerina file
     * @param context Context to start parsing the given source
     * @param assertFilePath File to assert the resulting tree after parsing
     */
    public static void test(Path sourceFilePath, ParserRuleContext context, Path assertFilePath) {
        try {
            String content =
                    new String(Files.readAllBytes(RESOURCE_DIRECTORY.resolve(sourceFilePath)), StandardCharsets.UTF_8);
            test(content, context, assertFilePath);
        } catch (IOException e) {
            Assert.fail();
        }
    }

    /**
     * Test parsing a valid source.
     * 
     * @param source Input source that represent a ballerina code
     * @param context Context to start parsing the given source
     * @param assertFilePath File to assert the resulting tree after parsing
     */
    public static void test(String source, ParserRuleContext context, Path assertFilePath) {
        // Parse the source
        BallerinaParser parser = new BallerinaParser(source);
        parser.parse(context);

        // Read the assertion file
        JsonObject assertJson = readAssertFile(RESOURCE_DIRECTORY.resolve(assertFilePath));

        // Validate the tree against the assertion file
        assertNode(parser.getTree(), assertJson);
    }

    private static JsonObject readAssertFile(Path filePath) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(filePath.toFile()), JsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertNode(STNode node, JsonObject json) {
        aseertNodeKind(json, node);

        if (isMissingToken(json)) {
            Assert.assertTrue(node instanceof STMissingToken,
                    "'" + node.toString().trim() + "' expected to be a STMissingToken, but found '" + node.kind + "'.");
            return;
        }

        // If the expected token is not a missing node, then validate it's content
        Assert.assertFalse(node instanceof STMissingToken);
        if (isTerminalNode(node.kind)) {
            assertTerminalNode(json, node);
        } else {
            assertNonTerminalNode(json, node);
        }
    }

    private static boolean isMissingToken(JsonObject json) {
        JsonElement isMissing = json.get(IS_MISSING_FIELD);
        return isMissing != null && isMissing.getAsBoolean();
    }

    private static void aseertNodeKind(JsonObject json, STNode node) {
        SyntaxKind expectedNodeKind = getNodeKind(json.get(KIND_FIELD).getAsString());
        SyntaxKind actualNodeKind = node.kind;
        Assert.assertEquals(actualNodeKind, expectedNodeKind, "error at node [" + node.toString() + "].");
    }

    private static void assertTerminalNode(JsonObject json, STNode node) {
        // If this is a terminal node, it has to be a STToken (i.e: lexeme)
        Assert.assertTrue(node instanceof STToken);

        // Validate the token text, if this is not a syntax token.
        // e.g: identifiers, basic-literals, etc.
        if (!isSyntaxToken(node.kind)) {
            String expectedText = json.get(VALUE_FIELD).getAsString();
            String actualText = node.toString().trim();
            Assert.assertEquals(actualText, expectedText);
        }
    }

    private static void assertNonTerminalNode(JsonObject json, STNode tree) {
        JsonArray children = json.getAsJsonArray(CHILDREN_FIELD);
        int size = children.size();
        int j = 0;

        Assert.assertEquals(getNonEmptyChildCount(tree), size, "mismatching child count for '" + tree.toString() + "'");

        for (int i = 0; i < size; i++) {
            // Skip the optional fields that are not present and get the next
            // available node.
            STNode nextChild = tree.childInBucket(j++);
            while (nextChild.kind == SyntaxKind.NONE) {
                nextChild = tree.childInBucket(j++);
            }

            // Assert the actual child node against the expected child node.
            assertNode(nextChild, (JsonObject) children.get(i));
        }
    }

    private static int getNonEmptyChildCount(STNode tree) {
        int count = 0;
        for (int i = 0; i < tree.bucketCount(); i++) {
            STNode nextChild = tree.childInBucket(i);
            if (nextChild.kind == SyntaxKind.NONE) {
                continue;
            }
            count++;
        }

        return count;
    }

    private static boolean isTerminalNode(SyntaxKind syntaxKind) {
        return SyntaxKind.FUNCTION_DEFINITION.compareTo(syntaxKind) > 0;
    }

    private static boolean isSyntaxToken(SyntaxKind syntaxKind) {
        return SyntaxKind.IDENTIFIER_TOKEN.compareTo(syntaxKind) > 0;
    }

    private static SyntaxKind getNodeKind(String kind) {
        switch (kind) {
            case "FUNCTION_DEFINITION":
                return SyntaxKind.FUNCTION_DEFINITION;
            case "PUBLIC_KEYWORD":
                return SyntaxKind.PUBLIC_KEYWORD;
            case "FUNCTION_KEYWORD":
                return SyntaxKind.FUNCTION_KEYWORD;
            case "LIST":
                return SyntaxKind.LIST;
            case "RETURN_TYPE_DESCRIPTOR":
                return SyntaxKind.RETURN_TYPE_DESCRIPTOR;
            case "RETURNS_KEYWORD":
                return SyntaxKind.RETURNS_KEYWORD;
            case "EXTERNAL_FUNCTION_BODY":
                return SyntaxKind.EXTERNAL_FUNCTION_BODY;
            case "EXTERNAL_KEYWORD":
                return SyntaxKind.EXTERNAL_KEYWORD;
            case "PARAMETER":
                return SyntaxKind.PARAMETER;

            // Operators
            case "PLUS_TOKEN":
                return SyntaxKind.PLUS_TOKEN;
            case "MINUS_TOKEN":
                return SyntaxKind.MINUS_TOKEN;
            case "ASTERISK_TOKEN":
                return SyntaxKind.ASTERISK_TOKEN;
            case "SLASH_TOKEN":
                return SyntaxKind.SLASH_TOKEN;
            case "LT_TOKEN":
                return SyntaxKind.LT_TOKEN;
            case "EQUAL_TOKEN":
                return SyntaxKind.EQUAL_TOKEN;
            case "DOUBLE_EQUAL_TOKEN":
                return SyntaxKind.DOUBLE_EQUAL_TOKEN;
            case "TRIPPLE_EQUAL_TOKEN":
                return SyntaxKind.TRIPPLE_EQUAL_TOKEN;
            case "PERCENT_TOKEN":
                return SyntaxKind.PERCENT_TOKEN;
            case "EQUAL_LT_TOKEN":
                return SyntaxKind.EQUAL_LT_TOKEN;
            case "GT_TOKEN":
                return SyntaxKind.GT_TOKEN;
            case "EQUAL_GT_TOKEN":
                return SyntaxKind.EQUAL_GT_TOKEN;

            // Separators
            case "OPEN_BRACE_TOKEN":
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case "CLOSE_BRACE_TOKEN":
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case "OPEN_PAREN_TOKEN":
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case "CLOSE_PAREN_TOKEN":
                return SyntaxKind.CLOSE_PAREN_TOKEN;
            case "OPEN_BRACKET_TOKEN":
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case "CLOSE_BRACKET_TOKEN":
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case "SEMICOLON_TOKEN":
                return SyntaxKind.SEMICOLON_TOKEN;
            case "DOT_TOKEN":
                return SyntaxKind.DOT_TOKEN;
            case "COLON_TOKEN":
                return SyntaxKind.COLON_TOKEN;
            case "COMMA_TOKEN":
                return SyntaxKind.COMMA_TOKEN;
            case "ELLIPSIS_TOKEN":
                return SyntaxKind.ELLIPSIS_TOKEN;

            // Expressions
            case "IDENTIFIER_TOKEN":
                return SyntaxKind.IDENTIFIER_TOKEN;
            case "BRACED_EXPRESSION":
                return SyntaxKind.BRACED_EXPRESSION;
            case "BINARY_EXPRESSION":
                return SyntaxKind.BINARY_EXPRESSION;
            case "STRING_LITERAL_TOKEN":
                return SyntaxKind.STRING_LITERAL_TOKEN;
            case "NUMERIC_LITERAL_TOKEN":
                return SyntaxKind.NUMERIC_LITERAL_TOKEN;

            // Statements
            case "BLOCK_STATEMENT":
                return SyntaxKind.BLOCK_STATEMENT;
            case "LOCAL_VARIABLE_DECL":
                return SyntaxKind.LOCAL_VARIABLE_DECL;
            case "ASSIGNMENT_STATEMENT":
                return SyntaxKind.ASSIGNMENT_STATEMENT;

            // Others
            case "TYPE_TOKEN":
                return SyntaxKind.TYPE_TOKEN;

            // Unsupported
            default:
                throw new UnsupportedOperationException("cannot find syntax kind: " + kind);
        }
    }
}
