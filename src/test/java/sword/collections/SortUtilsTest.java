package sword.collections;

import junit.framework.TestCase;

public final class SortUtilsTest extends TestCase {

    public void testCompareCharSequenceByUnicode() {
        final CharSequence[] values = {
                null, "", "0", "00", "1", "9", "@", "A", "Z", "a", "abc", "bcd", "z", "あ"
        };

        for (int aIndex = 0; aIndex < values.length; aIndex++) {
            final CharSequence a = values[aIndex];
            for (int bIndex = 0; bIndex < values.length; bIndex++) {
                final CharSequence b = values[bIndex];
                final boolean result = SortUtils.compareCharSequenceByUnicode(a, b);
                assertEquals(aIndex < bIndex, result);
            }
        }
    }
}
