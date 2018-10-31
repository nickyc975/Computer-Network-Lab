public class PatternFinder {
    public static int find(final byte[] pattern, final byte[] array) {
        return find(pattern, array, pattern.length, array.length);
    }

    public static int find(final byte[] pattern, final byte[] array, final int patn_len, final int aray_len) {
        int pos = -1, str_pos = 0, patn_pos = 0;
        int[] next = new int[patn_len];

        if(patn_len <= 0 || aray_len <= 0 || pattern.length < patn_len || array.length < aray_len)
            return pos;

        get_next(pattern, next, patn_len);
        while (str_pos < aray_len && patn_pos < patn_len)
        {
            if(pattern[patn_pos] == array[str_pos])
            {
                patn_pos++;
                str_pos++;
            }
            else
            {
                patn_pos = next[patn_pos];
                str_pos += patn_pos == 0 ? 1 : 0;
            }
        }

        if(patn_pos >= patn_len)
            pos = str_pos - patn_len;
        return pos;
    }

    private static void get_next(final byte[] pattern, final int[] next, final int patn_len)
    {
        int i, j, k, temp_next;
        if(patn_len <= 0)
            return;

        next[0] = 0;
        for(i = 1; i < patn_len; i++)
        {
            temp_next = 0;
            for(j = 0; j < i; j++)
            {
                for(k = 0; k < j; k++)
                    if(pattern[k] != pattern[i - j + k])
                        break;
                if(k == j && temp_next < j)
                    temp_next = j;
            }
            next[i] = temp_next;
        }
    }
}
