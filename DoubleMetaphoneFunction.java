/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.drill.contrib.function;

import com.google.common.base.Strings;
import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.BitHolder;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "double_metaphone",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class DoubleMetaphoneFunction implements DrillSimpleFunc{

    @Param
    NullableVarCharHolder input_string;

    @Output
    NullableVarCharHolder out;

    @Inject
    DrillBuf buffer;


    public void setup() {
    }

    public void eval() {
        String plain_string = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(input_string.start, input_string.end, input_string.buffer);

        String result =  org.apache.drill.contrib.function.PhoneticFunctions.doubleMetaphone( plain_string, false   );

        out.buffer = buffer;
        out.start = 0;
        out.end = result.getBytes().length;
        buffer.setBytes(0, result.getBytes());
    }
}