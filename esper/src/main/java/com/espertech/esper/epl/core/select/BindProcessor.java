/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.epl.core.select;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.codegen.base.CodegenBlock;
import com.espertech.esper.codegen.base.CodegenClassScope;
import com.espertech.esper.codegen.base.CodegenMethodScope;
import com.espertech.esper.codegen.model.blocks.CodegenLegoMayVoid;
import com.espertech.esper.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.codegen.base.CodegenMethodNode;
import com.espertech.esper.epl.expression.core.ExprEvaluator;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;

/**
 * Works in conjunction with {@link SelectExprResultProcessor} to present
 * a result as an object array for 'natural' delivery.
 */
public class BindProcessor {
    private final BindProcessorForge forge;
    private final ExprEvaluator[] expressionNodes;

    public BindProcessor(BindProcessorForge forge, ExprEvaluator[] expressionNodes) {
        this.forge = forge;
        this.expressionNodes = expressionNodes;
    }

    public Object[] process(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        Object[] parameters = new Object[expressionNodes.length];

        for (int i = 0; i < parameters.length; i++) {
            Object result = expressionNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            parameters[i] = result;
        }

        return parameters;
    }

    protected static CodegenMethodNode processCodegen(BindProcessorForge forge, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethodNode methodNode = codegenMethodScope.makeChild(Object[].class, BindProcessor.class, codegenClassScope);
        CodegenBlock block = methodNode.getBlock()
                .declareVar(Object[].class, "parameters", newArray(Object.class, constant(forge.getExpressionForges().length)));
        for (int i = 0; i < forge.getExpressionForges().length; i++) {
            block.assignArrayElement("parameters", constant(i), CodegenLegoMayVoid.expressionMayVoid(Object.class, forge.getExpressionForges()[i], methodNode, exprSymbol, codegenClassScope));
        }
        block.methodReturn(ref("parameters"));
        return methodNode;
    }

    /**
     * Returns the expression types generated by the select-clause expressions.
     *
     * @return types
     */
    public Class[] getExpressionTypes() {
        return forge.getExpressionTypes();
    }

    /**
     * Returns the column names of select-clause expressions.
     *
     * @return column names
     */
    public String[] getColumnNamesAssigned() {
        return forge.getColumnNamesAssigned();
    }
}