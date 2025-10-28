package org.example.infrastructure.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {
    private boolean enabled = true;
    private String fixedDelay = "PT5S";
    private String initialDelay = "PT0S";
    private int top = 1000;
    private Pagination pagination = new Pagination();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getFixedDelay() { return fixedDelay; }
    public void setFixedDelay(String fixedDelay) { this.fixedDelay = fixedDelay; }
    public String getInitialDelay() { return initialDelay; }
    public void setInitialDelay(String initialDelay) { this.initialDelay = initialDelay; }
    public int getTop() { return top; }
    public void setTop(int top) { this.top = top; }
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    public static class Pagination {
        private boolean enabled = false;
        private int pageSize = 1;
        private String delayBetweenMessages = "PT1S";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public String getDelayBetweenMessages() { return delayBetweenMessages; }
        public void setDelayBetweenMessages(String delayBetweenMessages) { this.delayBetweenMessages = delayBetweenMessages; }
    }
}


