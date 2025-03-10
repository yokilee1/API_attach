package io.dongtai.iast.core.service;

import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

/**
 * @author owefsad
 */
public class ReportThread implements Runnable {

    private final String report;
    private final String uri;

    public ReportThread(String uri, String report) {
        this.uri = uri;
        this.report = report;
    }

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see #start()
     * @see #stop()
     * @see #Thread(ThreadGroup, Runnable, String)
     */
    @Override
    public void run() {
        try {
            HttpClientUtils.sendPost(uri, report);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("REPORT_SEND_FAILED"), uri, report, e);
        }
    }
}
