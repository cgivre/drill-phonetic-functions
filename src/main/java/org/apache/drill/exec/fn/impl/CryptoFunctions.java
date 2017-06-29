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

package org.apache.drill.contrib.function;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.crypto.*;
import javax.inject.Inject;

public class CryptoFunctions{
    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CryptoFunctions.class);

    private CryptoFunctions() {}

    @FunctionTemplate(
        name = "md5",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
    )
    public static class md5Function implements DrillSimpleFunc {

        @Param
        VarCharHolder raw_input;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        @Workspace
        java.security.MessageDigest md;

        public void setup() {
            try {
                md = java.security.MessageDigest.getInstance("MD5");
            } catch( Exception e ) {
            }
        }


        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_input.start, raw_input.end, raw_input.buffer);
            byte[] thedigest = null;
            String output_string = "";

            try {
                byte[] bytesOfMessage = input.getBytes("UTF-8");
                thedigest = md.digest(bytesOfMessage);
                output_string = String.format("%032X", new java.math.BigInteger(1, thedigest));
                output_string = output_string.toLowerCase();

            } catch( Exception e ) {
            }
            out.buffer = buffer;
            out.start = 0;
            out.end = output_string.getBytes().length;
            buffer.setBytes(0, output_string.getBytes());
        }

    }


    @FunctionTemplate(
        names = {"sha", "sha1"},
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
    )
    public static class sha1Function implements DrillSimpleFunc {

        @Param
        VarCharHolder raw_input;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        public void setup() {

        }


        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_input.start, raw_input.end, raw_input.buffer);

            String sha1 = org.apache.commons.codec.digest.DigestUtils.sha1Hex(input);

            out.buffer = buffer;
            out.start = 0;
            out.end = sha1.getBytes().length;
            buffer.setBytes(0, sha1.getBytes());
        }

    }

    @FunctionTemplate(
        names = {"sha256", "sha2"},
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
    )
    public static class sha256Function implements DrillSimpleFunc {

        @Param
        VarCharHolder raw_input;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        public void setup() {

        }


        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_input.start, raw_input.end, raw_input.buffer);

            String sha2 = org.apache.commons.codec.digest.DigestUtils.sha256Hex(input);

            out.buffer = buffer;
            out.start = 0;
            out.end = sha2.getBytes().length;
            buffer.setBytes(0, sha2.getBytes());
        }

    }

    @FunctionTemplate(
        name = "aes_encrypt",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
    )
    public static class AESEncryptFunction implements DrillSimpleFunc {

        @Param
        VarCharHolder raw_input;

        @Param
        VarCharHolder raw_key;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        @Workspace
        String key;

        public void setup() {
            key = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_key.start, raw_key.end, raw_key.buffer);
        }


        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_input.start, raw_input.end, raw_input.buffer);

            String encrypted_text = org.apache.drill.contrib.function.AES.encrypt( input, key );

            out.buffer = buffer;
            out.start = 0;
            out.end = encrypted_text.getBytes().length;
            buffer.setBytes(0, encrypted_text.getBytes());
        }

    }

    @FunctionTemplate(
        name = "aes_decrypt",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
    )
    public static class AESDecryptFunction implements DrillSimpleFunc {

        @Param
        VarCharHolder raw_input;

        @Param
        VarCharHolder raw_key;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        @Workspace
        String key;

        public void setup() {
            key = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_key.start, raw_key.end, raw_key.buffer);
        }


        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_input.start, raw_input.end, raw_input.buffer);

            String decrypted_text = org.apache.drill.contrib.function.AES.decrypt( input, key );

            out.buffer = buffer;
            out.start = 0;
            out.end = decrypted_text.getBytes().length;
            buffer.setBytes(0, decrypted_text.getBytes());
        }

    }

    @FunctionTemplate(
        name = "des_encrypt",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
    )
    public static class desEncryptFunction implements DrillSimpleFunc {

        @Param
        VarCharHolder raw_input;

        @Param
        VarCharHolder raw_key;

        @Output
        VarCharHolder out;

        @Inject
        DrillBuf buffer;

        @Workspace
        KeyGenerator keygenerator;

        @Workspace
        SecretKey myDesKey;

        @Workspace
        Cipher desCipher;

        public void setup() {

            try {
                String keyString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_key.start, raw_key.end, raw_key.buffer);

                byte[] keyBytes = javax.xml.bind.DatatypeConverter.parseHexBinary(keyString);

                javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("DES");
                myDesKey = factory.generateSecret(new javax.crypto.spec.DESKeySpec(keyBytes));

                // Create the cipher
                desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

                // Initialize the cipher for encryption
                desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);

            } catch(Exception e) {
                e.printStackTrace();
            }

        }


        public void eval() {

            String input = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(raw_input.start, raw_input.end, raw_input.buffer);
            String output = "";
            byte[] textEncrypted = new byte[10];
            try{
                //sensitive information
                byte[] text = input.getBytes();

                // Encrypt the text
                textEncrypted = desCipher.doFinal(text);
                output = textEncrypted.toString();

            }catch(Exception e){
                e.printStackTrace();
            }

            out.buffer = buffer;
            out.start = 0;
            out.end = output.getBytes().length;
            buffer.setBytes(0, output.getBytes());
        }

    }

}