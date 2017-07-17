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
import org.apache.drill.exec.expr.holders.IntHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

public class StringDistanceFunctions {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StringDistanceFunctions.class);

  private StringDistanceFunctions() {}

  @FunctionTemplate(name = "simple_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class SimpleRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.ratio(input1,input2);
      out.value = result;
    }
  }

  @FunctionTemplate(name = "partial_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class PartialRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio(input1,input2);
      out.value = result;
    }
  }

  @FunctionTemplate(name = "token_sort_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class TokenSortRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.tokenSortRatio(input1,input2);
      out.value = result;
    }
  }

  @FunctionTemplate(name = "token_sort_partial_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class TokenSortPartialRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.tokenSortPartialRatio(input1,input2);
      out.value = result;
    }
  }

  @FunctionTemplate(name = "token_set_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class TokenSetRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.tokenSetRatio(input1,input2);
      out.value = result;
    }
  }

  @FunctionTemplate(name = "token_set_partial_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class TokenSetPartialRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.tokenSetPartialRatio(input1,input2);
      out.value = result;
    }
  }

  @FunctionTemplate(name = "weighted_ratio", scope = FunctionTemplate.FunctionScope.SIMPLE, nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
  public static class WeightedRatioFunction implements DrillSimpleFunc {

    @Param
    VarCharHolder rawInput1;

    @Param
    VarCharHolder rawInput2;

    @Output
    IntHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {

      String input1 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput1.start, rawInput1.end, rawInput1.buffer);
      String input2 = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(rawInput2.start, rawInput2.end, rawInput2.buffer);

      int result = me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio(input1,input2);
      out.value = result;
    }
  }
}
