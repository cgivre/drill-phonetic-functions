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

public class PhoneticFunctions
{
    /**
     * Five values in the English language
     */
    private static final String VOWELS = "AEIOU";

    /**
     * Variable used in Metaphone algorithm
     */
    private static final String FRONTV = "EIY";

    /**
     * Variable used in Metaphone algorithm
     */
    private static final String VARSON = "CSPTG";

    /**
     * The max code length for metaphone is 4
     */
    private static int maxCodeLen = 4;
    /**
     * Prefixes when present which are not pronounced
     * Used for DoubleMetaphone
     */
    private static final String[] SILENT_START =
            { "GN", "KN", "PN", "WR", "PS" };
    private static final String[] L_R_N_M_B_H_F_V_W_SPACE =
            { "L", "R", "N", "M", "B", "H", "F", "V", "W", " " };
    private static final String[] ES_EP_EB_EL_EY_IB_IL_IN_IE_EI_ER =
            { "ES", "EP", "EB", "EL", "EY", "IB", "IL", "IN", "IE", "EI", "ER" };
    private static final String[] L_T_K_S_N_M_B_Z =
            { "L", "T", "K", "S", "N", "M", "B", "Z" };


    public static String soundex(String s) {
        char[] x = s.toUpperCase().toCharArray();
        char firstLetter = x[0];

        // convert letters to numeric code
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {

                case 'B':
                case 'F':
                case 'P':
                case 'V':
                    x[i] = '1';
                    break;

                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z':
                    x[i] = '2';
                    break;

                case 'D':
                case 'T':
                    x[i] = '3';
                    break;

                case 'L':
                    x[i] = '4';
                    break;

                case 'M':
                case 'N':
                    x[i] = '5';
                    break;

                case 'R':
                    x[i] = '6';
                    break;

                default:
                    x[i] = '0';
                    break;
            }
        }

        // remove duplicates
        String output = "" + firstLetter;
        for (int i = 1; i < x.length; i++)
            if (x[i] != x[i-1] && x[i] != '0')
                output += x[i];

        // pad with 0's or truncate
        output = output + "0000";
        return output.substring(0, 4);
    }




    /**
     * Find the metaphone value of a String. This is similar to the
     * soundex algorithm, but better at finding similar sounding words.
     * All input is converted to upper case.
     * Limitations: Input format is expected to be a single ASCII word
     * with only characters in the A - Z range, no punctuation or numbers.
     *
     * @param txt String to find the metaphone code for
     * @return A metaphone code corresponding to the String supplied
     */
    public static String metaphone(final String txt) {
        boolean hard = false;
        int txtLength;
        if (txt == null || (txtLength = txt.length()) == 0) {
            return "";
        }
        // single character is itself
        if (txtLength == 1) {
            return txt.toUpperCase(java.util.Locale.ENGLISH);
        }

        final char[] inwd = txt.toUpperCase(java.util.Locale.ENGLISH).toCharArray();

        final StringBuilder local = new StringBuilder(40); // manipulate
        final StringBuilder code = new StringBuilder(10); //   output
        // handle initial 2 characters exceptions
        switch(inwd[0]) {
            case 'K':
            case 'G':
            case 'P': /* looking for KN, etc*/
                if (inwd[1] == 'N') {
                    local.append(inwd, 1, inwd.length - 1);
                } else {
                    local.append(inwd);
                }
                break;
            case 'A': /* looking for AE */
                if (inwd[1] == 'E') {
                    local.append(inwd, 1, inwd.length - 1);
                } else {
                    local.append(inwd);
                }
                break;
            case 'W': /* looking for WR or WH */
                if (inwd[1] == 'R') {   // WR -> R
                    local.append(inwd, 1, inwd.length - 1);
                    break;
                }
                if (inwd[1] == 'H') {
                    local.append(inwd, 1, inwd.length - 1);
                    local.setCharAt(0, 'W'); // WH -> W
                } else {
                    local.append(inwd);
                }
                break;
            case 'X': /* initial X becomes S */
                inwd[0] = 'S';
                local.append(inwd);
                break;
            default:
                local.append(inwd);
        } // now local has working string with initials fixed

        final int wdsz = local.length();
        int n = 0;

        while (code.length() < maxCodeLen       &&
                n < wdsz ) { // max code size of 4 works well
            final char symb = local.charAt(n);
            // remove duplicate letters except C
            if (symb != 'C' && isPreviousChar( local, n, symb ) ) {
                n++;
            } else { // not dup
                switch(symb) {
                    case 'A':
                    case 'E':
                    case 'I':
                    case 'O':
                    case 'U':
                        if (n == 0) {
                            code.append(symb);
                        }
                        break; // only use vowel if leading char
                    case 'B':
                        if ( isPreviousChar(local, n, 'M') &&
                                isLastChar(wdsz, n) ) { // B is silent if word ends in MB
                            break;
                        }
                        code.append(symb);
                        break;
                    case 'C': // lots of C special cases
                    /* discard if SCI, SCE or SCY */
                        if ( isPreviousChar(local, n, 'S') &&
                                !isLastChar(wdsz, n) &&
                                FRONTV.indexOf(local.charAt(n + 1)) >= 0 ) {
                            break;
                        }
                        if (regionMatch(local, n, "CIA")) { // "CIA" -> X
                            code.append('X');
                            break;
                        }
                        if (!isLastChar(wdsz, n) &&
                                FRONTV.indexOf(local.charAt(n + 1)) >= 0) {
                            code.append('S');
                            break; // CI,CE,CY -> S
                        }
                        if (isPreviousChar(local, n, 'S') &&
                                isNextChar(local, n, 'H') ) { // SCH->sk
                            code.append('K');
                            break;
                        }
                        if (isNextChar(local, n, 'H')) { // detect CH
                            if (n == 0 &&
                                    wdsz >= 3 &&
                                    isVowel(local,2) ) { // CH consonant -> K consonant
                                code.append('K');
                            } else {
                                code.append('X'); // CHvowel -> X
                            }
                        } else {
                            code.append('K');
                        }
                        break;
                    case 'D':
                        if (!isLastChar(wdsz, n + 1) &&
                                isNextChar(local, n, 'G') &&
                                FRONTV.indexOf(local.charAt(n + 2)) >= 0) { // DGE DGI DGY -> J
                            code.append('J'); n += 2;
                        } else {
                            code.append('T');
                        }
                        break;
                    case 'G': // GH silent at end or before consonant
                        if (isLastChar(wdsz, n + 1) &&
                                isNextChar(local, n, 'H')) {
                            break;
                        }
                        if (!isLastChar(wdsz, n + 1) &&
                                isNextChar(local,n,'H') &&
                                !isVowel(local,n+2)) {
                            break;
                        }
                        if (n > 0 &&
                                ( regionMatch(local, n, "GN") ||
                                        regionMatch(local, n, "GNED") ) ) {
                            break; // silent G
                        }
                        if (isPreviousChar(local, n, 'G')) {
                            // NOTE: Given that duplicated chars are removed, I don't see how this can ever be true
                            hard = true;
                        } else {
                            hard = false;
                        }
                        if (!isLastChar(wdsz, n) &&
                                FRONTV.indexOf(local.charAt(n + 1)) >= 0 &&
                                !hard) {
                            code.append('J');
                        } else {
                            code.append('K');
                        }
                        break;
                    case 'H':
                        if (isLastChar(wdsz, n)) {
                            break; // terminal H
                        }
                        if (n > 0 &&
                                VARSON.indexOf(local.charAt(n - 1)) >= 0) {
                            break;
                        }
                        if (isVowel(local,n+1)) {
                            code.append('H'); // Hvowel
                        }
                        break;
                    case 'F':
                    case 'J':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'R':
                        code.append(symb);
                        break;
                    case 'K':
                        if (n > 0) { // not initial
                            if (!isPreviousChar(local, n, 'C')) {
                                code.append(symb);
                            }
                        } else {
                            code.append(symb); // initial K
                        }
                        break;
                    case 'P':
                        if (isNextChar(local,n,'H')) {
                            // PH -> F
                            code.append('F');
                        } else {
                            code.append(symb);
                        }
                        break;
                    case 'Q':
                        code.append('K');
                        break;
                    case 'S':
                        if (regionMatch(local,n,"SH") ||
                                regionMatch(local,n,"SIO") ||
                                regionMatch(local,n,"SIA")) {
                            code.append('X');
                        } else {
                            code.append('S');
                        }
                        break;
                    case 'T':
                        if (regionMatch(local,n,"TIA") ||
                                regionMatch(local,n,"TIO")) {
                            code.append('X');
                            break;
                        }
                        if (regionMatch(local,n,"TCH")) {
                            // Silent if in "TCH"
                            break;
                        }
                        // substitute numeral 0 for TH (resembles theta after all)
                        if (regionMatch(local,n,"TH")) {
                            code.append('0');
                        } else {
                            code.append('T');
                        }
                        break;
                    case 'V':
                        code.append('F'); break;
                    case 'W':
                    case 'Y': // silent if not followed by vowel
                        if (!isLastChar(wdsz,n) &&
                                isVowel(local,n+1)) {
                            code.append(symb);
                        }
                        break;
                    case 'X':
                        code.append('K');
                        code.append('S');
                        break;
                    case 'Z':
                        code.append('S');
                        break;
                    default:
                        // do nothing
                        break;
                } // end switch
                n++;
            } // end else from symb != 'C'
            /*if (code.length() > maxCodeLen {
                code.setLength(this.getMaxCodeLen());
            }*/
        }
        return code.toString();
    }

    private static boolean isVowel(final StringBuilder string, final int index) {
        return VOWELS.indexOf(string.charAt(index)) >= 0;
    }

    private static boolean isPreviousChar(final StringBuilder string, final int index, final char c) {
        boolean matches = false;
        if( index > 0 &&
                index < string.length() ) {
            matches = string.charAt(index - 1) == c;
        }
        return matches;
    }

    private static boolean isNextChar(final StringBuilder string, final int index, final char c) {
        boolean matches = false;
        if( index >= 0 &&
                index < string.length() - 1 ) {
            matches = string.charAt(index + 1) == c;
        }
        return matches;
    }

    private static boolean regionMatch(final StringBuilder string, final int index, final String test) {
        boolean matches = false;
        if( index >= 0 &&
                index + test.length() - 1 < string.length() ) {
            final String substring = string.substring( index, index + test.length());
            matches = substring.equals( test );
        }
        return matches;
    }

    private static boolean isLastChar(final int wdsz, final int n) {
        return n + 1 == wdsz;
    }

    public static boolean isMetaphoneEqual(final String str1, final String str2) {
        return metaphone(str1).equals(metaphone(str2));
    }

    public static String doubleMetaphone(String value, boolean alternate) {
        value = cleanInput(value);
        if (value == null) {
            return null;
        }

        boolean slavoGermanic = isSlavoGermanic(value);
        int index = isSilentStart(value) ? 1 : 0;

        DoubleMetaphoneResult result = new DoubleMetaphoneResult(maxCodeLen);

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
                case 'A':
                case 'E':
                case 'I':
                case 'O':
                case 'U':
                case 'Y':
                    index = handleAEIOUY(value, result, index);
                    break;
                case 'B':
                    result.append('P');
                    index = charAt(value, index + 1) == 'B' ? index + 2 : index + 1;
                    break;
                case '\u00C7':
                    // A C with a Cedilla
                    result.append('S');
                    index++;
                    break;
                case 'C':
                    index = handleC(value, result, index);
                    break;
                case 'D':
                    index = handleD(value, result, index);
                    break;
                case 'F':
                    result.append('F');
                    index = charAt(value, index + 1) == 'F' ? index + 2 : index + 1;
                    break;
                case 'G':
                    index = handleG(value, result, index, slavoGermanic);
                    break;
                case 'H':
                    index = handleH(value, result, index);
                    break;
                case 'J':
                    index = handleJ(value, result, index, slavoGermanic);
                    break;
                case 'K':
                    result.append('K');
                    index = charAt(value, index + 1) == 'K' ? index + 2 : index + 1;
                    break;
                case 'L':
                    index = handleL(value, result, index);
                    break;
                case 'M':
                    result.append('M');
                    index = conditionM0(value, index) ? index + 2 : index + 1;
                    break;
                case 'N':
                    result.append('N');
                    index = charAt(value, index + 1) == 'N' ? index + 2 : index + 1;
                    break;
                case '\u00D1':
                    // N with a tilde (spanish ene)
                    result.append('N');
                    index++;
                    break;
                case 'P':
                    index = handleP(value, result, index);
                    break;
                case 'Q':
                    result.append('K');
                    index = charAt(value, index + 1) == 'Q' ? index + 2 : index + 1;
                    break;
                case 'R':
                    index = handleR(value, result, index, slavoGermanic);
                    break;
                case 'S':
                    index = handleS(value, result, index, slavoGermanic);
                    break;
                case 'T':
                    index = handleT(value, result, index);
                    break;
                case 'V':
                    result.append('F');
                    index = charAt(value, index + 1) == 'V' ? index + 2 : index + 1;
                    break;
                case 'W':
                    index = handleW(value, result, index);
                    break;
                case 'X':
                    index = handleX(value, result, index);
                    break;
                case 'Z':
                    index = handleZ(value, result, index, slavoGermanic);
                    break;
                default:
                    index++;
                    break;
            }
        }

        return alternate ? result.getAlternate() : result.getPrimary();
    }

    //-- BEGIN HANDLERS --//

    /**
     * Handles 'A', 'E', 'I', 'O', 'U', and 'Y' cases
     */
    private static int handleAEIOUY(String value, DoubleMetaphoneResult result, int
            index) {
        if (index == 0) {
            result.append('A');
        }
        return index + 1;
    }

    /**
     * Handles 'C' cases
     */
    private static int handleC(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        if (conditionC0(value, index)) {  // very confusing, moved out
            result.append('K');
            index += 2;
        } else if (index == 0 && contains(value, index, 6, "CAESAR")) {
            result.append('S');
            index += 2;
        } else if (contains(value, index, 2, "CH")) {
            index = handleCH(value, result, index);
        } else if (contains(value, index, 2, "CZ") &&
                !contains(value, index - 2, 4, "WICZ")) {
            //-- "Czerny" --//
            result.append('S', 'X');
            index += 2;
        } else if (contains(value, index + 1, 3, "CIA")) {
            //-- "focaccia" --//
            result.append('X');
            index += 3;
        } else if (contains(value, index, 2, "CC") &&
                !(index == 1 && charAt(value, 0) == 'M')) {
            //-- double "cc" but not "McClelland" --//
            return handleCC(value, result, index);
        } else if (contains(value, index, 2, "CK", "CG", "CQ")) {
            result.append('K');
            index += 2;
        } else if (contains(value, index, 2, "CI", "CE", "CY")) {
            //-- Italian vs. English --//
            if (contains(value, index, 3, "CIO", "CIE", "CIA")) {
                result.append('S', 'X');
            } else {
                result.append('S');
            }
            index += 2;
        } else {
            result.append('K');
            if (contains(value, index + 1, 2, " C", " Q", " G")) {
                //-- Mac Caffrey, Mac Gregor --//
                index += 3;
            } else if (contains(value, index + 1, 1, "C", "K", "Q") &&
                    !contains(value, index + 1, 2, "CE", "CI")) {
                index += 2;
            } else {
                index++;
            }
        }

        return index;
    }

    /**
     * Handles 'CC' cases
     */
    private static int handleCC(String value,
                         DoubleMetaphoneResult result,
                         int index) {
        if (contains(value, index + 2, 1, "I", "E", "H") &&
                !contains(value, index + 2, 2, "HU")) {
            //-- "bellocchio" but not "bacchus" --//
            if ((index == 1 && charAt(value, index - 1) == 'A') ||
                    contains(value, index - 1, 5, "UCCEE", "UCCES")) {
                //-- "accident", "accede", "succeed" --//
                result.append("KS");
            } else {
                //-- "bacci", "bertucci", other Italian --//
                result.append('X');
            }
            index += 3;
        } else {    // Pierce's rule
            result.append('K');
            index += 2;
        }

        return index;
    }

    /**
     * Handles 'CH' cases
     */
    private static int handleCH(String value,
                         DoubleMetaphoneResult result,
                         int index) {
        if (index > 0 && contains(value, index, 4, "CHAE")) {   // Michael
            result.append('K', 'X');
            return index + 2;
        } else if (conditionCH0(value, index)) {
            //-- Greek roots ("chemistry", "chorus", etc.) --//
            result.append('K');
            return index + 2;
        } else if (conditionCH1(value, index)) {
            //-- Germanic, Greek, or otherwise 'ch' for 'kh' sound --//
            result.append('K');
            return index + 2;
        } else {
            if (index > 0) {
                if (contains(value, 0, 2, "MC")) {
                    result.append('K');
                } else {
                    result.append('X', 'K');
                }
            } else {
                result.append('X');
            }
            return index + 2;
        }
    }

    /**
     * Handles 'D' cases
     */
    private static int handleD(String value,
                        DoubleMetaphoneResult result,
                        int index) {
        if (contains(value, index, 2, "DG")) {
            //-- "Edge" --//
            if (contains(value, index + 2, 1, "I", "E", "Y")) {
                result.append('J');
                index += 3;
                //-- "Edgar" --//
            } else {
                result.append("TK");
                index += 2;
            }
        } else if (contains(value, index, 2, "DT", "DD")) {
            result.append('T');
            index += 2;
        } else {
            result.append('T');
            index++;
        }
        return index;
    }

    /**
     * Handles 'G' cases
     */
    private static int handleG(String value,
                        DoubleMetaphoneResult result,
                        int index,
                        boolean slavoGermanic) {
        if (charAt(value, index + 1) == 'H') {
            index = handleGH(value, result, index);
        } else if (charAt(value, index + 1) == 'N') {
            if (index == 1 && isVowel(charAt(value, 0)) && !slavoGermanic) {
                result.append("KN", "N");
            } else if (!contains(value, index + 2, 2, "EY") &&
                    charAt(value, index + 1) != 'Y' && !slavoGermanic) {
                result.append("N", "KN");
            } else {
                result.append("KN");
            }
            index = index + 2;
        } else if (contains(value, index + 1, 2, "LI") && !slavoGermanic) {
            result.append("KL", "L");
            index += 2;
        } else if (index == 0 && (charAt(value, index + 1) == 'Y' || contains(value, index + 1, 2, ES_EP_EB_EL_EY_IB_IL_IN_IE_EI_ER))) {
            //-- -ges-, -gep-, -gel-, -gie- at beginning --//
            result.append('K', 'J');
            index += 2;
        } else if ((contains(value, index + 1, 2, "ER") ||
                charAt(value, index + 1) == 'Y') &&
                !contains(value, 0, 6, "DANGER", "RANGER", "MANGER") &&
                !contains(value, index - 1, 1, "E", "I") &&
                !contains(value, index - 1, 3, "RGY", "OGY")) {
            //-- -ger-, -gy- --//
            result.append('K', 'J');
            index += 2;
        } else if (contains(value, index + 1, 1, "E", "I", "Y") ||
                contains(value, index - 1, 4, "AGGI", "OGGI")) {
            //-- Italian "biaggi" --//
            if ((contains(value, 0 ,4, "VAN ", "VON ") || contains(value, 0, 3, "SCH")) || contains(value, index + 1, 2, "ET")) {
                //-- obvious germanic --//
                result.append('K');
            } else if (contains(value, index + 1, 4, "IER")) {
                result.append('J');
            } else {
                result.append('J', 'K');
            }
            index += 2;
        } else if (charAt(value, index + 1) == 'G') {
            index += 2;
            result.append('K');
        } else {
            index++;
            result.append('K');
        }
        return index;
    }

    /**
     * Handles 'GH' cases
     */
    private static int handleGH(String value,
                                DoubleMetaphoneResult result,
                         int index) {
        if (index > 0 && !isVowel(charAt(value, index - 1))) {
            result.append('K');
            index += 2;
        } else if (index == 0) {
            if (charAt(value, index + 2) == 'I') {
                result.append('J');
            } else {
                result.append('K');
            }
            index += 2;
        } else if ((index > 1 && contains(value, index - 2, 1, "B", "H", "D")) ||
                (index > 2 && contains(value, index - 3, 1, "B", "H", "D")) ||
                (index > 3 && contains(value, index - 4, 1, "B", "H"))) {
            //-- Parker's rule (with some further refinements) - "hugh"
            index += 2;
        } else {
            if (index > 2 && charAt(value, index - 1) == 'U' &&
                    contains(value, index - 3, 1, "C", "G", "L", "R", "T")) {
                //-- "laugh", "McLaughlin", "cough", "gough", "rough", "tough"
                result.append('F');
            } else if (index > 0 && charAt(value, index - 1) != 'I') {
                result.append('K');
            }
            index += 2;
        }
        return index;
    }

    /**
     * Handles 'H' cases
     */
    private static int handleH(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        //-- only keep if first & before vowel or between 2 vowels --//
        if ((index == 0 || isVowel(charAt(value, index - 1))) &&
                isVowel(charAt(value, index + 1))) {
            result.append('H');
            index += 2;
            //-- also takes car of "HH" --//
        } else {
            index++;
        }
        return index;
    }

    /**
     * Handles 'J' cases
     */
    private static int handleJ(String value, DoubleMetaphoneResult result, int index,
                        boolean slavoGermanic) {
        if (contains(value, index, 4, "JOSE") || contains(value, 0, 4, "SAN ")) {
            //-- obvious Spanish, "Jose", "San Jacinto" --//
            if ((index == 0 && (charAt(value, index + 4) == ' ') ||
                    value.length() == 4) || contains(value, 0, 4, "SAN ")) {
                result.append('H');
            } else {
                result.append('J', 'H');
            }
            index++;
        } else {
            if (index == 0 && !contains(value, index, 4, "JOSE")) {
                result.append('J', 'A');
            } else if (isVowel(charAt(value, index - 1)) && !slavoGermanic &&
                    (charAt(value, index + 1) == 'A' || charAt(value, index + 1) == 'O')) {
                result.append('J', 'H');
            } else if (index == value.length() - 1) {
                result.append('J', ' ');
            } else if (!contains(value, index + 1, 1, L_T_K_S_N_M_B_Z) && !contains(value, index - 1, 1, "S", "K", "L")) {
                result.append('J');
            }

            if (charAt(value, index + 1) == 'J') {
                index += 2;
            } else {
                index++;
            }
        }
        return index;
    }

    /**
     * Handles 'L' cases
     */
    private static int handleL(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        result.append('L');
        if (charAt(value, index + 1) == 'L') {
            if (conditionL0(value, index)) {
                result.appendAlternate(' ');
            }
            index += 2;
        } else {
            index++;
        }
        return index;
    }

    /**
     * Handles 'P' cases
     */
    private static int handleP(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        if (charAt(value, index + 1) == 'H') {
            result.append( 'F' );
            index += 2;
        } else {
            result.append( 'P');
            index = contains(value, index + 1, 1, "P", "B") ? index + 2 : index + 1;
        }
        return index;
    }

    /**
     * Handles 'R' cases
     */
    private static int handleR(String value,
                               DoubleMetaphoneResult result,
                        int index,
                        boolean slavoGermanic) {
        if (index == value.length() - 1 && !slavoGermanic &&
                contains(value, index - 2, 2, "IE") &&
                !contains(value, index - 4, 2, "ME", "MA")) {
            result.appendAlternate('R');
        } else {
            result.append('R');
        }
        return charAt(value, index + 1) == 'R' ? index + 2 : index + 1;
    }

    /**
     * Handles 'S' cases
     */
    private static int handleS(String value,
                        DoubleMetaphoneResult result,
                        int index,
                        boolean slavoGermanic) {
        if (contains(value, index - 1, 3, "ISL", "YSL")) {
            //-- special cases "island", "isle", "carlisle", "carlysle" --//
            index++;
        } else if (index == 0 && contains(value, index, 5, "SUGAR")) {
            //-- special case "sugar-" --//
            result.append('X', 'S');
            index++;
        } else if (contains(value, index, 2, "SH")) {
            if (contains(value, index + 1, 4,
                    "HEIM", "HOEK", "HOLM", "HOLZ")) {
                //-- germanic --//
                result.append('S');
            } else {
                result.append('X');
            }
            index += 2;
        } else if (contains(value, index, 3, "SIO", "SIA") || contains(value, index, 4, "SIAN")) {
            //-- Italian and Armenian --//
            if (slavoGermanic) {
                result.append('S');
            } else {
                result.append('S', 'X');
            }
            index += 3;
        } else if ((index == 0 && contains(value, index + 1, 1, "M", "N", "L", "W")) || contains(value, index + 1, 1, "Z")) {
            //-- german & anglicisations, e.g. "smith" match "schmidt" //
            // "snider" match "schneider" --//
            //-- also, -sz- in slavic language altho in hungarian it //
            //   is pronounced "s" --//
            result.append('S', 'X');
            index = contains(value, index + 1, 1, "Z") ? index + 2 : index + 1;
        } else if (contains(value, index, 2, "SC")) {
            index = handleSC(value, result, index);
        } else {
            if (index == value.length() - 1 && contains(value, index - 2,
                    2, "AI", "OI")){
                //-- french e.g. "resnais", "artois" --//
                result.appendAlternate('S');
            } else {
                result.append('S');
            }
            index = contains(value, index + 1, 1, "S", "Z") ? index + 2 : index + 1;
        }
        return index;
    }

    /**
     * Handles 'SC' cases
     */
    private static int handleSC(String value,
                         DoubleMetaphoneResult result,
                         int index) {
        if (charAt(value, index + 2) == 'H') {
            //-- Schlesinger's rule --//
            if (contains(value, index + 3,
                    2, "OO", "ER", "EN", "UY", "ED", "EM")) {
                //-- Dutch origin, e.g. "school", "schooner" --//
                if (contains(value, index + 3, 2, "ER", "EN")) {
                    //-- "schermerhorn", "schenker" --//
                    result.append("X", "SK");
                } else {
                    result.append("SK");
                }
            } else {
                if (index == 0 && !isVowel(charAt(value, 3)) && charAt(value, 3) != 'W') {
                    result.append('X', 'S');
                } else {
                    result.append('X');
                }
            }
        } else if (contains(value, index + 2, 1, "I", "E", "Y")) {
            result.append('S');
        } else {
            result.append("SK");
        }
        return index + 3;
    }

    /**
     * Handles 'T' cases
     */
    private static int handleT(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        if (contains(value, index, 4, "TION")) {
            result.append('X');
            index += 3;
        } else if (contains(value, index, 3, "TIA", "TCH")) {
            result.append('X');
            index += 3;
        } else if (contains(value, index, 2, "TH") || contains(value, index,
                3, "TTH")) {
            if (contains(value, index + 2, 2, "OM", "AM") ||
                    //-- special case "thomas", "thames" or germanic --//
                    contains(value, 0, 4, "VAN ", "VON ") ||
                    contains(value, 0, 3, "SCH")) {
                result.append('T');
            } else {
                result.append('0', 'T');
            }
            index += 2;
        } else {
            result.append('T');
            index = contains(value, index + 1, 1, "T", "D") ? index + 2 : index + 1;
        }
        return index;
    }

    /**
     * Handles 'W' cases
     */
    private static int handleW(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        if (contains(value, index, 2, "WR")) {
            //-- can also be in middle of word --//
            result.append('R');
            index += 2;
        } else {
            if (index == 0 && (isVowel(charAt(value, index + 1)) ||
                    contains(value, index, 2, "WH"))) {
                if (isVowel(charAt(value, index + 1))) {
                    //-- Wasserman should match Vasserman --//
                    result.append('A', 'F');
                } else {
                    //-- need Uomo to match Womo --//
                    result.append('A');
                }
                index++;
            } else if ((index == value.length() - 1 && isVowel(charAt(value, index - 1))) ||
                    contains(value, index - 1,
                            5, "EWSKI", "EWSKY", "OWSKI", "OWSKY") ||
                    contains(value, 0, 3, "SCH")) {
                //-- Arnow should match Arnoff --//
                result.appendAlternate('F');
                index++;
            } else if (contains(value, index, 4, "WICZ", "WITZ")) {
                //-- Polish e.g. "filipowicz" --//
                result.append("TS", "FX");
                index += 4;
            } else {
                index++;
            }
        }
        return index;
    }

    /**
     * Handles 'X' cases
     */
    private static int handleX(String value,
                               DoubleMetaphoneResult result,
                        int index) {
        if (index == 0) {
            result.append('S');
            index++;
        } else {
            if (!((index == value.length() - 1) &&
                    (contains(value, index - 3, 3, "IAU", "EAU") ||
                            contains(value, index - 2, 2, "AU", "OU")))) {
                //-- French e.g. breaux --//
                result.append("KS");
            }
            index = contains(value, index + 1, 1, "C", "X") ? index + 2 : index + 1;
        }
        return index;
    }

    /**
     * Handles 'Z' cases
     */
    private static int handleZ(String value, DoubleMetaphoneResult result, int index,
                        boolean slavoGermanic) {
        if (charAt(value, index + 1) == 'H') {
            //-- Chinese pinyin e.g. "zhao" or Angelina "Zhang" --//
            result.append('J');
            index += 2;
        } else {
            if (contains(value, index + 1, 2, "ZO", "ZI", "ZA") || (slavoGermanic && (index > 0 && charAt(value, index - 1) != 'T'))) {
                result.append("S", "TS");
            } else {
                result.append('S');
            }
            index = charAt(value, index + 1) == 'Z' ? index + 2 : index + 1;
        }
        return index;
    }

    //-- BEGIN CONDITIONS --//

    /**
     * Complex condition 0 for 'C'
     */
    private static boolean conditionC0(String value, int index) {
        if (contains(value, index, 4, "CHIA")) {
            return true;
        } else if (index <= 1) {
            return false;
        } else if (isVowel(charAt(value, index - 2))) {
            return false;
        } else if (!contains(value, index - 1, 3, "ACH")) {
            return false;
        } else {
            char c = charAt(value, index + 2);
            return (c != 'I' && c != 'E')
                    || contains(value, index - 2, 6, "BACHER", "MACHER");
        }
    }

    /**
     * Complex condition 0 for 'CH'
     */
    private static boolean conditionCH0(String value, int index) {
        if (index != 0) {
            return false;
        } else if (!contains(value, index + 1, 5, "HARAC", "HARIS") &&
                !contains(value, index + 1, 3, "HOR", "HYM", "HIA", "HEM")) {
            return false;
        } else if (contains(value, 0, 5, "CHORE")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Complex condition 1 for 'CH'
     */
    private static boolean conditionCH1(String value, int index) {
        return ((contains(value, 0, 4, "VAN ", "VON ") || contains(value, 0,
                3, "SCH")) ||
                contains(value, index - 2, 6, "ORCHES", "ARCHIT", "ORCHID") ||
                contains(value, index + 2, 1, "T", "S") ||
                ((contains(value, index - 1, 1, "A", "O", "U", "E") || index == 0) &&
                        (contains(value, index + 2, 1, L_R_N_M_B_H_F_V_W_SPACE) || index + 1 == value.length() - 1)));
    }

    /**
     * Complex condition 0 for 'L'
     */
    private static boolean conditionL0(String value, int index) {
        if (index == value.length() - 3 &&
                contains(value, index - 1, 4, "ILLO", "ILLA", "ALLE")) {
            return true;
        } else if ((contains(value, index - 1, 2, "AS", "OS") ||
                contains(value, value.length() - 1, 1, "A", "O")) &&
                contains(value, index - 1, 4, "ALLE")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Complex condition 0 for 'M'
     */
    private static boolean conditionM0(String value, int index) {
        if (charAt(value, index + 1) == 'M') {
            return true;
        }
        return contains(value, index - 1, 3, "UMB")
                && ((index + 1) == value.length() - 1 || contains(value,
                index + 2, 2, "ER"));
    }

    //-- BEGIN HELPER FUNCTIONS --//

    /**
     * Determines whether or not a value is of slavo-germanic orgin. A value is
     * of slavo-germanic origin if it contians any of 'W', 'K', 'CZ', or 'WITZ'.
     */
    private static boolean isSlavoGermanic(String value) {
        return value.indexOf('W') > -1 || value.indexOf('K') > -1 ||
                value.indexOf("CZ") > -1 || value.indexOf("WITZ") > -1;
    }

    /**
     * Determines whether or not a character is a vowel or not
     */
    private static boolean isVowel(char ch) {
        return VOWELS.indexOf(ch) != -1;
    }

    /**
     * Determines whether or not the value starts with a silent letter.  It will
     * return <code>true</code> if the value starts with any of 'GN', 'KN',
     * 'PN', 'WR' or 'PS'.
     */
    private static boolean isSilentStart(String value) {
        boolean result = false;
        for (int i = 0; i < SILENT_START.length; i++) {
            if (value.startsWith(SILENT_START[i])) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Cleans the input
     */
    private static String cleanInput(String input) {
        if (input == null) {
            return null;
        }
        input = input.trim();
        if (input.length() == 0) {
            return null;
        }
        return input.toUpperCase();
    }

    /**
     * Gets the character at index <code>index</code> if available, otherwise
     * it returns <code>Character.MIN_VALUE</code> so that there is some sort
     * of a default
     */
    protected static char charAt(String value, int index) {
        if (index < 0 || index >= value.length()) {
            return Character.MIN_VALUE;
        }
        return value.charAt(index);
    }

    /**
     * Shortcut method with 1 criteria
     */
    private static boolean contains(String value, int start, int length,
                                    String criteria) {
        return contains(value, start, length,
                new String[] { criteria });
    }

    /**
     * Shortcut method with 2 criteria
     */
    private static boolean contains(String value, int start, int length,
                                    String criteria1, String criteria2) {
        return contains(value, start, length,
                new String[] { criteria1, criteria2 });
    }

    /**
     * Shortcut method with 3 criteria
     */
    private static boolean contains(String value, int start, int length,
                                    String criteria1, String criteria2,
                                    String criteria3) {
        return contains(value, start, length,
                new String[] { criteria1, criteria2, criteria3 });
    }

    /**
     * Shortcut method with 4 criteria
     */
    private static boolean contains(String value, int start, int length,
                                    String criteria1, String criteria2,
                                    String criteria3, String criteria4) {
        return contains(value, start, length,
                new String[] { criteria1, criteria2, criteria3,
                        criteria4 });
    }

    /**
     * Shortcut method with 5 criteria
     */
    private static boolean contains(String value, int start, int length,
                                    String criteria1, String criteria2,
                                    String criteria3, String criteria4,
                                    String criteria5) {
        return contains(value, start, length,
                new String[] { criteria1, criteria2, criteria3,
                        criteria4, criteria5 });
    }

    /**
     * Shortcut method with 6 criteria
     */
    private static boolean contains(String value, int start, int length,
                                    String criteria1, String criteria2,
                                    String criteria3, String criteria4,
                                    String criteria5, String criteria6) {
        return contains(value, start, length,
                new String[] { criteria1, criteria2, criteria3,
                        criteria4, criteria5, criteria6 });
    }

    /**
     * Determines whether <code>value</code> contains any of the criteria
     starting
     * at index <code>start</code> and matching up to length <code>length</code>
     */
    protected static boolean contains(String value, int start, int length,
                                      String[] criteria) {
        boolean result = false;
        if (start >= 0 && start + length <= value.length()) {
            String target = value.substring(start, start + length);

            for (int i = 0; i < criteria.length; i++) {
                if (target.equals(criteria[i])) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    public static class DoubleMetaphoneResult {

        private StringBuffer primary = new StringBuffer(maxCodeLen);
        private StringBuffer alternate = new StringBuffer(maxCodeLen);
        private int maxLength;

        public DoubleMetaphoneResult(int maxLength) {
            this.maxLength = maxLength;
        }

        public void append(char value) {
            appendPrimary(value);
            appendAlternate(value);
        }

        public void append(char primary, char alternate) {
            appendPrimary(primary);
            appendAlternate(alternate);
        }

        public void appendPrimary(char value) {
            if (this.primary.length() < this.maxLength) {
                this.primary.append(value);
            }
        }

        public void appendAlternate(char value) {
            if (this.alternate.length() < this.maxLength) {
                this.alternate.append(value);
            }
        }

        public void append(String value) {
            appendPrimary(value);
            appendAlternate(value);
        }

        public void append(String primary, String alternate) {
            appendPrimary(primary);
            appendAlternate(alternate);
        }

        public void appendPrimary(String value) {
            int addChars = this.maxLength - this.primary.length();
            if (value.length() <= addChars) {
                this.primary.append(value);
            } else {
                this.primary.append(value.substring(0, addChars));
            }
        }

        public void appendAlternate(String value) {
            int addChars = this.maxLength - this.alternate.length();
            if (value.length() <= addChars) {
                this.alternate.append(value);
            } else {
                this.alternate.append(value.substring(0, addChars));
            }
        }

        public String getPrimary() {
            return this.primary.toString();
        }

        public String getAlternate() {
            return this.alternate.toString();
        }

        public boolean isComplete() {
            return this.primary.length() >= this.maxLength &&
                    this.alternate.length() >= this.maxLength;
        }
    }
}
