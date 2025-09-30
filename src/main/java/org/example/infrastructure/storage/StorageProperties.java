package org.example.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    
    private Cleanup cleanup = new Cleanup();
    
    public Cleanup getCleanup() { return cleanup; }
    public void setCleanup(Cleanup cleanup) { this.cleanup = cleanup; }
    
    public static class Cleanup {
        private boolean enabled = true;
        private int daysToKeep = 30;
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getDaysToKeep() { return daysToKeep; }
        public void setDaysToKeep(int daysToKeep) { this.daysToKeep = daysToKeep; }
    }
}
