package org.example.infrastructure.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {
    private boolean enabled = true;
    private String fixedDelay = "PT10M";     // ISO-8601 duration
    private String initialDelay = "PT10M";   // ISO-8601 duration
    private int top = 20;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getFixedDelay() { return fixedDelay; }
    public void setFixedDelay(String fixedDelay) { this.fixedDelay = fixedDelay; }
    public String getInitialDelay() { return initialDelay; }
    public void setInitialDelay(String initialDelay) { this.initialDelay = initialDelay; }
    public int getTop() { return top; }
    public void setTop(int top) { this.top = top; }
}


