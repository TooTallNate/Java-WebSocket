package org.java_websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <tt>HighCycleThrottler</tt> is a class that checks for high cycling caused by the jdk epoll selector bug.  For some
 * reason the selector.select() code becomes not blocking causing a high CPU condition.  When this condition is detected,
 * this class will add a 1ms delay to keep from loading up the CPU when the selector.select() code is no longer blocking.
 *  */
public class HighCycleThrottler
{
   private final Logger log = LoggerFactory.getLogger( HighCycleThrottler.class );
   private static final int CYCLE_THRESHOLD = 600 * 60;  // 600 cycles a second for 60 seconds;
   private final boolean  enabled;
   private long nextCycle;
   private long cyclesPerMinute = 0;
   private boolean highCPUDetected = false;

   public HighCycleThrottler() {
      enabled = isEnabled( );
      nextCycle = System.currentTimeMillis() + 60 * 1000;
   }

   /**
    * Checks for high cycling and throttles with a 1ms delay if detected.
    */
   public void checkHighCycleRate() {
      if ( enabled ) {
         cyclesPerMinute++;
         if ( System.currentTimeMillis() >= nextCycle ) {
            String cycles = String.format( "Cycles last minute = %d", cyclesPerMinute );
            log.warn( cycles );

            if ( cyclesPerMinute > CYCLE_THRESHOLD ){
             if( !highCPUDetected ){
                  highCPUDetected = true;
                  log.warn( "High CPU condition detected" );
               }
            } else if ( highCPUDetected ) {
               log.warn( "High CPU condition cleared" );
               highCPUDetected = false;
            }

            nextCycle = System.currentTimeMillis() + 60 * 1000;
            cyclesPerMinute = 0;
         }

         if ( highCPUDetected ) {
            try {
               Thread.sleep( 1L );
            } catch ( InterruptedException e ) {
               log.warn( "Thread.sleep(1L) failed" );
            }
         }
      }
   }

   /**
    * Set the enabled flag and log if USE_EPOLL_SELECTOR_FIX is defined.
    */
   private boolean isEnabled() {
      if (System.getenv( "USE_EPOLL_SELECTOR_FIX" ) != null){
         log.warn( "Using EPoll Selector Fix" );
         return true;
      }
      return false;
   }
}
