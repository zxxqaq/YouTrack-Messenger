package org.example.infrastructure.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {
    private boolean enabled = true;
    private String fixedDelay = "PT5S";
    private String initialDelay = "PT0S";
    private int top = 1000;
    private Pagination pagination = new Pagination();
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

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
    public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }
    public void setCircuitBreaker(CircuitBreaker circuitBreaker) { this.circuitBreaker = circuitBreaker; }

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

    public static class CircuitBreaker {
        private int maxConsecutiveFailures = 3;
        private boolean autoPause = true;
        private String pauseDuration = "PT1H";
        private boolean sendSingleAlert = true;

        public int getMaxConsecutiveFailures() { return maxConsecutiveFailures; }
        public void setMaxConsecutiveFailures(int maxConsecutiveFailures) { this.maxConsecutiveFailures = maxConsecutiveFailures; }
        
        public boolean isAutoPause() { return autoPause; }
        public void setAutoPause(boolean autoPause) { this.autoPause = autoPause; }
        
        public String getPauseDuration() { return pauseDuration; }
        public void setPauseDuration(String pauseDuration) { this.pauseDuration = pauseDuration; }
        
        public boolean isSendSingleAlert() { return sendSingleAlert; }
        public void setSendSingleAlert(boolean sendSingleAlert) { this.sendSingleAlert = sendSingleAlert; }
    }
}


