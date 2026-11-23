package com.loansystem.model;

import com.loansystem.util.StringUtil;

/** Week 5 - Interface for objects that can print themselves in a report. */
public interface Reportable {
    String toReportLine();

    default String reportHeader(String title) {
        return StringUtil.line('=', 90) + System.lineSeparator()
                + title + System.lineSeparator()
                + StringUtil.line('=', 90);
    }
}
