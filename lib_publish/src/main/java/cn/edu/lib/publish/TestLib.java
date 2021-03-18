package cn.edu.lib.publish;

import androidx.annotation.IntRange;

public class TestLib {

    /**
     * 获取消息内容(用于测试远程依赖包是否能正常使用)
     *
     * @param index
     * @return
     */
    public static String getMessage(@IntRange(from = 1, to = 2) int index) {
        if (index == 1) {
            return "first msg.";
        }

        if (index == 2) {
            return "second msg.";
        }

        return "Did not match msg.";
    }

}
