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

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.BitHolder;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "double_metaphone_sounds_like",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)

public class DoubleMetaphoneSoundsLikeFunction implements DrillSimpleFunc {

    @Param
    NullableVarCharHolder input_string1;

    @Param
    NullableVarCharHolder input_string2;

    @Output
    BitHolder out;

    @Inject
    DrillBuf buffer;


    public void setup() {
    }

    public void eval() {
        String string1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(input_string1.start, input_string1.end, input_string1.buffer);

        String string2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(input_string2.start, input_string2.end, input_string2.buffer);

        int result = 0;

        if( org.apache.drill.contrib.function.PhoneticFunctions.doubleMetaphone( string1, false ).equals(  org.apache.drill.contrib.function.PhoneticFunctions.doubleMetaphone( string2,false ) ) )
        {
            result = 1;
        }

        out.value = result;
    }

}
