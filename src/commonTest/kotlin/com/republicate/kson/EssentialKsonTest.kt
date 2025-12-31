package com.republicate.kson

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
// import kotlinx.datetime.DateTimeParseException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val logger = KotlinLogging.logger { "test" }

private val watchFile = "test_parsing/i_number_huge_exp.json"

class EssentialJsonTest : BaseTestUnit()
{
    @Test
    fun test() = runTest {

        logger.trace { "trace is enabled "}
        logger.debug { "debug is enabled"}
        logger.info { "info is enabled"}
        logger.warn { "warn is enabled"}
        logger.error { "error is enabled"}

        for (file in files) {
            testFile(file)
        }
    }

    companion object {

        // TODO - this should be dynamically generated
        val files = arrayListOf(
            "nst_files/n_101.json",
            "nst_files/n_10.json",
            "nst_files/n_11.json",
            "nst_files/n_125.json",
            "nst_files/n_127.json",
            "nst_files/n_12.json",
            "nst_files/n_136.json",
            "nst_files/n_137.json",
            "nst_files/n_139.json",
            "nst_files/n_13.json",
            "nst_files/n_140.json",
            "nst_files/n_145.json",
            "nst_files/n_147.json",
            "nst_files/n_149.json",
            "nst_files/n_14.json",
            "nst_files/n_150.json",
            "nst_files/n_151.json",
            "nst_files/n_153.json",
            "nst_files/n_154.json",
            "nst_files/n_155.json",
            "nst_files/n_156.json",
            "nst_files/n_157.json",
            "nst_files/n_158.json",
            "nst_files/n_159.json",
            "nst_files/n_15.json",
            "nst_files/n_160.json",
            "nst_files/n_161.json",
            "nst_files/n_162.json",
            "nst_files/n_163.json",
            "nst_files/n_164.json",
            "nst_files/n_165.json",
            "nst_files/n_169.json",
            "nst_files/n_16.json",
            "nst_files/n_171.json",
            "nst_files/n_172.json",
            "nst_files/n_173.json",
            "nst_files/n_175.json",
            "nst_files/n_176.json",
            "nst_files/n_177.json",
            "nst_files/n_178.json",
            "nst_files/n_179.json",
            "nst_files/n_17.json",
            "nst_files/n_180.json",
            "nst_files/n_181.json",
            "nst_files/n_183.json",
            "nst_files/n_184.json",
            "nst_files/n_185.json",
            "nst_files/n_186.json",
            "nst_files/n_187.json",
            "nst_files/n_188.json",
            "nst_files/n_189.json",
            "nst_files/n_18.json",
            "nst_files/n_190.json",
            "nst_files/n_191.json",
            "nst_files/n_192.json",
            "nst_files/n_193.json",
            "nst_files/n_194.json",
            "nst_files/n_195.json",
            "nst_files/n_197.json",
            "nst_files/n_198.json",
            "nst_files/n_199.json",
            "nst_files/n_19.json",
            "nst_files/n_200.json",
            "nst_files/n_201.json",
            "nst_files/n_202.json",
            "nst_files/n_203.json",
            "nst_files/n_204.json",
            "nst_files/n_205.json",
            "nst_files/n_206.json",
            "nst_files/n_207.json",
            "nst_files/n_208.json",
            "nst_files/n_209.json",
            "nst_files/n_20.json",
            "nst_files/n_210.json",
            "nst_files/n_211.json",
            "nst_files/n_212.json",
            "nst_files/n_213.json",
            "nst_files/n_214.json",
            "nst_files/n_216.json",
            "nst_files/n_217.json",
            "nst_files/n_218.json",
            "nst_files/n_219.json",
            "nst_files/n_21.json",
            "nst_files/n_220.json",
            "nst_files/n_221.json",
            "nst_files/n_222.json",
            "nst_files/n_223.json",
            "nst_files/n_22.json",
            "nst_files/n_23.json",
            "nst_files/n_24.json",
            "nst_files/n_25.json",
            "nst_files/n_26.json",
            "nst_files/n_27.json",
            "nst_files/n_28.json",
            "nst_files/n_29.json",
            "nst_files/n_2.json",
            "nst_files/n_30.json",
            "nst_files/n_31.json",
            "nst_files/n_32.json",
            "nst_files/n_33.json",
            "nst_files/n_34.json",
            "nst_files/n_35.json",
            "nst_files/n_36.json",
            "nst_files/n_37.json",
            "nst_files/n_3.json",
            "nst_files/n_40.json",
            "nst_files/n_44.json",
            "nst_files/n_46.json",
            "nst_files/n_4.json",
            "nst_files/n_54.json",
            "nst_files/n_55.json",
            "nst_files/n_59.json",
            "nst_files/n_5.json",
            "nst_files/n_63.json",
            "nst_files/n_64.json",
            "nst_files/n_66.json",
            "nst_files/n_69.json",
            "nst_files/n_6.json",
            "nst_files/n_77.json",
            "nst_files/n_78.json",
            "nst_files/n_79.json",
            "nst_files/n_7.json",
            "nst_files/n_80.json",
            "nst_files/n_81.json",
            "nst_files/n_82.json",
            "nst_files/n_83.json",
            "nst_files/n_85.json",
            "nst_files/n_86.json",
            "nst_files/n_87.json",
            "nst_files/n_8.json",
            "nst_files/n_91.json",
            "nst_files/n_9.json",
            "nst_files/y_100.json",
            "nst_files/y_102.json",
            "nst_files/y_103.json",
            "nst_files/y_104.json",
            "nst_files/y_105.json",
            "nst_files/y_106.json",
            "nst_files/y_107.json",
            "nst_files/y_108.json",
            "nst_files/y_109.json",
            "nst_files/y_110.json",
            "nst_files/y_111.json",
            "nst_files/y_112.json",
            "nst_files/y_113.json",
            "nst_files/y_114.json",
            "nst_files/y_115.json",
            "nst_files/y_116.json",
            "nst_files/y_117.json",
            "nst_files/y_118.json",
            "nst_files/y_119.json",
            "nst_files/y_120.json",
            "nst_files/y_121.json",
            "nst_files/y_122.json",
            "nst_files/y_123.json",
            "nst_files/y_124.json",
            "nst_files/y_126.json",
            "nst_files/y_128.json",
            "nst_files/y_129.json",
            "nst_files/y_130.json",
            "nst_files/y_131.json",
            "nst_files/y_132.json",
            "nst_files/y_133.json",
            "nst_files/y_134.json",
            "nst_files/y_135.json",
            "nst_files/y_138.json",
            "nst_files/y_141.json",
            "nst_files/y_142.json",
            "nst_files/y_143.json",
            "nst_files/y_144.json",
            "nst_files/y_146.json",
            "nst_files/y_148.json",
            "nst_files/y_152.json",
            "nst_files/y_166.json",
            "nst_files/y_167.json",
            "nst_files/y_168.json",
            "nst_files/y_170.json",
            "nst_files/y_174.json",
            "nst_files/y_182.json",
            "nst_files/y_196.json",
            "nst_files/y_215.json",
            "nst_files/y_38.json",
            "nst_files/y_39.json",
            "nst_files/y_41.json",
            "nst_files/y_42.json",
            "nst_files/y_43.json",
            "nst_files/y_45.json",
            "nst_files/y_47.json",
            "nst_files/y_48.json",
            "nst_files/y_49.json",
            "nst_files/y_50.json",
            "nst_files/y_51.json",
            "nst_files/y_52.json",
            "nst_files/y_53.json",
            "nst_files/y_56.json",
            "nst_files/y_57.json",
            "nst_files/y_58.json",
            "nst_files/y_60.json",
            "nst_files/y_61.json",
            "nst_files/y_62.json",
            "nst_files/y_65.json",
            "nst_files/y_67.json",
            "nst_files/y_68.json",
            "nst_files/y_70.json",
            "nst_files/y_71.json",
            "nst_files/y_72.json",
            "nst_files/y_73.json",
            "nst_files/y_74.json",
            "nst_files/y_75.json",
            "nst_files/y_76.json",
            "nst_files/y_84.json",
            "nst_files/y_88.json",
            "nst_files/y_89.json",
            "nst_files/y_90.json",
            "nst_files/y_92.json",
            "nst_files/y_93.json",
            "nst_files/y_94.json",
            "nst_files/y_95.json",
            "nst_files/y_96.json",
            "nst_files/y_97.json",
            "nst_files/y_98.json",
            "nst_files/y_99.json",
            "test_Json.NET/project.json",
            "test_Json.NET/project.lock.json",
            "test_Json.NET/test_Json.NET.deps.json",
            "test_Json.NET/test_Json.NET.runtimeconfig.json",
            "test_json-rustc_serialize/rj-8f8b750d20f3bf10/bin-rj.json",
            "test_json-rustc_serialize/rj-da0f8f63b87cf531/bin-rj.json",
            "test_json-rustc_serialize/rustc-serialize-80c565222c5ccdbb/lib-rustc-serialize.json",
            "test_json-rust/json-74a82b4ec4439e90/lib-json.json",
            "test_json-rust/tj-b5d63a9d8da2968e/bin-tj.json",
            "test_json-rust/tj-bb890ae6f101bb25/bin-tj.json",
            "test_json-rust/tj-bf24d0bdc8d0f4d6/bin-tj.json",
            "test_parsing/i_number_double_huge_neg_exp.json",
            "test_parsing/i_number_huge_exp.json",
            "test_parsing/i_number_neg_int_huge_exp.json",
            "test_parsing/i_number_pos_double_huge_exp.json",
            "test_parsing/i_number_real_neg_overflow.json",
            "test_parsing/i_number_real_pos_overflow.json",
            "test_parsing/i_number_real_underflow.json",
            "test_parsing/i_number_too_big_neg_int.json",
            "test_parsing/i_number_too_big_pos_int.json",
            "test_parsing/i_number_very_big_negative_int.json",
            "test_parsing/i_object_key_lone_2nd_surrogate.json",
            "test_parsing/i_string_1st_surrogate_but_2nd_missing.json",
            "test_parsing/i_string_1st_valid_surrogate_2nd_invalid.json",
            "test_parsing/i_string_incomplete_surrogate_and_escape_valid.json",
            "test_parsing/i_string_incomplete_surrogate_pair.json",
            "test_parsing/i_string_incomplete_surrogates_escape_valid.json",
            "test_parsing/i_string_invalid_lonely_surrogate.json",
            "test_parsing/i_string_invalid_surrogate.json",
            "test_parsing/i_string_invalid_utf-8.json",
            "test_parsing/i_string_inverted_surrogates_U+1D11E.json",
            "test_parsing/i_string_iso_latin_1.json",
            "test_parsing/i_string_lone_second_surrogate.json",
            "test_parsing/i_string_lone_utf8_continuation_byte.json",
            "test_parsing/i_string_not_in_unicode_range.json",
            "test_parsing/i_string_overlong_sequence_2_bytes.json",
            "test_parsing/i_string_overlong_sequence_6_bytes.json",
            "test_parsing/i_string_overlong_sequence_6_bytes_null.json",
            "test_parsing/i_string_truncated-utf-8.json",
            "test_parsing/i_string_utf16BE_no_BOM.json",
            "test_parsing/i_string_utf16LE_no_BOM.json",
            "test_parsing/i_string_UTF-16LE_with_BOM.json",
            "test_parsing/i_string_UTF-8_invalid_sequence.json",
            "test_parsing/i_string_UTF8_surrogate_U+D800.json",
            "test_parsing/i_structure_500_nested_arrays.json",
            "test_parsing/i_structure_UTF-8_BOM_empty_object.json",
            "test_parsing/n_array_1_true_without_comma.json",
            "test_parsing/n_array_a_invalid_utf8.json",
            "test_parsing/n_array_colon_instead_of_comma.json",
            "test_parsing/n_array_comma_after_close.json",
            "test_parsing/n_array_comma_and_number.json",
            "test_parsing/n_array_double_comma.json",
            "test_parsing/n_array_double_extra_comma.json",
            "test_parsing/n_array_extra_close.json",
            "test_parsing/n_array_extra_comma.json",
            "test_parsing/n_array_incomplete_invalid_value.json",
            "test_parsing/n_array_incomplete.json",
            "test_parsing/n_array_inner_array_no_comma.json",
            "test_parsing/n_array_invalid_utf8.json",
            "test_parsing/n_array_items_separated_by_semicolon.json",
            "test_parsing/n_array_just_comma.json",
            "test_parsing/n_array_just_minus.json",
            "test_parsing/n_array_missing_value.json",
            "test_parsing/n_array_newlines_unclosed.json",
            "test_parsing/n_array_number_and_comma.json",
            "test_parsing/n_array_number_and_several_commas.json",
            "test_parsing/n_array_spaces_vertical_tab_formfeed.json",
            "test_parsing/n_array_star_inside.json",
            "test_parsing/n_array_unclosed.json",
            "test_parsing/n_array_unclosed_trailing_comma.json",
            "test_parsing/n_array_unclosed_with_new_lines.json",
            "test_parsing/n_array_unclosed_with_object_inside.json",
            "test_parsing/n_incomplete_false.json",
            "test_parsing/n_incomplete_null.json",
            "test_parsing/n_incomplete_true.json",
            "test_parsing/n_multidigit_number_then_00.json",
            "test_parsing/n_number_0.1.2.json",
            "test_parsing/n_number_-01.json",
            "test_parsing/n_number_0.3e+.json",
            "test_parsing/n_number_0.3e.json",
            "test_parsing/n_number_0_capital_E+.json",
            "test_parsing/n_number_0_capital_E.json",
            "test_parsing/n_number_0.e1.json",
            "test_parsing/n_number_0e+.json",
            "test_parsing/n_number_0e.json",
            "test_parsing/n_number_1_000.json",
            "test_parsing/n_number_1.0e+.json",
            "test_parsing/n_number_1.0e-.json",
            "test_parsing/n_number_1.0e.json",
            "test_parsing/n_number_-1.0..json",
            "test_parsing/n_number_1eE2.json",
            "test_parsing/n_number_+1.json",
            "test_parsing/n_number_.-1.json",
            "test_parsing/n_number_2.e+3.json",
            "test_parsing/n_number_2.e-3.json",
            "test_parsing/n_number_2.e3.json",
            "test_parsing/n_number_.2e-3.json",
            "test_parsing/n_number_-2..json",
            "test_parsing/n_number_9.e+.json",
            "test_parsing/n_number_expression.json",
            "test_parsing/n_number_hex_1_digit.json",
            "test_parsing/n_number_hex_2_digits.json",
            "test_parsing/n_number_infinity.json",
            "test_parsing/n_number_+Inf.json",
            "test_parsing/n_number_Inf.json",
            "test_parsing/n_number_invalid+-.json",
            "test_parsing/n_number_invalid-negative-real.json",
            "test_parsing/n_number_invalid-utf-8-in-bigger-int.json",
            "test_parsing/n_number_invalid-utf-8-in-exponent.json",
            "test_parsing/n_number_invalid-utf-8-in-int.json",
            "test_parsing/n_number_++.json",
            "test_parsing/n_number_minus_infinity.json",
            "test_parsing/n_number_minus_sign_with_trailing_garbage.json",
            "test_parsing/n_number_minus_space_1.json",
            "test_parsing/n_number_-NaN.json",
            "test_parsing/n_number_NaN.json",
            "test_parsing/n_number_neg_int_starting_with_zero.json",
            "test_parsing/n_number_neg_real_without_int_part.json",
            "test_parsing/n_number_neg_with_garbage_at_end.json",
            "test_parsing/n_number_real_garbage_after_e.json",
            "test_parsing/n_number_real_with_invalid_utf8_after_e.json",
            "test_parsing/n_number_real_without_fractional_part.json",
            "test_parsing/n_number_starting_with_dot.json",
            "test_parsing/n_number_U+FF11_fullwidth_digit_one.json",
            "test_parsing/n_number_with_alpha_char.json",
            "test_parsing/n_number_with_alpha.json",
            "test_parsing/n_number_with_leading_zero.json",
            "test_parsing/n_object_bad_value.json",
            "test_parsing/n_object_bracket_key.json",
            "test_parsing/n_object_comma_instead_of_colon.json",
            "test_parsing/n_object_double_colon.json",
            "test_parsing/n_object_emoji.json",
            "test_parsing/n_object_garbage_at_end.json",
            "test_parsing/n_object_key_with_single_quotes.json",
            "test_parsing/n_object_lone_continuation_byte_in_key_and_trailing_comma.json",
            "test_parsing/n_object_missing_colon.json",
            "test_parsing/n_object_missing_key.json",
            "test_parsing/n_object_missing_semicolon.json",
            "test_parsing/n_object_missing_value.json",
            "test_parsing/n_object_no-colon.json",
            "test_parsing/n_object_non_string_key_but_huge_number_instead.json",
            "test_parsing/n_object_non_string_key.json",
            "test_parsing/n_object_repeated_null_null.json",
            "test_parsing/n_object_several_trailing_commas.json",
            "test_parsing/n_object_single_quote.json",
            "test_parsing/n_object_trailing_comma.json",
            "test_parsing/n_object_trailing_comment.json",
            "test_parsing/n_object_trailing_comment_open.json",
            "test_parsing/n_object_trailing_comment_slash_open_incomplete.json",
            "test_parsing/n_object_trailing_comment_slash_open.json",
            "test_parsing/n_object_two_commas_in_a_row.json",
            "test_parsing/n_object_unquoted_key.json",
            "test_parsing/n_object_unterminated-value.json",
            "test_parsing/n_object_with_single_string.json",
            "test_parsing/n_object_with_trailing_garbage.json",
            "test_parsing/n_single_space.json",
            "test_parsing/n_string_1_surrogate_then_escape.json",
            "test_parsing/n_string_1_surrogate_then_escape_u1.json",
            "test_parsing/n_string_1_surrogate_then_escape_u1x.json",
            "test_parsing/n_string_1_surrogate_then_escape_u.json",
            "test_parsing/n_string_accentuated_char_no_quotes.json",
            "test_parsing/n_string_backslash_00.json",
            "test_parsing/n_string_escaped_backslash_bad.json",
            "test_parsing/n_string_escaped_ctrl_char_tab.json",
            "test_parsing/n_string_escaped_emoji.json",
            "test_parsing/n_string_escape_x.json",
            "test_parsing/n_string_incomplete_escaped_character.json",
            "test_parsing/n_string_incomplete_escape.json",
            "test_parsing/n_string_incomplete_surrogate_escape_invalid.json",
            "test_parsing/n_string_incomplete_surrogate.json",
            "test_parsing/n_string_invalid_backslash_esc.json",
            "test_parsing/n_string_invalid_unicode_escape.json",
            "test_parsing/n_string_invalid_utf8_after_escape.json",
            "test_parsing/n_string_invalid-utf-8-in-escape.json",
            "test_parsing/n_string_leading_uescaped_thinspace.json",
            "test_parsing/n_string_no_quotes_with_bad_escape.json",
            "test_parsing/n_string_single_doublequote.json",
            "test_parsing/n_string_single_quote.json",
            "test_parsing/n_string_single_string_no_double_quotes.json",
            "test_parsing/n_string_start_escape_unclosed.json",
            "test_parsing/n_string_unescaped_crtl_char.json",
            "test_parsing/n_string_unescaped_newline.json",
            "test_parsing/n_string_unescaped_tab.json",
            "test_parsing/n_string_unicode_CapitalU.json",
            "test_parsing/n_string_with_trailing_garbage.json",
            "test_parsing/n_structure_100000_opening_arrays.json",
            "test_parsing/n_structure_angle_bracket_..json",
            "test_parsing/n_structure_angle_bracket_null.json",
            "test_parsing/n_structure_array_trailing_garbage.json",
            "test_parsing/n_structure_array_with_extra_array_close.json",
            "test_parsing/n_structure_array_with_unclosed_string.json",
            "test_parsing/n_structure_ascii-unicode-identifier.json",
            "test_parsing/n_structure_capitalized_True.json",
            "test_parsing/n_structure_close_unopened_array.json",
            "test_parsing/n_structure_comma_instead_of_closing_brace.json",
            "test_parsing/n_structure_double_array.json",
            "test_parsing/n_structure_end_array.json",
            "test_parsing/n_structure_incomplete_UTF8_BOM.json",
            "test_parsing/n_structure_lone-invalid-utf-8.json",
            "test_parsing/n_structure_lone-open-bracket.json",
            "test_parsing/n_structure_no_data.json",
            "test_parsing/n_structure_null-byte-outside-string.json",
            "test_parsing/n_structure_number_with_trailing_garbage.json",
            "test_parsing/n_structure_object_followed_by_closing_object.json",
            "test_parsing/n_structure_object_unclosed_no_value.json",
            "test_parsing/n_structure_object_with_comment.json",
            "test_parsing/n_structure_object_with_trailing_garbage.json",
            "test_parsing/n_structure_open_array_apostrophe.json",
            "test_parsing/n_structure_open_array_comma.json",
            "test_parsing/n_structure_open_array_object.json",
            "test_parsing/n_structure_open_array_open_object.json",
            "test_parsing/n_structure_open_array_open_string.json",
            "test_parsing/n_structure_open_array_string.json",
            "test_parsing/n_structure_open_object_close_array.json",
            "test_parsing/n_structure_open_object_comma.json",
            "test_parsing/n_structure_open_object.json",
            "test_parsing/n_structure_open_object_open_array.json",
            "test_parsing/n_structure_open_object_open_string.json",
            "test_parsing/n_structure_open_object_string_with_apostrophes.json",
            "test_parsing/n_structure_open_open.json",
            "test_parsing/n_structure_single_eacute.json",
            "test_parsing/n_structure_single_star.json",
            "test_parsing/n_structure_trailing_#.json",
            "test_parsing/n_structure_U+2060_word_joined.json",
            "test_parsing/n_structure_uescaped_LF_before_string.json",
            "test_parsing/n_structure_unclosed_array.json",
            "test_parsing/n_structure_unclosed_array_partial_null.json",
            "test_parsing/n_structure_unclosed_array_unfinished_false.json",
            "test_parsing/n_structure_unclosed_array_unfinished_true.json",
            "test_parsing/n_structure_unclosed_object.json",
            "test_parsing/n_structure_unicode-identifier.json",
            "test_parsing/n_structure_UTF8_BOM_no_data.json",
            "test_parsing/n_structure_whitespace_formfeed.json",
            "test_parsing/n_structure_whitespace_U+2060_word_joiner.json",
            "test_parsing/y_array_arraysWithSpaces.json",
            "test_parsing/y_array_empty.json",
            "test_parsing/y_array_empty-string.json",
            "test_parsing/y_array_ending_with_newline.json",
            "test_parsing/y_array_false.json",
            "test_parsing/y_array_heterogeneous.json",
            "test_parsing/y_array_null.json",
            "test_parsing/y_array_with_1_and_newline.json",
            "test_parsing/y_array_with_leading_space.json",
            "test_parsing/y_array_with_several_null.json",
            "test_parsing/y_array_with_trailing_space.json",
            "test_parsing/y_number_0e+1.json",
            "test_parsing/y_number_0e1.json",
            "test_parsing/y_number_after_space.json",
            "test_parsing/y_number_double_close_to_zero.json",
            "test_parsing/y_number_int_with_exp.json",
            "test_parsing/y_number.json",
            "test_parsing/y_number_minus_zero.json",
            "test_parsing/y_number_negative_int.json",
            "test_parsing/y_number_negative_one.json",
            "test_parsing/y_number_negative_zero.json",
            "test_parsing/y_number_real_capital_e.json",
            "test_parsing/y_number_real_capital_e_neg_exp.json",
            "test_parsing/y_number_real_capital_e_pos_exp.json",
            "test_parsing/y_number_real_exponent.json",
            "test_parsing/y_number_real_fraction_exponent.json",
            "test_parsing/y_number_real_neg_exp.json",
            "test_parsing/y_number_real_pos_exponent.json",
            "test_parsing/y_number_simple_int.json",
            "test_parsing/y_number_simple_real.json",
            "test_parsing/y_object_basic.json",
            "test_parsing/y_object_duplicated_key_and_value.json",
            "test_parsing/y_object_duplicated_key.json",
            "test_parsing/y_object_empty.json",
            "test_parsing/y_object_empty_key.json",
            "test_parsing/y_object_escaped_null_in_key.json",
            "test_parsing/y_object_extreme_numbers.json",
            "test_parsing/y_object.json",
            "test_parsing/y_object_long_strings.json",
            "test_parsing/y_object_simple.json",
            "test_parsing/y_object_string_unicode.json",
            "test_parsing/y_object_with_newlines.json",
            "test_parsing/y_string_1_2_3_bytes_UTF-8_sequences.json",
            "test_parsing/y_string_accepted_surrogate_pair.json",
            "test_parsing/y_string_accepted_surrogate_pairs.json",
            "test_parsing/y_string_allowed_escapes.json",
            "test_parsing/y_string_backslash_and_u_escaped_zero.json",
            "test_parsing/y_string_backslash_doublequotes.json",
            "test_parsing/y_string_comments.json",
            "test_parsing/y_string_double_escape_a.json",
            "test_parsing/y_string_double_escape_n.json",
            "test_parsing/y_string_escaped_control_character.json",
            "test_parsing/y_string_escaped_noncharacter.json",
            "test_parsing/y_string_in_array.json",
            "test_parsing/y_string_in_array_with_leading_space.json",
            "test_parsing/y_string_last_surrogates_1_and_2.json",
            "test_parsing/y_string_nbsp_uescaped.json",
            "test_parsing/y_string_nonCharacterInUTF-8_U+10FFFF.json",
            "test_parsing/y_string_nonCharacterInUTF-8_U+FFFF.json",
            "test_parsing/y_string_null_escape.json",
            "test_parsing/y_string_one-byte-utf-8.json",
            "test_parsing/y_string_pi.json",
            "test_parsing/y_string_reservedCharacterInUTF-8_U+1BFFF.json",
            "test_parsing/y_string_simple_ascii.json",
            "test_parsing/y_string_space.json",
            "test_parsing/y_string_surrogates_U+1D11E_MUSICAL_SYMBOL_G_CLEF.json",
            "test_parsing/y_string_three-byte-utf-8.json",
            "test_parsing/y_string_two-byte-utf-8.json",
            "test_parsing/y_string_u+2028_line_sep.json",
            "test_parsing/y_string_u+2029_par_sep.json",
            "test_parsing/y_string_uescaped_newline.json",
            "test_parsing/y_string_uEscape.json",
            "test_parsing/y_string_unescaped_char_delete.json",
            "test_parsing/y_string_unicode_2.json",
            "test_parsing/y_string_unicodeEscapedBackslash.json",
            "test_parsing/y_string_unicode_escaped_double_quote.json",
            "test_parsing/y_string_unicode.json",
            "test_parsing/y_string_unicode_U+10FFFE_nonchar.json",
            "test_parsing/y_string_unicode_U+1FFFE_nonchar.json",
            "test_parsing/y_string_unicode_U+200B_ZERO_WIDTH_SPACE.json",
            "test_parsing/y_string_unicode_U+2064_invisible_plus.json",
            "test_parsing/y_string_unicode_U+FDD0_nonchar.json",
            "test_parsing/y_string_unicode_U+FFFE_nonchar.json",
            "test_parsing/y_string_utf8.json",
            "test_parsing/y_string_with_del_character.json",
            "test_parsing/y_structure_lonely_false.json",
            "test_parsing/y_structure_lonely_int.json",
            "test_parsing/y_structure_lonely_negative_real.json",
            "test_parsing/y_structure_lonely_null.json",
            "test_parsing/y_structure_lonely_string.json",
            "test_parsing/y_structure_lonely_true.json",
            "test_parsing/y_structure_string_empty.json",
            "test_parsing/y_structure_trailing_newline.json",
            "test_parsing/y_structure_true_in_array.json",
            "test_parsing/y_structure_whitespace_array.json",
            "test_transform/number_1.000000000000000005.json",
            "test_transform/number_10000000000000000999.json",
            "test_transform/number_1000000000000000.json",
            "test_transform/number_1.0.json",
            "test_transform/number_1e6.json",
            "test_transform/number_1e-999.json",
            "test_transform/number_1.0e-999.json",
            "test_transform/number_9223372036854775807.json",
            "test_transform/number_-9223372036854775808.json",
            "test_transform/number_9223372036854775808.json",
            "test_transform/number_-9223372036854775809.json",
            "test_transform/object_key_nfc_nfd.json",
            "test_transform/object_key_nfd_nfc.json",
            "test_transform/object_same_key_different_values.json",
            "test_transform/object_same_key_same_value.json",
            "test_transform/object_same_key_unclear_values.json",
            "test_transform/string_1_escaped_invalid_codepoint.json",
            "test_transform/string_1_invalid_codepoint.json",
            "test_transform/string_2_escaped_invalid_codepoints.json",
            "test_transform/string_2_invalid_codepoints.json",
            "test_transform/string_3_escaped_invalid_codepoints.json",
            "test_transform/string_3_invalid_codepoints.json",
            "test_transform/string_with_escaped_NULL.json"
        )

        val skipByFilename = mutableSetOf(
            "nst_files/n_223.json", // \u0000 is valid
            "test_parsing/n_structure_whitespace_formfeed.json", // form feed should be valid
            "test_parsing/y_string_allowed_escapes.json", // no form feed in kotlin
            "test_parsing/y_string_nonCharacterInUTF-8_U+FFFF.json" // why should it be successful?!
        ).apply {
            if (Platform.native() || Platform.wasm()) {
                // stack overflow due to deep nesting
                // TODO convert recursive parser to iterative to handle arbitrary nesting depth
                addAll(listOf(
                    "test_parsing/n_structure_100000_opening_arrays.json",
                    "test_parsing/n_structure_open_array_object.json",
                    "test_parsing/i_structure_500_nested_arrays.json"
                ))
            }
        }

        val skipChecksumTestContent = Regex("^\\[?\"[^\"]*\"\\]?$|^\\[?[0-9.eE+-]+\\]?$", RegexOption.IGNORE_CASE)

        val skipChecksumTestFilename = mutableSetOf(
            "test_parsing/y_number_double_close_to_zero.json",
            "test_transform/number_1.0.json", // JS outputs [1] instead of [1.0]
            "test_parsing/y_object_duplicated_key.json",
            "test_parsing/y_object_duplicated_key_and_value.json",
            "test_parsing/y_object_escaped_null_in_key.json",
            "test_parsing/y_object_string_unicode.json",
            "test_parsing/y_string_1_2_3_bytes_UTF-8_sequences.json",
            "test_parsing/y_string_accepted_surrogate_pair.json",
            "test_parsing/y_string_accepted_surrogate_pairs.json",
            "test_parsing/y_string_allowed_escapes.json",
            "test_parsing/y_string_escaped_noncharacter.json",
            "test_parsing/y_string_last_surrogates_1_and_2.json",
            "test_parsing/y_string_nbsp_uescaped.json",
            "test_parsing/y_string_one-byte-utf-8.json",
            "test_transform/number_1e6.json",
            "test_transform/number_1e-999.json",
            "test_transform/object_same_key_different_values.json",
            "test_transform/object_same_key_same_value.json",
            "test_transform/object_same_key_unclear_values.json"
        ).apply {
            if (Platform.native()) {
                // There is a problem with escape sequences on the native platform.
                // TODO - see why
                addAll(listOf(
                    "nst_files/y_100.json",
                    "nst_files/y_166.json",
                    "nst_files/y_167.json",
                    "nst_files/y_168.json",
                    "nst_files/y_170.json",
                    "nst_files/y_182.json",
                    "nst_files/y_95.json",
                    "nst_files/y_96.json",
                    "nst_files/y_97.json",
                    "nst_files/y_98.json",
                    "nst_files/y_99.json",
                    "test_parsing/y_string_backslash_doublequotes.json"
                ))
            }
        }

        val awaitExceptionByFilename = setOf(
            "test_transform/string_1_escaped_invalid_codepoint.json",
            "test_transform/string_1_invalid_codepoint.json",
            "test_transform/string_2_escaped_invalid_codepoints.json",
            "test_transform/string_2_invalid_codepoints.json",
            "test_transform/string_3_escaped_invalid_codepoints.json",
            "test_transform/string_3_invalid_codepoints.json"
        )
    }

    suspend fun testFile(path: String) {
        val base = path.indexOfLast { c -> c == '/' }
        val filename = path.substring(base + 1)
        if (path == watchFile) {
            logger.info { "watched file!" }
        }
        if (skipByFilename.contains(path)) {
            logger.info { "skipping $path" }
            return
        }
        logger.error { "considering file $path" }
        val awaitError = filename.startsWith("n_") || awaitExceptionByFilename.contains(path)
        val mayThrow = filename.startsWith("i_")
        val content = getResource(path)

        if (awaitError || mayThrow)
        {
            try {
                /* val instance = */ Json.parseValue(content)
                if (awaitError) fail("Exception awaited!")
                // skip further tests
                return
            }
            catch (e: Throwable) {
            }
        }
        else
        {
            startTiming()
            val instance = Json.parseValue(content)
            stopTiming()
            if (filename.equals("y_structure_lonely_null.json")) return
            startTiming()
            val output : String = when(instance) {
                null -> "null"
                is String -> Json.escape(instance)
                else -> instance.toString()
            }
            stopTiming()
            // skip vicious ones that defeat the naive checksum algorithm
            val skipChecksum = skipChecksumTestContent.matches(content) || skipChecksumTestFilename.contains(path)
            if (!skipChecksum)
            {
                // logger.info { ">> $content"}
                // logger.info { "<< $output"}
                var success = false
                try {
                    assertEquals(checksum(content), checksum(output))
                    success = true
                } finally {
                    if (!success) {
                        logger.error { "Checksum test failed for file $path" }
                        logger.error { "  expected: $content"}
                        logger.error { "  received: $output"}
                    }
                }
            }
        }
    }

    @Test
    fun testEqualsJson() = runTest {
        val o1 = Json.MutableObject()
        o1.put("foo", "bar")
        o1.put("bar", 45.65)
        o1.put("baz", Json.Array(1L, 2L, 3L))
        val o2 = Json.parse("{ \"foo\":\"bar\", \"bar\":45.65, \"baz\":[1,2,3] }")?.asObject()
        assertEquals(o1, o2)
    }

    @Test
    fun testEqualsString() = runTest {
        val payload = "{\"access_token\":\"MWJkNjA5NjItYzhmOS00OTkwLWFhZmEtOGQ4OTEyZDk4ZmJh\",\"token_type\":\"Bearer\",\"expires_in\":1800,\"scope\":\"openid profile\",\"refresh_token\":\"YWVmOGJjZjYtMzhlYi00YmM3LTg1NzQtNWMzOTY0NmY1MGUz\",\"id_token\":\"eyJjaWQiOiJydHIiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhcHAuZGV2LnJ0cmV4cGVyaWVuY2Uub3JnIiwiYXVkIjoiQ2hhbmNlIiwic3ViIjoiZjViN2I1MWMtNjM4Yi0xMWVhLWJjOTYtYTRiZjAxMWRkNDg0IiwibmFtZSI6InRhbGVudF9jbGF1ZGUiLCJlbWFpbCI6ImNsYXVkZTJAcmVuZWdhdC5uZXQiLCJnaXZlbl9uYW1lIjoiQ2xhdWRlIiwiZmFtaWx5X25hbWUiOiJCcmlzc29uIiwiaWF0IjoxNTg0MjQ5MjI0LCJleHAiOjE1ODQyNDkyMjZ9.e2pgWxKe_1ZaUr06QPre65zqyOBzTDez16G4OE-OFBP-Dry2UAFISCAdX85jqqw0EjzJy-X8I5Ho7jD5hD2qeXtlT3Ee3oI2GdekW_sNpC1AhzwItfcdHhh7fIddaViOtpQbe11V7PDS0bcJsAc99SsR_kFIOwQi9T_xPmXoQyMN3bkHB-Ydty3jLKSINx-o7Dg7sFLwCNe1xpIHV5OwuzxiVBYW9Y_QpngtXqpwlBSDrq38WR-Y2w1IYZi2hUva9V5f8nezWM5fmXYe3DdRCa608w-AchWDGm7o-E7YJtNnKms57D1hYFIFJCKRPZsoBpGT0cVLPAcR7zCa4btKDJfRB2_B-u-vIn7lpSbuHBYF-SIzcJ5DP-rt-x1ritRFXnpxwqehvOEggG_l0nWHy6Tbl5uN2lsNrxAsDyIlvfrjw6BQJzQxfXNmrfXATjaGhFLVwl-pzsQa0N4Ullkv_5IQ6HUepQXwT_s-4VZlBXidcCJ0ypLo9n4JAxL-juTpgRCu2TAK4sIEajvXTt0UckHFV11oUXKA2Jz_V3XmmKzv1inn6uvYvi0bxm7zhGIqWSXKsDF4wnL-IsZ6-Ck7sSE9cUo54sListdz2mv9yrsD8R3P1PRyNiUqpMFBQn4LYm9vTvcozhOk4IbnSI2XjixeKBfrDWXrnSMCnyEAB@4\"}"
        val parsed = Json.parse(payload)?.asObject()
        val toString = parsed.toString()
        assertEquals(payload, toString)
    }

    private fun expect(exp: Any?, op: () -> Any?) {
        when (exp) {
            ignoreEvaluation -> return
            is KClass<*> -> {
                try {
                    val v = op()
                    fail("was expecting ${exp.simpleName}, got $v")
                } catch (t: Throwable) {
                    assertTrue(exp.isInstance(t), "was expecting ${exp.simpleName}, got ${t::class.simpleName}: ${t.message}")
                }
            }
            else -> {
                try {
                    val v = op()
                    if (exp != ignoreResult) assertEquals(exp, v, "was expecting <$exp>, got <$v>")
                } catch (assertError: AssertionError) {
                    throw assertError
                } catch (t: Throwable) {
                    fail("was expecting $exp, got ${t::class.simpleName}")
                }
            }
        }
    }

    private val ignoreEvaluation = object: Any() {}
    private val ignoreResult = object: Any() {}

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun testGenericsConverter() = runTest {
        val payload = Json.MutableObject()
        payload["str"] = "12.345"
        payload["bn"] = true
        payload["c"] = 'f'
        payload["b"] = 0x31.toByte()
        payload["s"] = 123.toShort()
        payload["i"] = 123456
        payload["l"] = 123456789L
        payload["f"] = 123.456F
        payload["d"] = 123.456789
        payload["o"] = payload.toString()
        payload["a"] = "[1, 2, 3]"
        payload["t"] = "12:18:05"
        payload["dt"] = "2022-04-08"
        payload["dtm"] = "2022-04-08T12:18:05"
        payload["uuid"] = "f5906f2b-c6d4-43fa-a2ed-e70b25e3a507"
        payload["n"] = null

        val expected = mutableMapOf<String, Array<Any?>>()
        expected["str"] = arrayOf("12.345", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, 12.345f, 12.345, ClassCastException::class, ClassCastException::class, IllegalArgumentException::class, IllegalArgumentException::class, IllegalArgumentException::class, NumberFormatException::class, BigDecimal.fromDouble(12.345), IllegalArgumentException::class, null)
        expected["bn"] = arrayOf("true", true, 't', 1.toByte(), 1.toShort(), 1, 1L, JsonException::class, JsonException::class, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.fromInt(1), JsonException::class, JsonException::class, null)
        expected["c"] = arrayOf("f", false, 'f', JsonException::class, JsonException::class, JsonException::class, JsonException::class, JsonException::class, JsonException::class, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, JsonException::class, JsonException::class, JsonException::class, null)
        expected["b"] = arrayOf("49", true, JsonException::class, 49.toByte(), 49.toShort(), 49, 49L, 49.0F, 49.0, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.fromByte(0x31.toByte()), BigDecimal.fromByte(0x31.toByte()), JsonException::class, null)
        expected["s"] = arrayOf("123", true, JsonException::class, 123.toByte(), 123.toShort(), 123, 123L, 123F, 123.0, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.fromShort(123.toShort()), BigDecimal.fromShort(123.toShort()), JsonException::class, null)
        expected["i"] = arrayOf("123456", true, JsonException::class, 64.toByte(), (-7616).toShort(), 123456, 123456L, 123456F, 123456.0, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.fromInt(123456), BigDecimal.fromInt(123456), JsonException::class, null)
        expected["l"] = arrayOf("123456789", true, JsonException::class, 21.toByte(), (-13035).toShort(), 123456789, 123456789L, ignoreResult, 123456789.0, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.fromLong(123456789L), BigDecimal.fromLong(123456789L), JsonException::class, null)
        expected["f"] = arrayOf("123.456", true, JsonException::class, 123.toByte(), 123.toShort(), 123, 123L, 123.456F, ignoreResult, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.tryFromFloat(123.456F), ignoreResult, JsonException::class, null)
        expected["d"] = arrayOf("123.456789", true, JsonException::class, 123.toByte(), 123.toShort(), 123, 123L, ignoreResult, 123.456789, ClassCastException::class, ClassCastException::class, JsonException::class, JsonException::class, JsonException::class, BigInteger.fromInt(123), BigDecimal.fromDouble(123.456789), JsonException::class, null)
        expected["o"] = arrayOf("{\"str\":\"12.345\",\"bn\":true,\"c\":\"f\",\"b\":49,\"s\":123,\"i\":123456,\"l\":123456789,\"f\":123.456,\"d\":123.456789}", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, ClassCastException::class, ClassCastException::class, IllegalArgumentException::class, IllegalArgumentException::class, IllegalArgumentException::class, ArithmeticException::class, ArithmeticException::class, IllegalArgumentException::class, null)
        expected["a"] = arrayOf("[1, 2, 3]", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, ClassCastException::class, ClassCastException::class, IllegalArgumentException::class, IllegalArgumentException::class, IllegalArgumentException::class, NumberFormatException::class, NumberFormatException::class, IllegalArgumentException::class, null)
        expected["t"] = arrayOf("12:18:05", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, ClassCastException::class, ClassCastException::class, LocalTime.parse("12:18:05"), IllegalArgumentException::class, IllegalArgumentException::class, NumberFormatException::class, NumberFormatException::class, IllegalArgumentException::class, null)
        expected["dt"] = arrayOf("2022-04-08", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, ClassCastException::class, ClassCastException::class, IllegalArgumentException::class, LocalDate.parse("2022-04-08"), IllegalArgumentException::class, NumberFormatException::class, NumberFormatException::class, IllegalArgumentException::class, null)
        expected["dtm"] = arrayOf("2022-04-08T12:18:05", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, ClassCastException::class, ClassCastException::class, IllegalArgumentException::class, IllegalArgumentException::class, LocalDateTime.parse("2022-04-08T12:18:05"), NumberFormatException::class, NumberFormatException::class, IllegalArgumentException::class, null)
        expected["uuid"] = arrayOf("f5906f2b-c6d4-43fa-a2ed-e70b25e3a507", JsonException::class, JsonException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, NumberFormatException::class, ClassCastException::class, ClassCastException::class, IllegalArgumentException::class, IllegalArgumentException::class, IllegalArgumentException::class, NumberFormatException::class, NumberFormatException::class, Uuid.parse("f5906f2b-c6d4-43fa-a2ed-e70b25e3a507"), null)
        expected["n"] = arrayOf(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)


        for (key in expected.keys) {
            val exp = expected[key] ?: throw Error("missing expected values array")
            if (exp.size != 18) throw Error("wrong length for expected values array for key $key: ${exp.size}")
            // logger.warn { "conversion test on key $key: value = ${payload[key]},  expected = [ ${exp.joinToString(", ")} ]"}
            if (!Platform.wasm() || key !in setOf("f", "o")) {
                // Float.toString() gives extra erroneous digits in wasmjs 2.0.21
                // See https://youtrack.jetbrains.com/issue/KT-59118/WASM-floating-point-toString-inconsistencies
                expect(exp[0]) { payload.getAs<String>(key) }
            }
            expect(exp[1]) { payload.getAs<Boolean>(key) }
            expect(exp[2]) { payload.getAs<Char>(key) }
            expect(exp[3]) { payload.getAs<Byte>(key) }
            expect(exp[4]) { payload.getAs<Short>(key) }
            expect(exp[5]) { payload.getAs<Int>(key) }
            expect(exp[6]) { payload.getAs<Long>(key) }
            expect(exp[7]) { payload.getAs<Float>(key) }
            expect(exp[8]) { payload.getAs<Double>(key) }
            expect(exp[9]) { payload.getAs<Json.Object>(key) }
            expect(exp[10]) { payload.getAs<Json.Array>(key) }
            expect(exp[11]) { payload.getAs<LocalTime>(key) }
            expect(exp[12]) { payload.getAs<LocalDate>(key) }
            expect(exp[13]) { payload.getAs<LocalDateTime>(key) }
            expect(exp[14]) { payload.getAs<BigInteger>(key) }
            expect(exp[15]) { payload.getAs<BigDecimal>(key) }
            expect(exp[16]) { payload.getAs<Uuid>(key) }
            expect(exp[17]) { null }
        }
    }

    @Test
    fun testConversions() {
        val payload = Pair("a", Pair("b", 123))
        val json = Json.toJson(payload)
        assertEquals("""["a",["b",123]]""", json.toString())
    }

    @Test
    fun testInlineDsl() {
        // Simple object
        val simple = obj {
            "name" to "test"
            "count" to 42
            "active" to true
            "empty" to null
        }
        assertEquals("""{"name":"test","count":42,"active":true,"empty":null}""", simple.toString())

        // Nested object
        val nested = obj {
            "user" to obj {
                "id" to 1
                "profile" to obj {
                    "email" to "test@example.com"
                }
            }
        }
        assertEquals("""{"user":{"id":1,"profile":{"email":"test@example.com"}}}""", nested.toString())

        // Array literal
        val simpleArr = arr[1, 2, 3, "four"]
        assertEquals("""[1,2,3,"four"]""", simpleArr.toString())

        // Mixed types in array
        val mixedArr = arr[1, "text", true, null]
        assertEquals("""[1,"text",true,null]""", mixedArr.toString())

        // Complex nested structure
        val complex = obj {
            "items" to arr[1, 2, 3]
            "matrix" to arr[arr[1, 2], arr[3, 4]]
            "objects" to arr[obj { "x" to 10 }, obj { "y" to 20 }]
        }
        assertEquals(
            """{"items":[1,2,3],"matrix":[[1,2],[3,4]],"objects":[{"x":10},{"y":20}]}""",
            complex.toString()
        )

        // Deeply nested
        val deep = obj {
            "a" to obj {
                "b" to obj {
                    "c" to obj {
                        "value" to 123
                    }
                }
            }
        }
        assertEquals("""{"a":{"b":{"c":{"value":123}}}}""", deep.toString())

        // Standard collections work via toJsonOrIntegral conversion
        val withList = obj {
            "list" to listOf(1, 2, 3)
        }
        assertEquals("""{"list":[1,2,3]}""", withList.toString())
    }
}
