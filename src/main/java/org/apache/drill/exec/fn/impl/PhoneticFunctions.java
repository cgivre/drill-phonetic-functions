/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.drill.exec.fn.impl;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

public class PhoneticFunctions{
    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PhoneticFunctions.class);

    private PhoneticFunctions() {}


    @FunctionTemplate(name = "soundex", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class SoundexFunction implements DrillSimpleFunc {

        @Param
        VarCharHolder rawInput;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        @Override
        public void setup() {
        }

        @Override
        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput.start, rawInput.end, rawInput.buffer);
            String outputString = new org.apache.commons.codec.language.Soundex().soundex(input);

            out.buffer = buffer;
            out.start = 0;
            out.end = outputString.getBytes().length;
            buffer.setBytes(0, outputString.getBytes());
        }

    }

    @FunctionTemplate(name = "metaphone", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
    public static class MetaphoneFunction implements DrillSimpleFunc {

        @Param
        VarCharHolder rawInput;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        @Override
        public void setup() {
        }

        @Override
        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput.start, rawInput.end, rawInput.buffer);
            String outputString = new org.apache.commons.codec.language.Metaphone().metaphone(input);

            out.buffer = buffer;
            out.start = 0;
            out.end = outputString.getBytes().length;
            buffer.setBytes(0, outputString.getBytes());
        }

    }

  @FunctionTemplate(name = "double_metaphone", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class DoubleMetaphoneFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput;

    @Output
    VarCharHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput.start, rawInput.end, rawInput.buffer);
      String outputString = new org.apache.commons.codec.language.DoubleMetaphone().doubleMetaphone(input);

      out.buffer = buffer;
      out.start = 0;
      out.end = outputString.getBytes().length;
      buffer.setBytes(0, outputString.getBytes());
    }

  }
}