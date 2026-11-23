package com.loansystem.model;

import com.loansystem.util.StringUtil;

public interface Reportable {
    String toReportLine();

    default String reportHeader(String title) {
        return StringUtil.line('=', 90) + System.lineSeparator()
                + title + System.lineSeparator()
                + StringUtil.line('=', 90);
    }
}
