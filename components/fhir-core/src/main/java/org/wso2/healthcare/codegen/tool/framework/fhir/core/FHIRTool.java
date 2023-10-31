/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.healthcare.codegen.tool.framework.fhir.core;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.IOUtils;
import org.wso2.healthcare.codegen.tool.framework.commons.config.ToolConfig;
import org.wso2.healthcare.codegen.tool.framework.commons.core.AbstractTool;
import org.wso2.healthcare.codegen.tool.framework.commons.core.Tool;
import org.wso2.healthcare.codegen.tool.framework.commons.core.ToolContext;
import org.wso2.healthcare.codegen.tool.framework.commons.exception.CodeGenException;
import org.wso2.healthcare.codegen.tool.framework.fhir.core.common.FHIRSpecificationData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the FHIR tool library which will hold the contexual data and utilities for the
 * FHIR tool implementations to generate artifacts.
 */
public class FHIRTool extends AbstractTool {

    public static final String BASE_OAS_MODEL_PROPERTY = "baseOAS";
    private FHIRToolContext toolContext;
    private Map<String, Tool> toolImplementations;

    public FHIRTool() {
        toolImplementations = new HashMap<>();
    }

    public void initialize(ToolConfig toolConfig) throws CodeGenException {
        toolContext = new FHIRToolContext();
        toolContext.setConfig(toolConfig);
        try {
            toolContext.addCustomToolProperty(BASE_OAS_MODEL_PROPERTY, populateFhirOASStructure());
        } catch (IOException e) {
            String msg = "Error occurred while populating base FHIR OAS structure.";
            throw new CodeGenException(msg, e);
        }
        FHIRSpecParser specParser = new FHIRSpecParser();
        specParser.parse(toolConfig);
        toolContext.setSpecificationData(FHIRSpecificationData.getDataHolderInstance());
    }

    public ToolContext getToolContext() {
        return toolContext;
    }

    public void setToolContext(ToolContext toolContext) {
        this.toolContext = (FHIRToolContext) toolContext;
    }

    public Map<String, Tool> getToolImplementations() {
        return toolImplementations;
    }

    public void setToolImplementations(Map<String, Tool> toolImplementations) {
        this.toolImplementations = toolImplementations;
    }

    /**
     * Populates base FHIR OAS definition structure.
     *
     * @return OAS model of the base structure
     */
    private OpenAPI populateFhirOASStructure() throws IOException {
        try (InputStream inputStream = FHIRTool.class.getClassLoader().getResourceAsStream(
                "api-defs" + File.separator + "oas-static-content.yaml")) {
            if (inputStream != null) {
                String parsedYamlContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                return new OpenAPIV3Parser().readContents(parsedYamlContent).getOpenAPI();
            }
        }
        return null;
    }
}
